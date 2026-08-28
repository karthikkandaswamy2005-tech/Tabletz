package com.example.data.repository

import com.example.data.dao.HospitalDao
import com.example.data.entity.DeliveryLogEntity
import com.example.data.entity.MedicineEntity
import com.example.data.entity.NotificationEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PatientEntity
import com.example.data.entity.RobotStatusEntity
import com.example.data.entity.UserEntity
import com.example.data.model.OrderPriority
import com.example.data.model.OrderStatus
import com.example.data.model.RobotState
import com.example.data.model.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class HospitalRepository(
  private val dao: HospitalDao,
  private val appScope: CoroutineScope
) {
  val allOrders: Flow<List<OrderEntity>> = dao.getAllOrdersFlow()
  val activeAndPendingOrders: Flow<List<OrderEntity>> = dao.getActiveAndPendingOrdersFlow()
  val allMedicines: Flow<List<MedicineEntity>> = dao.getAllMedicinesFlow()
  val allPatients: Flow<List<PatientEntity>> = dao.getAllPatientsFlow()
  val allRobots: Flow<List<RobotStatusEntity>> = dao.getAllRobotsFlow()
  val allLogs: Flow<List<DeliveryLogEntity>> = dao.getAllLogsFlow()
  val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotificationsFlow()
  val allOrderItems: Flow<List<OrderItemEntity>> = dao.getAllOrderItemsFlow()

  fun getOrdersByNurse(nurseId: String): Flow<List<OrderEntity>> = dao.getOrdersByNurseFlow(nurseId)
  fun getOrdersByPatient(patientId: String): Flow<List<OrderEntity>> = dao.getOrdersByPatientFlow(patientId)
  fun getItemsForOrder(orderId: String): Flow<List<OrderItemEntity>> = dao.getItemsForOrderFlow(orderId)

  private fun getFormattedNow(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date())
  }

  private fun getTimeOnlyNow(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
  }

  // Authentication
  suspend fun login(username: String, password: String): UserEntity? {
    val user = dao.getUserByUsername(username)
    return if (user != null && user.passwordHash == password) user else null
  }

  // Create Order by Nurse
  suspend fun createMedicineRequest(
    patient: PatientEntity,
    nurse: UserEntity,
    priority: OrderPriority,
    items: List<Pair<MedicineEntity, Int>>
  ): String {
    val timestamp = System.currentTimeMillis()
    val orderIdNumber = (100..999).random()
    val orderId = "MED-2026-00$orderIdNumber"
    val timeStr = getFormattedNow()

    val total = items.sumOf { it.first.unitPrice * it.second }

    val order = OrderEntity(
      orderId = orderId,
      patientId = patient.patientId,
      patientName = patient.name,
      ward = patient.ward,
      bed = patient.bed,
      nurseId = nurse.userId,
      nurseName = nurse.name,
      priority = priority,
      totalAmount = total,
      requestTime = timestamp,
      requestTimeString = timeStr,
      status = OrderStatus.REQUEST_RECEIVED,
      assignedRobotId = "R01",
      currentCheckpoint = "PHARMACY"
    )

    dao.insertOrder(order)

    val itemEntities = items.map { (med, qty) ->
      OrderItemEntity(
        orderId = orderId,
        medicineId = med.medicineId,
        medicineName = med.name,
        dosage = med.dosage,
        quantity = qty,
        unitPrice = med.unitPrice,
        subtotal = med.unitPrice * qty
      )
    }
    dao.insertOrderItems(itemEntities)

    // Log & Notify
    dao.insertLog(
      DeliveryLogEntity(
        orderId = orderId,
        robotId = "R01",
        eventType = "ORDER_CREATED",
        message = "New medicine request created by ${nurse.name} for ${patient.name} (${patient.ward}, Bed ${patient.bed}) - Total: ₹${total.toInt()}",
        checkpoint = "PHARMACY",
        formattedTime = getFormattedNow()
      )
    )

    dao.insertNotification(
      NotificationEntity(
        recipientRole = "PHARMACIST",
        title = if (priority == OrderPriority.URGENT) "🚨 URGENT Medicine Request" else "New Medicine Request",
        message = "Order $orderId received for ${patient.name} (${patient.ward} - Bed ${patient.bed})",
        type = "REQUEST",
        orderId = orderId,
        formattedTime = getTimeOnlyNow()
      )
    )

    return orderId
  }

  // Pharmacist Actions
  suspend fun acceptOrder(orderId: String) {
    val order = dao.getOrderById(orderId) ?: return
    dao.updateOrderStatus(orderId, OrderStatus.PHARMACIST_ACCEPTED)

    dao.insertLog(
      DeliveryLogEntity(
        orderId = orderId,
        robotId = order.assignedRobotId,
        eventType = "REQUEST_ACCEPTED",
        message = "Pharmacist accepted order $orderId for preparation.",
        checkpoint = "PHARMACY",
        formattedTime = getFormattedNow()
      )
    )

    dao.insertNotification(
      NotificationEntity(
        recipientRole = "NURSE",
        title = "Request Accepted",
        message = "Pharmacist is preparing medicines for Order $orderId (${order.patientName})",
        type = "ACCEPTED",
        orderId = orderId,
        formattedTime = getTimeOnlyNow()
      )
    )
  }

  suspend fun markMedicineLoaded(orderId: String, robotId: String = "R01") {
    val order = dao.getOrderById(orderId) ?: return
    dao.updateOrderStatus(orderId, OrderStatus.MEDICINE_LOADED)

    dao.updateRobotTelemetry(
      robotId = robotId,
      state = RobotState.MEDICINE_LOADED,
      orderId = orderId,
      ward = order.ward,
      bed = order.bed,
      checkpoint = "PHARMACY",
      hasObstacle = false,
      obstacleMsg = "",
      distance = 150.0f
    )

    dao.insertLog(
      DeliveryLogEntity(
        orderId = orderId,
        robotId = robotId,
        eventType = "MEDICINE_LOADED",
        message = "Medicines packed & securely locked in $robotId cargo bay.",
        checkpoint = "PHARMACY",
        formattedTime = getFormattedNow()
      )
    )

    dao.insertNotification(
      NotificationEntity(
        recipientRole = "ALL",
        title = "Medicine Loaded",
        message = "Medicines locked in $robotId for Order $orderId. Ready for dispatch.",
        type = "LOADED",
        orderId = orderId,
        formattedTime = getTimeOnlyNow()
      )
    )
  }

  suspend fun dispatchRobot(orderId: String, robotId: String = "R01") {
    val order = dao.getOrderById(orderId) ?: return
    dao.updateOrderStatus(orderId, OrderStatus.ROBOT_DISPATCHED)
    dao.updateOrderCheckpoint(orderId, "PHARMACY")

    dao.updateRobotTelemetry(
      robotId = robotId,
      state = RobotState.DISPATCHED,
      orderId = orderId,
      ward = order.ward,
      bed = order.bed,
      checkpoint = "PHARMACY",
      hasObstacle = false,
      obstacleMsg = "",
      distance = 140.0f
    )

    dao.insertLog(
      DeliveryLogEntity(
        orderId = orderId,
        robotId = robotId,
        eventType = "ROBOT_DISPATCHED",
        message = "Autonomous Robot $robotId departed Pharmacy towards ${order.ward} (Bed ${order.bed}).",
        checkpoint = "PHARMACY",
        formattedTime = getFormattedNow()
      )
    )

    dao.insertNotification(
      NotificationEntity(
        recipientRole = "NURSE",
        title = "Robot Dispatched 🤖",
        message = "Robot $robotId is on its way to ${order.ward} for ${order.patientName}",
        type = "DISPATCH",
        orderId = orderId,
        formattedTime = getTimeOnlyNow()
      )
    )

    // Transition to EN_ROUTE
    delay(500)
    dao.updateOrderStatus(orderId, OrderStatus.EN_ROUTE)
    dao.updateRobotTelemetry(
      robotId = robotId,
      state = RobotState.EN_ROUTE,
      orderId = orderId,
      ward = order.ward,
      bed = order.bed,
      checkpoint = "CHECKPOINT_C1",
      hasObstacle = false,
      obstacleMsg = "",
      distance = 120.0f
    )
  }

  // Simulation & Hardware event triggers
  suspend fun advanceToCheckpoint(checkpoint: String, robotId: String = "R01") {
    val robots = dao.getAllRobotsFlow().firstOrNull()
    val robot = robots?.find { it.robotId == robotId } ?: return
    val orderId = robot.currentOrderId

    dao.updateOrderCheckpoint(orderId, checkpoint)
    dao.updateRobotTelemetry(
      robotId = robotId,
      state = RobotState.EN_ROUTE,
      orderId = orderId,
      ward = robot.destinationWard,
      bed = robot.destinationBed,
      checkpoint = checkpoint,
      hasObstacle = false,
      obstacleMsg = "",
      distance = 130.0f
    )

    dao.insertLog(
      DeliveryLogEntity(
        orderId = orderId,
        robotId = robotId,
        eventType = "CHECKPOINT_REACHED",
        message = "Robot reached navigation $checkpoint via line tracking sensors.",
        checkpoint = checkpoint,
        formattedTime = getFormattedNow()
      )
    )
  }

  suspend fun triggerObstacle(obstacleDetail: String = "Obstacle detected near Checkpoint C2 by HC-SR04 ultrasonic sensor (Distance: 18 cm)", robotId: String = "R01") {
    val robots = dao.getAllRobotsFlow().firstOrNull()
    val robot = robots?.find { it.robotId == robotId } ?: return
    val orderId = robot.currentOrderId

    dao.updateOrderObstacle(orderId, true, obstacleDetail)
    dao.updateRobotTelemetry(
      robotId = robotId,
      state = RobotState.OBSTACLE_DETECTED,
      orderId = orderId,
      ward = robot.destinationWard,
      bed = robot.destinationBed,
      checkpoint = robot.currentCheckpoint,
      hasObstacle = true,
      obstacleMsg = obstacleDetail,
      distance = 18.0f
    )

    val timeNow = getTimeOnlyNow()
    dao.insertLog(
      DeliveryLogEntity(
        orderId = orderId,
        robotId = robotId,
        eventType = "OBSTACLE_DETECTED",
        message = "⚠ $timeNow — $robotId — $obstacleDetail. Robot halted safely.",
        checkpoint = robot.currentCheckpoint,
        formattedTime = getFormattedNow()
      )
    )

    dao.insertNotification(
      NotificationEntity(
        recipientRole = "ALL",
        title = "⚠ OBSTACLE DETECTED",
        message = "Robot $robotId stopped near ${robot.currentCheckpoint}. $obstacleDetail",
        type = "OBSTACLE",
        orderId = orderId,
        formattedTime = timeNow
      )
    )
  }

  suspend fun clearObstacle(robotId: String = "R01") {
    val robots = dao.getAllRobotsFlow().firstOrNull()
    val robot = robots?.find { it.robotId == robotId } ?: return
    val orderId = robot.currentOrderId

    dao.updateOrderObstacle(orderId, false, "")
    dao.updateRobotTelemetry(
      robotId = robotId,
      state = RobotState.EN_ROUTE,
      orderId = orderId,
      ward = robot.destinationWard,
      bed = robot.destinationBed,
      checkpoint = robot.currentCheckpoint,
      hasObstacle = false,
      obstacleMsg = "",
      distance = 135.0f
    )

    dao.insertLog(
      DeliveryLogEntity(
        orderId = orderId,
        robotId = robotId,
        eventType = "OBSTACLE_CLEARED",
        message = "Path cleared. Robot resumed navigation towards ${robot.destinationWard}.",
        checkpoint = robot.currentCheckpoint,
        formattedTime = getFormattedNow()
      )
    )

    dao.insertNotification(
      NotificationEntity(
        recipientRole = "ALL",
        title = "Obstacle Cleared",
        message = "Robot $robotId path is clear. Resuming route to ${robot.destinationWard}.",
        type = "RESUMED",
        orderId = orderId,
        formattedTime = getTimeOnlyNow()
      )
    )
  }

  suspend fun robotReachedDestination(robotId: String = "R01") {
    val robots = dao.getAllRobotsFlow().firstOrNull()
    val robot = robots?.find { it.robotId == robotId } ?: return
    val orderId = robot.currentOrderId

    dao.updateOrderStatus(orderId, OrderStatus.DESTINATION_REACHED)
    dao.updateOrderCheckpoint(orderId, robot.destinationWard)

    dao.updateRobotTelemetry(
      robotId = robotId,
      state = RobotState.DESTINATION_REACHED,
      orderId = orderId,
      ward = robot.destinationWard,
      bed = robot.destinationBed,
      checkpoint = robot.destinationWard,
      hasObstacle = false,
      obstacleMsg = "",
      distance = 100.0f
    )

    dao.insertLog(
      DeliveryLogEntity(
        orderId = orderId,
        robotId = robotId,
        eventType = "DESTINATION_REACHED",
        message = "Robot arrived at ${robot.destinationWard} (Bed ${robot.destinationBed}). Awaiting nurse RFID badge authentication.",
        checkpoint = robot.destinationWard,
        formattedTime = getFormattedNow()
      )
    )

    dao.insertNotification(
      NotificationEntity(
        recipientRole = "NURSE",
        title = "Robot Arrived at Destination 📍",
        message = "Please tap your staff RFID card on Robot $robotId to unlock the medicine container.",
        type = "RFID",
        orderId = orderId,
        formattedTime = getTimeOnlyNow()
      )
    )
  }

  suspend fun verifyRfidAndDeliver(
    orderId: String,
    rfidTag: String,
    robotId: String = "R01"
  ): Boolean {
    val isValid = rfidTag.startsWith("STAFF-RFID") || rfidTag == "STAFF-RFID-8829" || rfidTag == "STAFF-RFID-4412" || rfidTag == "PHARM-RFID-0011" || rfidTag.contains("VALID")

    if (!isValid) {
      dao.insertLog(
        DeliveryLogEntity(
          orderId = orderId,
          robotId = robotId,
          eventType = "RFID_FAILED",
          message = "❌ RFID Verification Failed: Tag '$rfidTag' unauthorized. Medicine compartment remains locked.",
          checkpoint = "DESTINATION",
          formattedTime = getFormattedNow()
        )
      )

      dao.insertNotification(
        NotificationEntity(
          recipientRole = "NURSE",
          title = "RFID Verification Failed ❌",
          message = "Unauthorized card detected on $robotId. Please use a registered nurse badge.",
          type = "ERROR",
          orderId = orderId,
          formattedTime = getTimeOnlyNow()
        )
      )
      return false
    }

    val timeNow = getFormattedNow()

    // Successful RFID verification
    dao.insertLog(
      DeliveryLogEntity(
        orderId = orderId,
        robotId = robotId,
        eventType = "RFID_VERIFIED",
        message = "✓ RFID Verified: Authorized Staff Tag '$rfidTag'. Medicine chamber unlocked.",
        checkpoint = "DESTINATION",
        formattedTime = timeNow
      )
    )

    // Complete delivery
    dao.markOrderRfidVerifiedAndDelivered(orderId, OrderStatus.DELIVERED, rfidTag, timeNow)

    dao.updateRobotTelemetry(
      robotId = robotId,
      state = RobotState.DELIVERED,
      orderId = "",
      ward = "",
      bed = "",
      checkpoint = "PHARMACY",
      hasObstacle = false,
      obstacleMsg = "",
      distance = 150.0f
    )

    dao.insertLog(
      DeliveryLogEntity(
        orderId = orderId,
        robotId = robotId,
        eventType = "MEDICINE_DELIVERED",
        message = "✓ Medicine Delivery completed successfully. Order closed. Robot returning to Pharmacy base.",
        checkpoint = "PHARMACY",
        formattedTime = timeNow
      )
    )

    dao.insertNotification(
      NotificationEntity(
        recipientRole = "ALL",
        title = "Medicine Delivered Successfully ✓",
        message = "Order $orderId delivered to patient. Billing and medical logs updated.",
        type = "DELIVERED",
        orderId = orderId,
        formattedTime = getTimeOnlyNow()
      )
    )

    // Set robot to idle after brief delay
    appScope.launch(Dispatchers.IO) {
      delay(2000)
      dao.updateRobotTelemetry(
        robotId = robotId,
        state = RobotState.IDLE,
        orderId = "",
        ward = "",
        bed = "",
        checkpoint = "PHARMACY",
        hasObstacle = false,
        obstacleMsg = "",
        distance = 150.0f
      )
    }

    return true
  }

  // Medicine Inventory Management by Pharmacist
  suspend fun saveMedicine(medicine: MedicineEntity) {
    dao.insertMedicine(medicine)
  }

  suspend fun updateMedicine(medicine: MedicineEntity) {
    dao.updateMedicine(medicine)
  }

  suspend fun deleteMedicine(medicineId: String) {
    dao.deleteMedicine(medicineId)
  }

  suspend fun markAllNotificationsRead() {
    dao.markAllNotificationsAsRead()
  }

  suspend fun addPatient(patient: PatientEntity) {
    dao.insertPatients(listOf(patient))
  }
}
