package com.hairsalon.tycoon.game

/**
 * The complete, immutable game state. Every update produces a new copy, which makes
 * Compose recomposition trivial and the game logic easy to reason about.
 */
data class GameState(
    val phase: Phase = Phase.MENU,
    val day: Int = 1,
    val money: Int = Balance.START_MONEY,
    val reputation: Int = Balance.START_REPUTATION,
    val maxReputation: Int = Balance.maxReputationForTier(1),
    val salonTier: Int = 1,
    val stationCount: Int = Balance.START_STATIONS,
    val equipmentLevel: Int = 0,
    val stylists: List<Stylist> = emptyList(),
    val queue: List<Client> = emptyList(),
    val active: List<ActiveService> = emptyList(),
    val dayTimeLeft: Float = Balance.DAY_LENGTH,
    val spawnTimer: Float = 1.2f,
    // per-day tallies
    val servedToday: Int = 0,
    val lostToday: Int = 0,
    val earnedToday: Int = 0,
    val tipsToday: Int = 0,
    // misc
    val lastSummary: DaySummary? = null,
    val message: String? = null,
    val gameOverReason: String? = null
) {
    val tierName: String
        get() = when (salonTier) {
            1 -> "Corner Shop"
            2 -> "Style Studio"
            3 -> "Boutique Salon"
            else -> "Luxury Spa"
        }
}
