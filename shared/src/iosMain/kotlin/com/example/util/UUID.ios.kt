package com.example.util

import kotlinx.datetime.Clock
import kotlin.random.Random

actual fun generateUUID(): String {
    // Générateur d'UUID v4 Kotlin pur (évite d'appeler NSUUID sous Windows)
    val randomBytes = ByteArray(16)
    Random.nextBytes(randomBytes)
    randomBytes[6] = (randomBytes[6].toInt() and 0x0f or 0x40).toByte()
    randomBytes[8] = (randomBytes[8].toInt() and 0x3f or 0x80).toByte()

    return buildString {
        for (i in randomBytes.indices) {
            if (i == 4 || i == 6 || i == 8 || i == 10) append('-')
            append(randomBytes[i].toUByte().toString(16).padStart(2, '0'))
        }
    }
}

actual fun getCurrentTimeMillis(): Long {
    return Clock.System.now().toEpochMilliseconds()
}