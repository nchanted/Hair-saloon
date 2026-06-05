package com.hairsalon.tycoon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hairsalon.tycoon.game.GameViewModel
import com.hairsalon.tycoon.ui.GameApp
import com.hairsalon.tycoon.ui.theme.SalonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SalonTheme {
                val vm: GameViewModel = viewModel()
                GameApp(vm)
            }
        }
    }
}
