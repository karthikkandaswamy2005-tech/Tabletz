package com.example.ui.nurse

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.NotificationEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.RobotStatusEntity
import com.example.data.entity.UserEntity
import com.example.data.model.OrderStatus
import com.example.data.model.RobotState
import com.example.ui.components.HospitalRouteVisualizer
import com.example.ui.components.ObstacleAlertBanner
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.MedEmeraldTertiary
import com.example.ui.theme.MedNavySecondary
import com.example.ui.theme.MedTealPrimary
import com.example.ui.theme.StatusObstacle

@Composable
fun NurseDashboardTab(
  currentUser: UserEntity,
  orders: List<OrderEntity>,
  robot: RobotStatusEntity?,
  notifications: List<NotificationEntity>,
  onNavigateNewRequest: () -> Unit,
  onSelectOrder: (OrderEntity) -> Unit,
  onOpenRfidDialog: (OrderEntity) -> Unit,
  onClearObstacle: () -> Unit
) {
  val myOrders = orders.filter { it.nurseId == currentUser.userId || it.ward == currentUser.ward }
  val pendingCount = myOrders.count { it.status == OrderStatus.REQUEST_RECEIVED || it.status == OrderStatus.PHARMACIST_ACCEPTED }
  val inProgressCount = myOrders.count {
    it.status == OrderStatus.MEDICINE_LOADED || it.status == OrderStatus.ROBOT_DISPATCHED ||
    it.status == OrderStatus.EN_ROUTE || it.status == OrderStatus.DESTINATION_REACHED
  }
  val deliveredTodayCount = myOrders.count { it.status == OrderStatus.DELIVERED }
  val unreadNotificationsCount = notifications.count { !it.isRead }

  val activeDeliveryOrder = myOrders.firstOrNull {
    it.status == OrderStatus.EN_ROUTE || it.status == OrderStatus.ROBOT_DISPATCHED ||
    it.status == OrderStatus.DESTINATION_REACHED || it.status == OrderStatus.MEDICINE_LOADED
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    item {
      // Nurse Identity Card
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MedTealPrimary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MedTealPrimary,
                modifier = Modifier.size(28.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = currentUser.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Staff ID: ${currentUser.userId} • RFID: ${currentUser.staffBadgeId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Assigned Ward: ${currentUser.ward}",
                style = MaterialTheme.typography.bodySmall,
                color = MedTealPrimary,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          // Online Status indicator
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFE8F8F0))
              .border(1.dp, MedEmeraldTertiary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(MedEmeraldTertiary)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Online",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MedEmeraldTertiary
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Primary Prominent Action: New Medicine Request Button
      Button(
        onClick = onNavigateNewRequest,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("new_medicine_request_button"),
        colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "+ New Medicine Request",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Summary Cards Grid
      Text(
        text = "Ward Medicine Activity",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        NurseStatCard(
          title = "My Pending",
          count = "$pendingCount",
          subtitle = "Awaiting pharmacy",
          icon = Icons.Default.HourglassTop,
          iconTint = Color(0xFFE67E22),
          modifier = Modifier.weight(1f)
        )
        NurseStatCard(
          title = "In Progress",
          count = "$inProgressCount",
          subtitle = "Active deliveries",
          icon = Icons.Default.Speed,
          iconTint = MedTealPrimary,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        NurseStatCard(
          title = "Delivered Today",
          count = "$deliveredTodayCount",
          subtitle = "Completed orders",
          icon = Icons.Default.CheckCircle,
          iconTint = MedEmeraldTertiary,
          modifier = Modifier.weight(1f)
        )
        NurseStatCard(
          title = "Notifications",
          count = "$unreadNotificationsCount",
          subtitle = "Unread alerts",
          icon = Icons.Default.Notifications,
          iconTint = if (unreadNotificationsCount > 0) Color(0xFFEF4444) else Color(0xFF64748B),
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Obstacle Alert Banner if active
      ObstacleAlertBanner(
        robot = robot,
        onClearObstacle = onClearObstacle
      )

      if (robot?.hasObstacle == true) {
        Spacer(modifier = Modifier.height(14.dp))
      }

      // Active Robot Delivery Card & Route Visualizer
      if (activeDeliveryOrder != null && robot != null) {
        Text(
          text = "Active Robot Delivery Track",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(2.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Order ${activeDeliveryOrder.orderId}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
                Text(
                  text = "For: ${activeDeliveryOrder.patientName} (${activeDeliveryOrder.ward}, Bed ${activeDeliveryOrder.bed})",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              StatusBadge(status = activeDeliveryOrder.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route visualizer component
            HospitalRouteVisualizer(
              robot = robot,
              destinationWard = activeDeliveryOrder.ward,
              destinationBed = activeDeliveryOrder.bed
            )

            // If robot arrived, show prominent RFID Scan button!
            if (activeDeliveryOrder.status == OrderStatus.DESTINATION_REACHED ||
                robot.currentStatus == RobotState.DESTINATION_REACHED ||
                robot.currentStatus == RobotState.RFID_VERIFICATION_REQUIRED) {
              Spacer(modifier = Modifier.height(12.dp))
              Button(
                onClick = { onOpenRfidDialog(activeDeliveryOrder) },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("nurse_scan_rfid_action_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MedEmeraldTertiary),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(imageVector = Icons.Default.Sensors, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Verify Staff RFID Card (RC522)", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(18.dp))
      }

      // Recent Requests List Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "My Recent Medicine Requests",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${myOrders.size} total",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
    }

    if (myOrders.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No medicine requests yet. Tap '+ New Medicine Request' to create one.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      items(myOrders.take(6)) { order ->
        NurseOrderItemCard(
          order = order,
          onClick = { onSelectOrder(order) },
          onRfidClick = { onOpenRfidDialog(order) }
        )
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

@Composable
fun NurseStatCard(
  title: String,
  count: String,
  subtitle: String,
  icon: ImageVector,
  iconTint: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Text(
          text = title,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(iconTint.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = count,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun NurseOrderItemCard(
  order: OrderEntity,
  onClick: () -> Unit,
  onRfidClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("nurse_order_card_${order.orderId}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = order.orderId,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MedTealPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          PriorityBadge(priority = order.priority)
        }
        StatusBadge(status = order.status)
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "${order.patientName} (${order.patientId})",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "${order.ward} • Bed ${order.bed} • Req: ${order.requestTimeString}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Text(
          text = "₹${order.totalAmount.toInt()}",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MedEmeraldTertiary
        )
      }

      if (order.status == OrderStatus.DESTINATION_REACHED) {
        Spacer(modifier = Modifier.height(8.dp))
        Button(
          onClick = onRfidClick,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = MedEmeraldTertiary),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(imageVector = Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Verify RFID to Receive Medicine", fontSize = 12.sp)
        }
      }
    }
  }
}
