package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.UserEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// Navigation route representations
sealed class Screen {
    object Lobby : Screen()
    object GamePlay : Screen()
}

// Simulated avatars list for profile configurations
val AVATARS = listOf(
    "👨‍💼", "👩‍💼", "🦁", "🐼", "🦊", "🐱", "🐰", "🐻"
)

@Composable
fun MainAppNavigation(viewModel: GameViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Lobby) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CosmicNavy
    ) {
        when (currentScreen) {
            is Screen.Lobby -> {
                LobbyScreen(
                    viewModel = viewModel,
                    onJoinGame = {
                        currentScreen = Screen.GamePlay
                    }
                )
            }
            is Screen.GamePlay -> {
                GamePlayScreen(
                    viewModel = viewModel,
                    onBackToLobby = {
                        currentScreen = Screen.Lobby
                    }
                )
            }
        }
    }
}

// SECTION 1: LOBBY SCREEN (Matches Frame 8 UX)
@Composable
fun LobbyScreen(
    viewModel: GameViewModel,
    onJoinGame: () -> Unit
) {
    val context = LocalContext.current
    val activeUser by viewModel.activeUser.collectAsState(initial = null)
    
    var showJoinDialog by remember { mutableStateOf(false) }
    var usernameInput by remember { mutableStateOf("") }
    var selectedAvatarIdx by remember { mutableStateOf(0) }
    var referralCodeInput by remember { mutableStateOf("") }

    // State for other tabs/activities in lobby
    var showRulesDialog by remember { mutableStateOf(false) }
    var showRechargeDialog by remember { mutableStateOf(false) }
    var showReferralDialog by remember { mutableStateOf(false) }
    var showLeaderboardDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2211), // Lush mist top
                        Color(0xFF030A05)  // Dark deep jungle base
                    )
                )
            )
            .testTag("lobby_screen_root")
    ) {
        // Decorative golden fireflies in background canopy
        Canvas(modifier = Modifier.fillMaxSize()) {
            val random = java.util.Random(42)
            for (i in 0..100) {
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height
                val radius = random.nextFloat() * 2.2f + 0.4f
                val alpha = random.nextFloat() * 0.7f + 0.2f
                drawCircle(
                    color = Color(0xFFFDE047).copy(alpha = alpha), // Glowing golden fireflies
                    radius = radius,
                    center = Offset(x, y)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spacer for edge-to-edge
            Spacer(modifier = Modifier.height(35.dp))

            // TOP NAVIGATION SHELF
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Quick Info Card
                if (activeUser != null) {
                    val user = activeUser!!
                    Row(
                        modifier = Modifier
                            .background(Color(0xD90F172A), RoundedCornerShape(25.dp))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(25.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AVATARS.getOrNull(user.avatarId) ?: "👨‍💼",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Column {
                            Text(
                                text = user.username,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 100.dp)
                            )
                            Text(
                                text = "ID:${user.id + 2317800}",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        // Balance adding trigger
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF22C55E), RoundedCornerShape(15.dp))
                                .clickable { showRechargeDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format("%.0f", user.balance),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Deposit",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                } else {
                    // Profile placeholder leading to signup dialog
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(25.dp))
                            .clickable { showJoinDialog = true }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Not Logged In", color = Color.White, fontSize = 13.sp)
                    }
                }

                // Viewer Count and Close Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("1", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { Toast.makeText(context, "Returning to main page", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier
                            .background(Color(0xFF1E293B), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SIX COG / SECTOR BADGES FRAME (Matches Console, Rebate, Top 50+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Console badge
                LobbyBadgeItem(
                    emoji = "🎮",
                    score = "0",
                    label = "Start",
                    onClick = {
                        if (activeUser == null) {
                            showJoinDialog = true
                        } else {
                            onJoinGame()
                        }
                    },
                    modifier = Modifier.testTag("console_b")
                )

                // Rebate badge
                LobbyBadgeItem(
                    emoji = "🎁",
                    score = "Rebate",
                    label = "Daily Reward",
                    onClick = {
                        if (activeUser == null) {
                            showJoinDialog = true
                        } else {
                            viewModel.claimDailyReward { status ->
                                when {
                                    status == "ALREADY_CLAIMED" -> {
                                        Toast.makeText(context, "You have already claimed today! Please return tomorrow.", Toast.LENGTH_SHORT).show()
                                    }
                                    status.startsWith("SUCCESS") -> {
                                        val amt = status.split(":")[1]
                                        Toast.makeText(context, "Successfully claimed +$amt coins!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    }
                )

                // Top 50+ badge
                LobbyBadgeItem(
                    emoji = "🏆",
                    score = "Top 50+",
                    label = "Creators",
                    onClick = { showLeaderboardDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // MAIN INVITE COINS BANNER (Invite Bonus wheel Up to 1,000,000)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clickable { showReferralDialog = true }
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFD97706),
                                Color(0xFFB45309),
                                Color(0xFF78350F)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .border(2.dp, GoldYellow, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Referral Bonus Board!",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Earn up to 1,000,000 coins for every successful referral invitation.",
                            color = GoldYellowLight,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "👉 Refer",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // CENTER LOGO / BIG USER ICON WITH "JOIN" BUTTON
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White.copy(alpha = 0.04f), CircleShape)
            ) {
                // Breathing glow animation
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scaleFactor by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize(0.85f)
                        .scale(scaleFactor)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    GoldYellow, Color(0xFFEAB308), Color(0xFF9A3412), GoldYellow
                                )
                            ),
                            CircleShape
                        )
                        .padding(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1E293B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (activeUser != null) {
                            Text(
                                text = AVATARS.getOrNull(activeUser!!.avatarId) ?: "👨‍💼",
                                fontSize = 80.sp
                            )
                        } else {
                            Text(text = "❓", fontSize = 70.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // JOIN LOBBY PRIMARY BUTTON
            Button(
                onClick = {
                    if (activeUser == null) {
                        showJoinDialog = true
                    } else {
                        onJoinGame()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .width(180.dp)
                    .height(55.dp)
                    .shadow(12.dp, RoundedCornerShape(30.dp))
                    .testTag("join_lobby_button")
            ) {
                Text(
                    text = if (activeUser == null) "LOG IN" else "PLAY NOW  🎮",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Secondary Info Badges below Join
            Text(
                text = "Lucky animal mini-game integrated directly",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // --- SIGN-UP / JOIN PROFILE MODAL ---
        if (showJoinDialog) {
            Dialog(onDismissRequest = { showJoinDialog = false }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Create Gaming Profile",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Avatar Selection Row
                        Text("Choose your avatar appearance:", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AVATARS.forEachIndexed { idx, emo ->
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selectedAvatarIdx == idx) GoldYellow else Color.White.copy(alpha = 0.1f)
                                        )
                                        .clickable { selectedAvatarIdx = idx }
                                        .padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF0F172A), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emo, fontSize = 22.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Username Input
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            label = { Text("Username (Minimum 3 characters)", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldYellow,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Referral Optional Code
                        OutlinedTextField(
                            value = referralCodeInput,
                            onValueChange = { referralCodeInput = it },
                            label = { Text("Referral code (Optional, get +200 coins)", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Green,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showJoinDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CANCEL", color = Color.White)
                            }

                            Button(
                                onClick = {
                                    if (usernameInput.trim().length >= 3) {
                                        viewModel.registerOrLoginUsername(
                                            usernameInput, selectedAvatarIdx, referralCodeInput
                                        ) {
                                            showJoinDialog = false
                                            Toast.makeText(context, "Welcome to Mix Spin Wheel!", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Please enter a name with at least 3 characters.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("REGISTER", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- RECHARGE / WITHDRAW PORTAL ---
        if (showRechargeDialog) {
            SimulatedFinancesDialog(
                viewModel = viewModel,
                onDismiss = { showRechargeDialog = false }
            )
        }

        // --- REFERRAL SYSTEM MODAL ---
        if (showReferralDialog) {
            ReferralPortalDialog(
                viewModel = viewModel,
                onDismiss = { showReferralDialog = false }
            )
        }

        // --- LEADERBOARD DIALOG ---
        if (showLeaderboardDialog) {
            LeaderboardDialog(
                viewModel = viewModel,
                onDismiss = { showLeaderboardDialog = false }
            )
        }
    }
}

@Composable
fun LobbyBadgeItem(
    emoji: String,
    score: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(Color(0xD90F172A), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .width(85.dp)
    ) {
        Text(emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = score,
            color = GoldYellow,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}


// SECTION 2: GAMEPLAY SCREEN (Matches Frame 5 UI faithfully)
@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    onBackToLobby: () -> Unit
) {
    val context = LocalContext.current
    val activeUser by viewModel.activeUser.collectAsState(initial = null)
    val gameState by viewModel.gameState.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val selectedChip by viewModel.selectedChip.collectAsState()
    val highlightedIndex by viewModel.highlightedIndex.collectAsState()
    val playerCurrentBets by viewModel.currentPlayerBets.collectAsState()
    val itemWinningsHistory by viewModel.listSessions.collectAsState(initial = emptyList())
    val activeViewers by viewModel.activeViewers.collectAsState()
    val isMusicEnabled by viewModel.isMusicOn.collectAsState()
    val lastBetsPlaced by viewModel.lastBetsPlaced.collectAsState()

    var showAdminDashboard by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Coroutine Scope for triggering explosive particle animations on user wins
    val coroutineScope = rememberCoroutineScope()
    var celebratoryCoinsVisible by remember { mutableStateOf(false) }
    var userWonCelebrationAmount by remember { mutableStateOf(0.0) }

    // Listen to recent winners to trigger satisfying particle animations
    LaunchedEffect(key1 = "victory_listener") {
        viewModel.recentWinnings.collect { (animalName, winCoins) ->
            if (winCoins > 0.0) {
                userWonCelebrationAmount = winCoins
                celebratoryCoinsVisible = true
                delay(3500L)
                celebratoryCoinsVisible = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A2210), // Deep forest mist top
                        Color(0xFF041208), // Dark jungle bottom
                        Color(0xFF010502)
                    )
                )
            )
            .testTag("gameplay_screen_root")
    ) {
        // 1. STARRY NIGHT CANOPY & HANGING VINES GRAPHICS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val random = java.util.Random(12345)
            // Sparkly jungle fireflies
            for (i in 0..60) {
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height
                val radius = random.nextFloat() * 2f + 0.6f
                val alpha = random.nextFloat() * 0.5f + 0.2f
                drawCircle(
                    color = Color(0xFFA7E6A0).copy(alpha = alpha),
                    radius = radius,
                    center = Offset(x, y)
                )
            }

            // Draw Left Hanging Vines with details
            val vineColor = Color(0xFF1E3F20)
            val vineHighlight = Color(0xFF347A3B)
            val leafGreen = Color(0xFF4ADE80)

            // Vine Left 1
            val leftPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(20f, -20f)
                quadraticTo(40f, 200f, 15f, 450f)
                quadraticTo(50f, 700f, 25f, 950f)
            }
            drawPath(path = leftPath, color = vineColor, style = Stroke(width = 4.dp.toPx()))
            drawPath(path = leftPath, color = vineHighlight, style = Stroke(width = 1.5.dp.toPx()))

            // Add organic leaves on left
            for (i in 0..12) {
                val progress = i / 12f
                val leafY = progress * 900f
                val leafX = 25f + (if (i % 2 == 0) 24f else -24f)
                // Drawing individual leaf shapes
                drawOval(
                    color = Color(0xFF155E39),
                    topLeft = Offset(leafX - 12f, leafY - 8f),
                    size = androidx.compose.ui.geometry.Size(26f, 16f)
                )
                drawOval(
                    color = leafGreen,
                    topLeft = Offset(leafX - 10f, leafY - 6f),
                    size = androidx.compose.ui.geometry.Size(20f, 12f)
                )
            }

            // Vine Right 1
            val rightPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width - 20f, -20f)
                quadraticTo(size.width - 45f, 250f, size.width - 15f, 550f)
                quadraticTo(size.width - 50f, 800f, size.width - 30f, 1050f)
            }
            drawPath(path = rightPath, color = vineColor, style = Stroke(width = 4.dp.toPx()))
            drawPath(path = rightPath, color = vineHighlight, style = Stroke(width = 1.5.dp.toPx()))

            // Add organic leaves on right
            for (i in 0..12) {
                val progress = i / 12f
                val leafY = progress * 1000f
                val leafX = size.width - 25f + (if (i % 2 == 0) -24f else 24f)
                drawOval(
                    color = Color(0xFF155E39),
                    topLeft = Offset(leafX - 14f, leafY - 8f),
                    size = androidx.compose.ui.geometry.Size(28f, 16f)
                )
                drawOval(
                    color = leafGreen,
                    topLeft = Offset(leafX - 11f, leafY - 6f),
                    size = androidx.compose.ui.geometry.Size(22f, 12f)
                )
            }
        }

        // Main Vertical Layout Containment
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(35.dp))

            // TOP NAVIGATION HEADER SHELF (Settings & Sound Buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back, Settings and Sound Triggers
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToLobby,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { showAdminDashboard = true },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .size(36.dp)
                            .testTag("admin_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Admin settings", tint = GoldYellow, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { viewModel.setMusicEnabled(!isMusicEnabled) },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isMusicEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "sound switcher",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Centered Room Title - stylized exactly as shown
                Text(
                    text = "Mix",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                // Extra controls: Help Button & Lobby quick logout
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.QuestionMark, contentDescription = "Help dialog", tint = GoldYellow, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onBackToLobby,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "cancel gameplay", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. HORIZONTAL HISTORY PILLS & CROWD BADGES ROW (Top segment in user main design)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT OVERLAPPING ANIMAL CROWD LOGO (Tiger, Lion, Bear, Panda style)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                        .border(1.5.dp, GoldYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Overlapping small emoji heads
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text("🐯", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopStart).padding(4.dp))
                        Text("🦁", fontSize = 20.sp, modifier = Modifier.align(Alignment.Center))
                        Text("🐼", fontSize = 16.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp))
                    }
                }

                // CENTERED HISTORY ROLLS BAR (Matches hist pills scroll)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(34.dp)
                        .background(Color(0xE60D2511), RoundedCornerShape(17.dp))
                        .border(1.dp, Color(0xFF1E5E2F), RoundedCornerShape(17.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (itemWinningsHistory.isEmpty()) {
                        Text("Empty history", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            itemWinningsHistory.take(12).forEach { history ->
                                val matchConf = viewModel.animalsList.firstOrNull { it.name == history.winningAnimal }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color.White.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        matchConf?.iconEmoji ?: "🐾",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // RIGHT OVERLAPPING ANIMAL CROWD LOGO (Rabbit, Cat, Dog, Sheep style)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                        .border(1.5.dp, Color(0xFF3B82F6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text("🐰", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopStart).padding(4.dp))
                        Text("🐱", fontSize = 20.sp, modifier = Modifier.align(Alignment.Center))
                        Text("🐑", fontSize = 16.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. CENTER AMBER PEACH COUNTDOWN PROGRESS CAP (gradient_circle)
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(16.dp, CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFD482), // Soft bright peach
                                    Color(0xFFF99D4B)  // Vivid orange-amber
                                )
                            ),
                            CircleShape
                        )
                        .border(4.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when (gameState) {
                                GameRoundState.BETTING -> "BET"
                                GameRoundState.SPINNING -> "SPIN"
                                GameRoundState.SETTLED -> "WIN!"
                            },
                            color = Color(0xFF5D2411),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (gameState == GameRoundState.BETTING) "${countdown}s" else "0s",
                            color = Color(0xFF421204),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // 4. THE SPIN WHEEL GAME BOARD Frame WITH LION LOGO (lion_game_logo.png styled)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Outer Radial Wood Slots and gold rim backgrounds
                Canvas(modifier = Modifier.size(285.dp)) {
                    val centerPoint = Offset(size.width / 2, size.height / 2)
                    
                    // Outer polished gold plate rim
                    drawCircle(
                        color = Color(0xFFD97706),
                        radius = size.width / 2,
                        center = centerPoint,
                        style = Stroke(width = 10.dp.toPx())
                    )
                    
                    // Golden outer neon line
                    drawCircle(
                        color = Color(0xFFFCD34D),
                        radius = (size.width / 2) - 4.dp.toPx(),
                        center = centerPoint,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Inner golden border rim
                    drawCircle(
                        color = Color(0xFFB45309),
                        radius = (size.width / 2) - 15.dp.toPx(),
                        center = centerPoint,
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Spoke slot tracks
                    for (i in 0 until 8) {
                        val angleRad = Math.toRadians((i * 45.0) - 90.0)
                        val endX = (size.width / 2) + ((size.width / 2) - 16.dp.toPx()) * cos(angleRad).toFloat()
                        val endY = (size.height / 2) + ((size.height / 2) - 16.dp.toPx()) * sin(angleRad).toFloat()
                        drawLine(
                            color = Color(0xFFFCD34D).copy(alpha = 0.5f),
                            start = centerPoint,
                            end = Offset(endX, endY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                // Spotlight glow orbiting
                val glowTransition = rememberInfiniteTransition(label = "ring_glow").animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
                    label = "glow"
                )

                // Layout our 8 Animal Betting slots around the orbit path
                val radiusOffset = 114.dp
                viewModel.animalsList.forEachIndexed { index, animalConfig ->
                    val radian = Math.toRadians((index * 45.0) - 90.0)
                    val xPos = radiusOffset * cos(radian).toFloat()
                    val yPos = radiusOffset * sin(radian).toFloat()

                    val isSpinWinningSpot = (index == highlightedIndex)
                    val isUserWagered = (playerCurrentBets[animalConfig.name] ?: 0.0) > 0.0

                    Box(
                        modifier = Modifier
                            .offset(xPos, yPos)
                            .testTag("animal_bet_option_${animalConfig.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Slot container beautifully styled with animal emoji & multiplier odds
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSpinWinningSpot && gameState == GameRoundState.SPINNING -> Color.White.copy(alpha = 0.95f)
                                            isSpinWinningSpot && gameState == GameRoundState.SETTLED -> Color(0xFFEAB308) // Winning color glow
                                            else -> Color(0xFF0F2611).copy(alpha = 0.85f)
                                        }
                                    )
                                    .border(
                                        width = if (isSpinWinningSpot) 3.5.dp else 1.5.dp,
                                        color = when {
                                            isSpinWinningSpot -> Color(0xFFEF4444)
                                            isUserWagered -> Color(0xFF22C55E) // Placed bet indicates green ring
                                            else -> Color(0xFFFCD34D)
                                        },
                                        shape = CircleShape
                                    )
                                    .scale(if (isSpinWinningSpot) glowTransition.value else 1.0f)
                                    .clickable {
                                        if (gameState == GameRoundState.BETTING) {
                                            viewModel.placeBet(animalConfig.name)
                                        } else {
                                            Toast.makeText(context, "Betting time is locked!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(0.85f)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF225528), Color(0xFF061A0C))
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = animalConfig.iconEmoji,
                                        fontSize = 28.sp
                                    )
                                }
                            }

                            // Odds Multiplier Label Box (wood pattern styled badge)
                            Box(
                                modifier = Modifier
                                    .offset(y = (-4).dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF854D0E), Color(0xFF451A03))
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(1.dp, Color(0xFFFEF08A).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = "x${animalConfig.multiplier.toInt()}",
                                    color = Color(0xFFFEF08A),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            // User Placed Chip counter Badge (positioned dynamically)
                            val userBetOnThis = playerCurrentBets[animalConfig.name] ?: 0.0
                            if (userBetOnThis > 0) {
                                Box(
                                    modifier = Modifier
                                        .offset(y = (-68).dp)
                                        .background(Color(0xFF22C55E), RoundedCornerShape(10.dp))
                                        .border(0.8.dp, Color.White, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${userBetOnThis.toInt()}",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                // WHEEL INNER CENTER CARD: THE LION BOARD LOGO (lion_game_logo.png styled center)
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD97D), Color(0xFFB45309))
                            )
                        )
                        .border(3.2.dp, Color(0xFFFEF08A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (gameState == GameRoundState.SPINNING) {
                            Text("🦁", fontSize = 42.sp)
                            Text(
                                "SPINNING...",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        } else if (gameState == GameRoundState.SETTLED) {
                            val winIndex = highlightedIndex
                            val winnerObj = viewModel.animalsList.getOrNull(winIndex)
                            Text(
                                text = winnerObj?.iconEmoji ?: "🦁",
                                fontSize = 38.sp
                            )
                            Text(
                                text = "DONE!",
                                color = Color(0xFFFFF9C4),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        } else {
                            // Idle/Betting lion logo center
                            Text("🦁", fontSize = 44.sp)
                            Text(
                                text = "MIX SPIN",
                                color = Color(0xFFFFF9C4),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. SOCIAL INDICATORS & STATE GATE (Matches lower section above footer)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold viewer's social badge (social_group_badge)
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Viewers group",
                            tint = GoldYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$activeViewers",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Status Gate label indicator
                Box(
                    modifier = Modifier
                        .background(
                            if (gameState == GameRoundState.BETTING) Color(0xFF15803D).copy(alpha = 0.25f) else Color(0xFFB91C1C).copy(alpha = 0.25f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (gameState == GameRoundState.BETTING) Color(0xFF22C55E) else Color(0xFFEF4444),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (gameState == GameRoundState.BETTING) "BETTING OPEN" else "BETTING LOCKED",
                        color = if (gameState == GameRoundState.BETTING) Color(0xFF4ADE80) else Color(0xFFF87171),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. HIGH FIDELITY WOODEN BETTING PANEL (wood_control_footer.png)
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF532E14)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFFD97706),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF7E4A25), // Warm polish wood grain
                                    Color(0xFF3F1D08)  // Deep brown shadow
                                )
                            )
                        )
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Balance status pill & Repeat bets selector row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Wallet gold balance block
                        Row(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .clickable {
                                    Toast.makeText(context, "To Deposit/Withdraw, click the Add button in the Lobby!", Toast.LENGTH_LONG).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🪙", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (activeUser != null) String.format("%.0f", activeUser!!.balance) else "0",
                                color = GoldYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "reload currency balance",
                                tint = GoldYellow,
                                modifier = Modifier.size(11.dp)
                            )
                        }

                        // Play Repeat Last Betting schema button
                        val hasLastBetsConfig = lastBetsPlaced.isNotEmpty()
                        Button(
                            onClick = {
                                if (hasLastBetsConfig) {
                                    viewModel.repeatLastBets()
                                } else {
                                    Toast.makeText(context, "No betting history found!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasLastBetsConfig && gameState == GameRoundState.BETTING) Color(0xFFEAB308) else Color(0x803F1D08)
                            ),
                            border = BorderStroke(1.dp, if (hasLastBetsConfig) Color(0xFFFEF08A) else Color.Transparent),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("repeat_bet_button")
                        ) {
                            Text(
                                "Rebet",
                                color = if (hasLastBetsConfig) Color.Black else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 7. HORIZONTAL MULTI-COLORED POLISHED CHIPS SELECTION BAR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        viewModel.chipOptions.forEach { chipValue ->
                            val isSelectedChip = selectedChip == chipValue
                            val chipBackground = when (chipValue) {
                                10 -> Chip10Color
                                100 -> Chip100Color
                                500 -> Chip500Color
                                1000 -> Chip1000Color
                                else -> Chip10kColor
                            }

                            Box(
                                modifier = Modifier
                                    .size(if (isSelectedChip) 52.dp else 44.dp)
                                    .clip(CircleShape)
                                    .background(chipBackground)
                                    .border(
                                        width = if (isSelectedChip) 3.dp else 1.5.dp,
                                        color = if (isSelectedChip) Color.White else Color.Black.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .shadow(elevation = 6.dp, shape = CircleShape)
                                    .clickable { viewModel.selectChipValue(chipValue) }
                                    .testTag("bet_chip_$chipValue"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(0.85f)
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (chipValue >= 1000) "${chipValue / 1000}K" else "$chipValue",
                                        color = Color.White,
                                        fontSize = if (isSelectedChip) 12.sp else 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))
        }

        // --- VICTORY DECORATIVE COINS EXPLODING BANNER MODAL ---
        if (celebratoryCoinsVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF15803D)),
                    modifier = Modifier
                        .padding(24.dp)
                        .border(3.dp, GoldYellow, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✨ VICTORY! ✨", color = GoldYellow, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Large spin item
                        Text("🎉 🪙 🎉", fontSize = 54.sp)
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "You predicted correctly and received:",
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "+${userWonCelebrationAmount.toInt()} COINS",
                            color = GoldYellow,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // --- ADMIN SYSTEM CONTROL PANEL ---
        if (showAdminDashboard) {
            AdminDashboardDialog(
                viewModel = viewModel,
                onDismiss = { showAdminDashboard = false }
            )
        }

        // --- HELP / HOW TO PLAY RULES DIALOG ---
        if (showHelpDialog) {
            Dialog(onDismissRequest = { showHelpDialog = false }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "How to Play & Odds",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "1. The system features 8 animals, each categorized with different winning odds.\n" +
                                    "2. Players select their betting chip size and click on any animal to place bets before the 15-second timer runs out.\n" +
                                    "3. Once the countdown timer reaches 0s, the animal selector spins and lands randomly on a winning creature.\n" +
                                    "4. If the selector lands on your chosen animal, you get paid: [Your Bet Amount x Multiplier Mode].\n\n" +
                                    "Payout Rates:\n" +
                                    "• Rabbit, Cat, Dog, Sheep: x5 bet\n" +
                                    "• Panda: x10 bet\n" +
                                    "• Bear: x15 bet\n" +
                                    "• Tiger: x25 bet\n" +
                                    "• Lion: x45 bet",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showHelpDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("UNDERSTOOD", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


// SECTION 4: SIMULATED FINANCE & BANKING MODAL (Recharge/Deposit & Withdrawal)
@Composable
fun SimulatedFinancesDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activeUser by viewModel.activeUser.collectAsState(initial = null)
    
    var financeTypeIsDeposit by remember { mutableStateOf(true) }
    var inputAmount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("MOMO") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tab Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (financeTypeIsDeposit) GoldYellow else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { financeTypeIsDeposit = true }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "DEPOSIT",
                            color = if (financeTypeIsDeposit) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (!financeTypeIsDeposit) GoldYellow else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { financeTypeIsDeposit = false }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "WITHDRAW",
                            color = if (!financeTypeIsDeposit) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (financeTypeIsDeposit) {
                    Text(
                        "Automatic Deposit Portal (Simulated)",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Recharge unlimited mock coins to test and play!", color = Color.Gray, fontSize = 11.sp)
                } else {
                    Text(
                        "Withdraw Coins to Bank",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Withdraw winning coins to ZaloPay, MoMo, or Banks",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("MOMO", "ZALOPAY", "BANK").forEach { method ->
                        val isSelected = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) Color(0xFF334155) else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) GoldYellow else Color.Gray.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMethod = method }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(method, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Text Field
                OutlinedTextField(
                    value = inputAmount,
                    onValueChange = { inputAmount = it },
                    label = { Text("Enter coin amount", color = Color.LightGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldYellow,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Confirm Button
                Button(
                    onClick = {
                        val parsed = inputAmount.toDoubleOrNull()
                        if (parsed == null || parsed <= 0.0) {
                            Toast.makeText(context, "Please enter a valid coin amount.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (activeUser == null) {
                            Toast.makeText(context, "Please create a profile first.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (financeTypeIsDeposit) {
                            viewModel.depositFunds(parsed) { success ->
                                if (success) {
                                    Toast.makeText(context, "Successfully deposited +${parsed.toInt()} coins!", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                }
                            }
                        } else {
                            viewModel.withdrawFunds(parsed) { resCode ->
                                if (resCode == "SUCCESS") {
                                    Toast.makeText(context, "Successfully ordered withdrawal of ${parsed.toInt()} coins to $selectedMethod!", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, resCode, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (financeTypeIsDeposit) "CONFIRM DEPOSIT" else "CONFIRM WITHDRAWAL",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CLOSE", color = Color.White)
                }
            }
        }
    }
}


// SECTION 5: REFERRAL PORTAL & TASKS PANEL
@Composable
fun ReferralPortalDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activeUser by viewModel.activeUser.collectAsState(initial = null)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.padding(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Referrals & Sharing",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Invite friends to download the app and enter your code to receive double rewards",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (activeUser != null) {
                    val user = activeUser!!
                    
                    // Display referral code
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("YOUR REFERRAL CODE:", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                user.referralCode,
                                color = GoldYellow,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Receive +300 coins immediately when your friend signs up.",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF334155), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Invites", color = Color.Gray, fontSize = 10.sp)
                                Text("${user.totalReferralsCount}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF334155), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Accumulated", color = Color.Gray, fontSize = 10.sp)
                                Text("+${user.totalReferralsCount * 300} coins", color = GoldYellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Text("Please log in first to view your referral code.", color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// SECTION 6: Leaderboard Dialog Representation
@Composable
fun LeaderboardDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val leaderboardValues by viewModel.leaderboard.collectAsState(initial = emptyList())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .height(460.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Leaderboard",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Hall of fame displaying the top 20 users with the most coins",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (leaderboardValues.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No ranked members yet.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(leaderboardValues.withIndex().toList()) { (index, ranker) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Rank marker
                                    val medalEmoji = when (index) {
                                        0 -> "🥇"
                                        1 -> "🥈"
                                        2 -> "🥉"
                                        else -> "${index + 1}"
                                    }
                                    
                                    Text(
                                        medalEmoji,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.width(32.dp),
                                        textAlign = TextAlign.Center
                                    )

                                    // Avatar
                                    Text(
                                        AVATARS.getOrNull(ranker.avatarId) ?: "🐱",
                                        fontSize = 22.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )

                                    // User details
                                    Text(
                                        ranker.username,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Score / Wallet balance
                                    Text(
                                        String.format("%.0f coins", ranker.balance),
                                        color = GoldYellow,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CLOSE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// SECTION 7: ADMIN CONTROL CENTER (Allows instant RTP settings and history overrides)
@Composable
fun AdminDashboardDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentAdminConfig by viewModel.adminSettings.collectAsState(initial = null)
    val listSystemLogs by viewModel.listLogs.collectAsState(initial = emptyList())

    val customScope = rememberCoroutineScope()

    var rtpInput by remember { mutableStateOf("0.70") }
    var selectedRigMode by remember { mutableStateOf("NORMAL") }
    var countdownSecsInput by remember { mutableStateOf("15") }

    LaunchedEffect(currentAdminConfig) {
        if (currentAdminConfig != null) {
            val conf = currentAdminConfig!!
            rtpInput = String.format("%.2f", conf.rtpRatio)
            selectedRigMode = conf.isRiggedMode
            countdownSecsInput = "${conf.countdownSeconds}"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(550.dp)
                .border(2.dp, GoldYellow, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Topic header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🛡️ Admin Panel Controlling",
                        color = GoldYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                TabRow(
                    selectedTabIndex = 0,
                    containerColor = Color.Transparent,
                    contentColor = GoldYellow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(selected = true, onClick = {}, text = { Text("Game RTP Controls", color = Color.White) })
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // RTP Target settings
                    Text("RETURN TO PLAYER RATIO (RTP Target):", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = rtpInput.toFloatOrNull() ?: 0.5f,
                            onValueChange = { rtpInput = String.format("%.2f", it) },
                            valueRange = 0.0f..1.0f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = GoldYellow,
                                activeTrackColor = GoldYellow
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${((rtpInput.toFloatOrNull() ?: 0.5f) * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rig override selector
                    Text("SPIN ALGORITHM CONFIGURATION (Auto Rig Settings):", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    listOf(
                        "NORMAL" to "Random / Based on RTP",
                        "FORCE_WIN" to "Force Win",
                        "FORCE_MIN_LOSS" to "Force Lose"
                    ).forEach { (modeCode, modeLabel) ->
                        val isSel = selectedRigMode == modeCode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSel) Color(0xFF1E293B) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedRigMode = modeCode }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSel,
                                onClick = { selectedRigMode = modeCode },
                                colors = RadioButtonDefaults.colors(selectedColor = GoldYellow)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(modeCode, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(modeLabel, color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Timer config
                    Text("BETTING COUNTDOWN (Seconds):", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = countdownSecsInput,
                        onValueChange = { countdownSecsInput = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldYellow,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Apply actions
                    Button(
                        onClick = {
                            val rtpFloat = rtpInput.toFloatOrNull() ?: 0.70f
                            val secs = countdownSecsInput.toIntOrNull() ?: 15
                            viewModel.updateAdminToggles(rtpFloat, selectedRigMode, secs)
                            Toast.makeText(context, "RTP configurations saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("APPLY SETTINGS", color = Color.Black, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Audit transactions log
                    Text("SYSTEM LOGS & TRANSACTIONS:", color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .height(140.dp)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        if (listSystemLogs.isEmpty()) {
                            Text("No deposit or withdrawal history recorded.", color = Color.Gray, fontSize = 11.sp)
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(listSystemLogs) { log ->
                                    Text(
                                        text = "[${log.type}] User:${log.userId + 2317800} -> ${log.amount.toInt()} coins (${log.status})",
                                        color = if (log.type == "DEPOSIT") Color.Green else Color.Red,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
