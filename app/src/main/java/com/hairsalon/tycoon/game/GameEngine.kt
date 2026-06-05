package com.hairsalon.tycoon.game

import kotlin.math.min
import kotlin.random.Random

/**
 * Pure game logic. Every function takes the current [GameState] and returns a new one.
 * No Android dependencies live here, so the rules are unit-testable and deterministic.
 */
object GameEngine {

    private var idCounter = 0L
    private fun nextId(): Long {
        idCounter += 1
        return idCounter
    }

    private val CLIENT_NAMES = listOf(
        "Ava", "Noah", "Sofia", "Liam", "Emma", "Mateo", "Zoe", "Omar",
        "Lily", "Jack", "Nina", "Ravi", "Cleo", "Maya", "Iris", "Theo"
    )
    private val CLIENT_FACES = listOf("\uD83E\uDDD1", "\uD83D\uDC69", "\uD83D\uDC68", "\uD83E\uDDD4", "\uD83D\uDC71\u200D\u2640\uFE0F", "\uD83D\uDC71", "\uD83D\uDC75", "\uD83D\uDC74", "\uD83E\uDDD3", "\uD83E\uDDD5")
    private val STYLIST_NAMES = listOf(
        "Mia", "Leo", "Coco", "Dax", "Remy", "Juno", "Pip", "Sage", "Bex", "Kit", "Nova", "Ace"
    )
    private val STYLIST_EMOJI = listOf("\uD83D\uDC87\u200D\u2640\uFE0F", "\uD83D\uDC88", "\uD83D\uDC87\u200D\u2642\uFE0F", "\uD83E\uDDD1\u200D\uD83C\uDFA4", "\uD83D\uDC85")

    // -------------------------------------------------------------------------
    //  Lifecycle
    // -------------------------------------------------------------------------

    fun newGame(): GameState {
        idCounter = 0
        val stylists = listOf(
            Stylist(1, "Mia", "\uD83D\uDC87\u200D\u2640\uFE0F", skill = 5, speed = 1.0f, maxStamina = 100f, stamina = 100f, wage = 30),
            Stylist(2, "Leo", "\uD83D\uDC88", skill = 4, speed = 1.1f, maxStamina = 100f, stamina = 100f, wage = 28)
        )
        return GameState(
            phase = Phase.PLAYING,
            day = 1,
            money = Balance.START_MONEY,
            reputation = Balance.START_REPUTATION,
            maxReputation = Balance.maxReputationForTier(1),
            salonTier = 1,
            stationCount = Balance.START_STATIONS,
            equipmentLevel = 0,
            stylists = stylists,
            dayTimeLeft = Balance.DAY_LENGTH,
            spawnTimer = 1.2f,
            message = "Tap a waiting client to seat them. Don't keep people waiting!"
        )
    }

    // -------------------------------------------------------------------------
    //  Real-time tick (called many times per second while PLAYING)
    // -------------------------------------------------------------------------

    fun tick(s: GameState, dt: Float): GameState {
        if (s.phase != Phase.PLAYING) return s

        var money = s.money
        var rep = s.reputation
        var served = s.servedToday
        var lost = s.lostToday
        var earned = s.earnedToday
        var tips = s.tipsToday

        // 1. Advance in-progress services and pay out the ones that finished.
        val ongoing = ArrayList<ActiveService>(s.active.size)
        for (a in s.active) {
            val np = a.progress + dt / a.durationSec
            if (np < 1f) {
                ongoing.add(a.copy(progress = np))
                continue
            }
            // Service complete -> settle up.
            val c = a.client
            if (a.quality >= c.expectation) {
                val margin = a.quality - c.expectation
                money += c.payValue
                earned += c.payValue
                val tip = (c.payValue * (0.10f + min(0.25f, margin / 120f))).toInt()
                if (tip > 0) {
                    money += tip
                    tips += tip
                }
                rep += if (margin >= 15) 3 else 2
            } else {
                val shortfall = c.expectation - a.quality
                val pay = (c.payValue * (a.quality.toFloat() / c.expectation).coerceAtLeast(0.2f)).toInt()
                money += pay
                earned += pay
                rep -= when {
                    shortfall >= 25 -> 4
                    shortfall >= 12 -> 2
                    else -> 1
                }
            }
            served += 1
        }

        // 2. Stamina: busy stylists tire, idle ones recover.
        val busyIds = ongoing.map { it.stylistId }.toHashSet()
        val stylists = s.stylists.map { st ->
            val delta = if (st.id in busyIds) -6f * dt else 9f * dt
            st.copy(stamina = (st.stamina + delta).coerceIn(0f, st.maxStamina))
        }

        // 3. Queue patience drains; impatient clients storm out.
        val drain = Balance.patienceDrainPerSec(s.day) * dt
        val newQueue = ArrayList<Client>(s.queue.size)
        for (c in s.queue) {
            val p = c.patience - drain
            if (p <= 0f) {
                rep -= 4
                lost += 1
            } else {
                newQueue.add(c.copy(patience = p))
            }
        }

        // 4. Spawn new clients while the salon is open.
        var spawnTimer = s.spawnTimer
        val timeLeft = (s.dayTimeLeft - dt).coerceAtLeast(0f)
        if (s.dayTimeLeft > 0f && newQueue.size < Balance.MAX_QUEUE) {
            spawnTimer -= dt
            if (spawnTimer <= 0f) {
                newQueue.add(makeClient(s.day, s.salonTier))
                spawnTimer = Balance.spawnInterval(s.day) * (0.8f + Random.nextFloat() * 0.4f)
            }
        }

        rep = rep.coerceIn(0, s.maxReputation)

        // Reputation bottoming out ends the run immediately.
        if (rep <= 0) {
            return s.copy(
                money = money, reputation = 0, stylists = stylists,
                queue = newQueue, active = ongoing,
                servedToday = served, lostToday = lost, earnedToday = earned, tipsToday = tips,
                phase = Phase.GAME_OVER,
                gameOverReason = "Your reputation hit zero. Bad reviews forced the salon to close its doors."
            )
        }

        val base = s.copy(
            money = money, reputation = rep, stylists = stylists,
            queue = newQueue, active = ongoing, spawnTimer = spawnTimer, dayTimeLeft = timeLeft,
            servedToday = served, lostToday = lost, earnedToday = earned, tipsToday = tips
        )

        // Auto-close once the day is over and there's nobody left to serve.
        return if (timeLeft <= 0f && newQueue.isEmpty() && ongoing.isEmpty()) endDay(base) else base
    }

    // -------------------------------------------------------------------------
    //  Player actions during a day
    // -------------------------------------------------------------------------

    fun seatClient(s: GameState, clientId: Long): GameState {
        if (s.phase != Phase.PLAYING) return s
        if (s.active.size >= s.stationCount) return s.copy(message = "Every chair is full!")

        val busy = s.active.map { it.stylistId }.toHashSet()
        val stylist = s.stylists
            .filter { it.id !in busy && it.stamina >= 20f }
            .maxByOrNull { it.skill }
            ?: return s.copy(message = "No stylist is free & rested right now.")

        val client = s.queue.firstOrNull { it.id == clientId } ?: return s
        val quality = computeQuality(stylist, s.equipmentLevel)
        val duration = client.service.baseDurationSec / (stylist.speed * (1f + s.equipmentLevel * 0.06f))

        return s.copy(
            queue = s.queue.filter { it.id != clientId },
            active = s.active + ActiveService(client, stylist.id, 0f, quality, duration),
            message = null
        )
    }

    fun endDay(s: GameState): GameState {
        val rent = Balance.rent(s.salonTier)
        val wages = s.stylists.sumOf { it.wage }
        val money = s.money - rent - wages
        val net = s.earnedToday + s.tipsToday - rent - wages
        val summary = DaySummary(
            day = s.day, served = s.servedToday, lost = s.lostToday,
            earned = s.earnedToday, tips = s.tipsToday, rent = rent, wages = wages, net = net
        )

        if (money < 0) {
            return s.copy(
                money = money, phase = Phase.GAME_OVER, lastSummary = summary,
                queue = emptyList(), active = emptyList(),
                gameOverReason = "Bankrupt! You couldn't cover today's rent and wages."
            )
        }

        return s.copy(
            money = money, phase = Phase.SHOP, lastSummary = summary,
            queue = emptyList(), active = emptyList(),
            stylists = s.stylists.map { it.copy(stamina = it.maxStamina) },
            message = null
        )
    }

    fun startNextDay(s: GameState): GameState = s.copy(
        phase = Phase.PLAYING,
        day = s.day + 1,
        dayTimeLeft = Balance.DAY_LENGTH,
        spawnTimer = 1.2f,
        servedToday = 0, lostToday = 0, earnedToday = 0, tipsToday = 0,
        queue = emptyList(), active = emptyList(),
        stylists = s.stylists.map { it.copy(stamina = it.maxStamina) },
        message = null
    )

    fun toMenu(): GameState = GameState()

    // -------------------------------------------------------------------------
    //  Shop / upgrades (between days)
    // -------------------------------------------------------------------------

    fun hire(s: GameState): GameState {
        val cost = Balance.hireCost(s.stylists.size)
        if (s.money < cost) return s.copy(message = "Not enough money to hire.")
        val newId = (s.stylists.maxOfOrNull { it.id } ?: 0) + 1
        val skill = Random.nextInt(3, 7)
        val speed = 0.9f + Random.nextInt(0, 4) * 0.1f
        val name = STYLIST_NAMES.filter { n -> s.stylists.none { it.name == n } }
            .randomOrNull() ?: "Stylist $newId"
        val stylist = Stylist(
            id = newId, name = name, emoji = STYLIST_EMOJI.random(),
            skill = skill, speed = speed, maxStamina = 100f, stamina = 100f, wage = 20 + skill * 4
        )
        return s.copy(money = s.money - cost, stylists = s.stylists + stylist, message = "Hired $name!")
    }

    fun addStation(s: GameState): GameState {
        val cap = Balance.chairCapForTier(s.salonTier)
        if (s.stationCount >= cap) return s.copy(message = "Renovate to fit more chairs.")
        val cost = Balance.stationCost(s.stationCount)
        if (s.money < cost) return s.copy(message = "Not enough money for a new chair.")
        return s.copy(money = s.money - cost, stationCount = s.stationCount + 1, message = "Added a chair!")
    }

    fun upgradeEquipment(s: GameState): GameState {
        if (s.equipmentLevel >= Balance.MAX_EQUIPMENT_LEVEL) return s.copy(message = "Equipment maxed out.")
        val cost = Balance.equipmentCost(s.equipmentLevel)
        if (s.money < cost) return s.copy(message = "Not enough money for equipment.")
        return s.copy(money = s.money - cost, equipmentLevel = s.equipmentLevel + 1, message = "Equipment upgraded!")
    }

    fun trainSkill(s: GameState, id: Int): GameState {
        val st = s.stylists.firstOrNull { it.id == id } ?: return s
        if (st.skill >= 12) return s.copy(message = "${st.name} is already a master.")
        val cost = Balance.trainSkillCost(st.skill)
        if (s.money < cost) return s.copy(message = "Not enough money to train.")
        return s.copy(
            money = s.money - cost,
            stylists = s.stylists.map { if (it.id == id) it.copy(skill = it.skill + 1) else it },
            message = "${st.name}'s skill improved!"
        )
    }

    fun trainSpeed(s: GameState, id: Int): GameState {
        val st = s.stylists.firstOrNull { it.id == id } ?: return s
        if (st.speed >= 1.6f) return s.copy(message = "${st.name} is already lightning fast.")
        if (s.money < Balance.TRAIN_SPEED_COST) return s.copy(message = "Not enough money to train.")
        return s.copy(
            money = s.money - Balance.TRAIN_SPEED_COST,
            stylists = s.stylists.map { if (it.id == id) it.copy(speed = (it.speed + 0.08f)) else it },
            message = "${st.name} works faster now!"
        )
    }

    fun trainStamina(s: GameState, id: Int): GameState {
        val st = s.stylists.firstOrNull { it.id == id } ?: return s
        if (st.maxStamina >= 200f) return s.copy(message = "${st.name} has boundless energy.")
        if (s.money < Balance.TRAIN_STAMINA_COST) return s.copy(message = "Not enough money to train.")
        return s.copy(
            money = s.money - Balance.TRAIN_STAMINA_COST,
            stylists = s.stylists.map {
                if (it.id == id) it.copy(maxStamina = it.maxStamina + 20f, stamina = it.maxStamina + 20f) else it
            },
            message = "${st.name}'s stamina increased!"
        )
    }

    fun canRenovate(s: GameState): Boolean = s.salonTier < Balance.MAX_TIER

    fun renovate(s: GameState): GameState {
        if (s.salonTier >= Balance.MAX_TIER) return s.copy(message = "Your salon is already top tier!")
        val cost = Balance.renovateCost(s.salonTier)
        val repReq = Balance.renovateRepRequirement(s.salonTier)
        if (s.reputation < repReq) return s.copy(message = "Need $repReq reputation to renovate.")
        if (s.money < cost) return s.copy(message = "Not enough money to renovate.")
        val newTier = s.salonTier + 1
        val newState = s.copy(
            money = s.money - cost,
            salonTier = newTier,
            maxReputation = Balance.maxReputationForTier(newTier)
        )
        return newState.copy(message = "Renovated into a ${newState.tierName}! Pricier clients incoming.")
    }

    // -------------------------------------------------------------------------
    //  Helpers
    // -------------------------------------------------------------------------

    private fun computeQuality(st: Stylist, equip: Int): Int {
        val staminaFactor = (st.stamina / st.maxStamina) * 8f
        val q = st.skill * 9 + equip * 5 + staminaFactor + Random.nextInt(-4, 5)
        return q.toInt().coerceAtLeast(1)
    }

    private fun makeClient(day: Int, tier: Int): Client {
        val service = pickService(tier)
        val expectation = (service.baseExpectation + Balance.expectationBonus(day) + Random.nextInt(-3, 6))
            .coerceAtLeast(10)
        val pay = (service.basePay * Balance.payMultiplier(tier)).toInt()
        return Client(
            id = nextId(),
            name = CLIENT_NAMES.random(),
            face = CLIENT_FACES.random(),
            service = service,
            expectation = expectation,
            payValue = pay,
            patience = 100f,
            maxPatience = 100f
        )
    }

    /** Cheaper services show up more often; premium work is rarer. */
    private fun pickService(tier: Int): ServiceType {
        val available = ServiceType.entries.filter { it.minTier <= tier }
        val weights = available.map { 120f / it.basePay }
        val total = weights.sum()
        var r = Random.nextFloat() * total
        for (i in available.indices) {
            r -= weights[i]
            if (r <= 0f) return available[i]
        }
        return available.last()
    }
}
