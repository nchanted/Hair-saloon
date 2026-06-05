package com.hairsalon.tycoon.game

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Holds the live [GameState] and drives the real-time game loop with a coroutine.
 * Also handles persistence: the game auto-saves at natural checkpoints (entering the
 * shop, starting a day, buying upgrades, and when the app goes to the background) so
 * the player can quit and later continue.
 */
class GameViewModel(app: Application) : AndroidViewModel(app) {

    var state by mutableStateOf(GameState())
        private set

    /** Whether a resumable save exists. Drives the menu's "Continue" button. */
    var hasSave by mutableStateOf(false)
        private set

    init {
        hasSave = SaveManager.hasSave(getApplication())
        viewModelScope.launch {
            var last = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                // Clamp dt so a paused/backgrounded app doesn't fast-forward on resume.
                val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.1f)
                last = now
                if (state.phase == Phase.PLAYING) {
                    val next = GameEngine.tick(state, dt)
                    state = next
                    // A tick can auto-close the day (-> SHOP) or end the run (-> GAME_OVER).
                    when (next.phase) {
                        Phase.SHOP -> persist()
                        Phase.GAME_OVER -> clearSave()
                        else -> {}
                    }
                }
                delay(50) // ~20 updates per second
            }
        }
    }

    // ---- persistence helpers ----

    private fun persist() {
        if (state.phase == Phase.GAME_OVER) return
        SaveManager.save(getApplication(), state)
        hasSave = true
    }

    private fun clearSave() {
        SaveManager.clear(getApplication())
        hasSave = false
    }

    /** Called when the app is backgrounded, to checkpoint even mid-day progress. */
    fun onAppBackground() {
        if (state.phase == Phase.PLAYING || state.phase == Phase.SHOP) persist()
    }

    // ---- lifecycle / navigation ----

    fun newGame() {
        clearSave()
        state = GameEngine.newGame()
        persist()
    }

    fun continueGame() {
        val loaded = SaveManager.load(getApplication())
        if (loaded != null && loaded.phase != Phase.GAME_OVER) {
            state = GameEngine.resumeFrom(loaded)
                .copy(message = "Welcome back! Picking up where you left off.")
        } else {
            // Corrupt or absent save: fall back to a fresh game.
            clearSave()
            state = GameEngine.newGame()
            persist()
        }
    }

    fun seat(clientId: Long) { state = GameEngine.seatClient(state, clientId) }

    fun endDay() {
        state = GameEngine.endDay(state)
        if (state.phase == Phase.GAME_OVER) clearSave() else persist()
    }

    fun startNextDay() {
        state = GameEngine.startNextDay(state)
        persist()
    }

    fun toMenu() { state = GameEngine.toMenu() }

    // ---- shop actions (each persists so progress survives a quit) ----

    fun hire() { state = GameEngine.hire(state); persist() }
    fun buyStation() { state = GameEngine.addStation(state); persist() }
    fun upgradeEquipment() { state = GameEngine.upgradeEquipment(state); persist() }
    fun trainSkill(id: Int) { state = GameEngine.trainSkill(state, id); persist() }
    fun trainSpeed(id: Int) { state = GameEngine.trainSpeed(state, id); persist() }
    fun trainStamina(id: Int) { state = GameEngine.trainStamina(state, id); persist() }
    fun renovate() { state = GameEngine.renovate(state); persist() }
}
