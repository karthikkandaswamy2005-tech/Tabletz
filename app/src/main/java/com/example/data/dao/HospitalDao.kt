package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.DeliveryLogEntity
import com.example.data.entity.MedicineEntity
import com.example.data.entity.NotificationEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PatientEntity
import com.example.data.entity.RobotStatusEntity
import com.example.data.entity.UserEntity
import com.example.data.model.OrderStatus
import com.example.data.model.RobotState
import kotlinx.coroutines.flow.Flow

data class OrderWithItems(
  val order: OrderEntity,
  val items: List<OrderItemEntity>
)

@Dao
interface HospitalDao {

  // Users
  @Query("SELECT * FROM users")
  fun getAllUsers(): Flow<List<UserEntity>>

  @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
  suspend fun getUserByUsername(username: String): UserEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<UserEntity>)

  // Patients
  @Query("SELECT * FROM patients ORDER BY name ASC")
  fun getAllPatientsFlow(): Flow<List<PatientEntity>>

  @Query("SELECT * FROM patients WHERE patientId = :patientId LIMIT 1")
  suspend fun getPatientById(patientId: String): PatientEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPatients(patients: List<PatientEntity>)

  // Medicines
  @Query("SELECT * FROM medicines ORDER BY name ASC")
  fun getAllMedicinesFlow(): Flow<List<MedicineEntity>>

  @Query("SELECT * FROM medicines WHERE medicineId = :medicineId LIMIT 1")
  suspend fun getMedicineById(medicineId: String): MedicineEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMedicines(medicines: List<MedicineEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMedicine(medicine: MedicineEntity)

  @Update
  suspend fun updateMedicine(medicine: MedicineEntity)

  @Query("DELETE FROM medicines WHERE medicineId = :medicineId")
  suspend fun deleteMedicine(medicineId: String)

  // Orders
  @Query("SELECT * FROM orders ORDER BY requestTime DESC")
  fun getAllOrdersFlow(): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE nurseId = :nurseId ORDER BY requestTime DESC")
  fun getOrdersByNurseFlow(nurseId: String): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE patientId = :patientId ORDER BY requestTime DESC")
  fun getOrdersByPatientFlow(patientId: String): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
  suspend fun getOrderById(orderId: String): OrderEntity?

  @Query("SELECT * FROM orders WHERE status NOT IN ('DELIVERED', 'CANCELLED') ORDER BY CASE WHEN priority = 'URGENT' THEN 1 WHEN priority = 'NORMAL' THEN 2 ELSE 3 END, requestTime ASC")
  fun getActiveAndPendingOrdersFlow(): Flow<List<OrderEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: OrderEntity)

  @Update
  suspend fun updateOrder(order: OrderEntity)

  @Query("UPDATE orders SET status = :newStatus WHERE orderId = :orderId")
  suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus)

  @Query("UPDATE orders SET currentCheckpoint = :checkpoint WHERE orderId = :orderId")
  suspend fun updateOrderCheckpoint(orderId: String, checkpoint: String)

  @Query("UPDATE orders SET obstacleReported = :hasObstacle, obstacleDetail = :detail WHERE orderId = :orderId")
  suspend fun updateOrderObstacle(orderId: String, hasObstacle: Boolean, detail: String)

  @Query("UPDATE orders SET status = :status, rfidTagVerified = :rfidTag, completionTimeString = :timeStr WHERE orderId = :orderId")
  suspend fun markOrderRfidVerifiedAndDelivered(orderId: String, status: OrderStatus, rfidTag: String, timeStr: String)

  // Order Items
  @Query("SELECT * FROM order_items WHERE orderId = :orderId")
  fun getItemsForOrderFlow(orderId: String): Flow<List<OrderItemEntity>>

  @Query("SELECT * FROM order_items WHERE orderId = :orderId")
  suspend fun getItemsForOrder(orderId: String): List<OrderItemEntity>

  @Query("SELECT * FROM order_items")
  fun getAllOrderItemsFlow(): Flow<List<OrderItemEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrderItems(items: List<OrderItemEntity>)

  // Robot Status
  @Query("SELECT * FROM robot_status WHERE robotId = :robotId LIMIT 1")
  fun getRobotStatusFlow(robotId: String): Flow<RobotStatusEntity?>

  @Query("SELECT * FROM robot_status ORDER BY robotId ASC")
  fun getAllRobotsFlow(): Flow<List<RobotStatusEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRobots(robots: List<RobotStatusEntity>)

  @Update
  suspend fun updateRobot(robot: RobotStatusEntity)

  @Query("UPDATE robot_status SET currentStatus = :state, currentOrderId = :orderId, destinationWard = :ward, destinationBed = :bed, currentCheckpoint = :checkpoint, hasObstacle = :hasObstacle, obstacleMessage = :obstacleMsg, ultrasonicDistanceCm = :distance, lastPingTime = :time WHERE robotId = :robotId")
  suspend fun updateRobotTelemetry(
    robotId: String,
    state: RobotState,
    orderId: String,
    ward: String,
    bed: String,
    checkpoint: String,
    hasObstacle: Boolean,
    obstacleMsg: String,
    distance: Float,
    time: Long = System.currentTimeMillis()
  )

  // Delivery Logs
  @Query("SELECT * FROM delivery_logs ORDER BY timestamp DESC LIMIT 50")
  fun getAllLogsFlow(): Flow<List<DeliveryLogEntity>>

  @Query("SELECT * FROM delivery_logs WHERE orderId = :orderId ORDER BY timestamp ASC")
  fun getLogsForOrderFlow(orderId: String): Flow<List<DeliveryLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: DeliveryLogEntity)

  // Notifications
  @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT 40")
  fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: NotificationEntity)

  @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0")
  suspend fun markAllNotificationsAsRead()
}
