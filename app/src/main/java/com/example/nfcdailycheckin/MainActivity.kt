package com.example.nfcdailycheckin

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.nfcdailycheckin.nfc.NfcTextParser
import com.example.nfcdailycheckin.ui.AppRoot
import com.example.nfcdailycheckin.ui.HomeViewModel
import com.example.nfcdailycheckin.ui.HomeViewModelFactory
import com.example.nfcdailycheckin.ui.theme.DailyCheckinTheme
import com.example.nfcdailycheckin.ui.theme.ThemeMode
import com.example.nfcdailycheckin.ui.theme.ThemePref

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(application)
    }

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            // Aktuelle Theme-Einstellung, beim Start aus dem Speicher geladen.
            var themeMode by remember { mutableStateOf(ThemePref.load(this)) }

            DailyCheckinTheme(themeMode = themeMode) {
                AppRoot(
                    viewModel = viewModel,
                    themeMode = themeMode,
                    onThemeChange = { newMode ->
                        themeMode = newMode
                        ThemePref.save(this, newMode)
                    }
                )
            }
        }

        // Handle launch via NFC intent-filter (NDEF_DISCOVERED)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        enableReaderMode()
    }

    override fun onPause() {
        super.onPause()
        disableReaderMode()
    }

    private fun enableReaderMode() {
        val adapter = nfcAdapter ?: return
        adapter.enableReaderMode(
            this,
            { tag ->
                val text = NfcTextParser.readNfctext(tag)
                if (text != null) {
                    viewModel.onNfcTextScanned(text)
                } else {
                    viewModel.onNfcScanFailed()
                }
            },
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V,
            Bundle()
        )
    }

    private fun disableReaderMode() {
        nfcAdapter?.disableReaderMode(this)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
        val text = NfcTextParser.readNfctext(tag)
        if (text != null) {
            viewModel.onNfcTextScanned(text)
        } else {
            viewModel.onNfcScanFailed()
        }
    }
}