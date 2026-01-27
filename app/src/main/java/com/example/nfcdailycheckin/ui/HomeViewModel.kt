package com.example.nfcdailycheckin.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nfcdailycheckin.data.*
import com.example.nfcdailycheckin.notifications.ReminderScheduler
import com.example.nfcdailycheckin.util.DateTime
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class UiTask(
    val id: Long,
    val title: String,
    val reminder: String,
    val isDoneToday: Boolean,
    val nfcText: String,
)

data class DayStatus(
    val date: LocalDate,
    val isFulfilled: Boolean,
    val isFuture: Boolean,
)

data class HomeState(
    val todayDone: Int = 0,
    val todayTotal: Int = 0,
    val streak: Int = 0,
    val days: List<DayStatus> = emptyList(), // <- jetzt Mo–So (7 Tage)
    val tasks: List<TaskEntity> = emptyList(),
    val uiTasksToday: List<UiTask> = emptyList(),
)

sealed class HomeEvent {
    data class Snack(val message: String) : HomeEvent()
}

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: Repository
    private val _events = MutableSharedFlow<HomeEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        val db = AppDatabase.get(app)
        repo = Repository(db.taskDao(), db.completionDao())

        val today = DateTime.today()

        // Für Streak nicht nur 14 Tage beobachten, sonst cappt es die Berechnung.
        // (120 Tage sind für ein Bastelprojekt absolut okay.)
        val from = today.minusDays(120)
        val to = today

        viewModelScope.launch {
            repo.observeDashboard(from, to).collect { data ->
                val next = buildState(today, data)
                _state.value = next

                // Reminder up-to-date halten
                data.tasks.filter { it.isActive }.forEach { ReminderScheduler.scheduleNext(app, it) }
            }
        }
    }

    fun onNfcTextScanned(nfcText: String) {
        viewModelScope.launch {
            val today = DateTime.today()
            val match = _state.value.tasks.firstOrNull { it.nfcText == nfcText }
            if (match == null) {
                _events.tryEmit(HomeEvent.Snack("Kein Match: nfctext::$nfcText"))
                return@launch
            }
            if (!TaskRules.isDueOn(match, today)) {
                _events.tryEmit(HomeEvent.Snack("\"${match.title}\" ist heute nicht fällig."))
                return@launch
            }

            val inserted = repo.markDone(match.id, today)
            if (inserted) {
                ReminderScheduler.scheduleNext(getApplication(), match)
                _events.tryEmit(HomeEvent.Snack("Erledigt ✅ ${match.title}"))
            } else {
                _events.tryEmit(HomeEvent.Snack("Schon erledigt ✅ ${match.title}"))
            }
        }
    }

    fun onNfcScanFailed() {
        _events.tryEmit(HomeEvent.Snack("NFC-Tag nicht lesbar oder ohne passenden Text."))
    }

    fun resetSelectedToday(taskId: Long) {
        viewModelScope.launch {
            val today = DateTime.today()
            repo.resetToday(taskId, today)
            val task = _state.value.tasks.firstOrNull { it.id == taskId }
            if (task != null) ReminderScheduler.scheduleNext(getApplication(), task)
            _events.tryEmit(HomeEvent.Snack("Zurückgesetzt."))
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repo.softDeleteTask(taskId)
            ReminderScheduler.cancel(getApplication(), taskId)
            _events.tryEmit(HomeEvent.Snack("Gelöscht."))
        }
    }

    fun saveTask(draft: TaskDraft) {
        viewModelScope.launch {
            val now = DateTime.nowInstant().toEpochMilli()
            val entity = TaskEntity(
                id = 0,
                title = draft.title.trim(),
                scheduleType = draft.scheduleType,
                weeklyDay = draft.weeklyDay,
                monthlyDay = draft.monthlyDay,
                onceDateIso = draft.onceDateIso,
                reminderTimeMinutes = draft.reminderMinutes,
                nfcText = draft.nfcText.trim(),
                isActive = true,
                createdAtEpochMs = now,
                updatedAtEpochMs = now,
            )
            val id = repo.upsertTask(entity)
            val created = entity.copy(id = id)
            ReminderScheduler.scheduleNext(getApplication(), created)
            _events.tryEmit(HomeEvent.Snack("Aufgabe angelegt."))
        }
    }

    private fun buildState(today: LocalDate, data: DashboardData): HomeState {
        val completionsByTaskAndDay = data.completions.groupBy { it.taskId to it.dateIso }

        fun isDone(taskId: Long, day: LocalDate): Boolean =
            completionsByTaskAndDay.containsKey(taskId to day.toString())

        val tasksToday = data.tasks.filter { TaskRules.isDueOn(it, today) }
        val doneToday = tasksToday.count { isDone(it.id, today) }

        val uiTasksToday = tasksToday.map { t ->
            val hh = TaskRules.reminderHour(t).toString().padStart(2, '0')
            val mm = TaskRules.reminderMinute(t).toString().padStart(2, '0')
            UiTask(
                id = t.id,
                title = t.title,
                reminder = "$hh:$mm",
                isDoneToday = isDone(t.id, today),
                nfcText = t.nfcText,
            )
        }

        // ✅❌ Übersicht: Kalenderwoche Mo–So
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val days = (0..6).map { i ->
            val day = weekStart.plusDays(i.toLong())
            val due = data.tasks.filter { TaskRules.isDueOn(it, day) }
            val fulfilled = due.isNotEmpty() && due.all { isDone(it.id, day) }
            DayStatus(date = day, isFulfilled = fulfilled, isFuture = day.isAfter(today))
        }

        val streak = computeStreak(today, data.tasks, ::isDone)

        return HomeState(
            todayDone = doneToday,
            todayTotal = tasksToday.size,
            streak = streak,
            days = days,
            tasks = data.tasks,
            uiTasksToday = uiTasksToday,
        )
    }

    private fun computeStreak(
        today: LocalDate,
        tasks: List<TaskEntity>,
        isDone: (Long, LocalDate) -> Boolean,
    ): Int {
        var count = 0
        var d = today
        while (true) {
            val due = tasks.filter { TaskRules.isDueOn(it, d) }
            val fulfilled = due.isNotEmpty() && due.all { isDone(it.id, d) }
            if (!fulfilled) break
            count += 1
            d = d.minusDays(1)
        }
        return count
    }
}
