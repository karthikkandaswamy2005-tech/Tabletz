package com.example.ui.pharmacist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.entity.OrderEntity
import com.example.data.entity.RobotStatusEntity
import com.example.data.model.OrderStatus
import com.example.data.model.RobotState
import com.example.ui.components.HospitalRouteVisualizer
import com.example.ui.components.ObstacleAlertBanner
import com.example.ui.components.RobotStateBadge
import com.example.ui.theme.MedEmeraldTertiary
import com.example.ui.theme.MedNavySecondary
import com.example.ui.theme.MedTealPrimary
import com.example.ui.theme.StatusObstacle

@Composable
fun RobotMonitoringTab(
  robot: RobotStatusEntity?,
  activeOrders: List<OrderEntity>,
  onTriggerObstacle: () -> Unit,
  onClearObstacle: () -> Unit,
  onAdvanceCheckpoint: (String) -> Unit,
  onReturnToBase: () -> Unit
) {
  val currentOrder = activeOrders.firstOrNull { it.orderId == robot?.currentOrderId }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("robot_monitoring_screen")
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(MedTealPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.SmartToy,
              contentDescription = null,
              tint = MedTealPrimary,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Live Robot Telemetry Monitoring",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Autonomous Medicine Delivery Robot (${robot?.robotId ?: "R01"})",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        if (robot != null) {
          RobotStateBadge(state = robot.currentStatus)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Obstacle Alert Banner
      ObstacleAlertBanner(
        robot = robot,
        onClearObstacle = onClearObstacle
      )

      if (robot?.hasObstacle == true) {
        Spacer(modifier = Modifier.height(14.dp))
      }

      // Robot Key Specs Grid
      if (robot != null) {
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Live Hardware & Sensor Telemetry",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // Battery
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = if (robot.batteryLevelPercent > 30) MedEmeraldTertiary else Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "BATTERY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                Text(
                  text = "${robot.batteryLevelPercent}%",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (robot.batteryLevelPercent > 30) MedEmeraldTertiary else Color(0xFFEF4444)
                )
                Text(text = "12V 4.2Ah Li-Ion", fontSize = 10.sp, color = Color.Gray)
              }

              // Ultrasonic distance
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = if (robot.ultrasonicDistanceCm < 25) Color(0xFFD32F2F) else MedTealPrimary,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "ULTRASONIC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                Text(
                  text = "${robot.ultrasonicDistanceCm.toInt()} cm",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (robot.ultrasonicDistanceCm < 25) Color(0xFFD32F2F) else MedTealPrimary
                )
                Text(text = "HC-SR04 Front Buffer", fontSize = 10.sp, color = Color.Gray)
              }

              // Motor speed PWM
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MedNavySecondary,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "SPEED PWM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                Text(
                  text = "${robot.motorSpeedPwm}",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = MedNavySecondary
                )
                Text(text = "L298N Motor Driver", fontSize = 10.sp, color = Color.Gray)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Assigned Payload Info
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(text = "ASSIGNED PAYLOAD / ORDER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(
                  text = if (robot.currentOrderId.isNotEmpty()) "Order: ${robot.currentOrderId}" else "No active order assigned",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold
                )
                if (currentOrder != null) {
                  Text(
                    text = "Patient: ${currentOrder.patientName} (${currentOrder.patientId})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(text = "TARGET DESTINATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(
                  text = if (robot.destinationWard.isNotEmpty()) "${robot.destinationWard} (Bed ${robot.destinationBed})" else "Base Station",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MedTealPrimary
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hospital Route Visualizer
        Text(
          text = "Autonomous Corridor Navigation Map",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        HospitalRouteVisualizer(
          robot = robot,
          destinationWard = robot.destinationWard.ifEmpty { "Ward 3" },
          destinationBed = robot.destinationBed.ifEmpty { "Bed B-12" }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Telemetry Simulation & Manual Override Controls
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Robot Control & Checkpoint Advancement",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              text = "Manually move robot between optical line checkpoints:",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = { onAdvanceCheckpoint("CHECKPOINT_C1") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Pass C1", fontSize = 11.sp)
              }

              OutlinedButton(
                onClick = { onAdvanceCheckpoint("CHECKPOINT_C2") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Pass C2", fontSize = 11.sp)
              }

              OutlinedButton(
                onClick = { onAdvanceCheckpoint("CHECKPOINT_C3") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Pass C3", fontSize = 11.sp)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = onTriggerObstacle,
                colors = ButtonDefaults.buttonColors(containerColor = StatusObstacle),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Simulate Obstacle", fontSize = 11.sp)
              }

              Button(
                onClick = onReturnToBase,
                colors = ButtonDefaults.buttonColors(containerColor = MedNavySecondary),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Return to Base", fontSize = 11.sp)
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
