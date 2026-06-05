package com.hairsalon.tycoon.game

import kotlin.math.max

/**
 * All gameplay tuning lives here so the difficulty curve and economy are easy to balance.
 */
object Balance {
    const val DAY_LENGTH = 50f          // seconds the salon is "open" each day
    const val START_MONEY = 220
    const val START_REPUTATION = 50
    const val START_STATIONS = 2
    const val MAX_QUEUE = 8
    const val MAX_EQUIPMENT_LEVEL = 6
    const val MAX_TIER = 4

    // ---- Difficulty curve: everything gets harder as the day count climbs ----
    fun spawnInterval(day: Int): Float = max(0.9f, 3.2f - day * 0.13f)
    fun patienceDrainPerSec(day: Int): Float = 5f + day * 0.7f
    fun expectationBonus(day: Int): Int = ((day - 1) * 1.6f).toInt()

    // ---- Economy ----
    fun rent(tier: Int): Int = 40 * tier
    fun payMultiplier(tier: Int): Float = when (tier) {
        1 -> 1.0f
        2 -> 1.35f
        3 -> 1.8f
        else -> 2.4f
    }
    fun maxReputationForTier(tier: Int): Int = 100 + (tier - 1) * 40
    fun chairCapForTier(tier: Int): Int = when (tier) {
        1 -> 3
        2 -> 5
        3 -> 7
        else -> 9
    }

    // ---- Shop costs ----
    fun hireCost(currentStaff: Int): Int = 130 + currentStaff * 70
    fun stationCost(currentStations: Int): Int = 120 + (currentStations - 1) * 90
    fun equipmentCost(level: Int): Int = 150 * (level + 1)
    fun trainSkillCost(skill: Int): Int = 50 * skill
    const val TRAIN_SPEED_COST = 140
    const val TRAIN_STAMINA_COST = 100
    fun renovateCost(currentTier: Int): Int = 600 * currentTier
    fun renovateRepRequirement(currentTier: Int): Int = 40 + currentTier * 20
}
