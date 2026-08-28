package com.example.ui.pharmacist

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.NotificationEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.RobotStatusEntity
import com.example.data.entity.UserEntity
import com.example.data.model.OrderPriority
import com.example.data.model.OrderStatus
import com.example.data.model.RobotState
import com.example.ui.components.ObstacleAlertBanner
import com.example.ui.components.PriorityBadge
import com.example.ui.components.RobotStateBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.MedEmeraldTertiary
import com.example.ui.theme.MedNavySecondary
import com.example.ui.theme.MedTealPrimary
import com.example.ui.theme.StatusObstacle

@Composable
fun PharmacistDashboardTab(
  currentUser: UserEntity,
  orders: List<OrderEntity>,
  orderItems: List<OrderItemEntity>,
  robot: RobotStatusEntity?,
  notifications: List<NotificationEntity>,
  onAcceptOrder: (String) -> Unit,
  onLoadMedicine: (String, String) -> Unit,
  onDispatchRobot: (String, String) -> Unit,
  onNavigateToRobotMonitoring: () -> Unit,
  onClearObstacle: () -> Unit
) {
  val incomingRequests = orders.filter { it.status == OrderStatus.REQUEST_RECEIVED }
  val inPreparationOrders = orders.filter { it.status == OrderStatus.PHARMACIST_ACCEPTED || it.status == OrderStatus.MEDICINE_LOADED }
  val activeDispatchedOrders = orders.filter {
    it.status == OrderStatus.ROBOT_DISPATCHED || it.status == OrderStatus.EN_ROUTE || it.status == OrderStatus.DESTINATION_REACHED
  }
  val deliveredToday = orders.count { it.status == OrderStatus.DELIVERED }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("pharmacist_dashboard_screen")
  ) {
    item {
      // Pharmacist Info Header Card
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
                .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = null,
                tint = Color(0xFF7C3AED),
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
                text = "Staff ID: ${currentUser.userId} • Central Dispensary",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Pharmacy Dispensing Console",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7C3AED),
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFEDE9FE))
              .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "Dispensary Active",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF6D28D9)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Pharmacist Stat Metrics Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PharmacistMetricCard(
          title = "Incoming Queue",
          count = "${incomingRequests.size}",
          subtitle = "Needs acceptance",
          icon = Icons.Default.HourglassBottom,
          iconTint = Color(0xFFE67E22),
          modifier = Modifier.weight(1f)
        )
        PharmacistMetricCard(
          title = "In Preparation",
          count = "${inPreparationOrders.size}",
          subtitle = "Ready to load/dispatch",
          icon = Icons.Default.Inventory,
          iconTint = MedTealPrimary,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PharmacistMetricCard(
          title = "Active Dispatches",
          count = "${activeDispatchedOrders.size}",
          subtitle = "En route to wards",
          icon = Icons.Default.LocalShipping,
          iconTint = Color(0xFF0284C7),
          modifier = Modifier.weight(1f)
        )
        PharmacistMetricCard(
          title = "Delivered Today",
          count = "$deliveredToday",
          subtitle = "Completed orders",
          icon = Icons.Default.CheckCircle,
          iconTint = MedEmeraldTertiary,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Obstacle Alert Banner if robot encountered obstacle
      ObstacleAlertBanner(
        robot = robot,
        onClearObstacle = onClearObstacle
      )

      if (robot?.hasObstacle == true) {
        Spacer(modifier = Modifier.height(14.dp))
      }

      // Robot Telemetry Quick Status Widget
      if (robot != null) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToRobotMonitoring() },
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(2.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(MedTealPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = "Autonomous Robot ${robot.robotId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Spacer(modifier = Modifier.width(6.dp))
                  RobotStateBadge(state = robot.currentStatus)
                }
                Text(
                  text = "Battery: ${robot.batteryLevelPercent}% • Checkpoint: ${robot.currentCheckpoint} • Speed: ${robot.motorSpeedPwm} PWM",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Text(
              text = "Live Map →",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MedTealPrimary
            )
          }
        }
        Spacer(modifier = Modifier.height(18.dp))
      }

      // Incoming Requests Queue Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Prescription Orders Queue (${orders.size})",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Priority Sorted",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
    }

    if (orders.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "No prescription orders in queue", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    } else {
      items(orders) { order ->
        val itemsForThisOrder = orderItems.filter { it.orderId == order.orderId }
        PharmacistOrderItemCard(
          order = order,
          items = itemsForThisOrder,
          availableRobotId = robot?.robotId ?: "ROBOT_R01",
          onAccept = { onAcceptOrder(order.orderId) },
          onLoad = { onLoadMedicine(order.orderId, robot?.robotId ?: "ROBOT_R01") },
          onDispatch = { onDispatchRobot(order.orderId, robot?.robotId ?: "ROBOT_R01") }
        )
        Spacer(modifier = Modifier.height(10.dp))
      }
    }
  }
}

@Composable
fun PharmacistMetricCard(
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
fun PharmacistOrderItemCard(
  order: OrderEntity,
  items: List<OrderItemEntity>,
  availableRobotId: String,
  onAccept: () -> Unit,
  onLoad: () -> Unit,
  onDispatch: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("pharmacist_order_card_${order.orderId}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Top Row: Order ID, Priority & Status
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
          Spacer(modifier = Modifier.width(6.dp))
          PriorityBadge(priority = order.priority)
        }
        StatusBadge(status = order.status)
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Patient & Nurse info
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Patient: ${order.patientName} (${order.patientId})",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
          Text(
            text = "Destination: ${order.ward} • Bed: ${order.bed}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Requested by: ${order.nurseName} (${order.requestTimeString})",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "₹${order.totalAmount.toInt()}",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MedEmeraldTertiary
          )
          Text(
            text = "${items.size} items",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      HorizontalDivider(color = Color(0xFFF1F5F9))
      Spacer(modifier = Modifier.height(8.dp))

      // Medicines list inside order
      Text(text = "Prescription Items:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
      Spacer(modifier = Modifier.height(4.dp))

      items.forEach { itm ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "• ${itm.medicineName} (${itm.dosage}) × ${itm.quantity}", fontSize = 12.sp)
          Text(text = "₹${itm.subtotal.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MedNavySecondary)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Pharmacist Action Workflow Buttons
      when (order.status) {
        OrderStatus.REQUEST_RECEIVED -> {
          Button(
            onClick = onAccept,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("pharmacist_accept_button_${order.orderId}"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(imageVector = Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "1. Accept & Prepare Prescription", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }

        OrderStatus.PHARMACIST_ACCEPTED -> {
          Button(
            onClick = onLoad,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("pharmacist_load_button_${order.orderId}"),
            colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(imageVector = Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "2. Medicine Loaded into Robot ($availableRobotId)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }

        OrderStatus.MEDICINE_LOADED -> {
          Button(
            onClick = onDispatch,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("pharmacist_dispatch_button_${order.orderId}"),
            colors = ButtonDefaults.buttonColors(containerColor = MedEmeraldTertiary),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "3. Dispatch Robot to ${order.ward}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }

        OrderStatus.ROBOT_DISPATCHED, OrderStatus.EN_ROUTE -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFE0F2FE))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "🤖 Robot En Route: ${order.currentCheckpoint}",
              color = Color(0xFF0369A1),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        OrderStatus.DESTINATION_REACHED -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFFEF3C7))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "📍 Arrived at ${order.ward} - Awaiting RFID Scan",
              color = Color(0xFF92400E),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        OrderStatus.RFID_VERIFIED, OrderStatus.DELIVERED -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFE8F8F0))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "✓ Successfully Delivered at ${order.completionTimeString}",
              color = MedEmeraldTertiary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        OrderStatus.CANCELLED -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFFFEBEE))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "Order Cancelled", color = Color(0xFFD32F2F), fontSize = 12.sp)
          }
        }
      }
    }
  }
}
