package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.OrderPriority
import com.example.data.model.OrderStatus
import com.example.data.model.RobotState
import com.example.data.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val userId: String,
  val name: String,
  val username: String,
  val passwordHash: String,
  val role: UserRole,
  val ward: String,
  val staffBadgeId: String = "STAFF-RFID-8829"
)

@Entity(tableName = "patients")
data class PatientEntity(
  @PrimaryKey val patientId: String,
  val name: String,
  val ward: String,
  val bed: String,
  val age: Int,
  val admissionDiagnosis: String = "General Care",
  val admissionDate: String = "2026-08-20",
  val gender: String = "Male",
  val diagnosis: String = "General Care"
)

@Entity(tableName = "medicines")
data class MedicineEntity(
  @PrimaryKey val medicineId: String,
  val name: String,
  val dosage: String,
  val form: String, // tablet, capsule, packet, vial, syrup
  val unitPrice: Double,
  val stockQuantity: Int,
  val category: String,
  val inStock: Boolean = true
)

@Entity(tableName = "orders")
data class OrderEntity(
  @PrimaryKey val orderId: String,
  val patientId: String,
  val patientName: String,
  val ward: String,
  val bed: String,
  val nurseId: String,
  val nurseName: String,
  val priority: OrderPriority,
  val totalAmount: Double,
  val requestTime: Long,
  val requestTimeString: String,
  val status: OrderStatus,
  val assignedRobotId: String = "R01",
  val currentCheckpoint: String = "PHARMACY",
  val obstacleReported: Boolean = false,
  val obstacleDetail: String = "",
  val rfidTagVerified: String = "",
  val completionTimeString: String = ""
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val orderId: String,
  val medicineId: String,
  val medicineName: String,
  val dosage: String,
  val quantity: Int,
  val unitPrice: Double,
  val subtotal: Double
)

@Entity(tableName = "robot_status")
data class RobotStatusEntity(
  @PrimaryKey val robotId: String,
  val robotName: String,
  val currentStatus: RobotState,
  val currentOrderId: String = "",
  val destinationWard: String = "",
  val destinationBed: String = "",
  val currentCheckpoint: String = "PHARMACY",
  val batteryPercent: Int = 94,
  val batteryLevelPercent: Int = 94,
  val motorSpeedPwm: Int = 180,
  val hasObstacle: Boolean = false,
  val obstacleMessage: String = "",
  val ultrasonicDistanceCm: Float = 120.0f,
  val rfidReaderActive: Boolean = true,
  val isHardwareConnected: Boolean = false,
  val lastPingTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "delivery_logs")
data class DeliveryLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val orderId: String,
  val robotId: String,
  val eventType: String,
  val message: String,
  val eventDetail: String = "",
  val checkpoint: String,
  val timestamp: Long = System.currentTimeMillis(),
  val formattedTime: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val recipientRole: String, // "ALL", "NURSE", "PHARMACIST", or specific ward
  val title: String,
  val message: String,
  val type: String, // REQUEST, ACCEPTED, LOADED, DISPATCH, OBSTACLE, RFID, DELIVERED, ERROR
  val orderId: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val formattedTime: String,
  val isRead: Boolean = false
)
