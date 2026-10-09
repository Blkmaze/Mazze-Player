package com.example.ui.screens.settings

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.XtreamProfileEntity
import com.example.ui.theme.MaZzeAccentAmber
import com.example.ui.theme.MaZzeDarkBackground
import com.example.ui.theme.MaZzeError
import com.example.ui.theme.MaZzePrimary
import com.example.ui.theme.MaZzeSecondary
import com.example.ui.theme.MaZzeSuccess
import com.example.ui.theme.MaZzeSurfaceBorder
import com.example.ui.theme.MaZzeSurfaceDark
import com.example.ui.theme.MaZzeSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InternalSettingsScreen(
    activeProfile: XtreamProfileEntity?,
    allProfiles: List<XtreamProfileEntity>,
    isTesting: Boolean,
    testResult: String?,
    statusNotice: String?,
    onSaveProfile: (id: Long, profileName: String, serverUrl: String, username: String, password: String, streamFormat: String) -> Unit,
    onTestConnection: (serverUrl: String, username: String, password: String) -> Unit,
    onSwitchProfile: (profileId: Long) -> Unit,
    onDeleteProfile: (XtreamProfileEntity) -> Unit,
    onClearStatusNotice: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Form fields state
    var selectedProfileId by remember { mutableStateOf(activeProfile?.id ?: 0L) }
    var profileName by remember { mutableStateOf(activeProfile?.profileName ?: "MaZze Server") }
    var serverUrl by remember { mutableStateOf(activeProfile?.serverUrl ?: "") }
    var username by remember { mutableStateOf(activeProfile?.username ?: "") }
    var password by remember { mutableStateOf(activeProfile?.password ?: "") }
    var streamFormat by remember { mutableStateOf(activeProfile?.streamFormat ?: "m3u8") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Sync when activeProfile changes
    LaunchedEffect(activeProfile) {
        if (activeProfile != null) {
            selectedProfileId = activeProfile.id
            profileName = activeProfile.profileName
            serverUrl = activeProfile.serverUrl
            username = activeProfile.username
            password = activeProfile.password
            streamFormat = activeProfile.streamFormat
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaZzeDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Header
        item {
            Column {
                Text(
                    text = "Internal Configuration Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure your personal Xtream Codes API login credentials and server parameters below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Notification banner if saved or updated
        if (statusNotice != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaZzeSuccess.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaZzeSuccess
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = statusNotice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaZzeSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Section 2: Credential Configuration Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaZzeSurfaceBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Xtream Codes API Credentials",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaZzeSecondary
                        )

                        if (selectedProfileId != 0L) {
                            Text(
                                text = "Editing Profile #$selectedProfileId",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    // Profile Name / Alias
                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        label = { Text("Profile Label / Alias") },
                        placeholder = { Text("e.g. Home Server, VIP Stream") },
                        leadingIcon = {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = MaZzePrimary)
                        },
                        singleLine = true,
                        colors = outlinedTextFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_profile_name")
                    )

                    // Server URL Input
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Server Host / URL") },
                        placeholder = { Text("http://domain.com:8080") },
                        supportingText = {
                            Text("Include protocol and port, e.g. http://my-iptv-server.com:8080")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = MaZzeSecondary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        colors = outlinedTextFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_server_url")
                    )

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        placeholder = { Text("Enter Xtream username") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaZzePrimary)
                        },
                        singleLine = true,
                        colors = outlinedTextFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_username")
                    )

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("Enter Xtream password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaZzePrimary)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.testTag("toggle_password_visibility")
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = outlinedTextFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_password")
                    )

                    // Stream Format Selector
                    Column {
                        Text(
                            text = "Stream Playback Format",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = streamFormat == "m3u8",
                                onClick = { streamFormat = "m3u8" },
                                label = { Text("HLS (.m3u8)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaZzePrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("format_m3u8")
                            )
                            FilterChip(
                                selected = streamFormat == "ts",
                                onClick = { streamFormat = "ts" },
                                label = { Text("MPEG-TS (.ts)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaZzePrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("format_ts")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Test Button
                        OutlinedButton(
                            onClick = {
                                onClearStatusNotice()
                                onTestConnection(serverUrl, username, password)
                            },
                            enabled = !isTesting && serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_connection_button")
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaZzeSecondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testing...", color = MaZzeSecondary)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaZzeSecondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Ping", color = MaZzeSecondary)
                            }
                        }

                        // Save Button
                        Button(
                            onClick = {
                                onSaveProfile(
                                    selectedProfileId,
                                    profileName,
                                    serverUrl,
                                    username,
                                    password,
                                    streamFormat
                                )
                            },
                            enabled = serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaZzePrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_configuration_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Config", color = Color.White)
                        }
                    }
                }
            }
        }

        // Section 3: Connection Test Diagnostics Output (if run)
        if (testResult != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_result_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (testResult.startsWith("SUCCESS")) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (testResult.startsWith("SUCCESS")) MaZzeSuccess else MaZzeError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Server Ping & Handshake Diagnostics",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (testResult.startsWith("SUCCESS")) MaZzeSuccess else MaZzeError
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = testResult,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Section 4: Saved Xtream Profiles List
        item {
            Text(
                text = "Saved Xtream Server Profiles (${allProfiles.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (allProfiles.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "No saved profiles yet. Fill in your Xtream Codes server URL, username, and password above and tap 'Save Config'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(allProfiles) { profile ->
                SavedProfileItem(
                    profile = profile,
                    isActive = profile.isActive,
                    onSelect = {
                        selectedProfileId = profile.id
                        profileName = profile.profileName
                        serverUrl = profile.serverUrl
                        username = profile.username
                        password = profile.password
                        streamFormat = profile.streamFormat
                        onSwitchProfile(profile.id)
                    },
                    onDelete = { onDeleteProfile(profile) }
                )
            }
        }

        // Section 5: Xtream Codes Info & Architecture Guide
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaZzeAccentAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How MaZze Connects to Xtream Codes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaZzeAccentAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• player_api.php: MaZze authenticates against your Xtream server's standard API endpoints.\n" +
                                "• Stream URL: Built automatically as {server}/live/{user}/{pass}/{stream_id}.m3u8\n" +
                                "• Cleartext HTTP: Enabled in Android Manifest so both HTTP and HTTPS IPTV servers stream seamlessly without security blocking.\n" +
                                "• Offline/Demo fallback: If your server is offline or unreachable, MaZze provides test channels so the video player is always operational.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SavedProfileItem(
    profile: XtreamProfileEntity,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaZzeSurfaceElevated else MaZzeSurfaceDark
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isActive) MaZzeSuccess else Color.Gray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.profileName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaZzeSuccess.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaZzeSuccess,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${profile.serverUrl} • ${profile.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 1
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_profile_${profile.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete profile",
                    tint = TextMuted
                )
            }
        }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaZzePrimary,
    unfocusedBorderColor = MaZzeSurfaceBorder,
    focusedLabelColor = MaZzePrimary,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = MaZzeSurfaceElevated,
    unfocusedContainerColor = MaZzeSurfaceElevated
)
