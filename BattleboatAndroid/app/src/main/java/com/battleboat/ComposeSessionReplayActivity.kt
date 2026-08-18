package com.battleboat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amplitude.android.sessionreplay.compose.ampBlock
import com.amplitude.android.sessionreplay.compose.ampMask
import com.amplitude.android.sessionreplay.compose.ampUnmask
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Amplitude brand palette — mirrors res/values/colors.xml so this Compose
// screen matches the rest of the (View-based) app. Defined here as a Compose
// color scheme so every MaterialTheme.colorScheme.* lookup — and, crucially,
// the automatic contentColorFor() text color — resolves to a legible on-dark
// value instead of the default scheme's unmatched (dark) fallbacks.
// ---------------------------------------------------------------------------
private val AmpNavy = Color(0xFF0D1330)
private val AmpSurface = Color(0xFF1D2433)
private val AmpSurface2 = Color(0xFF252E45)
private val AmpSurface3 = Color(0xFF2F3A55)
private val AmpBlue = Color(0xFF1352CC)
private val AmpBlueLight = Color(0xFF3986F7)
private val AmpCoral = Color(0xFFE8410E)
private val AmpTeal = Color(0xFF00C2A8)
private val AmpLavender = Color(0xFF9164FA)
private val AmpTextPrimary = Color(0xFFE8EDF5)
private val AmpTextSecondary = Color(0xFF8B9DB5)
private val AmpBorder = Color(0xFF26314A)
private val AmpWater = Color(0xFF394B75)

private val AmpDarkColorScheme = darkColorScheme(
    primary = AmpBlue,
    onPrimary = Color.White,
    primaryContainer = AmpBlue,
    onPrimaryContainer = Color.White,
    secondary = AmpTeal,
    onSecondary = AmpNavy,
    tertiary = AmpLavender,
    onTertiary = Color.White,
    background = AmpNavy,
    onBackground = AmpTextPrimary,
    surface = AmpSurface,
    onSurface = AmpTextPrimary,
    surfaceVariant = AmpSurface2,
    onSurfaceVariant = AmpTextSecondary,
    outline = AmpBorder,
    outlineVariant = AmpBorder,
    error = AmpCoral,
    onError = Color.White,
)

/**
 * Compose-only Session Replay test bed.
 *
 * Exercises the Compose surfaces that historically break or under-capture in
 * Session Replay — scrolling lists, modal sheets, dialogs, TextFields, privacy
 * modifiers, custom Canvas, animations — themed around the Battleboat game so
 * it doubles as a representative in-app screen (sampleRate 1.0, MaskLevel.LIGHT).
 */
class ComposeSessionReplayActivity : ComponentActivity() {

    private lateinit var analyticsManager: AnalyticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsManager = AnalyticsManager.getInstance(this)
        if (!analyticsManager.isAmplitudeInitialized()) {
            analyticsManager.initialize()
        }

        setContent {
            MaterialTheme(colorScheme = AmpDarkColorScheme) {
                ComposeSessionReplayLab(
                    analyticsManager = analyticsManager,
                    onClose = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeSessionReplayLab(
    analyticsManager: AnalyticsManager,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("admiral@battleboat.io") }
    var password by remember { mutableStateOf("secret-passphrase") }
    var passwordVisible by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("Enemy carrier last seen near G7.") }
    var showSheet by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showSensitive by remember { mutableStateOf(true) }
    var shotCount by remember { mutableIntStateOf(0) }
    var selectedOpponent by remember { mutableStateOf("Nelson") }
    var progress by remember { mutableFloatStateOf(0.2f) }
    val animatedProgress by animateFloatAsState(progress, animationSpec = tween(600), label = "progress")

    // Rival admirals — a scrollable roster stands in for a leaderboard / opponent
    // picker, giving Session Replay a real scroll container to capture.
    val opponents = remember {
        listOf(
            "Nelson", "Yamamoto", "Nimitz", "Drake", "Halsey",
            "Rodney", "Togo", "Farragut", "Spruance", "Cunningham",
            "Doenitz", "Beatty", "Zheng He", "Barbarossa", "Themistocles"
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            progress = if (progress >= 0.95f) 0.15f else progress + 0.12f
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Session Replay Lab", fontWeight = FontWeight.Bold)
                        Text(
                            "alpha.2 · Light mask · 100% sample",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ReplayIdentityCard(analyticsManager)
            }

            item {
                SectionCard(title = "1. Interactions & animation") {
                    Text(
                        "Fire shots and watch the enemy-fleet ring animate — check whether intermediate frames appear in the replay (skipped interactions have been reported).",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = {
                            shotCount++
                        }) {
                            Text("Fire shot ($shotCount)")
                        }
                        Spacer(Modifier.width(12.dp))
                        ProgressRing(animatedProgress)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${(animatedProgress * 100).toInt()}% sunk",
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            item {
                SectionCard(title = "2. Text & inputs (Light mask)") {
                    Text(
                        "Light mask should leave most text visible, but mask password / email / phone-style fields.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Admiral email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Fleet passphrase") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Battle notes (should be visible at Light)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }

            item {
                SectionCard(title = "3. Privacy modifiers") {
                    Text(
                        "ampMask / ampUnmask / ampBlock — confirm modifiers render correctly in alpha.2.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Masked target coords: SECRET-G7",
                        modifier = Modifier
                            .ampMask()
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Unmasked callsign: Admiral Anuj",
                        modifier = Modifier
                            .ampUnmask()
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .ampBlock()
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(AmpCoral.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Blocked enemy fleet layout — should be a placeholder")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Show sensitive row")
                        Spacer(Modifier.weight(1f))
                        Switch(checked = showSensitive, onCheckedChange = { showSensitive = it })
                    }
                    AnimatedVisibility(visible = showSensitive) {
                        Text(
                            "Toggle-visible content: flagship anchored at D4",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            item {
                SectionCard(title = "4. Modal sheet & dialog") {
                    Text(
                        "Modal sheets were called out as missing / wrong colors in earlier Compose SR builds.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            showSheet = true
                        }) {
                            Text("Open bottom sheet")
                        }
                        OutlinedButton(onClick = {
                            showDialog = true
                        }) {
                            Text("Open dialog")
                        }
                    }
                }
            }

            item {
                SectionCard(title = "5. Custom Canvas / graphics") {
                    Text(
                        "Custom draw content often needs bitmap capture — verify the battle grid appears.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    BattleGridGraphic(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            item {
                SectionCard(title = "6. Scrollable opponent roster") {
                    Text(
                        "Scroll the list, then flush — check whether scroll position / items are captured.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        opponents.take(4).forEach { opponent ->
                            FilterChip(
                                selected = selectedOpponent == opponent,
                                onClick = {
                                    selectedOpponent = opponent
                                },
                                label = { Text(opponent) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        opponents.forEach { opponent ->
                            OpponentRow(
                                opponent = opponent,
                                selected = opponent == selectedOpponent,
                                onClick = { selectedOpponent = opponent }
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = "7. Flush & verify") {
                    Text(
                        "Interact above, then flush. Find this session in Amplitude with the device/session IDs in the header.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            analyticsManager.flushEvents()
                            analyticsManager.flushSessionReplay()
                            Toast.makeText(
                                context,
                                "Flushed analytics + session replay",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Flush analytics + Session Replay")
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Deploy Fleet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("Selected opponent: $selectedOpponent")
                Text("Wager: 500 gold", modifier = Modifier.ampMask())
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        showSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm deployment")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Replay check") },
            text = {
                Text("If this dialog is missing or transparent in the replay, that matches known Compose SR gaps.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text("Got it")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReplayIdentityCard(analyticsManager: AnalyticsManager) {
    val deviceId = analyticsManager.getDeviceId() ?: "(pending)"
    val sessionId = analyticsManager.getSessionId()?.toString() ?: "(pending)"
    val recording = analyticsManager.isSessionReplayRecording()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.width(8.dp))
                Text("Battleboat Compose capture test", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(10.dp))
            IdentityLine("Recording", if (recording) "YES" else "NO")
            IdentityLine("Device ID", deviceId)
            IdentityLine("Session ID", sessionId)
            IdentityLine("SDK", "plugin-session-replay-android:1.0.0-alpha.2")
            Text(
                analyticsManager.getSessionReplayStatus(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun IdentityLine(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            label,
            modifier = Modifier.width(92.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Text(
            value,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun OpponentRow(opponent: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) AmpBlue.copy(alpha = 0.28f) else AmpNavy)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (selected) AmpTeal else AmpSurface3),
            contentAlignment = Alignment.Center
        ) {
            Text(
                opponent.take(1),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (selected) AmpNavy else AmpTextPrimary
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(opponent)
    }
}

@Composable
private fun ProgressRing(progress: Float) {
    Canvas(modifier = Modifier.size(40.dp)) {
        drawArc(
            color = AmpSurface3,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
            color = AmpCoral,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * A small Battleboat target grid drawn with Canvas — the custom-draw surface
 * for the Session Replay bitmap-capture test. Renders a 10x10 sea grid with a
 * couple of hits (coral) and a miss (blue) so there is real custom content to
 * verify in the replay.
 */
@Composable
private fun BattleGridGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(AmpWater)) {
        val cells = 10
        val cell = minOf(size.width, size.height) / cells
        val originX = (size.width - cell * cells) / 2f
        val originY = (size.height - cell * cells) / 2f
        val line = AmpBorder

        // Grid lines
        for (i in 0..cells) {
            drawLine(
                color = line,
                start = Offset(originX + i * cell, originY),
                end = Offset(originX + i * cell, originY + cell * cells),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = line,
                start = Offset(originX, originY + i * cell),
                end = Offset(originX + cell * cells, originY + i * cell),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        fun mark(col: Int, row: Int, color: Color, filled: Boolean) {
            val center = Offset(originX + (col + 0.5f) * cell, originY + (row + 0.5f) * cell)
            if (filled) {
                drawCircle(color = color, radius = cell * 0.28f, center = center)
            } else {
                drawCircle(
                    color = color,
                    radius = cell * 0.24f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // A few hits and a miss to make the grid read as an in-progress game
        mark(2, 3, AmpCoral, filled = true)
        mark(3, 3, AmpCoral, filled = true)
        mark(6, 5, AmpCoral, filled = true)
        mark(5, 8, AmpBlueLight, filled = false)
        mark(8, 1, AmpBlueLight, filled = false)
    }
}
