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
                                        text = "Global Live",
                                        color = CyberCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        actions = {
                            // Real Identity Holder (No Switcher)
                            activeProfile?.username?.let { username ->
                                Text(
                                    text = "@$username",
                                    color = CyberCyan,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            // Notification Bell
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
                            currentProfile = activeProfile,
                            onNavigateToTab = { currentTab = it }
                        )
                        "marketplace" -> MarketplaceScreen(viewModel, gigs, activeProfile)
                        "forum" -> TechForumScreen(viewModel, threads, activeProfile)
                        "matchmaking" -> TeamMatchmakingScreen(viewModel, posts, activeProfile)
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
    }
}
