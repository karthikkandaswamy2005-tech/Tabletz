package com.example.ui.nurse

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.RobotStatusEntity
import com.example.data.entity.UserEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.HospitalRouteVisualizer
import com.example.ui.components.OrderStatusTimeline
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.MedEmeraldTertiary
import com.example.ui.theme.MedTealPrimary

@Composable
fun MyRequestsTab(
  currentUser: UserEntity,
  orders: List<OrderEntity>,
  orderItems: List<OrderItemEntity>,
  robot: RobotStatusEntity?,
  onOpenRfidDialog: (OrderEntity) -> Unit
) {
  var selectedFilterTab by remember { mutableIntStateOf(0) }
  val filterTabs = listOf("All Requests", "In Delivery", "Delivered")
  var searchQuery by remember { mutableStateOf("") }
  var selectedOrderForDetail by remember { mutableStateOf<OrderEntity?>(null) }

  val filteredOrders = orders.filter { order ->
    val matchesTab = when (selectedFilterTab) {
      1 -> order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED
      2 -> order.status == OrderStatus.DELIVERED
      else -> true
    }
    val matchesSearch = order.orderId.contains(searchQuery, ignoreCase = true) ||
      order.patientName.contains(searchQuery, ignoreCase = true) ||
      order.ward.contains(searchQuery, ignoreCase = true) ||
      order.bed.contains(searchQuery, ignoreCase = true)

    matchesTab && matchesSearch
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("my_requests_screen")
  ) {
    Text(
      text = "Medicine Delivery Requests",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Ward: ${currentUser.ward} • Staff: ${currentUser.name}",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search by Order ID, Patient, Bed...", fontSize = 12.sp) },
      leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Filter Tabs
    TabRow(
      selectedTabIndex = selectedFilterTab,
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
      filterTabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedFilterTab == index,
          onClick = { selectedFilterTab = index },
          text = { Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    if (filteredOrders.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "No medicine requests found",
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      LazyColumn(modifier = Modifier.weight(1f)) {
        items(filteredOrders) { order ->
          NurseOrderItemCard(
            order = order,
            onClick = { selectedOrderForDetail = order },
            onRfidClick = { onOpenRfidDialog(order) }
          )
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }

  // Order Details Modal Dialog with Full Timeline & Items
  if (selectedOrderForDetail != null) {
    val order = selectedOrderForDetail!!
    val itemsForThisOrder = orderItems.filter { it.orderId == order.orderId }

    Dialog(onDismissRequest = { selectedOrderForDetail = null }) {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
          .fillMaxWidth()
          .height(640.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = order.orderId,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MedTealPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                PriorityBadge(priority = order.priority)
              }
              Text(
                text = "Patient: ${order.patientName} (${order.ward} - Bed ${order.bed})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            IconButton(onClick = { selectedOrderForDetail = null }) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          LazyColumn(modifier = Modifier.weight(1f)) {
            item {
              // Current Status Badge Card
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = "Current Status:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                StatusBadge(status = order.status)
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Medicine Items Breakdown
              Text(text = "Requested Medicines:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(6.dp))

              if (itemsForThisOrder.isEmpty()) {
                Text(text = "Standard Prescription Package", fontSize = 12.sp, color = Color.Gray)
              } else {
                itemsForThisOrder.forEach { item ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(text = "• ${item.medicineName} (${item.dosage}) × ${item.quantity}", fontSize = 12.sp)
                    Text(text = "₹${item.subtotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  }
                }
              }

              Spacer(modifier = Modifier.height(8.dp))
              HorizontalDivider(color = Color(0xFFE2E8F0))
              Spacer(modifier = Modifier.height(6.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = "Total Amount:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "₹${order.totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MedEmeraldTertiary)
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Delivery Status Timeline
              OrderStatusTimeline(order = order)

              // If waiting for RFID
              if (order.status == OrderStatus.DESTINATION_REACHED) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                  onClick = {
                    selectedOrderForDetail = null
                    onOpenRfidDialog(order)
                  },
                  modifier = Modifier.fillMaxWidth(),
                  colors = ButtonDefaults.buttonColors(containerColor = MedEmeraldTertiary),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(imageVector = Icons.Default.Sensors, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Tap & Verify RFID (RC522)", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }
  }
}
