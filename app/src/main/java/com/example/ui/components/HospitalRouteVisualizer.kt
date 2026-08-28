package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.RobotStatusEntity
import com.example.data.model.RobotState
import com.example.ui.theme.MedEmeraldTertiary
import com.example.ui.theme.MedNavySecondary
import com.example.ui.theme.MedTealPrimary

data class RouteNode(
  val id: String,
  val label: String,
  val sublabel: String,
  val isCheckpoint: Boolean = false
)

@Composable
fun HospitalRouteVisualizer(
  robot: RobotStatusEntity,
  destinationWard: String = "Ward 3",
  destinationBed: String = "B-12",
  modifier: Modifier = Modifier
) {
  val targetWard = if (robot.destinationWard.isNotEmpty()) robot.destinationWard else destinationWard
  val targetBed = if (robot.destinationBed.isNotEmpty()) "Bed ${robot.destinationBed}" else destinationBed

  val nodes = listOf(
    RouteNode("PHARMACY", "Pharmacy Base", "Central Dispensary"),
    RouteNode("CHECKPOINT_C1", "Checkpoint C1", "Corridor North", isCheckpoint = true),
    RouteNode("CHECKPOINT_C2", "Checkpoint C2", "Elevator Hallway", isCheckpoint = true),
    RouteNode("CHECKPOINT_C3", "Checkpoint C3", "Ward Wing Jct", isCheckpoint = true),
    RouteNode(targetWard, targetWard, targetBed)
  )

  val activeNodeIndex = when {
    robot.currentStatus == RobotState.IDLE -> 0
    robot.currentCheckpoint.contains("C1", ignoreCase = true) -> 1
    robot.currentCheckpoint.contains("C2", ignoreCase = true) -> 2
    robot.currentCheckpoint.contains("C3", ignoreCase = true) -> 3
    robot.currentCheckpoint.contains("WARD", ignoreCase = true) || robot.currentStatus == RobotState.DESTINATION_REACHED || robot.currentStatus == RobotState.DELIVERED -> 4
    robot.currentStatus == RobotState.MEDICINE_LOADED -> 0
    robot.currentStatus == RobotState.DISPATCHED -> 0
    else -> 1
  }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.35f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = null,
            tint = MedTealPrimary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Predefined Hospital Corridor Route",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (robot.hasObstacle) Color(0xFFFFEBEE) else MedTealPrimary.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = if (robot.hasObstacle) "⚠ Path Blocked" else "Line-Tracking Active",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (robot.hasObstacle) Color(0xFFD32F2F) else MedTealPrimary
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Route Progress Bar / Diagram
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        nodes.forEachIndexed { index, node ->
          val isCurrent = activeNodeIndex == index
          val isPassed = activeNodeIndex > index

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.size(40.dp)
            ) {
              if (isCurrent) {
                // Pulsing ring
                Box(
                  modifier = Modifier
                    .size((34 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(
                      if (robot.hasObstacle) Color(0xFFD32F2F).copy(alpha = 0.25f)
                      else MedTealPrimary.copy(alpha = 0.25f)
                    )
                )
              }

              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(
                    when {
                      isCurrent && robot.hasObstacle -> Color(0xFFD32F2F)
                      isCurrent -> MedTealPrimary
                      isPassed -> MedEmeraldTertiary
                      else -> Color(0xFFCBD5E1)
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                when {
                  isCurrent && robot.hasObstacle -> {
                    Icon(
                      imageVector = Icons.Default.Warning,
                      contentDescription = "Blocked",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  index == 0 -> {
                    Icon(
                      imageVector = Icons.Default.Home,
                      contentDescription = "Base",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  index == nodes.size - 1 -> {
                    Icon(
                      imageVector = Icons.Default.LocalHospital,
                      contentDescription = "Dest",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  else -> {
                    Text(
                      text = "C$index",
                      color = Color.White,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = if (node.isCheckpoint) "C$index" else if (index == 0) "Pharmacy" else targetWard,
              fontSize = 11.sp,
              fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
              color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
              maxLines = 1
            )
            Text(
              text = node.sublabel,
              fontSize = 9.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Current Route Status Summary Footer
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
          .padding(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "CURRENT LOCATION",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 0.5.sp
            )
            Text(
              text = when (activeNodeIndex) {
                0 -> "Pharmacy Central Bay"
                1 -> "Checkpoint C1 (North Corridor)"
                2 -> "Checkpoint C2 (Hallway & Lift)"
                3 -> "Checkpoint C3 (Wing Junction)"
                else -> "$targetWard ($targetBed)"
              },
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "ULTRASONIC SENSOR",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 0.5.sp
            )
            Text(
              text = "${robot.ultrasonicDistanceCm.toInt()} cm front buffer",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (robot.ultrasonicDistanceCm < 25f) Color(0xFFD32F2F) else MedEmeraldTertiary
            )
          }
        }
      }
    }
  }
}
