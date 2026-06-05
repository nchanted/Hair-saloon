package com.hairsalon.tycoon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hairsalon.tycoon.game.GameViewModel
import com.hairsalon.tycoon.game.Phase
import com.hairsalon.tycoon.ui.theme.Cream
import com.hairsalon.tycoon.ui.theme.Pink500
import com.hairsalon.tycoon.ui.theme.PinkDark

@Composable
fun GameApp(vm: GameViewModel) {
    val s = vm.state
    when (s.phase) {
        Phase.MENU -> MenuScreen(
            hasSave = vm.hasSave,
            onContinue = vm::continueGame,
            onStart = vm::newGame
        )
        Phase.PLAYING -> GameScreen(s, onSeat = vm::seat, onEndDay = vm::endDay, onOpenShop = vm::openShop)
        Phase.SHOP -> ShopScreen(s, vm)
        Phase.DAY_SUMMARY -> DaySummaryScreen(s, onContinue = vm::startNextDay)
        Phase.GAME_OVER -> GameOverScreen(s, onRestart = vm::newGame, onMenu = vm::toMenu)
    }
}

@Composable
private fun MenuScreen(hasSave: Boolean, onContinue: () -> Unit, onStart: () -> Unit) {
    var showHelp by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Pink500, PinkDark)))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("\u2702\uFE0F", fontSize = 64.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "SALON TYCOON",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "Build your hair empire, one snip at a time.",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(40.dp))

            if (hasSave) {
                // Continue is the primary action when a save exists.
                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = Cream, contentColor = PinkDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("CONTINUE", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onStart,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text("New game") }
            } else {
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = Cream, contentColor = PinkDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("NEW GAME", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showHelp = !showHelp },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text(if (showHelp) "Hide instructions" else "How to play") }

            if (hasSave) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Starting a new game replaces your saved one.",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            if (showHelp) {
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Cream),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "\u2022 Clients walk in and wait. Tap a waiting client to seat them at a chair.\n" +
                        "\u2022 Your best free, rested stylist takes them automatically.\n" +
                        "\u2022 Beat their patience bar \u2014 if it empties, they leave and trash your reputation.\n" +
                        "\u2022 A high enough quality (vs. their expectation) earns tips and good reviews.\n" +
                        "\u2022 Stylists tire as they work and recover while idle, so don't run a one-person show.\n" +
                        "\u2022 At day's end you pay rent + wages, then start the next day.\n" +
                        "\u2022 Tap \uD83D\uDEE0\uFE0F Upgrades any time to hire, add chairs, train staff or renovate.\n" +
                        "\u2022 Each day clients are pickier, less patient and arrive faster.\n" +
                        "\u2022 Renovate late-game to unlock premium services and pricier clients!",
                        Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun DaySummaryScreen(
    s: com.hairsalon.tycoon.game.GameState,
    onContinue: () -> Unit
) {
    val sum = s.lastSummary
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Pink500, PinkDark)))
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(28.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("\uD83C\uDF19", fontSize = 48.sp)
            Text(
                "Day ${s.day} complete",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                s.tierName,
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(18.dp))
            if (sum != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Cream),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        SummaryLine("Clients served", "${sum.served}")
                        SummaryLine("Clients lost", "${sum.lost}")
                        SummaryLine("Earnings", "+\$${sum.earned}")
                        SummaryLine("Tips", "+\$${sum.tips}")
                        SummaryLine("Rent", "-\$${sum.rent}")
                        SummaryLine("Wages", "-\$${sum.wages}")
                        Spacer(Modifier.height(6.dp))
                        SummaryLine(
                            "Net",
                            if (sum.net >= 0) "+\$${sum.net}" else "-\$${-sum.net}",
                            bold = true
                        )
                        SummaryLine("In the till", "\$${s.money}", bold = true)
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Cream, contentColor = PinkDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("OPEN FOR DAY ${s.day + 1}", fontWeight = FontWeight.Bold, fontSize = 17.sp) }
            Spacer(Modifier.height(8.dp))
            Text(
                "Tip: tap \uD83D\uDEE0\uFE0F Upgrades on the salon floor to spend your earnings.",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontSize = 14.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun GameOverScreen(
    s: com.hairsalon.tycoon.game.GameState,
    onRestart: () -> Unit,
    onMenu: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PinkDark, androidx.compose.ui.graphics.Color(0xFF4A0E26))))
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("\uD83D\uDCBC", fontSize = 56.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "GAME OVER",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(12.dp))
            Text(
                s.gameOverReason ?: "The salon has closed.",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Cream),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Final report", fontWeight = FontWeight.Bold)
                    Text("Days survived: ${s.day}")
                    Text("Final salon: ${s.tierName} (Tier ${s.salonTier})")
                    Text("Reputation: ${s.reputation}")
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Cream, contentColor = PinkDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("PLAY AGAIN", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onMenu,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("Main menu") }
        }
    }
}
