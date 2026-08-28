package com.example.ui.pharmacist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.entity.DeliveryLogEntity
import com.example.data.entity.MedicineEntity
import com.example.data.entity.NotificationEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PatientEntity
import com.example.data.entity.RobotStatusEntity
import com.example.data.entity.UserEntity
import com.example.data.model.UserRole
import com.example.ui.components.DemoSimulationBottomSheet
import com.example.ui.components.HardwareEsp32GuideDialog
import com.example.ui.components.HospitalTopAppBar
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.RfidVerificationDialog
import com.example.ui.nurse.PatientHistoryTab
import com.example.ui.theme.MedNavySecondary
import com.example.ui.theme.MedTealPrimary

@Composable
fun PharmacistMainScreen(
  currentUser: UserEntity,
  orders: List<OrderEntity>,
  orderItems: List<OrderItemEntity>,
  medicines: List<MedicineEntity>,
  patients: List<PatientEntity>,
  robot: RobotStatusEntity?,
  deliveryLogs: List<DeliveryLogEntity>,
  notifications: List<NotificationEntity>,
  isAutoSimulating: Boolean,
  onSwitchRole: (UserRole) -> Unit,
  onLogout: () -> Unit,
  onAcceptOrder: (String) -> Unit,
  onLoadMedicine: (String, String) -> Unit,
  onDispatchRobot: (String, String) -> Unit,
  onUpdateMedicine: (MedicineEntity) -> Unit,
  onClearObstacle: () -> Unit,
  onTriggerObstacle: () -> Unit,
  onAdvanceCheckpoint: (String) -> Unit,
  onDestinationReached: () -> Unit,
  onReturnToBase: () -> Unit,
  onVerifyRfid: (orderId: String, rfidTag: String, onResult: (Boolean) -> Unit) -> Unit,
  onMarkAllNotificationsRead: () -> Unit,
  onStartFullDemo: () -> Unit,
  onStopFullDemo: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }

  // Sheets & Dialogs
  var showDemoSheet by remember { mutableStateOf(false) }
  var showHardwareGuide by remember { mutableStateOf(false) }
  var showNotificationsSheet by remember { mutableStateOf(false) }
  var rfidDialogOrder by remember { mutableStateOf<OrderEntity?>(null) }

  val unreadNotifCount = notifications.count { !it.isRead }

  Scaffold(
    topBar = {
      HospitalTopAppBar(
        currentUser = currentUser,
        robot = robot,
        unreadNotificationCount = unreadNotifCount,
        onOpenNotifications = { showNotificationsSheet = true },
        onOpenHardwareGuide = { showHardwareGuide = true },
        onOpenDemoSheet = { showDemoSheet = true },
        onSwitchRole = onSwitchRole,
        onLogout = onLogout
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MedNavySecondary,
        contentColor = Color.White
      ) {
        val navColors = NavigationBarItemDefaults.colors(
          selectedIconColor = Color(0xFFA78BFA),
          selectedTextColor = Color.White,
          unselectedIconColor = Color(0xFF94A3B8),
          unselectedTextColor = Color(0xFF94A3B8),
          indicatorColor = Color.White.copy(alpha = 0.15f)
        )

        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Queue") },
          label = { Text("Prescriptions", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
          colors = navColors,
          modifier = Modifier.testTag("nav_pharm_queue")
        )

        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = { Icon(imageVector = Icons.Default.SmartToy, contentDescription = "Robot Live") },
          label = { Text("Robot Monitor", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
          colors = navColors,
          modifier = Modifier.testTag("nav_pharm_robot")
        )

        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          icon = { Icon(imageVector = Icons.Default.Inventory, contentDescription = "Inventory") },
          label = { Text("Medicines", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
          colors = navColors,
          modifier = Modifier.testTag("nav_pharm_inventory")
        )

        NavigationBarItem(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          icon = { Icon(imageVector = Icons.Default.Assignment, contentDescription = "Logs") },
          label = { Text("Audit Logs", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
          colors = navColors,
          modifier = Modifier.testTag("nav_pharm_logs")
        )

        NavigationBarItem(
          selected = selectedTab == 4,
          onClick = { selectedTab = 4 },
          icon = { Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = "Billing History") },
          label = { Text("Billing", fontSize = 11.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
          colors = navColors,
          modifier = Modifier.testTag("nav_pharm_billing")
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (selectedTab) {
        0 -> PharmacistDashboardTab(
          currentUser = currentUser,
          orders = orders,
          orderItems = orderItems,
          robot = robot,
          notifications = notifications,
          onAcceptOrder = onAcceptOrder,
          onLoadMedicine = onLoadMedicine,
          onDispatchRobot = onDispatchRobot,
          onNavigateToRobotMonitoring = { selectedTab = 1 },
          onClearObstacle = onClearObstacle
        )
        1 -> RobotMonitoringTab(
          robot = robot,
          activeOrders = orders,
          onTriggerObstacle = onTriggerObstacle,
          onClearObstacle = onClearObstacle,
          onAdvanceCheckpoint = onAdvanceCheckpoint,
          onReturnToBase = onReturnToBase
        )
        2 -> MedicineInventoryTab(
          medicines = medicines,
          onUpdateMedicine = onUpdateMedicine
        )
        3 -> DeliveryLogsTab(
          logs = deliveryLogs
        )
        4 -> PatientHistoryTab(
          patients = patients,
          orders = orders,
          orderItems = orderItems
        )
      }
    }
  }

  // Sheets & Dialogs
  if (showDemoSheet) {
    DemoSimulationBottomSheet(
      robot = robot,
      activeOrders = orders,
      isAutoSimulating = isAutoSimulating,
      onDismiss = { showDemoSheet = false },
      onStartFullDemo = onStartFullDemo,
      onStopFullDemo = onStopFullDemo,
      onAdvanceCheckpoint = onAdvanceCheckpoint,
      onTriggerObstacle = onTriggerObstacle,
      onClearObstacle = onClearObstacle,
      onDestinationReached = onDestinationReached,
      onOpenRfidDialog = { rfidDialogOrder = it }
    )
  }

  if (showHardwareGuide) {
    HardwareEsp32GuideDialog(onDismiss = { showHardwareGuide = false })
  }

  if (showNotificationsSheet) {
    NotificationsSheet(
      notifications = notifications,
      onDismiss = { showNotificationsSheet = false },
      onMarkAllRead = onMarkAllNotificationsRead
    )
  }

  if (rfidDialogOrder != null) {
    RfidVerificationDialog(
      order = rfidDialogOrder,
      onDismiss = { rfidDialogOrder = null },
      onVerifyRfid = { orderId, tag, onResult ->
        onVerifyRfid(orderId, tag) { success ->
          onResult(success)
        }
      }
    )
  }
}
