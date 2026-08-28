package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.theme.MedEmeraldTertiary
import com.example.ui.theme.MedTealPrimary

data class TimelineStep(
  val status: OrderStatus,
  val title: String,
  val description: String
)

@Composable
fun OrderStatusTimeline(
  order: OrderEntity,
  modifier: Modifier = Modifier
) {
  val steps = listOf(
    TimelineStep(
      OrderStatus.REQUEST_RECEIVED,
      "Request Submitted",
      "Created by ${order.nurseName} (${order.requestTimeString})"
    ),
    TimelineStep(
      OrderStatus.PHARMACIST_ACCEPTED,
      "Pharmacist Accepted",
      "Order reviewed and queued for dispensing in Central Pharmacy"
    ),
    TimelineStep(
      OrderStatus.MEDICINE_LOADED,
      "Medicine Loaded",
      "Medicines secured inside ${order.assignedRobotId} cargo bay"
    ),
    TimelineStep(
      OrderStatus.ROBOT_DISPATCHED,
      "Robot Dispatched",
      "Autonomous robot departed base towards ${order.ward}"
    ),
    TimelineStep(
      OrderStatus.EN_ROUTE,
      "En Route",
      if (order.obstacleReported) "⚠ Paused: ${order.obstacleDetail}" else "Navigating hospital corridor via line tracking (Current: ${order.currentCheckpoint})"
    ),
    TimelineStep(
      OrderStatus.DESTINATION_REACHED,
      "Destination Reached",
      "Arrived at ${order.ward} - Bed ${order.bed}. Waiting for nurse RFID scan"
    ),
    TimelineStep(
      OrderStatus.RFID_VERIFIED,
      "RFID Verified ✓",
      if (order.rfidTagVerified.isNotEmpty()) "Staff card verified: ${order.rfidTagVerified}" else "RC522 reader validated authorized staff card"
    ),
    TimelineStep(
      OrderStatus.DELIVERED,
      "Medicine Delivered",
      if (order.completionTimeString.isNotEmpty()) "Completed at ${order.completionTimeString}" else "Delivery confirmed & billing updated"
    )
  )

  val currentStepIndex = when (order.status) {
    OrderStatus.REQUEST_RECEIVED -> 0
    OrderStatus.PHARMACIST_ACCEPTED -> 1
    OrderStatus.MEDICINE_LOADED -> 2
    OrderStatus.ROBOT_DISPATCHED -> 3
    OrderStatus.EN_ROUTE -> 4
    OrderStatus.DESTINATION_REACHED -> 5
    OrderStatus.RFID_VERIFIED -> 6
    OrderStatus.DELIVERED -> 7
    OrderStatus.CANCELLED -> -1
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      .padding(16.dp)
  ) {
    Text(
      text = "Delivery Status Timeline",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(14.dp))

    steps.forEachIndexed { index, step ->
      val isCompleted = currentStepIndex >= index
      val isCurrent = currentStepIndex == index
      val isLast = index == steps.size - 1

      val circleBgColor by animateColorAsState(
        targetValue = when {
          isCurrent && order.obstacleReported -> Color(0xFFD32F2F)
          isCurrent -> MedTealPrimary
          isCompleted -> MedEmeraldTertiary
          else -> Color(0xFFCBD5E1)
        },
        label = "circleColor"
      )

      val lineColor by animateColorAsState(
        targetValue = if (currentStepIndex > index) MedEmeraldTertiary else Color(0xFFE2E8F0),
        label = "lineColor"
      )

      Row(modifier = Modifier.fillMaxWidth()) {
        // Vertical step track
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.width(28.dp)
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(circleBgColor),
            contentAlignment = Alignment.Center
          ) {
            if (isCurrent && order.obstacleReported) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
            } else if (isCompleted && !isCurrent) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Done",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
            } else {
              Text(
                text = "${index + 1}",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          if (!isLast) {
            Box(
              modifier = Modifier
                .width(2.5.dp)
                .height(34.dp)
                .background(lineColor)
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Step text
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (!isLast) 12.dp else 0.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = step.title,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = if (isCurrent) FontWeight.Bold else if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
              color = if (isCurrent && order.obstacleReported) Color(0xFFD32F2F) else if (isCurrent) MedTealPrimary else if (isCompleted) MaterialTheme.colorScheme.onSurface else Color.Gray
            )

            if (isCurrent) {
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(if (order.obstacleReported) Color(0xFFFFEBEE) else MedTealPrimary.copy(alpha = 0.15f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = if (order.obstacleReported) "ATTENTION" else "ACTIVE STAGE",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (order.obstacleReported) Color(0xFFD32F2F) else MedTealPrimary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = step.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}
