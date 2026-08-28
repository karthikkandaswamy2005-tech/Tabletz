package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.entity.OrderEntity
import com.example.ui.theme.MedEmeraldTertiary
import com.example.ui.theme.MedTealPrimary

@Composable
fun RfidVerificationDialog(
  order: OrderEntity?,
  onDismiss: () -> Unit,
  onVerifyRfid: (orderId: String, rfidTag: String, (Boolean) -> Unit) -> Unit
) {
  if (order == null) return

  var rfidInput by remember { mutableStateOf("STAFF-RFID-8829") }
  var isScanning by remember { mutableStateOf(false) }
  var scanResult by remember { mutableStateOf<Boolean?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = { if (!isScanning) onDismiss() }) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("rfid_verification_dialog")
    ) {
      Column(modifier = Modifier.padding(24.dp)) {
        // Header
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
                imageVector = Icons.Default.Sensors,
                contentDescription = "RC522 RFID",
                tint = MedTealPrimary,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "RC522 RFID Verification",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Order: ${order.orderId}",
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

        // Info Banner
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(12.dp)
        ) {
          Column {
            Text(
              text = "DESTINATION REACHED",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = MedEmeraldTertiary,
              letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Patient: ${order.patientName} (${order.ward}, Bed ${order.bed})",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "«Please verify the authorized staff RFID card to receive the medicine.»",
              fontSize = 12.sp,
              fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scanner Visual Box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
              when (scanResult) {
                true -> Color(0xFFE8F8F0)
                false -> Color(0xFFFFEBEE)
                null -> Color(0xFFF1F5F9)
              }
            )
            .border(
              1.dp,
              when (scanResult) {
                true -> MedEmeraldTertiary
                false -> Color(0xFFD32F2F)
                null -> Color(0xFFCBD5E1)
              },
              RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isScanning) {
              CircularProgressIndicator(
                color = MedTealPrimary,
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Waiting for RC522 RFID Card...",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MedTealPrimary
              )
            } else if (scanResult == true) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = MedEmeraldTertiary,
                modifier = Modifier.size(40.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "RFID VERIFIED ✓",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MedEmeraldTertiary
              )
              Text(
                text = "MEDICINE DELIVERED ✓ Cargo bay unlocked.",
                fontSize = 12.sp,
                color = Color(0xFF003823),
                fontWeight = FontWeight.Medium
              )
            } else if (scanResult == false) {
              Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Failed",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(40.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "RFID VERIFICATION FAILED",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
              )
              Text(
                text = errorMessage ?: "Unauthorized card. Medicine remains locked.",
                fontSize = 12.sp,
                color = Color(0xFF7F1D1D)
              )
            } else {
              Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = "Card",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Waiting for RFID Verification…",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // RFID Badge ID Field
        OutlinedTextField(
          value = rfidInput,
          onValueChange = { rfidInput = it },
          label = { Text("Staff Badge UID / RFID Code") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("rfid_input_field"),
          singleLine = true,
          enabled = !isScanning && scanResult != true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Simulated Badges
        Text(
          text = "Quick Select Staff Card (Demo):",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = { rfidInput = "STAFF-RFID-8829" },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(text = "Sarah (RN)", fontSize = 11.sp)
          }

          OutlinedButton(
            onClick = { rfidInput = "STAFF-RFID-4412" },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(text = "David (RN)", fontSize = 11.sp)
          }

          OutlinedButton(
            onClick = { rfidInput = "UNAUTHORIZED-CARD-999" },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(text = "Invalid Card", fontSize = 11.sp, color = Color(0xFFD32F2F))
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action Button
        Button(
          onClick = {
            isScanning = true
            scanResult = null
            errorMessage = null

            onVerifyRfid(order.orderId, rfidInput) { success ->
              isScanning = false
              scanResult = success
              if (!success) {
                errorMessage = "Card '$rfidInput' not authorized for Ward 3"
              }
            }
          },
          enabled = !isScanning && rfidInput.isNotBlank() && scanResult != true,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("verify_rfid_button"),
          colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(imageVector = Icons.Default.Sensors, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Simulate / Receive RFID Tap", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
