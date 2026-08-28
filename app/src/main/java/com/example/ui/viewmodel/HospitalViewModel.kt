package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.auth.AuthState
import com.example.data.auth.LocalAuthStateManager
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
import com.example.data.repository.HospitalRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HospitalViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application, viewModelScope)
  val repository = HospitalRepository(database.hospitalDao(), viewModelScope)
  val authManager = LocalAuthStateManager(application.applicationContext, database.hospitalDao())

  // Authentication state flows
  val authState: StateFlow<AuthState> = authManager.authState
  val currentUser: StateFlow<UserEntity?> = authManager.currentUser
  val currentRole: StateFlow<UserRole?> = authManager.currentRole

  private val _loginError = MutableStateFlow<String?>(null)
  val loginError: StateFlow<String?> = combine(authState, _loginError) { state, customErr ->
    when (state) {
      is AuthState.AuthError -> state.message
      else -> customErr
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  private val _isAuthenticating = MutableStateFlow(false)
  val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

  // Global flows from repository
  val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allMedicines: StateFlow<List<MedicineEntity>> = repository.allMedicines
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allPatients: StateFlow<List<PatientEntity>> = repository.allPatients
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allRobots: StateFlow<List<RobotStatusEntity>> = repository.allRobots
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allLogs: StateFlow<List<DeliveryLogEntity>> = repository.allLogs
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allOrderItems: StateFlow<List<OrderItemEntity>> = repository.allOrderItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Primary active robot
  val primaryRobot: StateFlow<RobotStatusEntity?> = allRobots.combine(currentUser) { robots, _ ->
    robots.firstOrNull { it.robotId == "R01" } ?: robots.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Filtered orders for current nurse
  val nurseOrders: StateFlow<List<OrderEntity>> = combine(allOrders, currentUser) { orders, user ->
    if (user != null && user.role == UserRole.NURSE) {
      orders.filter { it.nurseId == user.userId || it.ward == user.ward }
    } else {
      orders
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Selected Order for Details / Timeline / RFID
  private val _selectedOrder = MutableStateFlow<OrderEntity?>(null)
  val selectedOrder: StateFlow<OrderEntity?> = _selectedOrder.asStateFlow()

  // UI Dialog / Modal States
  var showDemoSheet = MutableStateFlow(false)
  var showHardwareDialog = MutableStateFlow(false)
  var showNotificationsDialog = MutableStateFlow(false)
  var showRfidDialog = MutableStateFlow(false)
  var rfidOrderTarget = MutableStateFlow<OrderEntity?>(null)

  // Simulation auto-play job
  private var simulationJob: Job? = null
  val isAutoSimulating = MutableStateFlow(false)

  fun login(username: String, pass: String, role: UserRole, rememberMe: Boolean = true) {
    viewModelScope.launch {
      _loginError.value = null
      _isAuthenticating.value = true
      authManager.login(username, pass, role, rememberMe)
      _isAuthenticating.value = false
    }
  }

  fun switchUserRole(role: UserRole) {
    authManager.switchRole(role)
  }

  fun logout() {
    _loginError.value = null
    authManager.logout()
  }

  fun selectOrder(order: OrderEntity?) {
    _selectedOrder.value = order
  }

  fun openRfidVerification(order: OrderEntity) {
    rfidOrderTarget.value = order
    showRfidDialog.value = true
  }

  // Nurse Creates Order
  fun submitMedicineRequest(
    patient: PatientEntity,
    priority: OrderPriority,
    selectedItems: List<Pair<MedicineEntity, Int>>,
    onSuccess: (String) -> Unit
  ) {
    val nurse = currentUser.value ?: return
    viewModelScope.launch {
      val orderId = repository.createMedicineRequest(patient, nurse, priority, selectedItems)
      onSuccess(orderId)
    }
  }

  // Pharmacist accepts order
  fun acceptOrder(orderId: String) {
    viewModelScope.launch {
      repository.acceptOrder(orderId)
    }
  }

  // Pharmacist marks medicine loaded
  fun markMedicineLoaded(orderId: String) {
    viewModelScope.launch {
      repository.markMedicineLoaded(orderId, "R01")
    }
  }

  // Pharmacist dispatches robot
  fun dispatchRobot(orderId: String) {
    viewModelScope.launch {
      repository.dispatchRobot(orderId, "R01")
    }
  }

  // Simulation Triggers
  fun simulateAdvanceCheckpoint(checkpoint: String) {
    viewModelScope.launch {
      repository.advanceToCheckpoint(checkpoint, "R01")
    }
  }

  fun simulateObstacle(customDetail: String? = null) {
    viewModelScope.launch {
      val detail = customDetail ?: "HC-SR04 ultrasonic sensor detected obstacle in corridor (Distance: 16.5 cm). Robot halted."
      repository.triggerObstacle(detail, "R01")
    }
  }

  fun simulateClearObstacle() {
    viewModelScope.launch {
      repository.clearObstacle("R01")
    }
  }

  fun simulateDestinationReached() {
    viewModelScope.launch {
      repository.robotReachedDestination("R01")
    }
  }

  fun simulateRfidScan(orderId: String, rfidTag: String, onResult: (Boolean) -> Unit) {
    viewModelScope.launch {
      val success = repository.verifyRfidAndDeliver(orderId, rfidTag, "R01")
      onResult(success)
      if (success) {
        showRfidDialog.value = false
      }
    }
  }

  // Full Autonomous Delivery Demo Routine for College Presentation
  fun startFullDemoCuj(targetOrder: OrderEntity? = null) {
    if (isAutoSimulating.value) return
    isAutoSimulating.value = true

    simulationJob = viewModelScope.launch {
      val orderToUse = targetOrder ?: allOrders.value.firstOrNull { it.status != OrderStatus.DELIVERED }

      if (orderToUse == null) {
        // Create an example order if none exists
        val samplePatient = allPatients.value.firstOrNull() ?: PatientEntity("P1024", "Ravi Kumar", "Ward 3", "B-12", 45)
        val med1 = allMedicines.value.getOrNull(0) ?: MedicineEntity("MED01", "Paracetamol 500 mg", "500 mg", "tablet", 2.0, 500, "Analgesic")
        val med2 = allMedicines.value.getOrNull(1) ?: MedicineEntity("MED02", "Amoxicillin 500 mg", "500 mg", "capsule", 8.0, 320, "Antibiotic")
        val nurse = currentUser.value ?: UserEntity("NURSE-001", "Sarah Jenkins, RN", "nurse_sarah", "nurse123", UserRole.NURSE, "Ward 3")
        val createdId = repository.createMedicineRequest(samplePatient, nurse, OrderPriority.URGENT, listOf(med1 to 5, med2 to 5))
        delay(1200)
        runOrderLifecycle(createdId)
      } else {
        runOrderLifecycle(orderToUse.orderId)
      }

      isAutoSimulating.value = false
    }
  }

  private suspend fun runOrderLifecycle(orderId: String) {
    // 1. Accept
    repository.acceptOrder(orderId)
    delay(2000)

    // 2. Load Medicine
    repository.markMedicineLoaded(orderId, "R01")
    delay(2200)

    // 3. Dispatch
    repository.dispatchRobot(orderId, "R01")
    delay(2500)

    // 4. Checkpoint C1
    repository.advanceToCheckpoint("CHECKPOINT_C1", "R01")
    delay(2200)

    // 5. Obstacle Alert at C2
    repository.triggerObstacle("Obstacle detected near Checkpoint C2. Ultrasonic distance: 18 cm. Robot safely halted.", "R01")
    delay(3500)

    // 6. Obstacle Cleared
    repository.clearObstacle("R01")
    delay(2000)

    // 7. Checkpoint C2 & C3
    repository.advanceToCheckpoint("CHECKPOINT_C2", "R01")
    delay(2000)
    repository.advanceToCheckpoint("CHECKPOINT_C3", "R01")
    delay(2000)

    // 8. Destination Reached
    repository.robotReachedDestination("R01")
    delay(2500)

    // 9. RFID Verification & Delivery
    repository.verifyRfidAndDeliver(orderId, "STAFF-RFID-8829", "R01")
  }

  fun stopSimulation() {
    simulationJob?.cancel()
    isAutoSimulating.value = false
  }

  // Inventory modifications by Pharmacist
  fun saveMedicine(medicine: MedicineEntity) {
    viewModelScope.launch {
      repository.saveMedicine(medicine)
    }
  }

  fun deleteMedicine(medicineId: String) {
    viewModelScope.launch {
      repository.deleteMedicine(medicineId)
    }
  }

  fun markNotificationsAsRead() {
    viewModelScope.launch {
      repository.markAllNotificationsRead()
    }
  }
}
