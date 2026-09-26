package com.example.util

import java.util.UUID

actual fun generateUUID(): String {
    return UUID.randomUUID().toString()
}

actual fun getCurrentTimeMillis(): Long {
    return System.currentTimeMillis()
}