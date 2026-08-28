package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.data.entity.RobotStatusEntity
import com.example.ui.theme.StatusObstacle
import com.example.ui.theme.StatusObstacleBg

@Composable
fun ObstacleAlertBanner(
  robot: RobotStatusEntity?,
  onClearObstacle: () -> Unit,
  modifier: Modifier = Modifier
) {
  val hasObstacle = robot?.hasObstacle == true

  AnimatedVisibility(
    visible = hasObstacle,
    enter = fadeIn() + expandVertically(),
    exit = fadeOut() + shrinkVertically(),
    modifier = modifier
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(StatusObstacleBg)
        .border(1.5.dp, StatusObstacle, RoundedCornerShape(12.dp))
        .padding(14.dp)
        .testTag("obstacle_alert_banner")
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = "Obstacle Alert",
              tint = StatusObstacle,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "⚠ OBSTACLE DETECTED",
              color = StatusObstacle,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              letterSpacing = 0.5.sp
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(StatusObstacle)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "ROBOT HALTED",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = if (!robot?.obstacleMessage.isNullOrEmpty()) robot?.obstacleMessage ?: ""
                 else "Robot ${robot?.robotId} detected an obstacle while travelling to ${robot?.destinationWard}. Safe collision avoidance protocol activated.",
          color = Color(0xFF5C1313),
          fontSize = 13.sp,
          lineHeight = 18.sp,
          fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Robot: ${robot?.robotId} • Order: ${if (robot?.currentOrderId.isNullOrEmpty()) "N/A" else robot?.currentOrderId}",
              fontSize = 11.sp,
              color = Color(0xFF7F1D1D),
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Location: ${robot?.currentCheckpoint} • Sensor Buffer: ${robot?.ultrasonicDistanceCm?.toInt()} cm",
              fontSize = 11.sp,
              color = Color(0xFF7F1D1D)
            )
          }

          ElevatedButton(
            onClick = onClearObstacle,
            colors = ButtonDefaults.elevatedButtonColors(
              containerColor = StatusObstacle,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("clear_obstacle_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Clear & Resume", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
