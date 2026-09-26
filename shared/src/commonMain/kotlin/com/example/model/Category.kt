package com.example.model

enum class Category(
    val displayName: String,
    val emoji: String
) {
    SALARY("Salaire", "💰"),
    FREELANCE("Freelance", "💻"),
    GIFTS("Cadeaux", "🎁"),
    INVESTMENTS("Investissements", "📈"),
    FOOD("Alimentation", "🍔"),
    TRANSPORT("Transport", "🚗"),
    HOUSING("Logement", "🏠"),
    UTILITIES("Factures", "⚡"),
    ENTERTAINMENT("Loisirs", "🎬"),
    HEALTH("Santé", "💊"),
    SHOPPING("Achats", "🛍️"),
    OTHER("Autre", "📦");

    /**
     * Alias pour maintenir la compatibilité avec le code UI
     */
    val label: String
        get() = displayName
}