package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderPriority
import com.example.data.model.OrderStatus
import com.example.data.model.RobotState
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityLowBg
import com.example.ui.theme.PriorityNormal
import com.example.ui.theme.PriorityNormalBg
import com.example.ui.theme.PriorityUrgent
import com.example.ui.theme.PriorityUrgentBg
import com.example.ui.theme.StatusAccepted
import com.example.ui.theme.StatusAcceptedBg
import com.example.ui.theme.StatusArrived
import com.example.ui.theme.StatusArrivedBg
import com.example.ui.theme.StatusDelivered
import com.example.ui.theme.StatusDeliveredBg
import com.example.ui.theme.StatusEnRoute
import com.example.ui.theme.StatusEnRouteBg
import com.example.ui.theme.StatusLoaded
import com.example.ui.theme.StatusLoadedBg
import com.example.ui.theme.StatusObstacle
import com.example.ui.theme.StatusObstacleBg
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusPendingBg

@Composable
fun StatusBadge(
  status: OrderStatus,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, dotColor) = when (status) {
    OrderStatus.REQUEST_RECEIVED -> Triple(StatusPendingBg, StatusPending, StatusPending)
    OrderStatus.PHARMACIST_ACCEPTED -> Triple(StatusAcceptedBg, StatusAccepted, StatusAccepted)
    OrderStatus.MEDICINE_LOADED -> Triple(StatusLoadedBg, StatusLoaded, StatusLoaded)
    OrderStatus.ROBOT_DISPATCHED, OrderStatus.EN_ROUTE -> Triple(StatusEnRouteBg, StatusEnRoute, StatusEnRoute)
    OrderStatus.DESTINATION_REACHED -> Triple(StatusArrivedBg, StatusArrived, StatusArrived)
    OrderStatus.RFID_VERIFIED, OrderStatus.DELIVERED -> Triple(StatusDeliveredBg, StatusDelivered, StatusDelivered)
    OrderStatus.CANCELLED -> Triple(StatusObstacleBg, StatusObstacle, StatusObstacle)
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(bgColor)
      .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(dotColor)
      )
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = status.displayName,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
fun PriorityBadge(
  priority: OrderPriority,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor) = when (priority) {
    OrderPriority.LOW -> Pair(PriorityLowBg, PriorityLow)
    OrderPriority.NORMAL -> Pair(PriorityNormalBg, PriorityNormal)
    OrderPriority.URGENT -> Pair(PriorityUrgentBg, PriorityUrgent)
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(bgColor)
      .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = if (priority == OrderPriority.URGENT) "⚡ URGENT" else priority.name,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
fun RobotStateBadge(
  state: RobotState,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor) = when (state) {
    RobotState.IDLE -> Pair(Color(0xFFEDE7F6), Color(0xFF5E35B1))
    RobotState.MEDICINE_LOADED -> Pair(StatusLoadedBg, StatusLoaded)
    RobotState.DISPATCHED, RobotState.EN_ROUTE -> Pair(StatusEnRouteBg, StatusEnRoute)
    RobotState.OBSTACLE_DETECTED, RobotState.ERROR -> Pair(StatusObstacleBg, StatusObstacle)
    RobotState.WAITING, RobotState.RFID_VERIFICATION_REQUIRED -> Pair(StatusPendingBg, StatusPending)
    RobotState.DESTINATION_REACHED -> Pair(StatusArrivedBg, StatusArrived)
    RobotState.DELIVERED -> Pair(StatusDeliveredBg, StatusDelivered)
    RobotState.RETURNING -> Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(bgColor)
      .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = state.displayName,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold
    )
  }
}
