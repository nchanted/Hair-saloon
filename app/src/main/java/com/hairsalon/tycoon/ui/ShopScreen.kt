package com.hairsalon.tycoon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hairsalon.tycoon.game.Balance
import com.hairsalon.tycoon.game.GameEngine
import com.hairsalon.tycoon.game.GameState
import com.hairsalon.tycoon.game.GameViewModel
import com.hairsalon.tycoon.game.Stylist
import com.hairsalon.tycoon.ui.theme.Cream
import com.hairsalon.tycoon.ui.theme.Pink500
import com.hairsalon.tycoon.ui.theme.PinkDark
import com.hairsalon.tycoon.ui.theme.Teal

@Composable
fun ShopScreen(s: GameState, vm: GameViewModel) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Pink500, PinkDark)))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ---- Header ----
            Text(
                "\uD83D\uDEE0\uFE0F UPGRADES",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "${s.tierName}  \u2022  Tier ${s.salonTier}  \u2022  Day ${s.day}",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(14.dp))

            // ---- Wallet ----
            Card(
                colors = CardDefaults.cardColors(containerColor = Teal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "In the till",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        "\$${s.money}",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            s.message?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = androidx.compose.ui.graphics.Color.White, fontSize = 13.sp)
            }
            Spacer(Modifier.height(16.dp))

            // ---- Salon upgrades ----
            SectionTitle("Grow your salon")
            Spacer(Modifier.height(8.dp))

            val hireCost = Balance.hireCost(s.stylists.size)
            ShopRow(
                title = "Hire a stylist",
                subtitle = "Add another pair of hands. (${s.stylists.size} on staff)",
                cost = hireCost,
                affordable = s.money >= hireCost,
                onBuy = vm::hire
            )

            val chairCap = Balance.chairCapForTier(s.salonTier)
            val chairsMaxed = s.stationCount >= chairCap
            val stationCost = Balance.stationCost(s.stationCount)
            ShopRow(
                title = "Add a chair",
                subtitle = if (chairsMaxed) "At capacity \u2014 renovate for more (${s.stationCount}/$chairCap)"
                           else "Serve more clients at once. (${s.stationCount}/$chairCap)",
                cost = stationCost,
                affordable = !chairsMaxed && s.money >= stationCost,
                disabled = chairsMaxed,
                onBuy = vm::buyStation
            )

            val equipMaxed = s.equipmentLevel >= Balance.MAX_EQUIPMENT_LEVEL
            val equipCost = Balance.equipmentCost(s.equipmentLevel)
            ShopRow(
                title = "Upgrade equipment",
                subtitle = if (equipMaxed) "Top of the line! (Lv ${s.equipmentLevel})"
                           else "Better quality & faster work. (Lv ${s.equipmentLevel})",
                cost = equipCost,
                affordable = !equipMaxed && s.money >= equipCost,
                disabled = equipMaxed,
                onBuy = vm::upgradeEquipment
            )

            // ---- Renovate (late-game) ----
            Spacer(Modifier.height(8.dp))
            if (GameEngine.canRenovate(s)) {
                val renoCost = Balance.renovateCost(s.salonTier)
                val repReq = Balance.renovateRepRequirement(s.salonTier)
                val repOk = s.reputation >= repReq
                val nextName = nextTierName(s.salonTier)
                ShopRow(
                    title = "\u2B50 Renovate \u2192 $nextName",
                    subtitle = if (!repOk) "Needs $repReq reputation (you have ${s.reputation})"
                               else "Bigger space, pricier clients, premium services.",
                    cost = renoCost,
                    affordable = repOk && s.money >= renoCost,
                    disabled = !repOk,
                    highlight = true,
                    onBuy = vm::renovate
                )
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Cream),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "\uD83C\uDFC6 Luxury Spa \u2014 your salon is fully renovated!",
                        Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        color = PinkDark
                    )
                }
            }

            // ---- Train staff ----
            Spacer(Modifier.height(18.dp))
            SectionTitle("Train your staff")
            Spacer(Modifier.height(8.dp))
            s.stylists.forEach { st -> StylistTrainingCard(st, s, vm) }

            // ---- Back to the salon floor ----
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = vm::closeShop,
                colors = ButtonDefaults.buttonColors(containerColor = Cream, contentColor = PinkDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text("\u2190 Back to Salon", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = androidx.compose.ui.graphics.Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun ShopRow(
    title: String,
    subtitle: String,
    cost: Int,
    affordable: Boolean,
    disabled: Boolean = false,
    highlight: Boolean = false,
    onBuy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (highlight) Cream else Cream),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PinkDark)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onBuy,
                enabled = affordable && !disabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (highlight) PinkDark else Pink500,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) { Text(if (disabled) "\u2014" else "\$$cost", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun StylistTrainingCard(st: Stylist, s: GameState, vm: GameViewModel) {
    val skillMaxed = st.skill >= 12
    val speedMaxed = st.speed >= 1.6f
    val staminaMaxed = st.maxStamina >= 200f
    val skillCost = Balance.trainSkillCost(st.skill)
    Card(
        colors = CardDefaults.cardColors(containerColor = Cream),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                "${st.emoji}  ${st.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PinkDark
            )
            Text(
                "Skill ${st.skill}  \u2022  Speed ${"%.2f".format(st.speed)}x  \u2022  Stamina ${st.maxStamina.toInt()}  \u2022  Wage \$${st.wage}/day",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TrainButton(
                    label = "Skill",
                    cost = skillCost,
                    enabled = !skillMaxed && s.money >= skillCost,
                    maxed = skillMaxed,
                    onClick = { vm.trainSkill(st.id) },
                    modifier = Modifier.weight(1f)
                )
                TrainButton(
                    label = "Speed",
                    cost = Balance.TRAIN_SPEED_COST,
                    enabled = !speedMaxed && s.money >= Balance.TRAIN_SPEED_COST,
                    maxed = speedMaxed,
                    onClick = { vm.trainSpeed(st.id) },
                    modifier = Modifier.weight(1f)
                )
                TrainButton(
                    label = "Stamina",
                    cost = Balance.TRAIN_STAMINA_COST,
                    enabled = !staminaMaxed && s.money >= Balance.TRAIN_STAMINA_COST,
                    maxed = staminaMaxed,
                    onClick = { vm.trainStamina(st.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TrainButton(
    label: String,
    cost: Int,
    enabled: Boolean,
    maxed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = androidx.compose.ui.graphics.Color.White),
        shape = RoundedCornerShape(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 10.dp),
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(if (maxed) "MAX" else "\$$cost", fontSize = 11.sp)
        }
    }
}

private fun nextTierName(currentTier: Int): String = when (currentTier + 1) {
    2 -> "Style Studio"
    3 -> "Boutique Salon"
    else -> "Luxury Spa"
}
