package com.hairsalon.tycoon.game

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Saves and restores the whole [GameState] as JSON in SharedPreferences.
 * Uses only framework APIs (android.content + org.json), so no extra dependencies.
 */
object SaveManager {
    private const val PREFS = "salon_tycoon_save"
    private const val KEY = "game_state"

    fun hasSave(ctx: Context): Boolean =
        !ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null).isNullOrBlank()

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }

    fun save(ctx: Context, s: GameState) {
        runCatching {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, encode(s).toString()).apply()
        }
    }

    fun load(ctx: Context): GameState? {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null) ?: return null
        return runCatching { decode(JSONObject(raw)) }.getOrNull()
    }

    // ---- encode ----

    private fun encode(s: GameState): JSONObject = JSONObject().apply {
        put("phase", s.phase.name)
        put("day", s.day)
        put("money", s.money)
        put("reputation", s.reputation)
        put("maxReputation", s.maxReputation)
        put("salonTier", s.salonTier)
        put("stationCount", s.stationCount)
        put("equipmentLevel", s.equipmentLevel)
        put("stylists", JSONArray().also { arr -> s.stylists.forEach { arr.put(encStylist(it)) } })
        put("queue", JSONArray().also { arr -> s.queue.forEach { arr.put(encClient(it)) } })
        put("active", JSONArray().also { arr -> s.active.forEach { arr.put(encActive(it)) } })
        put("dayTimeLeft", s.dayTimeLeft.toDouble())
        put("spawnTimer", s.spawnTimer.toDouble())
        put("servedToday", s.servedToday)
        put("lostToday", s.lostToday)
        put("earnedToday", s.earnedToday)
        put("tipsToday", s.tipsToday)
        s.lastSummary?.let { put("lastSummary", encSummary(it)) }
    }

    private fun encStylist(st: Stylist) = JSONObject().apply {
        put("id", st.id)
        put("name", st.name)
        put("emoji", st.emoji)
        put("skill", st.skill)
        put("speed", st.speed.toDouble())
        put("maxStamina", st.maxStamina.toDouble())
        put("stamina", st.stamina.toDouble())
        put("wage", st.wage)
    }

    private fun encClient(c: Client) = JSONObject().apply {
        put("id", c.id)
        put("name", c.name)
        put("face", c.face)
        put("service", c.service.name)
        put("expectation", c.expectation)
        put("payValue", c.payValue)
        put("patience", c.patience.toDouble())
        put("maxPatience", c.maxPatience.toDouble())
    }

    private fun encActive(a: ActiveService) = JSONObject().apply {
        put("client", encClient(a.client))
        put("stylistId", a.stylistId)
        put("progress", a.progress.toDouble())
        put("quality", a.quality)
        put("durationSec", a.durationSec.toDouble())
    }

    private fun encSummary(d: DaySummary) = JSONObject().apply {
        put("day", d.day)
        put("served", d.served)
        put("lost", d.lost)
        put("earned", d.earned)
        put("tips", d.tips)
        put("rent", d.rent)
        put("wages", d.wages)
        put("net", d.net)
    }

    // ---- decode ----

    private fun decode(o: JSONObject): GameState = GameState(
        phase = Phase.valueOf(o.getString("phase")),
        day = o.getInt("day"),
        money = o.getInt("money"),
        reputation = o.getInt("reputation"),
        maxReputation = o.getInt("maxReputation"),
        salonTier = o.getInt("salonTier"),
        stationCount = o.getInt("stationCount"),
        equipmentLevel = o.getInt("equipmentLevel"),
        stylists = o.getJSONArray("stylists").objs().map { decStylist(it) },
        queue = o.getJSONArray("queue").objs().map { decClient(it) },
        active = o.getJSONArray("active").objs().map { decActive(it) },
        dayTimeLeft = o.getDouble("dayTimeLeft").toFloat(),
        spawnTimer = o.getDouble("spawnTimer").toFloat(),
        servedToday = o.getInt("servedToday"),
        lostToday = o.getInt("lostToday"),
        earnedToday = o.getInt("earnedToday"),
        tipsToday = o.getInt("tipsToday"),
        lastSummary = if (o.has("lastSummary")) decSummary(o.getJSONObject("lastSummary")) else null,
        message = null,
        gameOverReason = null
    )

    private fun decStylist(o: JSONObject) = Stylist(
        id = o.getInt("id"),
        name = o.getString("name"),
        emoji = o.getString("emoji"),
        skill = o.getInt("skill"),
        speed = o.getDouble("speed").toFloat(),
        maxStamina = o.getDouble("maxStamina").toFloat(),
        stamina = o.getDouble("stamina").toFloat(),
        wage = o.getInt("wage")
    )

    private fun decClient(o: JSONObject) = Client(
        id = o.getLong("id"),
        name = o.getString("name"),
        face = o.getString("face"),
        service = ServiceType.valueOf(o.getString("service")),
        expectation = o.getInt("expectation"),
        payValue = o.getInt("payValue"),
        patience = o.getDouble("patience").toFloat(),
        maxPatience = o.getDouble("maxPatience").toFloat()
    )

    private fun decActive(o: JSONObject) = ActiveService(
        client = decClient(o.getJSONObject("client")),
        stylistId = o.getInt("stylistId"),
        progress = o.getDouble("progress").toFloat(),
        quality = o.getInt("quality"),
        durationSec = o.getDouble("durationSec").toFloat()
    )

    private fun decSummary(o: JSONObject) = DaySummary(
        day = o.getInt("day"),
        served = o.getInt("served"),
        lost = o.getInt("lost"),
        earned = o.getInt("earned"),
        tips = o.getInt("tips"),
        rent = o.getInt("rent"),
        wages = o.getInt("wages"),
        net = o.getInt("net")
    )

    private fun JSONArray.objs(): List<JSONObject> = List(length()) { getJSONObject(it) }
}
