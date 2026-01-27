package com.example.nfcdailycheckin.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef

object NfcTextParser {
    private const val PREFIX = "nfctext::"

    fun readNfctext(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            val msg = ndef.ndefMessage ?: return null
            extractText(msg)
        } finally {
            try { ndef.close() } catch (_: Throwable) {}
        }
    }

    fun extractText(message: NdefMessage): String? {
        val records = message.records ?: return null
        for (record in records) {
            val text = parseTextRecord(record) ?: continue
            if (text.startsWith(PREFIX)) {
                return text.removePrefix(PREFIX)
            }
        }
        return null
    }

    private fun parseTextRecord(record: NdefRecord): String? {
        if (record.tnf != NdefRecord.TNF_WELL_KNOWN) return null
        if (!record.type.contentEquals(NdefRecord.RTD_TEXT)) return null
        val payload = record.payload ?: return null
        if (payload.isEmpty()) return null
        // NFC Forum "Text Record Type Definition"
        val status = payload[0].toInt()
        val isUtf16 = (status and 0x80) != 0
        val langLen = status and 0x3F
        val encoding = if (isUtf16) Charsets.UTF_16 else Charsets.UTF_8
        return try {
            String(payload, 1 + langLen, payload.size - 1 - langLen, encoding)
        } catch (_: Throwable) {
            null
        }
    }
}
