package com.example.util

/**
 * Génère une chaîne de caractères UUID v4 unique.
 */
expect fun generateUUID(): String

/**
 * Renvoie le temps système actuel en millisecondes.
 */
expect fun getCurrentTimeMillis(): Long