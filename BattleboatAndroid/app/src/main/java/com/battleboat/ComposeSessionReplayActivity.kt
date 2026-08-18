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

/**
 * Compose-only Session Replay test bed.
 *
 * Mirrors NBA's alpha.2 spike (sampleRate 1.0, MaskLevel.LIGHT) and exercises
 * Compose surfaces that historically break or under-capture in Session Replay:
 * scrolling lists, modal sheets, dialogs, TextFields, privacy modifiers,
 * custom Canvas, animations, and nested navigation-like panels.
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
            MaterialTheme(colorScheme = darkColorScheme()) {
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
    var email by remember { mutableStateOf("fan@nba.com") }
    var password by remember { mutableStateOf("secret-password") }
    var passwordVisible by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("Watch this text update in the replay.") }
    var showSheet by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showSensitive by remember { mutableStateOf(true) }
    var tapCount by remember { mutableIntStateOf(0) }
    var selectedTeam by remember { mutableStateOf("Lakers") }
    var progress by remember { mutableFloatStateOf(0.2f) }
    val animatedProgress by animateFloatAsState(progress, animationSpec = tween(600), label = "progress")

    val teams = remember {
        listOf(
            "Lakers", "Celtics", "Warriors", "Knicks", "Heat",
            "Bucks", "Nuggets", "Suns", "Mavs", "76ers",
            "Clippers", "Bulls", "Nets", "Raptors", "Hawks"
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
                        Text("Compose SR Lab", fontWeight = FontWeight.Bold)
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
                    containerColor = Color(0xFF0B1B33)
                )
            )
        },
        containerColor = Color(0xFF0A1220)
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
                        "Tap counter and animated progress — check whether intermediate frames appear in the replay (NBA reported skipped interactions).",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = {
                            tapCount++
                        }) {
                            Text("Tap me ($tapCount)")
                        }
                        Spacer(Modifier.width(12.dp))
                        ProgressRing(animatedProgress)
                        Spacer(Modifier.width(8.dp))
                        Text("${(animatedProgress * 100).toInt()}%", fontFamily = FontFamily.Monospace)
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
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
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
                        label = { Text("Notes (should be visible at Light)") },
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
                        "Masked account # ACCT-000049",
                        modifier = Modifier
                            .ampMask()
                            .fillMaxWidth()
                            .background(Color(0xFF1A2738), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Unmasked fan name: Anuj Test",
                        modifier = Modifier
                            .ampUnmask()
                            .fillMaxWidth()
                            .background(Color(0xFF1A2738), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .ampBlock()
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFF3D1F2B), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Blocked payment card — should be a placeholder")
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
                            "Toggle-visible content: season ticket holder ID 8821",
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
                        "Custom draw content often needs bitmap capture — verify the court graphic appears.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    BasketballCourtGraphic(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            item {
                SectionCard(title = "6. Scrollable team list") {
                    Text(
                        "Scroll the list, then flush — check whether scroll position / items are captured.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        teams.take(4).forEach { team ->
                            FilterChip(
                                selected = selectedTeam == team,
                                onClick = {
                                    selectedTeam = team
                                },
                                label = { Text(team) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .border(1.dp, Color(0xFF2A3A4F), RoundedCornerShape(10.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        teams.forEach { team ->
                            TeamRow(
                                team = team,
                                selected = team == selectedTeam,
                                onClick = { selectedTeam = team }
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
            containerColor = Color(0xFF152238)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Ticket Checkout Sheet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("Selected team: $selectedTeam")
                Text("Price: \$142.00", modifier = Modifier.ampMask())
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        showSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm purchase")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF122033)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFDB927)
                )
                Spacer(Modifier.width(8.dp))
                Text("NBA-style Compose capture test", fontWeight = FontWeight.SemiBold)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF152238)),
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
private fun TeamRow(team: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF1E3A5F) else Color(0xFF101A28))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (selected) Color(0xFFFDB927) else Color(0xFF2A3A4F)),
            contentAlignment = Alignment.Center
        ) {
            Text(team.take(1), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
        }
        Spacer(Modifier.width(10.dp))
        Text(team)
    }
}

@Composable
private fun ProgressRing(progress: Float) {
    Canvas(modifier = Modifier.size(40.dp)) {
        drawArc(
            color = Color(0xFF2A3A4F),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFF552583),
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun BasketballCourtGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(Color(0xFF1B4332))) {
        val w = size.width
        val h = size.height
        val paint = Color(0xFFE9ECEF)
        // Outer boundary
        drawRect(color = paint, style = Stroke(width = 3.dp.toPx()))
        // Center circle
        drawCircle(
            color = paint,
            radius = h * 0.22f,
            center = Offset(w / 2f, h / 2f),
            style = Stroke(width = 3.dp.toPx())
        )
        // Half-court line
        drawLine(
            color = paint,
            start = Offset(w / 2f, 0f),
            end = Offset(w / 2f, h),
            strokeWidth = 3.dp.toPx()
        )
        // Left key
        drawRect(
            color = paint,
            topLeft = Offset(0f, h * 0.25f),
            size = Size(w * 0.18f, h * 0.5f),
            style = Stroke(width = 3.dp.toPx())
        )
        // Right key
        drawRect(
            color = paint,
            topLeft = Offset(w * 0.82f, h * 0.25f),
            size = Size(w * 0.18f, h * 0.5f),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
