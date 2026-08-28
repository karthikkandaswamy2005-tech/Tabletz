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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.entity.MedicineEntity
import com.example.data.entity.PatientEntity
import com.example.data.entity.UserEntity
import com.example.data.model.OrderPriority
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.MedEmeraldTertiary
import com.example.ui.theme.MedNavySecondary
import com.example.ui.theme.MedTealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SelectedMedicineItem(
  val medicine: MedicineEntity,
  var quantity: Int
)

@Composable
fun NewMedicineRequestTab(
  currentUser: UserEntity,
  patients: List<PatientEntity>,
  medicines: List<MedicineEntity>,
  onSubmitRequest: (patient: PatientEntity, items: List<Pair<MedicineEntity, Int>>, priority: OrderPriority) -> Unit,
  onCancel: () -> Unit
) {

  // Selected Patient
  var selectedPatient by remember {
    mutableStateOf<PatientEntity?>(patients.firstOrNull { it.ward == currentUser.ward } ?: patients.firstOrNull())
  }
  var patientSearchQuery by remember { mutableStateOf("") }
  var showPatientDropdown by remember { mutableStateOf(false) }

  // Medicine Search & Selection
  var medicineSearchQuery by remember { mutableStateOf("") }
  val selectedItems = remember { mutableStateListOf<SelectedMedicineItem>() }

  // Priority
  var priority by remember { mutableStateOf(OrderPriority.NORMAL) }

  // Confirmation step dialog
  var showConfirmationDialog by remember { mutableStateOf(false) }

  // Success dialog
  var createdOrderId by remember { mutableStateOf<String?>(null) }

  val totalBill = selectedItems.sumOf { it.medicine.unitPrice * it.quantity }

  val filteredMedicines = medicines.filter {
    it.name.contains(medicineSearchQuery, ignoreCase = true) ||
    it.dosage.contains(medicineSearchQuery, ignoreCase = true) ||
    it.category.contains(medicineSearchQuery, ignoreCase = true)
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("new_medicine_request_screen")
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onCancel) {
          Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
          Text(
            text = "Create Medicine Request",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Autonomous Robot Delivery Dispatch Order",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 1. Patient Information Section
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MedTealPrimary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "1. Patient Information",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }

            TextButton(onClick = { showPatientDropdown = !showPatientDropdown }) {
              Text(if (showPatientDropdown) "Hide List" else "Select Patient", fontSize = 12.sp)
            }
          }

          if (selectedPatient != null) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(10.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = selectedPatient!!.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "ID: ${selectedPatient!!.patientId}",
                    fontSize = 12.sp,
                    color = MedTealPrimary,
                    fontWeight = FontWeight.SemiBold
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                  Text(
                    text = "Ward: ${selectedPatient!!.ward}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "Bed: ${selectedPatient!!.bed}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "Age: ${selectedPatient!!.age} yrs",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }

          if (showPatientDropdown) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
              value = patientSearchQuery,
              onValueChange = { patientSearchQuery = it },
              placeholder = { Text("Search hospital patients...", fontSize = 12.sp) },
              leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )
            Spacer(modifier = Modifier.height(6.dp))

            patients.filter {
              it.name.contains(patientSearchQuery, ignoreCase = true) ||
              it.patientId.contains(patientSearchQuery, ignoreCase = true) ||
              it.ward.contains(patientSearchQuery, ignoreCase = true)
            }.forEach { patient ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .clickable {
                    selectedPatient = patient
                    showPatientDropdown = false
                  }
                  .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(text = patient.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                  Text(text = "${patient.patientId} • ${patient.ward} (Bed ${patient.bed})", fontSize = 11.sp, color = Color.Gray)
                }
                if (selectedPatient?.patientId == patient.patientId) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MedEmeraldTertiary, modifier = Modifier.size(18.dp))
                }
              }
              HorizontalDivider(color = Color(0xFFF1F5F9))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Request Priority Section
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "2. Select Request Priority",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          Text(
            text = "Urgent requests appear prioritized at top of pharmacist queue",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(OrderPriority.LOW, OrderPriority.NORMAL, OrderPriority.URGENT).forEach { prio ->
              val isSelected = priority == prio
              val (color, bg) = when (prio) {
                OrderPriority.LOW -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
                OrderPriority.NORMAL -> Color(0xFF1565C0) to Color(0xFFE3F2FD)
                OrderPriority.URGENT -> Color(0xFFC62828) to Color(0xFFFFEBEE)
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) bg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                  .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) color else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                  )
                  .clickable { priority = prio }
                  .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (prio == OrderPriority.URGENT) "⚡ URGENT" else prio.name,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 12.sp,
                  color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Medicine Selection Catalog
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "3. Search & Add Medicines",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = medicineSearchQuery,
            onValueChange = { medicineSearchQuery = it },
            placeholder = { Text("Search Paracetamol, Amoxicillin, Insulin, ORS...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("medicine_search_input"),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Medicine Quick Catalog list
          Text(
            text = "Common Hospital Medicines Available (${filteredMedicines.size}):",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(6.dp))

          Column {
            filteredMedicines.take(6).forEach { med ->
              val alreadyAdded = selectedItems.any { it.medicine.medicineId == med.medicineId }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (alreadyAdded) MedTealPrimary.copy(alpha = 0.08f) else Color.Transparent)
                  .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = med.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                  Text(
                    text = "${med.dosage} • ${med.category} • ₹${med.unitPrice.toInt()}/${med.form}",
                    fontSize = 11.sp,
                    color = MedTealPrimary,
                    fontWeight = FontWeight.Medium
                  )
                }

                Button(
                  onClick = {
                    val existing = selectedItems.find { it.medicine.medicineId == med.medicineId }
                    if (existing != null) {
                      existing.quantity += 1
                      // Trigger state recomposition
                      val idx = selectedItems.indexOf(existing)
                      selectedItems[idx] = existing.copy(quantity = existing.quantity)
                    } else {
                      selectedItems.add(SelectedMedicineItem(med, 1))
                    }
                  },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (alreadyAdded) MedEmeraldTertiary else MedTealPrimary
                  ),
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = if (alreadyAdded) "+ Add More" else "+ Add", fontSize = 11.sp)
                }
              }
              HorizontalDivider(color = Color(0xFFF1F5F9))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 4. Selected Medicines & Bill Breakdown Table
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("selected_medicines_bill_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "4. Order Items & Bill Calculation",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              text = "${selectedItems.size} items added",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          if (selectedItems.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(16.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No medicines selected. Add from list above.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          } else {
            // Table Header
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 8.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "Medicine", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.45f))
              Text(text = "Qty", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.25f))
              Text(text = "Unit", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.15f))
              Text(text = "Subtotal", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.15f))
            }

            selectedItems.forEachIndexed { index, item ->
              val subtotal = item.medicine.unitPrice * item.quantity

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(0.45f)) {
                  Text(text = item.medicine.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  Text(text = item.medicine.dosage, fontSize = 10.sp, color = Color.Gray)
                }

                // Quantity Stepper
                Row(
                  modifier = Modifier.weight(0.25f),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  IconButton(
                    onClick = {
                      if (item.quantity > 1) {
                        item.quantity -= 1
                        selectedItems[index] = item.copy(quantity = item.quantity)
                      } else {
                        selectedItems.removeAt(index)
                      }
                    },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                  }

                  Text(
                    text = "${item.quantity}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                  )

                  IconButton(
                    onClick = {
                      item.quantity += 1
                      selectedItems[index] = item.copy(quantity = item.quantity)
                    },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                  }
                }

                Text(
                  text = "₹${item.medicine.unitPrice.toInt()}",
                  fontSize = 11.sp,
                  modifier = Modifier.weight(0.15f)
                )

                Text(
                  text = "₹${subtotal.toInt()}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MedTealPrimary,
                  modifier = Modifier.weight(0.15f)
                )
              }
              HorizontalDivider(color = Color(0xFFF1F5F9))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total Bill Calculation Row
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F8F0))
                .padding(12.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "TOTAL MEDICINE BILL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF003823),
                    letterSpacing = 0.5.sp
                  )
                  Text(
                    text = "Sum of all medicine subtotals",
                    fontSize = 10.sp,
                    color = Color(0xFF2E7D32)
                  )
                }

                Text(
                  text = "₹${totalBill.toInt()}",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = MedEmeraldTertiary
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Proceed to Confirmation Button
      Button(
        onClick = {
          if (selectedPatient != null && selectedItems.isNotEmpty()) {
            showConfirmationDialog = true
          }
        },
        enabled = selectedPatient != null && selectedItems.isNotEmpty(),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("review_and_submit_request_button"),
        colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(
          text = "Review & Submit Request (Total: ₹${totalBill.toInt()})",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // 5. Order Confirmation Dialog
  if (showConfirmationDialog && selectedPatient != null) {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val currentTime = sdf.format(Date())

    Dialog(onDismissRequest = { showConfirmationDialog = false }) {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("order_confirmation_dialog")
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Confirm Medicine Request",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showConfirmationDialog = false }) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Summary details
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
              .padding(12.dp)
          ) {
            Column {
              Text(
                text = "PATIENT DETAILS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MedTealPrimary,
                letterSpacing = 0.5.sp
              )
              Text(
                text = "${selectedPatient!!.name} (${selectedPatient!!.patientId})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Ward: ${selectedPatient!!.ward} • Bed: ${selectedPatient!!.bed}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Nurse: ${currentUser.name} (${currentUser.userId})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Date & Time: $currentTime",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Priority indicator
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Priority Level:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            PriorityBadge(priority = priority)
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Itemized list
          Text(text = "Medicines (${selectedItems.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))

          selectedItems.forEach { item ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "• ${item.medicine.name} (Qty: ${item.quantity} × ₹${item.medicine.unitPrice.toInt()})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "₹${(item.medicine.unitPrice * item.quantity).toInt()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = Color(0xFFE2E8F0))
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "Total Bill Amount:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
              text = "₹${totalBill.toInt()}",
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MedEmeraldTertiary
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Confirm Submit button
          Button(
            onClick = {
              showConfirmationDialog = false
              val formattedItems = selectedItems.map { it.medicine to it.quantity }
              onSubmitRequest(
                selectedPatient!!,
                formattedItems,
                priority
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("confirm_submit_medicine_request_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(text = "Submit Medicine Request", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }

        }
      }
    }
  }
}
