package com.example.nfcdailycheckin.ui.theme

import java.text.Normalizer

fun slugify(input: String): String {
    val normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    return normalized
        .lowercase()
        .replace("[^a-z0-9]+".toRegex(), "_")
        .trim('_')
        .take(48)
}
