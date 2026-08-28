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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PatientEntity
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.MedEmeraldTertiary
import com.example.ui.theme.MedNavySecondary
import com.example.ui.theme.MedTealPrimary

@Composable
fun PatientHistoryTab(
  patients: List<PatientEntity>,
  orders: List<OrderEntity>,
  orderItems: List<OrderItemEntity>
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedPatient by remember { mutableStateOf<PatientEntity?>(null) }

  val filteredPatients = patients.filter {
    it.name.contains(searchQuery, ignoreCase = true) ||
    it.patientId.contains(searchQuery, ignoreCase = true) ||
    it.ward.contains(searchQuery, ignoreCase = true)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("patient_history_screen")
  ) {
    if (selectedPatient == null) {
      // Patient List View
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.ReceiptLong,
          contentDescription = null,
          tint = MedTealPrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "Patient Medicine & Billing History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Select a patient to view historical deliveries & cumulative pharmacy bills",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search patient name, ID, ward...", fontSize = 12.sp) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(10.dp))

      LazyColumn(modifier = Modifier.weight(1f)) {
        items(filteredPatients) { patient ->
          val patientOrders = orders.filter { it.patientId == patient.patientId }
          val cumulativeBill = patientOrders.sumOf { it.totalAmount }

          ElevatedCard(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedPatient = patient }
              .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MedTealPrimary.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MedTealPrimary,
                    modifier = Modifier.size(22.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = patient.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  Text(
                    text = "${patient.patientId} • ${patient.ward} (Bed ${patient.bed})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "${patientOrders.size} orders on record",
                    fontSize = 11.sp,
                    color = MedTealPrimary
                  )
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "₹${cumulativeBill.toInt()}",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 15.sp,
                  color = MedEmeraldTertiary
                )
                Text(
                  text = "Total Bill",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    } else {
      // Patient Selected Detail View
      val patient = selectedPatient!!
      val patientOrders = orders.filter { it.patientId == patient.patientId }
      val cumulativeBill = patientOrders.sumOf { it.totalAmount }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = { selectedPatient = null }) {
          Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
          Text(
            text = "${patient.name} - Billing History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Patient ID: ${patient.patientId} • ${patient.ward} (Bed ${patient.bed})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Patient Summary Card
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "CUMULATIVE PHARMACY BILL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MedTealPrimary,
                letterSpacing = 0.5.sp
              )
              Text(
                text = "Total for all ${patientOrders.size} prescriptions delivered",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              text = "₹${cumulativeBill.toInt()}",
              fontSize = 24.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MedEmeraldTertiary
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          HorizontalDivider(color = Color(0xFFF1F5F9))
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "Age: ${patient.age} yrs • Gender: ${patient.gender}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Condition: ${patient.diagnosis}", fontSize = 11.sp, color = MedNavySecondary, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Individual Order Breakdown (${patientOrders.size})",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(8.dp))

      LazyColumn(modifier = Modifier.weight(1f)) {
        if (patientOrders.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(text = "No orders found for this patient", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        } else {
          items(patientOrders) { order ->
            val itemsForOrder = orderItems.filter { it.orderId == order.orderId }

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = order.orderId,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      color = MedTealPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    PriorityBadge(priority = order.priority)
                  }
                  StatusBadge(status = order.status)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Ordered: ${order.requestTimeString} • Nurse: ${order.nurseName}",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (order.completionTimeString.isNotEmpty()) {
                  Text(
                    text = "Delivered: ${order.completionTimeString} • Robot: ${order.assignedRobotId}",
                    fontSize = 11.sp,
                    color = MedEmeraldTertiary,
                    fontWeight = FontWeight.SemiBold
                  )
                }

                if (order.rfidTagVerified.isNotEmpty()) {
                  Text(
                    text = "RFID Verified: ${order.rfidTagVerified}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(6.dp))

                // Item breakdown
                itemsForOrder.forEach { itm ->
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(text = "• ${itm.medicineName} (${itm.dosage}) × ${itm.quantity}", fontSize = 11.sp)
                    Text(text = "₹${itm.subtotal.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = "Order Total:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  Text(
                    text = "₹${order.totalAmount.toInt()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MedEmeraldTertiary
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
