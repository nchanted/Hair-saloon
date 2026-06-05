package com.hairsalon.tycoon.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Holds the live [GameState] and drives the real-time game loop with a coroutine.
 * The UI just reads [state] and calls the action methods.
 */
class GameViewModel : ViewModel() {

    var state by mutableStateOf(GameState())
        private set

    init {
        viewModelScope.launch {
            var last = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                // Clamp dt so a paused/backgrounded app doesn't fast-forward on resume.
                val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.1f)
                last = now
                if (state.phase == Phase.PLAYING) {
                    state = GameEngine.tick(state, dt)
                }
                delay(50) // ~20 updates per second
            }
        }
    }

    fun newGame() { state = GameEngine.newGame() }
    fun seat(clientId: Long) { state = GameEngine.seatClient(state, clientId) }
    fun endDay() { state = GameEngine.endDay(state) }
    fun startNextDay() { state = GameEngine.startNextDay(state) }
    fun toMenu() { state = GameEngine.toMenu() }

    fun hire() { state = GameEngine.hire(state) }
    fun buyStation() { state = GameEngine.addStation(state) }
    fun upgradeEquipment() { state = GameEngine.upgradeEquipment(state) }
    fun trainSkill(id: Int) { state = GameEngine.trainSkill(state, id) }
    fun trainSpeed(id: Int) { state = GameEngine.trainSpeed(state, id) }
    fun trainStamina(id: Int) { state = GameEngine.trainStamina(state, id) }
    fun renovate() { state = GameEngine.renovate(state) }
}
