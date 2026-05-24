package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.TeenlancerViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeenlancerApp(viewModel: TeenlancerViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val activeNotifications by viewModel.activeNotifications.collectAsStateWithLifecycle()
    val unreadNotifCount by viewModel.activeUnreadNotificationCount.collectAsStateWithLifecycle()
    val gigs by viewModel.gigs.collectAsStateWithLifecycle()
    val threads by viewModel.techThreads.collectAsStateWithLifecycle()
    val posts by viewModel.matchmakingPosts.collectAsStateWithLifecycle()
    val activeReviews by viewModel.activeReviews.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf("home") }
    var showUserSwitcher by remember { mutableStateOf(false) }
    var showNotificationSheet by remember { mutableStateOf(false) }

    // Floating Custom Toast State
    var activeToastMsg by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            activeToastMsg = msg
            delay(2500)
            if (activeToastMsg == msg) {
                activeToastMsg = null
            }
        }
    }

    val loggedInUserId by viewModel.loggedInUserId.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
    ) {
        if (loggedInUserId == null) {
            TeenlancerAuthScreen(
                viewModel = viewModel,
                allProfiles = allProfiles
            )
        } else {
            Scaffold(
            topBar = {
                // Customized Header for Teenlancer Hub
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SlateSurface,
                        titleContentColor = Color.White
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Teenlancer Hub",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                modifier = Modifier.testTag("app_title"),
                                letterSpacing = 0.5.sp,
                                color = CyberIndigo
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(CyberCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "U18 Gigs",
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    actions = {
                        // User Profile Switcher Button
                        Button(
                            onClick = { showUserSwitcher = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("user_switcher_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwitchAccount,
                                contentDescription = "Switch Profile",
                                modifier = Modifier.size(16.dp),
                                tint = CyberCyan
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = activeProfile?.username?.let { "@$it" } ?: "Select Profile",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Notification Bell with Badge
                        IconButton(
                            onClick = { showNotificationSheet = true },
                            modifier = Modifier.testTag("notification_bell")
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = if (unreadNotifCount > 0) GoldStreak else Color.White
                                )
                                if (unreadNotifCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(17.dp)
                                            .clip(CircleShape)
                                            .background(AccentCoral)
                                            .align(Alignment.TopEnd)
                                            .offset(x = 6.dp, y = (-5).dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = unreadNotifCount.toString(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                // Standard Material 3 Responsive Navigation Bar
                NavigationBar(
                    containerColor = SlateSurface,
                    contentColor = TextPrimaryLight,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == "home",
                        onClick = { currentTab = "home" },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "home") Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SlateDark,
                            selectedTextColor = CyberIndigo,
                            indicatorColor = CyberIndigo,
                            unselectedTextColor = TextSecondaryLight,
                            unselectedIconColor = TextSecondaryLight
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentTab == "marketplace",
                        onClick = { currentTab = "marketplace" },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "marketplace") Icons.Default.Storefront else Icons.Outlined.Storefront,
                                contentDescription = "Marketplace"
                            )
                        },
                        label = { Text("Marketplace", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SlateDark,
                            selectedTextColor = CyberCyan,
                            indicatorColor = CyberCyan,
                            unselectedTextColor = TextSecondaryLight,
                            unselectedIconColor = TextSecondaryLight
                        ),
                        modifier = Modifier.testTag("nav_marketplace")
                    )

                    NavigationBarItem(
                        selected = currentTab == "forum",
                        onClick = { currentTab = "forum" },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "forum") Icons.Default.Forum else Icons.Outlined.Forum,
                                contentDescription = "Tech Forum"
                            )
                        },
                        label = { Text("Forum", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SlateDark,
                            selectedTextColor = CyberIndigo,
                            indicatorColor = CyberIndigo,
                            unselectedTextColor = TextSecondaryLight,
                            unselectedIconColor = TextSecondaryLight
                        ),
                        modifier = Modifier.testTag("nav_forum")
                    )

                    NavigationBarItem(
                        selected = currentTab == "matchmaking",
                        onClick = { currentTab = "matchmaking" },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "matchmaking") Icons.Default.Handshake else Icons.Outlined.Handshake,
                                contentDescription = "Matchmaking"
                            )
                        },
                        label = { Text("Teams", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SlateDark,
                            selectedTextColor = CyberCyan,
                            indicatorColor = CyberCyan,
                            unselectedTextColor = TextSecondaryLight,
                            unselectedIconColor = TextSecondaryLight
                        ),
                        modifier = Modifier.testTag("nav_teams")
                    )

                    NavigationBarItem(
                        selected = currentTab == "leaderboard",
                        onClick = { currentTab = "leaderboard" },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "leaderboard") Icons.Default.Leaderboard else Icons.Outlined.Leaderboard,
                                contentDescription = "Showcase"
                            )
                        },
                        label = { Text("Showcase", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SlateDark,
                            selectedTextColor = GoldStreak,
                            indicatorColor = GoldStreak,
                            unselectedTextColor = TextSecondaryLight,
                            unselectedIconColor = TextSecondaryLight
                        ),
                        modifier = Modifier.testTag("nav_leaderboard")
                    )

                    NavigationBarItem(
                        selected = currentTab == "profile",
                        onClick = { currentTab = "profile" },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "profile") Icons.Default.Person else Icons.Outlined.Person,
                                contentDescription = "My Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SlateDark,
                            selectedTextColor = CyberCyan,
                            indicatorColor = CyberCyan,
                            unselectedTextColor = TextSecondaryLight,
                            unselectedIconColor = TextSecondaryLight
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    "home" -> HomeScreen(
                        viewModel = viewModel,
                        gigs = gigs,
                        threads = threads,
                        posts = posts,
                        currentProfile = activeProfile ?: allProfiles.getOrNull(0),
                        onNavigateToTab = { currentTab = it }
                    )
                    "marketplace" -> MarketplaceScreen(viewModel, gigs, activeProfile ?: allProfiles.getOrNull(0))
                    "forum" -> TechForumScreen(viewModel, threads, activeProfile ?: allProfiles.getOrNull(0))
                    "matchmaking" -> TeamMatchmakingScreen(viewModel, posts, activeProfile ?: allProfiles.getOrNull(0))
                    "leaderboard" -> ShowcaseLeaderboardScreen(viewModel, allProfiles)
                    "profile" -> UserProfileScreen(viewModel, activeProfile, activeReviews)
                }
            }
        }
    }

        // Animated Notification Overlay Toast Message
        AnimatedVisibility(
            visible = activeToastMsg != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
                .padding(horizontal = 16.dp)
                .zIndex(100f)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberIndigo),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(0.95f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Alert",
                        tint = GoldStreak,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = activeToastMsg ?: "",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Active User Profile Switcher Bottom Modal/Overlay
        if (showUserSwitcher) {
            Dialog(onDismissRequest = { showUserSwitcher = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(16.dp)
                        .testTag("user_switcher_modal")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Switch Active Workspace User",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Choose any teen persona to simulate and test real-time communication, FamPay transaction review, co-founder match, notification trigger, and forum boards on this emulator device.",
                            fontSize = 12.sp,
                            color = TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        allProfiles.forEach { profile ->
                            val isCurrent = profile.id == activeProfile?.id
                            Card(
                                onClick = {
                                    viewModel.switchActiveUser(profile.id)
                                    showUserSwitcher = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = if (isCurrent) BorderStroke(1.dp, CyberCyan) else null,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) CyberIndigo.copy(alpha = 0.2f) else SlateSurfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(CyberIndigo, CyberCyan)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = profile.name.take(1),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = profile.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "@${profile.username} • ${profile.rank}",
                                            fontSize = 11.sp,
                                            color = TextSecondaryLight
                                        )
                                    }
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Active",
                                            tint = CyberCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = { showUserSwitcher = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Dismiss", color = CyberCyan)
                        }
                    }
                }
            }
        }

        // Notification List History Bottom Sheet Layer
        if (showNotificationSheet) {
            Dialog(onDismissRequest = { showNotificationSheet = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.7f)
                        .padding(16.dp)
                        .testTag("notification_history_modal")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Inbox Notifications",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )

                            TextButton(onClick = { viewModel.markAllNotificationsRead() }) {
                                Text("Mark Read", color = CyberCyan, fontSize = 12.sp)
                            }
                        }
                        
                        Divider(color = BorderSlate, modifier = Modifier.padding(vertical = 8.dp))

                        if (activeNotifications.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                        .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.MailOutline,
                                        contentDescription = "Empty Inbox",
                                        tint = TextSecondaryLight,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Inbox is empty!",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Switch users & try submitting checkout proofs or applying for matchmaking roles to see notification updates.",
                                        fontSize = 11.sp,
                                        color = TextSecondaryLight,
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f)
                            ) {
                                items(activeNotifications) { entry ->
                                    val isUnread = !entry.isRead
                                    Card(
                                        onClick = { viewModel.markNotificationRead(entry.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isUnread) CyberIndigo.copy(alpha = 0.15f) else SlateSurfaceVariant
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = when (entry.type) {
                                                            "PAYMENT_PROOF" -> Icons.Default.CreditCard
                                                            "COFOUNDER_APPLY" -> Icons.Default.Groups
                                                            "FORUM_REPLY" -> Icons.Default.ModeComment
                                                            else -> Icons.Default.NotificationImportant
                                                        },
                                                        contentDescription = null,
                                                        tint = when (entry.type) {
                                                            "PAYMENT_PROOF" -> CyberCyan
                                                            "COFOUNDER_APPLY" -> CyberIndigo
                                                            "FORUM_REPLY" -> GoldStreak
                                                            else -> Color.White
                                                        },
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = entry.title,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color.White
                                                    )
                                                }
                                                if (isUnread) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(AccentCoral, CircleShape)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${entry.senderName} ${entry.message}",
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Action Buttons inside notification cards!
                                            if (entry.type == "PAYMENT_PROOF") {
                                                Row(
                                                    horizontalArrangement = Arrangement.End,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.declinePaymentProof(entry.relatedId, "Incomplete Proof")
                                                            viewModel.markNotificationRead(entry.id)
                                                            showNotificationSheet = false
                                                        },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCoral),
                                                        border = BorderStroke(1.dp, AccentCoral),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Text("Decline", fontSize = 10.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Button(
                                                        onClick = {
                                                            viewModel.approvePaymentProof(entry.relatedId)
                                                            viewModel.markNotificationRead(entry.id)
                                                            showNotificationSheet = false
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Text("Approve", fontSize = 10.sp, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        TextButton(
                            onClick = { showNotificationSheet = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Dismiss", color = CyberCyan)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------
// 1. Marketplace Screen (Gig Hub)
// ------------------------------------
@Composable
fun MarketplaceScreen(viewModel: TeenlancerViewModel, gigs: List<Gig>, currentProfile: UserProfile?) {
    val context = LocalContext.current
    var showPostGigDialog by remember { mutableStateOf(false) }
    var showCheckoutModalGig by remember { mutableStateOf<Gig?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }
    var upiFormDialog by remember { mutableStateOf(false) }

    // Forms
    var upiInput by remember { mutableStateOf(currentProfile?.fampayUpi ?: "") }

    LaunchedEffect(currentProfile) {
        if (currentProfile != null) {
            upiInput = currentProfile.fampayUpi ?: ""
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
        // Welcome and Seller Setup Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Welcome, ${currentProfile?.name ?: "Teenlancer"}!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Earn money by doing digital gigs for other teens. Payments go through direct bank-to-bank FamPay UPI safely without age boundaries.",
                    fontSize = 11.sp,
                    color = TextSecondaryLight
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Quick Status Check of Payment configuration
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (currentProfile?.fampayUpi != null) AccentEmerald else AccentCoral,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentProfile?.fampayUpi != null) "FamPay Active: ${currentProfile.fampayUpi}" else "FamPay Setup Missing!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (currentProfile?.fampayUpi != null) AccentEmerald else AccentCoral
                        )
                    }

                    Button(
                        onClick = { upiFormDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Configure FamPay", fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Categories selector scroll row
        val categoriesList = listOf("All", "Video Editing", "Web Dev", "Design", "Writing")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categoriesList) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = {
                        selectedCategory = cat
                        viewModel.selectedGigCategory.value = cat
                    },
                    label = { Text(cat, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan,
                        selectedLabelColor = SlateDark,
                        containerColor = SlateSurface,
                        labelColor = TextSecondaryLight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Header Title + Create Gig Button
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Giga Marketplace ($selectedCategory)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimaryLight
            )

            Button(
                onClick = { showPostGigDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("post_gig_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp), tint = SlateDark)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Post a Gig", fontSize = 11.sp, color = SlateDark, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Gigs list
        if (gigs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterNone,
                        contentDescription = "No Open Gigs",
                        tint = TextSecondaryLight,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "No open opportunities in this category.", color = TextSecondaryLight, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(gigs) { gig ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                        border = BorderStroke(1.dp, BorderSlate),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gig_card_${gig.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(0.7f)) {
                                    Box(
                                        modifier = Modifier
                                            .background(CyberIndigo.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = gig.category,
                                            color = CyberIndigo,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = gig.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }

                                Text(
                                    text = "₹${gig.budget}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = CyberCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = gig.description,
                                fontSize = 12.sp,
                                color = TextSecondaryLight,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = BorderSlate.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Posted by @${gig.creatorName.split(" ").firstOrNull() ?: gig.creatorName}",
                                    fontSize = 11.sp,
                                    color = TextSecondaryLight
                                )

                                val isCreator = gig.creatorId == currentProfile?.id
                                val statusLabel = gig.status.replace("_", " ")

                                // Display action or status button
                                if (isCreator) {
                                    if (gig.status == "Proof_Submitted") {
                                        Box(
                                            modifier = Modifier
                                                .background(AccentEmerald.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .border(BorderStroke(1.dp, AccentEmerald), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Review Payment Proof",
                                                color = AccentEmerald,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable {
                                                    // Trigger review alert
                                                    viewModel.switchActiveUser(gig.creatorId)
                                                }
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "My Gig • $statusLabel",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyberCyan
                                        )
                                    }
                                } else {
                                    when (gig.status) {
                                        "Open" -> {
                                            Button(
                                                onClick = {
                                                    println("DEBUG: Proceed Checkout CLICKED for gig ID ${gig.id}")
                                                    showCheckoutModalGig = gig
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text("Proceed Checkout", fontSize = 11.sp)
                                            }
                                        }
                                        "In_Progress" -> {
                                            Text("In Progress", color = GoldStreak, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        "Proof_Submitted" -> {
                                            Text("Under Verification", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        "Completed" -> {
                                            Text("Completed 👍", color = AccentEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Sell Setup Profile Form Dialog
    if (upiFormDialog) {
        Dialog(onDismissRequest = { upiFormDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FamPay UPI Profile Configuration",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Under-18 entrepreneurs set up receipts. Input your FamPay UPI handle and upload your QR screenshot.",
                        fontSize = 11.sp,
                        color = TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = upiInput,
                        onValueChange = { upiInput = it },
                        label = { Text("FamPay UPI ID (username@fam)") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SlateSurfaceVariant,
                            unfocusedContainerColor = SlateSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondaryLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "FamPay Receipt QR Screenshot:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Simulated Upload Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .drawBehind {
                                val stroke = Stroke(
                                    width = 2f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                                drawRoundRect(
                                    color = BorderSlate,
                                    style = stroke
                                )
                            }
                            .clickable {
                                // Simulate QR select success
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "QR selection",
                                tint = CyberCyan,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Click to select QR screenshot", fontSize = 11.sp, color = TextSecondaryLight)
                            Text("Simulating: fampay_qr_active.png", fontSize = 10.sp, color = AccentEmerald, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { upiFormDialog = false }) {
                            Text("Discard", color = TextSecondaryLight)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.updateSellerPaymentInfo(upiInput, "custom_qr_uri")
                                upiFormDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Text("Save Credentials", color = SlateDark)
                        }
                    }
                }
            }
        }
    }

    // Post a New Gig Dialog Form
    if (showPostGigDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var budgetString by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Video Editing") }

        Dialog(onDismissRequest = { showPostGigDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(16.dp)
                    .testTag("post_gig_dialog")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Post a Digital Gig Offer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Gig Title") },
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Work Details & Instruction") },
                        maxLines = 4,
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = budgetString,
                            onValueChange = { budgetString = it },
                            label = { Text("Budget in ₹") },
                            colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )

                        // Category Dropdown simulated clicker
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Theme Category:", fontSize = 10.sp, color = TextSecondaryLight)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SlateSurfaceVariant, RoundedCornerShape(4.dp))
                                    .clickable {
                                        category = if (category == "Video Editing") "Web Dev" else if (category == "Web Dev") "Design" else "Video Editing"
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(category, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showPostGigDialog = false }) {
                            Text("Cancel", color = TextSecondaryLight)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val budget = budgetString.toIntOrNull() ?: 500
                                viewModel.postGig(title, description, budget, category)
                                showPostGigDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Text("Upload Gig", color = SlateDark)
                        }
                    }
                }
            }
        }
    }

    // Buyer Checkout Modal popup (Custom overlay Box instead of system Dialog to prevent OS-level focus/input crashes)
    println("DEBUG: MarketplaceScreen showCheckoutModalGig is: ${showCheckoutModalGig?.id}")
    if (showCheckoutModalGig != null) {
        val gig = showCheckoutModalGig!!
        var hasCopied by remember { mutableStateOf(false) }
        val upiToCopy = "aarav@fam" // simulated
        
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(enabled = true, onClick = { showCheckoutModalGig = null }),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(16.dp)
                    .clickable(enabled = true, onClick = { /* consume clicks */ })
                    .testTag("buyer_checkout_modal")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FamPay Direct Checkout Escrow",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Direct peer payment. Follow instructions below to verify trade completely:",
                        fontSize = 11.sp,
                        color = TextSecondaryLight
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 1: Copy UPI
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("STEP 1: Copy Seller FamPay UPI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "seller_handle@fam",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("upi_text_value")
                                )

                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("fampay_upi", "seller_handle@fam")
                                        clipboard.setPrimaryClip(clip)
                                        hasCopied = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (hasCopied) AccentEmerald else CyberIndigo),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text(if (hasCopied) "Copied!" else "Copy ID", fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 2: Display QR Code
                    Text("STEP 2: Scan FamPay UPI Code & Transfer ₹${gig.budget}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Visual Mock QR drawing using custom Composable
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing simulated beautiful QR screen inside Canvas
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(color = SlateDark, topLeft = Offset(10f, 10f), size = androidx.compose.ui.geometry.Size(30f, 30f))
                            drawRect(color = SlateDark, topLeft = Offset(size.width - 40f, 10f), size = androidx.compose.ui.geometry.Size(30f, 30f))
                            drawRect(color = SlateDark, topLeft = Offset(10f, size.height - 40f), size = androidx.compose.ui.geometry.Size(30f, 30f))
                            
                            // Random QR blocks decoration
                            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            drawRoundRect(
                                color = SlateDark,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f),
                                style = Stroke(width = 4f, pathEffect = pathEffect)
                            )
                        }

                        // Little FamPay logo icon simulated
                        Box(
                            modifier = Modifier
                                .background(CyberIndigo, RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("fam", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 3: Transaction proof upload simulator
                    Text("STEP 3: Upload Transfer Screenshot Receipt", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(SlateSurfaceVariant, RoundedCornerShape(6.dp))
                            .clickable {
                                // Cloud Simulator: Auto-fill proof to prevent gallery implicit intent crashes on headless env
                                android.widget.Toast.makeText(
                                    context,
                                    "Simulator attached transfer receipt screenshot: fampay_success_receipt_9921_fampay.png!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                selectedImageUri = Uri.parse("content://simulated/fampay_success_receipt_9921_fampay.png")
                            }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            val fileNameLabel = selectedImageUri?.lastPathSegment?.substringAfterLast("/") ?: "Tap to choose screenshot receipt"
                            Text(
                                text = fileNameLabel,
                                color = if (selectedImageUri != null) AccentEmerald else CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showCheckoutModalGig = null }) {
                            Text("Abort", color = TextSecondaryLight)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.submitPaymentProof(
                                    context = context,
                                    gigId = gig.id,
                                    proofUri = selectedImageUri?.toString() ?: "simulated_local_fampay_receipt",
                                    imageUri = selectedImageUri
                                )
                                showCheckoutModalGig = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                        ) {
                            Text("Submit Verification Proof", color = Color.White)
                        }
                    }
                }
            }
        }
    }
    }
}

// ------------------------------------
// 2. Tech Discussion Forum Screen
// ------------------------------------
@Composable
fun TechForumScreen(viewModel: TeenlancerViewModel, threads: List<TechThread>, currentProfile: UserProfile?) {
    var showCreateThread by remember { mutableStateOf(false) }
    var selectedThread by remember { mutableStateOf<TechThread?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }

    if (selectedThread != null) {
        ThreadDetailsView(viewModel, selectedThread!!, currentProfile, onClose = { selectedThread = null })
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Forum intro
            Text(
                text = "Teen Tech Discussion Forum",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Share builds, resolve system bugs, query AI prompts, or match on upcoming software hackathons.",
                fontSize = 11.sp,
                color = TextSecondaryLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Topics tags filter row
            val categoriesList = listOf("All", "AI Tools", "WebDev", "Bugs", "Hackathons")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categoriesList) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = cat
                            viewModel.selectedForumCategory.value = cat
                        },
                        label = { Text(cat, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberIndigo,
                            selectedLabelColor = Color.White,
                            containerColor = SlateSurface,
                            labelColor = TextSecondaryLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Topic Feed Header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Discussion Threads",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )

                Button(
                    onClick = { showCreateThread = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Thread", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Threads Feed
            if (threads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No forum threads available in this topic tag.", color = TextSecondaryLight, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(threads) { item ->
                        Card(
                            onClick = { selectedThread = item },
                            colors = CardDefaults.cardColors(containerColor = SlateSurface),
                            border = BorderStroke(1.dp, BorderSlate.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(CyberCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.category,
                                        color = CyberCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.content,
                                    fontSize = 12.sp,
                                    color = TextSecondaryLight,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = BorderSlate.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "By @${item.creatorName.split(" ").firstOrNull()}",
                                        fontSize = 11.sp,
                                        color = TextSecondaryLight
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ModeComment,
                                            contentDescription = "comments",
                                            tint = TextSecondaryLight,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Tap to reply",
                                            fontSize = 11.sp,
                                            color = CyberIndigo,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create New Thread Dialog Formulation
    if (showCreateThread) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("AI Tools") }
        var shareScreenshot by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showCreateThread = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Publish Discussion Thread",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Thread Topic Title") },
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Describe details...") },
                        maxLines = 4,
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Topic Tag Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Forum Tag:", fontSize = 10.sp, color = TextSecondaryLight)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { category = "AI Tools" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (category == "AI Tools") CyberIndigo else SlateSurfaceVariant)
                                ) { Text("AI", fontSize = 10.sp) }

                                Button(
                                    onClick = { category = "Bugs" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (category == "Bugs") CyberIndigo else SlateSurfaceVariant)
                                ) { Text("Bugs", fontSize = 10.sp) }

                                Button(
                                    onClick = { category = "Hackathons" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (category == "Hackathons") CyberIndigo else SlateSurfaceVariant)
                                ) { Text("Hackathons", fontSize = 10.sp) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Share Build Screenshot Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = shareScreenshot,
                            onCheckedChange = { shareScreenshot = it },
                            colors = CheckboxDefaults.colors(checkedColor = CyberCyan)
                        )
                        Column {
                            Text("Attach Code Builder Screenshot", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Simulate code screenshot for design feedback", fontSize = 10.sp, color = TextSecondaryLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showCreateThread = false }) {
                            Text("Discard", color = TextSecondaryLight)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.createTechThread(
                                    title,
                                    content,
                                    category,
                                    if (shareScreenshot) "simulated_code_snapshot" else null
                                )
                                showCreateThread = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Text("Publish Thread", color = SlateDark)
                        }
                    }
                }
            }
        }
    }
}

// Tech Thread Details Component (Deep Page)
@Composable
fun ThreadDetailsView(viewModel: TeenlancerViewModel, thread: TechThread, currentProfile: UserProfile?, onClose: () -> Unit) {
    val repliesFlow = remember(thread.id) { viewModel.getRepliesForThread(thread.id) }
    val replies by repliesFlow.collectAsStateWithLifecycle(emptyList())
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TextButton(onClick = onClose) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Go back", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back to threads Feed", color = CyberCyan)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Entire Thread block
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, CyberIndigo.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .background(CyberIndigo.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = thread.category, color = CyberIndigo, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = thread.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = thread.content,
                    fontSize = 13.sp,
                    color = Color.White
                )

                // If code screenshot is attached, draw a nice modern mock screenshot banner!
                if (thread.imageUri != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF020617)) // Dark tech terminal bg
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(AccentCoral, CircleShape))
                                Box(modifier = Modifier.size(6.dp).background(GoldStreak, CircleShape))
                                Box(modifier = Modifier.size(6.dp).background(AccentEmerald, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("NextjsHydrationBug.tsx", color = TextSecondaryLight, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1  export default function App() {\n2    const streak = localStorage.getItem(\"streak\")\n3    return (\n4      <div className=\"badge\">\n5         Streak: {streak} // Mismatch here!\n6      </div>\n7    )\n8  }",
                                color = Color(0xFFA5B4FC), // Nice indigo code text
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Authored by @${thread.creatorName} at ${java.text.SimpleDateFormat("hh:mm a").format(java.util.Date(thread.createdTime))}",
                    fontSize = 10.sp,
                    color = TextSecondaryLight
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Replies history
        Text("Board Replies (${replies.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        Spacer(modifier = Modifier.height(6.dp))

        replies.forEach { reply ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "@${reply.creatorName.split(" ").firstOrNull() ?: reply.creatorName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CyberCyan
                        )
                        Text(
                            text = java.text.SimpleDateFormat("hh:mm a").format(java.util.Date(reply.createdTime)),
                            fontSize = 10.sp,
                            color = TextSecondaryLight
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = reply.replyText, fontSize = 12.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Write a reply form entry
        OutlinedTextField(
            value = replyText,
            onValueChange = { replyText = it },
            placeholder = { Text("Write your expert help response...") },
            colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (replyText.isNotBlank()) {
                    viewModel.replyToTechThread(thread.id, replyText)
                    replyText = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Send Help Reply")
        }
    }
}

// ------------------------------------
// 3. Find a Co-Founder / Team Matchmaking
// ------------------------------------
@Composable
fun TeamMatchmakingScreen(viewModel: TeenlancerViewModel, posts: List<MatchmakingPost>, currentProfile: UserProfile?) {
    var showCreatePost by remember { mutableStateOf(false) }
    var applyToPost by remember { mutableStateOf<MatchmakingPost?>(null) }
    var pitchInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Intro Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Find a Teen Co-Founder 🤝",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Have a crazy project idea but missing a coder, artist, UI designer, or manager? Pitch it here and form a dedicated team of under-18 minds.",
                    fontSize = 11.sp,
                    color = TextSecondaryLight
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Active Team Pitch Listings",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimaryLight
            )

            Button(
                onClick = { showCreatePost = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = SlateDark)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Post Matchmaking", fontSize = 11.sp, color = SlateDark, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No active co-founder listings matching currently.", color = TextSecondaryLight, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(posts) { item ->
                    val isMyPost = item.creatorId == currentProfile?.id
                    
                    // Observe applications for this post dynamically
                    val applicationsFlow = remember(item.id) { viewModel.getRepliesForThread(item.id) } // fallback check
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                        border = BorderStroke(1.dp, BorderSlate),
                        modifier = Modifier.fillMaxWidth().testTag("team_post_card_${item.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )

                                Box(
                                    modifier = Modifier
                                        .background(CyberCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.roleRequired,
                                        color = CyberCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.description,
                                fontSize = 12.sp,
                                color = TextSecondaryLight
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = BorderSlate.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Pitched by @${item.creatorName.split(" ").firstOrNull()}",
                                    fontSize = 11.sp,
                                    color = TextSecondaryLight
                                )

                                if (isMyPost) {
                                    Text(
                                        text = "My Post (Under Review)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                } else {
                                    Button(
                                        onClick = { applyToPost = item },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Apply now", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Apply pitch formulation Dialog Overlay
    if (applyToPost != null) {
        val targetPost = applyToPost!!
        Dialog(onDismissRequest = { applyToPost = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pitch For Co-Founder Team",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Make a concise, expert reply about why you are perfect for the \"${targetPost.roleRequired}\" role.",
                        fontSize = 11.sp,
                        color = TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pitchInput,
                        onValueChange = { pitchInput = it },
                        placeholder = { Text("Describe your skills, portfolio links or tools stack which fits perfectly...") },
                        maxLines = 5,
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { applyToPost = null }) {
                            Text("Discard", color = TextSecondaryLight)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (pitchInput.isNotBlank()) {
                                    viewModel.applyToMatchmakingPost(targetPost.id, targetPost.title, pitchInput)
                                    pitchInput = ""
                                    applyToPost = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Text("Send Pitch Invite", color = SlateDark)
                        }
                    }
                }
            }
        }
    }

    // Create Matchmaking Post Dialog Overlay
    if (showCreatePost) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var roleRequired by remember { mutableStateOf("React Developer") }

        Dialog(onDismissRequest = { showCreatePost = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Publish Team Matchmaking Invite",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Project Concept / SaaS Name") },
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Scope & Division of Work Share") },
                        maxLines = 4,
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Role Required Selector option
                    Column {
                        Text("Partner Specialization Needed:", fontSize = 10.sp, color = TextSecondaryLight)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { roleRequired = "React Developer" },
                                colors = ButtonDefaults.buttonColors(containerColor = if (roleRequired == "React Developer") CyberIndigo else SlateSurfaceVariant)
                            ) { Text("React Dev", fontSize = 10.sp) }

                            Button(
                                onClick = { roleRequired = "UI/UX Designer" },
                                colors = ButtonDefaults.buttonColors(containerColor = if (roleRequired == "UI/UX Designer") CyberIndigo else SlateSurfaceVariant)
                            ) { Text("UI Designer", fontSize = 10.sp) }

                            Button(
                                onClick = { roleRequired = "Android Developer" },
                                colors = ButtonDefaults.buttonColors(containerColor = if (roleRequired == "Android Developer") CyberIndigo else SlateSurfaceVariant)
                            ) { Text("Android", fontSize = 10.sp) }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showCreatePost = false }) {
                            Text("Cancel", color = TextSecondaryLight)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.postMatchmakingPost(title, description, roleRequired)
                                showCreatePost = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Text("Publish Pitch", color = SlateDark)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------
// 4. Project Showcase & Leaderboard (Streaks and Badges)
// ------------------------------------
@Composable
fun ShowcaseLeaderboardScreen(viewModel: TeenlancerViewModel, profiles: List<UserProfile>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Gamification Header
        Text(
            text = "Gamified Project of the Week",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Text(
            text = "Outstanding creations uploaded by teens receive organic reach and milestone crown status badges.",
            fontSize = 11.sp,
            color = TextSecondaryLight
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Project of the week showcase board representation
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, GoldStreak),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(GoldStreak.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldStreak, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("👑 CHAMPION PICKS", color = GoldStreak, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(CyberIndigo.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Active Streak: 12 days", color = CyberIndigo, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Aora CLI: Local developer playground for Android UI",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "Built a tiny automated toolkit inside Node.js which renders instantaneous live code previews directly onto connected testing Android models. Under 18 hours build time!",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(CyberCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Created by @aarav_edits", fontSize = 11.sp, color = Color.White)
                    }

                    // Display reviews or badges
                    Text("⭐⭐⭐⭐⭐ (24 votes)", fontSize = 11.sp, color = GoldStreak, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Top Teenlancers Leaderboard
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Leaderboard, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Teenlancer Leaderboard Standings",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sort profiles by score to make it a REAL leaderboard!
        val sortedProfiles = profiles.sortedByDescending { it.score }

        sortedProfiles.forEachIndexed { idx, person ->
            val isFirst = idx == 0
            val isSecond = idx == 1

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = if (isFirst) BorderStroke(1.dp, GoldStreak) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leaderboard index/rank
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFirst) GoldStreak else if (isSecond) CyberCyan else SlateSurfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (idx + 1).toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (isFirst) Color.White else if (isSecond) SlateDark else TextPrimaryLight,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = person.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )

                            if (person.streak >= 5) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(GoldStreak.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = GoldStreak, modifier = Modifier.size(10.dp))
                                    Text("${person.streak}d", color = GoldStreak, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Display top 1 or 2 badges
                        val listBadges = person.badges.split(",").filter { it.isNotBlank() }
                        if (listBadges.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                listBadges.take(2).forEach { badge ->
                                    Box(
                                        modifier = Modifier
                                            .background(SlateSurfaceVariant, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(badge.trim(), fontSize = 9.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${person.score} XP",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = CyberCyan
                        )
                        Text(
                            text = "${person.completedGigs} Gigs Done",
                            fontSize = 10.sp,
                            color = TextSecondaryLight
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------
// 5. My Profile Screen
// ------------------------------------
@Composable
fun UserProfileScreen(viewModel: TeenlancerViewModel, currentProfile: UserProfile?, reviews: List<Review>) {
    if (currentProfile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CyberCyan)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Detailed Card Header
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, CyberIndigo)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(CyberIndigo, CyberCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentProfile.name.take(1),
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentProfile.name,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.White
                )

                Text(
                    text = "@${currentProfile.username} • ${currentProfile.rank}",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Location",
                        tint = AccentCoral,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentProfile.location,
                        color = TextSecondaryLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentProfile.bio,
                    fontSize = 12.sp,
                    color = TextSecondaryLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = BorderSlate.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Stats row
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("XP CREDITS", fontSize = 10.sp, color = TextSecondaryLight)
                        Text(
                            text = "₹${currentProfile.score}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldStreak
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("WORK STREAK", fontSize = 10.sp, color = TextSecondaryLight)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = GoldStreak, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${currentProfile.streak} Days",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldStreak
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("COMPLETED", fontSize = 10.sp, color = TextSecondaryLight)
                        Text(
                            text = "${currentProfile.completedGigs} Gigs",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Milestones and Streaks Tracker Block
        Text("Target Milestones & Goals Tracker", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        Spacer(modifier = Modifier.height(6.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Current Track Target Gigs: ${currentProfile.completedGigs}/${currentProfile.targetGoalGigs}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        text = "${((currentProfile.completedGigs.toFloat() / currentProfile.targetGoalGigs.toFloat()) * 100).toInt()}% Done",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Linear indicator Progress
                LinearProgressIndicator(
                    progress = { currentProfile.completedGigs.toFloat() / currentProfile.targetGoalGigs.toFloat() },
                    color = CyberCyan,
                    trackColor = SlateSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Hit target goals to unlock extra profile badges and credit crowns!",
                        fontSize = 11.sp,
                        color = TextSecondaryLight,
                        modifier = Modifier.weight(0.7f)
                    )

                    Button(
                        onClick = { viewModel.triggerDailyStreakProgress() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("increment_streak_btn")
                    ) {
                        Text("Increment Streak Test", fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Back-end Server Settings Bento Card
        Text("Backends & Network Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Sync and WebSocket server address configuration for under-18 cooperative transactions, matchmaking logs, and real file receipt verification uploads.",
                    fontSize = 11.sp,
                    color = TextSecondaryLight
                )
                Spacer(modifier = Modifier.height(10.dp))

                var localIpText by remember { mutableStateOf(viewModel.getServerAddress()) }

                OutlinedTextField(
                    value = localIpText,
                    onValueChange = { localIpText = it },
                    label = { Text("Server Base URL (IP:Port)", color = CyberCyan, fontSize = 11.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateSurfaceVariant,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondaryLight
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("server_address_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.updateServerAddress(localIpText) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                        modifier = Modifier.weight(1f).testTag("save_server_btn")
                    ) {
                        Text("Save & Connect", fontSize = 11.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { viewModel.triggerServerManualSync() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                        modifier = Modifier.weight(1f).testTag("sync_server_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manual Sync", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Earned Milestone Badges Shelf
        Text("My Unlocked Milestones Badges Shelf", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        Spacer(modifier = Modifier.height(6.dp))

        val listBadges = currentProfile.badges.split(",").filter { it.isNotBlank() }
        if (listBadges.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No milestones badges unlocked yet. Complete gigs and maintain streaks to achieve awards!",
                    fontSize = 11.sp,
                    color = TextSecondaryLight,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(listBadges) { badge ->
                    Box(
                        modifier = Modifier
                            .background(CyberIndigo.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                            .border(BorderStroke(1.dp, CyberIndigo), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = badge.trim(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Reviews Shelf
        Text("Client Reviews & Feedbacks (${reviews.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        Spacer(modifier = Modifier.height(6.dp))

        if (reviews.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No review feedbacks logged on your profile currently.",
                    fontSize = 11.sp,
                    color = TextSecondaryLight,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            reviews.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "By @${item.reviewerName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = CyberCyan
                            )

                            // Star ratings drawing
                            Text(
                                text = "★".repeat(item.rating.toInt()),
                                color = GoldStreak,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = "Project Match: ${item.gigTitle}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.comment,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text("Active Session Controls", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, AccentCoral.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "You are currently signed in as @${currentProfile.username} (${currentProfile.location}). For production environments or public devices, always lock or log out to protect your credits, ongoing contracts, and UPI info.",
                    fontSize = 11.sp,
                    color = TextSecondaryLight
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                    modifier = Modifier.fillMaxWidth().testTag("secure_logout_btn")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Secure Logout Session", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: TeenlancerViewModel,
    gigs: List<Gig>,
    threads: List<TechThread>,
    posts: List<MatchmakingPost>,
    currentProfile: UserProfile?,
    onNavigateToTab: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFEF7FF)) // Light lavender-rose white
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming header inside main bento
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back, ${currentProfile?.name ?: "Teenlancer"}!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )
                Text(
                    text = "Your daily hub for under-18 freelancer collaborations.",
                    fontSize = 12.sp,
                    color = Color(0xFF49454F)
                )
            }
        }

        // --- Row 1 of Bento Grid ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(intrinsicSize = IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero: Project of the Week (Span 4 of 6)
            Card(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight()
                    .clickable { onNavigateToTab("leaderboard") },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "PROJECT OF THE WEEK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "EcoTrack AI",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D)
                        )
                        Text(
                            text = "By @Arjun_Dev",
                            fontSize = 12.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Color.White.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "+ 420 Rep",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Text(text = "🏆", fontSize = 24.sp)
                    }
                }
            }

            // Streak Counter (Span 2 of 6)
            Card(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .clickable { 
                        viewModel.triggerDailyStreakProgress()
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "🔥", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${currentProfile?.streak ?: 14}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1D1B20)
                    )
                    Text(
                        text = "Days Streak",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // --- Row 2 of Bento Grid ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(intrinsicSize = IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Active Gigs: Marketplace (Span 3 of 6)
            Card(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
                    .clickable { onNavigateToTab("marketplace") },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top Gigs",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = "View all",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val previewGigs = gigs.take(2)
                        if (previewGigs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFEF7FF), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No gigs available", fontSize = 11.sp, color = Color(0xFF49454F))
                            }
                        } else {
                            previewGigs.forEach { gig ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFEF7FF), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFE8DEF8), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = gig.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1D1B20),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "₹${gig.budget.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1D192B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Forum Thread Snippets (Span 3 of 6)
            Card(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
                    .clickable { onNavigateToTab("forum") },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD0BCFF).copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Forum Threads",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val previewThreads = threads.take(2)
                        if (previewThreads.isEmpty()) {
                            Text(
                                text = "No recent threads",
                                fontSize = 11.sp,
                                color = Color(0xFF49454F),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        } else {
                            previewThreads.forEachIndexed { index, thread ->
                                val activeAlpha = if (index == 0) 1f else 0.6f
                                Box(
                                    modifier = Modifier
                                        .drawBehind {
                                            drawLine(
                                                color = Color(0xFF6750A4).copy(alpha = activeAlpha),
                                                start = Offset(0f, 0f),
                                                end = Offset(0f, size.height),
                                                strokeWidth = 3.dp.toPx()
                                            )
                                        }
                                        .padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
                                ) {
                                    Text(
                                        text = "\"${thread.title}\"",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1D1B20).copy(alpha = activeAlpha),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Row 3 of Bento Grid ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(intrinsicSize = IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Co-Founder Search (Span 4 of 6)
            Card(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Co-Founder Search",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Color(0xFFEADDFF)
                        ) {
                            Text(
                                text = "SaaS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF21005D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = posts.firstOrNull()?.description ?: "Need a React/Node dev for a fintech app for teens. Equity based.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onNavigateToTab("matchmaking") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Connect Now",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF381E72)
                        )
                    }
                }
            }

            // Reviews / Score level (Span 2 of 6)
            Card(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .clickable { onNavigateToTab("profile") },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD8E4)),
                border = BorderStroke(1.dp, Color(0xFFF9DEDC)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val points = currentProfile?.score ?: 0
                    val level = (points / 100) + 1
                    Text(
                        text = "Level $level",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF31111D)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "⭐️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "4.9 (24)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF31111D)
                    )
                }
            }
        }

        // Live alert/payment proof (Dynamic Simulator matching the active alert box in HTML)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToTab("marketplace") },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF313033)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "✅", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "New Payment Proof!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFEF7FF)
                        )
                        Text(
                            text = "Rahul uploaded ₹500 screenshot.",
                            fontSize = 11.sp,
                            color = Color(0xFFF4EFF4)
                        )
                    }
                }
                Text(
                    text = "VIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFD0BCFF),
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
