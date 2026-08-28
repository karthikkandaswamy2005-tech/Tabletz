package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.RobotStatusEntity
import com.example.data.entity.UserEntity
import com.example.data.model.RobotState
import com.example.data.model.UserRole
import com.example.ui.theme.MedEmeraldTertiary
import com.example.ui.theme.MedNavySecondary
import com.example.ui.theme.MedTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalTopAppBar(
  currentUser: UserEntity?,
  robot: RobotStatusEntity?,
  unreadNotificationCount: Int,
  onOpenNotifications: () -> Unit,
  onOpenHardwareGuide: () -> Unit,
  onOpenDemoSheet: () -> Unit,
  onSwitchRole: (UserRole) -> Unit,
  onLogout: () -> Unit
) {
  var showUserMenu by remember { mutableStateOf(false) }

  Surface(
    color = MedNavySecondary,
    shadowElevation = 4.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Hospital Brand & User
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { showUserMenu = true }
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MedTealPrimary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.LocalHospital,
            contentDescription = "MedBot",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "MedBot Hospital",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (currentUser?.role == UserRole.NURSE) Color(0xFF0284C7) else Color(0xFF7C3AED))
                .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
              Text(
                text = currentUser?.role?.name ?: "STAFF",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }

          Text(
            text = "${currentUser?.name ?: "Staff"} • ${currentUser?.ward ?: "Hospital"}",
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
          )
        }
      }

      // Action Buttons
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Robot live indicator chip
        if (robot != null) {
          val isEnRoute = robot.currentStatus == RobotState.EN_ROUTE || robot.currentStatus == RobotState.DISPATCHED
          val hasObstacle = robot.hasObstacle

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(
                if (hasObstacle) Color(0xFFD32F2F)
                else if (isEnRoute) MedEmeraldTertiary
                else Color(0xFF1E293B)
              )
              .clickable { onOpenDemoSheet() }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (hasObstacle) "R01 Blocked" else if (isEnRoute) "R01 Moving" else "R01 Idle",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
          Spacer(modifier = Modifier.width(4.dp))
        }

        // Demo Mode Controller Button
        IconButton(
          onClick = onOpenDemoSheet,
          modifier = Modifier.testTag("demo_mode_button")
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Demo Simulation",
            tint = Color(0xFF38BDF8)
          )
        }

        // Hardware / ESP32 Specs button
        IconButton(
          onClick = onOpenHardwareGuide,
          modifier = Modifier.testTag("hardware_guide_button")
        ) {
          Icon(
            imageVector = Icons.Default.DeveloperBoard,
            contentDescription = "ESP32 Hardware Guide",
            tint = Color(0xFF34D399)
          )
        }

        // Notifications
        IconButton(
          onClick = onOpenNotifications,
          modifier = Modifier.testTag("notifications_button")
        ) {
          BadgedBox(
            badge = {
              if (unreadNotificationCount > 0) {
                Badge(containerColor = Color(0xFFEF4444)) {
                  Text("$unreadNotificationCount", color = Color.White, fontSize = 9.sp)
                }
              }
            }
          ) {
            Icon(
              imageVector = Icons.Default.Notifications,
              contentDescription = "Notifications",
              tint = Color.White
            )
          }
        }

        // Dropdown menu for role switch and logout
        DropdownMenu(
          expanded = showUserMenu,
          onDismissRequest = { showUserMenu = false }
        ) {
          DropdownMenuItem(
            text = {
              Column {
                Text(currentUser?.name ?: "User", fontWeight = FontWeight.Bold)
                Text("ID: ${currentUser?.userId ?: "N/A"} • ${currentUser?.ward}", fontSize = 12.sp, color = Color.Gray)
              }
            },
            onClick = { showUserMenu = false }
          )

          DropdownMenuItem(
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  if (currentUser?.role == UserRole.NURSE) "Switch to Pharmacist View"
                  else "Switch to Nurse View"
                )
              }
            },
            onClick = {
              showUserMenu = false
              onSwitchRole(if (currentUser?.role == UserRole.NURSE) UserRole.PHARMACIST else UserRole.NURSE)
            }
          )

          DropdownMenuItem(
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = Color(0xFFEF4444))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", color = Color(0xFFEF4444))
              }
            },
            onClick = {
              showUserMenu = false
              onLogout()
            }
          )
        }
      }
    }
  }
}
