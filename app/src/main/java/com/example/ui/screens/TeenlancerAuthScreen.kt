package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.TeenlancerViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeenlancerAuthScreen(
    viewModel: TeenlancerViewModel,
    allProfiles: List<UserProfile>
) {
    var isRegisterTab by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Login Form State
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Signup Form State
    var signupName by remember { mutableStateOf("") }
    var signupUsername by remember { mutableStateOf("") }
    var signupLocation by remember { mutableStateOf("Jaipur, India") }
    var signupPassword by remember { mutableStateOf("") }
    var signupBio by remember { mutableStateOf("") }
    var signupUpi by remember { mutableStateOf("") }
    var signupTargetGoal by remember { mutableStateOf("5") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo Icon Glow Header
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(CyberCyan.copy(alpha = 0.4f), Color.Transparent),
                        radius = 120f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Global Connect",
                tint = CyberCyan,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "TEENLANCER HUB GLOBAL",
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            color = CyberCyan,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp
        )

        Text(
            text = "U18 International Micro-Gig, Collaboration & Mutual Growth Platform",
            color = TextSecondaryLight,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Custom High-Contrast Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurface, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { isRegisterTab = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isRegisterTab) CyberIndigo else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("tab_login")
            ) {
                Text(
                    text = "Log In",
                    fontWeight = FontWeight.Bold,
                    color = if (!isRegisterTab) Color.White else TextSecondaryLight,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = { isRegisterTab = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRegisterTab) CyberIndigo else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("tab_signup")
            ) {
                Text(
                    text = "Create Profile",
                    fontWeight = FontWeight.Bold,
                    color = if (isRegisterTab) Color.White else TextSecondaryLight,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main Credentials Form Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, CyberIndigo.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                if (!isRegisterTab) {
                    // --- LOGIN VIEW ---
                    Text(
                        text = "Sign In Credentials",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = loginUsername,
                        onValueChange = { loginUsername = it },
                        label = { Text("Username Handle", color = CyberCyan) },
                        prefix = { Text("@", color = CyberIndigo) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateSurfaceVariant,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondaryLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_username_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Workspace PIN / Password", color = CyberCyan) },
                        visualTransformation = PasswordVisualTransformation(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateSurfaceVariant,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondaryLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.loginUser(loginUsername, loginPassword)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_login_submit_btn")
                    ) {
                        Text(
                            text = "Authenticate Account Session",
                            fontWeight = FontWeight.Bold,
                            color = SlateDark,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Demo Accounts Helper Row
                    Text(
                        text = "Or quick-test with seeded profiles:",
                        color = TextSecondaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    allProfiles.forEach { demo ->
                        val locationHint = when (demo.id) {
                            1 -> "Jaipur, India"
                            2 -> "London, UK"
                            3 -> "Boston, US"
                            else -> demo.location
                        }
                        ElevatedCard(
                            onClick = {
                                loginUsername = demo.username
                                loginPassword = when (demo.id) {
                                    1 -> "aarav123"
                                    2 -> "riya123"
                                    3 -> "ishaan123"
                                    else -> "1234"
                                }
                            },
                            colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberIndigo),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = demo.name.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${demo.name} (@${demo.username})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "📍 $locationHint • ${demo.rank}",
                                        fontSize = 10.sp,
                                        color = TextSecondaryLight
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Autofill",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                } else {
                    // --- REGISTER VIEW ---
                    Text(
                        text = "Global Profile Enrollment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = signupName,
                        onValueChange = { signupName = it },
                        label = { Text("Display Full Name", color = CyberCyan) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateSurfaceVariant,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondaryLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_signup_name")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = signupUsername,
                        onValueChange = { signupUsername = it },
                        label = { Text("Unique Handle Username", color = CyberCyan) },
                        prefix = { Text("@", color = CyberIndigo) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateSurfaceVariant,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondaryLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_signup_username")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = signupLocation,
                        onValueChange = { signupLocation = it },
                        label = { Text("Global Hub Location (City, Country)", color = CyberCyan) },
                        placeholder = { Text("e.g. Jaipur, India / London, UK / New York, US", color = TextSecondaryLight.copy(alpha = 0.5f)) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateSurfaceVariant,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondaryLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_signup_location")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = signupPassword,
                        onValueChange = { signupPassword = it },
                        label = { Text("Choose PIN / Password", color = CyberCyan) },
                        visualTransformation = PasswordVisualTransformation(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateSurfaceVariant,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondaryLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_signup_password")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = signupUpi,
                        onValueChange = { signupUpi = it },
                        label = { Text("FamPay UPI ID (Optional)", color = CyberCyan) },
                        placeholder = { Text("e.g. handle@fam", color = TextSecondaryLight.copy(alpha = 0.5f)) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateSurfaceVariant,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondaryLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_signup_upi")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = signupTargetGoal,
                        onValueChange = { signupTargetGoal = it },
                        label = { Text("Target Goal Gigs Count", color = CyberCyan) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateSurfaceVariant,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondaryLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_signup_target")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = signupBio,
                        onValueChange = { signupBio = it },
                        label = { Text("Skills / Bio", color = CyberCyan) },
                        placeholder = { Text("Tell us your specialized editing, design, or coding micro-skills...", color = TextSecondaryLight.copy(alpha = 0.5f)) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateSurfaceVariant,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondaryLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .testTag("auth_signup_bio")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val goal = signupTargetGoal.toIntOrNull() ?: 5
                            coroutineScope.launch {
                                val success = viewModel.registerUser(
                                    username = signupUsername,
                                    name = signupName,
                                    bio = signupBio,
                                    location = signupLocation,
                                    pass = signupPassword,
                                    fampayUpi = if (signupUpi.isBlank()) null else signupUpi,
                                    targetGoal = goal
                                )
                                if (success) {
                                    // Reset fields
                                    signupName = ""
                                    signupUsername = ""
                                    signupPassword = ""
                                    signupBio = ""
                                    signupUpi = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                        shape = RoundedCornerShape(8.dp),
                        enabled = signupName.isNotBlank() && signupUsername.isNotBlank() && signupPassword.isNotBlank() && signupLocation.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_signup_submit_btn")
                    ) {
                        Text(
                            text = "Register Global Creator Account",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
