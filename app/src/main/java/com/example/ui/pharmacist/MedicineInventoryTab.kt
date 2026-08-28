package com.example.ui.pharmacist

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.window.Dialog
import com.example.data.entity.MedicineEntity
import com.example.ui.theme.MedEmeraldTertiary
import com.example.ui.theme.MedNavySecondary
import com.example.ui.theme.MedTealPrimary

@Composable
fun MedicineInventoryTab(
  medicines: List<MedicineEntity>,
  onUpdateMedicine: (MedicineEntity) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var editingMedicine by remember { mutableStateOf<MedicineEntity?>(null) }
  var showAddDialog by remember { mutableStateOf(false) }

  val filteredMedicines = medicines.filter {
    it.name.contains(searchQuery, ignoreCase = true) ||
    it.dosage.contains(searchQuery, ignoreCase = true) ||
    it.category.contains(searchQuery, ignoreCase = true)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("medicine_inventory_screen")
  ) {
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
            .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = Icons.Default.Inventory, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Pharmacy Medicine Catalog",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Manage unit prices, stock levels & dosages",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Button(
        onClick = { showAddDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
        shape = RoundedCornerShape(8.dp)
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "+ Add", fontSize = 12.sp)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search medicine name, category, dosage...", fontSize = 12.sp) },
      leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(modifier = Modifier.weight(1f)) {
      items(filteredMedicines) { med ->
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = med.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                text = "Dosage: ${med.dosage} • Category: ${med.category}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "In Stock: ${med.stockQuantity} ${med.form}s",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (med.stockQuantity > 50) MedEmeraldTertiary else Color(0xFFEF4444)
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "₹${med.unitPrice.toInt()}",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = MedTealPrimary
                )
                Text(
                  text = "per ${med.form}",
                  fontSize = 10.sp,
                  color = Color.Gray
                )
              }

              IconButton(onClick = { editingMedicine = med }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MedNavySecondary, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
    }
  }

  // Edit / Add Medicine Dialog
  if (editingMedicine != null || showAddDialog) {
    val isNew = showAddDialog
    var name by remember { mutableStateOf(editingMedicine?.name ?: "") }
    var dosage by remember { mutableStateOf(editingMedicine?.dosage ?: "") }
    var priceText by remember { mutableStateOf(editingMedicine?.unitPrice?.toInt()?.toString() ?: "10") }
    var stockText by remember { mutableStateOf(editingMedicine?.stockQuantity?.toString() ?: "100") }
    var form by remember { mutableStateOf(editingMedicine?.form ?: "tablet") }
    var category by remember { mutableStateOf(editingMedicine?.category ?: "General") }

    Dialog(onDismissRequest = {
      editingMedicine = null
      showAddDialog = false
    }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isNew) "Add New Hospital Medicine" else "Edit Medicine Details",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {
              editingMedicine = null
              showAddDialog = false
            }) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Medicine Name (e.g. Paracetamol)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = dosage,
            onValueChange = { dosage = it },
            label = { Text("Dosage / Strength (e.g. 500 mg)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = priceText,
              onValueChange = { priceText = it },
              label = { Text("Unit Price (₹)") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
            OutlinedTextField(
              value = stockText,
              onValueChange = { stockText = it },
              label = { Text("Stock Qty") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category (e.g. Antibiotic, Analgesic)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              val price = priceText.toDoubleOrNull() ?: 10.0
              val stock = stockText.toIntOrNull() ?: 100
              val entity = if (isNew) {
                MedicineEntity(
                  medicineId = "MED_${System.currentTimeMillis() % 10000}",
                  name = name,
                  dosage = dosage,
                  unitPrice = price,
                  stockQuantity = stock,
                  form = form,
                  category = category
                )
              } else {
                editingMedicine!!.copy(
                  name = name,
                  dosage = dosage,
                  unitPrice = price,
                  stockQuantity = stock,
                  form = form,
                  category = category
                )
              }
              onUpdateMedicine(entity)
              editingMedicine = null
              showAddDialog = false
            },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(text = if (isNew) "Add to Hospital Catalog" else "Save Changes", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
