package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.update.DownloadState
import com.example.data.update.UpdateInfo
import com.example.ui.theme.MaZzeAccentAmber
import com.example.ui.theme.MaZzeDarkBackground
import com.example.ui.theme.MaZzeError
import com.example.ui.theme.MaZzePrimary
import com.example.ui.theme.MaZzeSuccess
import com.example.ui.theme.MaZzeSurfaceBorder
import com.example.ui.theme.MaZzeSurfaceDark
import com.example.ui.theme.MaZzeSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    downloadState: DownloadState,
    onUpdateClick: () -> Unit,
    onLaterClick: () -> Unit,
    onRetryInstall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onLaterClick,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier
                .padding(24.dp)
                .widthIn(min = 360.dp, max = 560.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, MaZzePrimary.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .testTag("update_dialog"),
            color = MaZzeSurfaceDark,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(MaZzeSurfaceDark)
                    .padding(24.dp)
            ) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaZzePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "Update Icon",
                            tint = MaZzePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Update available: build ${updateInfo.latestBuildNumber}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Installed: build ${updateInfo.currentBuildNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Release info or description card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "A new version of MaZze Player is available to install.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        if (updateInfo.releaseBody.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = updateInfo.releaseBody.trim(),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                maxLines = 4
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic Download / State Section
                when (downloadState) {
                    is DownloadState.Downloading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = MaZzePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Downloading MaZze.apk...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                                val percentText = if (downloadState.percent != null) {
                                    "${downloadState.percent}%"
                                } else {
                                    "..."
                                }
                                Text(
                                    text = percentText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaZzePrimary
                                )
                            }

                            if (downloadState.percent != null) {
                                LinearProgressIndicator(
                                    progress = { downloadState.percent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaZzePrimary,
                                    trackColor = MaZzeSurfaceElevated
                                )
                            } else {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaZzePrimary,
                                    trackColor = MaZzeSurfaceElevated
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    is DownloadState.NeedsPermission -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaZzeAccentAmber.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaZzeAccentAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Permission required: Please allow installing unknown apps for MaZze Player in system settings to proceed with the update.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaZzeAccentAmber
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    is DownloadState.ReadyToInstall -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaZzeSuccess.copy(alpha = 0.15f))
                        ) {
                            Text(
                                text = "Download complete! Tap Install to update MaZze Player.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaZzeSuccess,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    is DownloadState.Failed -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaZzeError.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaZzeError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = downloadState.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaZzeError
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    DownloadState.Idle -> {
                        // Default state
                    }
                }

                // D-Pad Focusable Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Later Button (Always focusable and dismisses)
                    OutlinedButton(
                        onClick = onLaterClick,
                        modifier = Modifier
                            .tvFocusable(shape = RoundedCornerShape(12.dp))
                            .testTag("update_dialog_later_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        )
                    ) {
                        Text(
                            text = "Later",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Primary Action Button (Update / Install / Settings)
                    val isDownloading = downloadState is DownloadState.Downloading
                    val primaryButtonText = when (downloadState) {
                        is DownloadState.Downloading -> "Downloading..."
                        is DownloadState.NeedsPermission -> "Open Settings"
                        is DownloadState.ReadyToInstall -> "Install"
                        is DownloadState.Failed -> "Retry"
                        DownloadState.Idle -> "Update"
                    }

                    Button(
                        onClick = {
                            when (downloadState) {
                                is DownloadState.NeedsPermission,
                                is DownloadState.ReadyToInstall -> onRetryInstall()
                                is DownloadState.Failed -> onUpdateClick()
                                DownloadState.Idle -> onUpdateClick()
                                is DownloadState.Downloading -> Unit
                            }
                        },
                        enabled = !isDownloading,
                        modifier = Modifier
                            .tvFocusable(shape = RoundedCornerShape(12.dp))
                            .testTag("update_dialog_update_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaZzePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = primaryButtonText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
