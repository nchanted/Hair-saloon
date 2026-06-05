package com.hairsalon.tycoon.game

/** Which screen / mode the game is in. */
enum class Phase { MENU, PLAYING, SHOP, DAY_SUMMARY, GAME_OVER }

/** A type of service a client can request. Higher tiers unlock pricier work. */
enum class ServiceType(
    val label: String,
    val baseDurationSec: Float,
    val basePay: Int,
    val baseExpectation: Int,
    val minTier: Int,
    val emoji: String
) {
    TRIM("Trim", 4f, 14, 25, 1, "✂\uFE0F"),
    BLOWDRY("Blow-dry", 6f, 20, 35, 1, "\uD83D\uDCA8"),
    HAIRCUT("Haircut", 7f, 26, 42, 1, "\uD83D\uDC87"),
    COLOR("Color", 12f, 55, 60, 1, "\uD83C\uDFA8"),
    UPDO("Updo", 9f, 44, 56, 2, "\uD83D\uDC70"),
    PREMIUM("Makeover", 15f, 120, 82, 2, "\u2728");
}

/** A walk-in customer waiting in the queue. */
data class Client(
    val id: Long,
    val name: String,
    val face: String,
    val service: ServiceType,
    val expectation: Int,   // quality required to fully satisfy them
    val payValue: Int,      // what a satisfied client pays
    val patience: Float,    // current patience (0..maxPatience)
    val maxPatience: Float
)

/** A member of staff. */
data class Stylist(
    val id: Int,
    val name: String,
    val emoji: String,
    val skill: Int,         // drives quality of result
    val speed: Float,       // multiplier on how fast they work
    val maxStamina: Float,
    val stamina: Float,     // drains while working, recovers while idle
    val wage: Int           // paid at the end of every day
)

/** A client currently being served at a chair. */
data class ActiveService(
    val client: Client,
    val stylistId: Int,
    val progress: Float,    // 0..1
    val quality: Int,       // locked in when the client is seated
    val durationSec: Float
)

/** End-of-day financial recap shown in the shop. */
data class DaySummary(
    val day: Int,
    val served: Int,
    val lost: Int,
    val earned: Int,
    val tips: Int,
    val rent: Int,
    val wages: Int,
    val net: Int
)
