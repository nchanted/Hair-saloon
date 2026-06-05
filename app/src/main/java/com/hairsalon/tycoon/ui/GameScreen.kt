package com.hairsalon.tycoon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hairsalon.tycoon.game.ActiveService
import com.hairsalon.tycoon.game.Balance
import com.hairsalon.tycoon.game.Client
import com.hairsalon.tycoon.game.GameState
import com.hairsalon.tycoon.game.Stylist
import com.hairsalon.tycoon.ui.theme.Amber
import com.hairsalon.tycoon.ui.theme.Bad
import com.hairsalon.tycoon.ui.theme.Good
import com.hairsalon.tycoon.ui.theme.Pink500
import com.hairsalon.tycoon.ui.theme.Teal

/**
 * Mirrors GameEngine.seatClient's choice so the UI can tell the player, up front,
 * which stylist will take the next client they tap.
 */
private fun nextStylistFor(s: GameState): Stylist? {
    val busy = s.active.map { it.stylistId }.toHashSet()
    return s.stylists.filter { it.id !in busy && it.stamina >= 20f }.maxByOrNull { it.skill }
}

@Composable
fun GameScreen(s: GameState, onSeat: (Long) -> Unit, onEndDay: () -> Unit) {
    val closing = s.dayTimeLeft <= 0f
    val busyIds = s.active.map { it.stylistId }.toSet()
    val chairFree = s.active.size < s.stationCount
    val nextStylist = nextStylistFor(s)

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        val landscape = maxWidth > maxHeight
        if (landscape) {
            Row(Modifier.fillMaxSize()) {
                // ---- Left pane: status, staff, controls ----
                Column(Modifier.width(300.dp).fillMaxHeight()) {
                    TopBar(s, closing)
                    Spacer(Modifier.height(10.dp))
                    SectionLabel("Stylists")
                    LazyColumn(
                        Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(s.stylists, key = { it.id }) { st ->
                            StylistRowCard(st, busyIds.contains(st.id), nextStylist?.id == st.id)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    EndDayButton(closing, onEndDay)
                }
                Spacer(Modifier.width(12.dp))
                // ---- Right pane: the salon floor ----
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    AssignmentHint(chairFree, nextStylist)
                    Spacer(Modifier.height(8.dp))
                    SectionLabel("Chairs (${s.active.size}/${s.stationCount})")
                    ChairsRow(s)
                    Spacer(Modifier.height(10.dp))
                    SectionLabel(if (closing) "Closing \u2014 finish your clients" else "Waiting (${s.queue.size})")
                    QueueArea(s, closing, nextStylist, Modifier.weight(1f), onSeat)
                    MessageLine(s)
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                TopBar(s, closing)
                Spacer(Modifier.height(10.dp))
                AssignmentHint(chairFree, nextStylist)
                Spacer(Modifier.height(10.dp))

                SectionLabel("Chairs (${s.active.size}/${s.stationCount})")
                ChairsRow(s)
                Spacer(Modifier.height(10.dp))

                SectionLabel("Stylists")
                StylistsRow(s.stylists, busyIds, nextStylist?.id)
                Spacer(Modifier.height(10.dp))

                SectionLabel(if (closing) "Closing \u2014 finish your clients" else "Waiting (${s.queue.size})")
                QueueArea(s, closing, nextStylist, Modifier.weight(1f), onSeat)

                MessageLine(s)
                EndDayButton(closing, onEndDay)
            }
        }
    }
}

/** Tells the player exactly how seating works, and who's next in line to take a client. */
@Composable
private fun AssignmentHint(chairFree: Boolean, nextStylist: Stylist?) {
    val (icon, msg, tint) = when {
        !chairFree -> Triple("\uD83D\uDCBA", "All chairs are full \u2014 wait for a client to finish.", Amber)
        nextStylist == null -> Triple("\uD83D\uDCA4", "No stylist is free & rested \u2014 wait for one to recover.", Bad)
        else -> Triple(
            "\uD83D\uDC46",
            "Tap a waiting client \u2192 ${nextStylist.emoji} ${nextStylist.name} (skill ${nextStylist.skill}) will take them.",
            Teal
        )
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text(msg, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = tint)
        }
    }
}

@Composable
private fun MessageLine(s: GameState) {
    s.message?.let {
        Text(
            it,
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            color = Pink500,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EndDayButton(closing: Boolean, onEndDay: () -> Unit) {
    Button(
        onClick = onEndDay,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (closing) Pink500 else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (closing) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) { Text(if (closing) "CLOSE UP \u2192 SHOP" else "End day early", fontWeight = FontWeight.Bold) }
}

@Composable
private fun QueueArea(
    s: GameState,
    closing: Boolean,
    nextStylist: Stylist?,
    modifier: Modifier = Modifier,
    onSeat: (Long) -> Unit
) {
    Box(modifier.fillMaxWidth()) {
        if (s.queue.isEmpty()) {
            Text(
                if (closing) "No one left waiting." else "No clients yet\u2026",
                Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(s.queue, key = { it.id }) { c ->
                    ClientCard(c, nextStylist) { onSeat(c.id) }
                }
            }
        }
    }
}

@Composable
private fun TopBar(s: GameState, closing: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Day ${s.day}", fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text(s.tierName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("\uD83D\uDCB0 ${s.money}", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Good)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("\u2B50", fontSize = 13.sp)
                Spacer(Modifier.width(6.dp))
                LinearProgressIndicator(
                    progress = (s.reputation.toFloat() / s.maxReputation).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(6.dp)),
                    color = Amber,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Text("${s.reputation}/${s.maxReputation}", fontSize = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (closing) "\uD83D\uDD12" else "\u23F1\uFE0F", fontSize = 13.sp)
                Spacer(Modifier.width(6.dp))
                LinearProgressIndicator(
                    progress = (s.dayTimeLeft / Balance.DAY_LENGTH).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(6.dp)),
                    color = Teal,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

private val CAPE_PALETTE = listOf(
    Color(0xFF37474F), Color(0xFF00897B), Color(0xFFAD1457),
    Color(0xFF5E35B1), Color(0xFF455A64), Color(0xFF00838F)
)

@Composable
private fun ChairsRow(s: GameState) {
    val emptyChairs = (s.stationCount - s.active.size).coerceAtLeast(0)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(s.active, key = { it.client.id }) { a ->
            val stylist = s.stylists.firstOrNull { it.id == a.stylistId }
            val cape = CAPE_PALETTE[(a.client.id % CAPE_PALETTE.size).toInt()]
            ActiveChairCard(a, stylist, cape)
        }
        items(emptyChairs) {
            Card(
                Modifier.size(width = 150.dp, height = 200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    ChairVisual(
                        occupied = false,
                        faceEmoji = null,
                        capeColor = Color.Transparent,
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    Text("Open chair", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text("waiting for a client", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun ActiveChairCard(a: ActiveService, stylist: Stylist?, capeColor: Color) {
    val onTrack = a.quality >= a.client.expectation
    Card(
        Modifier.size(width = 150.dp, height = 200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(8.dp)) {
            ChairVisual(
                occupied = true,
                faceEmoji = a.client.face,
                capeColor = capeColor,
                modifier = Modifier.fillMaxWidth().height(116.dp)
            )
            Text(
                "${stylist?.emoji ?: ""} ${stylist?.name ?: "?"} \u2702\uFE0F",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1
            )
            Text("${a.client.service.emoji} ${a.client.service.label}", fontSize = 11.sp, maxLines = 1)
            Text(
                "Quality ${a.quality} / need ${a.client.expectation}",
                fontSize = 10.sp,
                color = if (onTrack) Good else Bad
            )
            Spacer(Modifier.weight(1f))
            LinearProgressIndicator(
                progress = a.progress.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Pink500,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

/** Horizontal stylist chip (portrait). Highlights whoever is next to be auto-assigned. */
@Composable
private fun StylistsRow(stylists: List<Stylist>, busyIds: Set<Int>, nextId: Int?) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(stylists, key = { it.id }) { st ->
            val busy = busyIds.contains(st.id)
            val resting = !busy && st.stamina < 20f
            val isNext = st.id == nextId
            Card(
                Modifier.size(width = 116.dp, height = 92.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNext) Teal else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(Modifier.padding(8.dp)) {
                    Text(
                        "${st.emoji} ${st.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNext) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        when {
                            busy -> "working\u2026"
                            isNext -> "UP NEXT \u2022 sk ${st.skill}"
                            resting -> "resting"
                            else -> "ready \u2022 sk ${st.skill}"
                        },
                        fontSize = 10.sp,
                        color = when {
                            isNext -> Color.White
                            resting -> Bad
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(Modifier.weight(1f))
                    LinearProgressIndicator(
                        progress = (st.stamina / st.maxStamina).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (st.stamina < 25f) Bad else if (isNext) Color.White else Teal,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

/** Full-width stylist row (landscape left pane). */
@Composable
private fun StylistRowCard(st: Stylist, busy: Boolean, isNext: Boolean) {
    val resting = !busy && st.stamina < 20f
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNext) Teal else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${st.emoji} ${st.name}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isNext) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    when {
                        busy -> "working\u2026"
                        isNext -> "UP NEXT"
                        resting -> "resting"
                        else -> "ready"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isNext -> Color.White
                        resting -> Bad
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Text(
                "skill ${st.skill} \u2022 speed ${"%.2f".format(st.speed)}x",
                fontSize = 10.sp,
                color = if (isNext) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = (st.stamina / st.maxStamina).coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                color = if (st.stamina < 25f) Bad else if (isNext) Color.White else Teal,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun ClientCard(c: Client, nextStylist: Stylist?, onClick: () -> Unit) {
    val patienceFrac = (c.patience / c.maxPatience).coerceIn(0f, 1f)
    val patienceColor = when {
        patienceFrac > 0.5f -> Good
        patienceFrac > 0.25f -> Amber
        else -> Bad
    }
    Card(
        Modifier
            .size(width = 122.dp, height = 162.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(patienceColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) { Text(c.face, fontSize = 22.sp) }
                Spacer(Modifier.width(6.dp))
                Text(c.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
            }
            Spacer(Modifier.height(4.dp))
            Text("${c.service.emoji} ${c.service.label}", fontSize = 11.sp)
            Text(
                "\uD83D\uDCB0 ${c.payValue}  \u2022  exp ${c.expectation}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(3.dp))
            Text(
                if (nextStylist != null) "\uD83D\uDC46 Tap \u2192 ${nextStylist.name}" else "\uD83D\uDC46 Tap to seat",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Teal,
                maxLines = 1
            )
            Spacer(Modifier.weight(1f))
            LinearProgressIndicator(
                progress = patienceFrac,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = patienceColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
