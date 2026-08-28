package com.example.ui.nurse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
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
import com.example.ui.theme.MedNavySecondary
import com.example.ui.theme.MedTealPrimary

@Composable
fun NurseMainScreen(
  currentUser: UserEntity,
  patients: List<PatientEntity>,
  medicines: List<MedicineEntity>,
  orders: List<OrderEntity>,
  orderItems: List<OrderItemEntity>,
  robot: RobotStatusEntity?,
  notifications: List<NotificationEntity>,
  isAutoSimulating: Boolean,
  onSwitchRole: (UserRole) -> Unit,
  onLogout: () -> Unit,
  onCreateRequest: (patient: PatientEntity, items: List<Pair<MedicineEntity, Int>>, priority: com.example.data.model.OrderPriority) -> Unit,
  onClearObstacle: () -> Unit,
  onVerifyRfid: (orderId: String, rfidTag: String, onResult: (Boolean) -> Unit) -> Unit,
  onMarkAllNotificationsRead: () -> Unit,
  onStartFullDemo: () -> Unit,
  onStopFullDemo: () -> Unit,
  onAdvanceCheckpoint: (String) -> Unit,
  onTriggerObstacle: () -> Unit,
  onDestinationReached: () -> Unit
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
          selectedIconColor = MedTealPrimary,
          selectedTextColor = Color.White,
          unselectedIconColor = Color(0xFF94A3B8),
          unselectedTextColor = Color(0xFF94A3B8),
          indicatorColor = Color.White.copy(alpha = 0.15f)
        )

        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") },
          label = { Text("Dashboard", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
          colors = navColors,
          modifier = Modifier.testTag("nav_nurse_dashboard")
        )

        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = { Icon(imageVector = Icons.Default.AddCircle, contentDescription = "New Request") },
          label = { Text("New Request", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
          colors = navColors,
          modifier = Modifier.testTag("nav_nurse_new_request")
        )

        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          icon = { Icon(imageVector = Icons.Default.ListAlt, contentDescription = "My Requests") },
          label = { Text("Orders", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
          colors = navColors,
          modifier = Modifier.testTag("nav_nurse_orders")
        )

        NavigationBarItem(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          icon = { Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = "Patient Billing") },
          label = { Text("Billing History", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
          colors = navColors,
          modifier = Modifier.testTag("nav_nurse_history")
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
        0 -> NurseDashboardTab(
          currentUser = currentUser,
          orders = orders,
          robot = robot,
          notifications = notifications,
          onNavigateNewRequest = { selectedTab = 1 },
          onSelectOrder = { selectedTab = 2 },
          onOpenRfidDialog = { rfidDialogOrder = it },
          onClearObstacle = onClearObstacle
        )
        1 -> NewMedicineRequestTab(
          currentUser = currentUser,
          patients = patients,
          medicines = medicines,
          onSubmitRequest = { patient, items, priority ->
            onCreateRequest(patient, items, priority)
            selectedTab = 2
          },
          onCancel = { selectedTab = 0 }
        )
        2 -> MyRequestsTab(
          currentUser = currentUser,
          orders = orders,
          orderItems = orderItems,
          robot = robot,
          onOpenRfidDialog = { rfidDialogOrder = it }
        )
        3 -> PatientHistoryTab(
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
