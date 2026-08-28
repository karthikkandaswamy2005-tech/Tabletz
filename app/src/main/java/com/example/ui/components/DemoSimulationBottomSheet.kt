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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.ui.theme.MedEmeraldTertiary
import com.example.ui.theme.MedTealPrimary
import com.example.ui.theme.StatusObstacle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoSimulationBottomSheet(
  robot: RobotStatusEntity?,
  activeOrders: List<OrderEntity>,
  isAutoSimulating: Boolean,
  onDismiss: () -> Unit,
  onStartFullDemo: () -> Unit,
  onStopFullDemo: () -> Unit,
  onAdvanceCheckpoint: (String) -> Unit,
  onTriggerObstacle: () -> Unit,
  onClearObstacle: () -> Unit,
  onDestinationReached: () -> Unit,
  onOpenRfidDialog: (OrderEntity) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("demo_simulation_sheet")
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      item {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0F2FE)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "⚡ College Project Demo & Simulation Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Simulate hardware & robot telemetry when ESP32 is offline",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Auto-run card
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(
            containerColor = if (isAutoSimulating) Color(0xFFE0F2FE) else Color(0xFFF1F5F9)
          ),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "End-to-End Automated CUJ Demo",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
                Text(
                  text = "Runs complete cycle: Request ➔ Load ➔ Route ➔ Obstacle ➔ RFID ➔ Deliver",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isAutoSimulating) {
              Button(
                onClick = onStartFullDemo,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("start_auto_demo_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Start Full Autonomous Delivery Demo")
              }
            } else {
              Button(
                onClick = onStopFullDemo,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = StatusObstacle),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Stop Auto Simulation")
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step-by-Step Manual Simulation Triggers
        Text(
          text = "Manual Telemetry & Event Triggers",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Trigger specific embedded sensor events on Robot ${robot?.robotId ?: "R01"}:",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Checkpoint navigation
        Text(
          text = "1. Hospital Corridor Checkpoints (Line-Tracking):",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = { onAdvanceCheckpoint("CHECKPOINT_C1") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Reach C1", fontSize = 11.sp)
          }
          OutlinedButton(
            onClick = { onAdvanceCheckpoint("CHECKPOINT_C2") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Reach C2", fontSize = 11.sp)
          }
          OutlinedButton(
            onClick = { onAdvanceCheckpoint("CHECKPOINT_C3") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Reach C3", fontSize = 11.sp)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Obstacle Triggers
        Text(
          text = "2. Ultrasonic Sensor (HC-SR04) Obstacle Simulation:",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onTriggerObstacle,
            colors = ButtonDefaults.buttonColors(containerColor = StatusObstacle),
            modifier = Modifier
              .weight(1f)
              .testTag("trigger_obstacle_button"),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Trigger Obstacle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = onClearObstacle,
            colors = ButtonDefaults.buttonColors(containerColor = MedEmeraldTertiary),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Clear Obstacle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Destination & RFID Triggers
        Text(
          text = "3. Arrival & RFID Access (RC522):",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onDestinationReached,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Arrive at Ward", fontSize = 11.sp)
          }

          val currentOrder = activeOrders.firstOrNull { it.status != com.example.data.model.OrderStatus.DELIVERED }
          Button(
            onClick = {
              if (currentOrder != null) {
                onOpenRfidDialog(currentOrder)
              }
            },
            enabled = currentOrder != null,
            colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(imageVector = Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Scan RFID Tap", fontSize = 11.sp)
          }
        }

        Spacer(modifier = Modifier.height(30.dp))
      }
    }
  }
}
