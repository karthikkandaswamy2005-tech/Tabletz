package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.model.UserRole
import com.example.ui.auth.LoginScreen
import com.example.ui.nurse.NurseMainScreen
import com.example.ui.pharmacist.PharmacistMainScreen
import com.example.ui.viewmodel.HospitalViewModel

@Composable
fun HospitalApp(
  viewModel: HospitalViewModel
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val patients by viewModel.allPatients.collectAsState()
  val medicines by viewModel.allMedicines.collectAsState()
  val orders by viewModel.allOrders.collectAsState()
  val orderItems by viewModel.allOrderItems.collectAsState()
  val robot by viewModel.primaryRobot.collectAsState()
  val deliveryLogs by viewModel.allLogs.collectAsState()
  val notifications by viewModel.allNotifications.collectAsState()
  val isAutoSimulating by viewModel.isAutoSimulating.collectAsState()
  val authError by viewModel.loginError.collectAsState()
  val isAuthenticating by viewModel.isAuthenticating.collectAsState()

  Surface(modifier = Modifier.fillMaxSize()) {
    Crossfade(targetState = currentUser, label = "UserRoleCrossfade") { user ->
      if (user == null) {
        LoginScreen(
          onLoginSuccess = { username, pass, role, rememberMe ->
            viewModel.login(username, pass, role, rememberMe)
          },
          errorMessage = authError,
          isLoading = isAuthenticating
        )
      } else {
        when (user.role) {
          UserRole.NURSE -> {
            NurseMainScreen(
              currentUser = user,
              patients = patients,
              medicines = medicines,
              orders = orders,
              orderItems = orderItems,
              robot = robot,
              notifications = notifications,
              isAutoSimulating = isAutoSimulating,
              onSwitchRole = { newRole -> viewModel.switchUserRole(newRole) },
              onLogout = { viewModel.logout() },
              onCreateRequest = { patient, items, priority ->
                viewModel.submitMedicineRequest(patient, priority, items) {}
              },
              onClearObstacle = { viewModel.simulateClearObstacle() },
              onVerifyRfid = { orderId, rfidTag, onResult ->
                viewModel.simulateRfidScan(orderId, rfidTag) { success ->
                  onResult(success)
                }
              },
              onMarkAllNotificationsRead = { viewModel.markNotificationsAsRead() },
              onStartFullDemo = { viewModel.startFullDemoCuj() },
              onStopFullDemo = { viewModel.stopSimulation() },
              onAdvanceCheckpoint = { cp -> viewModel.simulateAdvanceCheckpoint(cp) },
              onTriggerObstacle = { viewModel.simulateObstacle() },
              onDestinationReached = { viewModel.simulateDestinationReached() }
            )
          }

          UserRole.PHARMACIST -> {
            PharmacistMainScreen(
              currentUser = user,
              orders = orders,
              orderItems = orderItems,
              medicines = medicines,
              patients = patients,
              robot = robot,
              deliveryLogs = deliveryLogs,
              notifications = notifications,
              isAutoSimulating = isAutoSimulating,
              onSwitchRole = { newRole -> viewModel.switchUserRole(newRole) },
              onLogout = { viewModel.logout() },
              onAcceptOrder = { orderId -> viewModel.acceptOrder(orderId) },
              onLoadMedicine = { orderId, _ -> viewModel.markMedicineLoaded(orderId) },
              onDispatchRobot = { orderId, _ -> viewModel.dispatchRobot(orderId) },
              onUpdateMedicine = { med -> viewModel.saveMedicine(med) },
              onClearObstacle = { viewModel.simulateClearObstacle() },
              onTriggerObstacle = { viewModel.simulateObstacle() },
              onAdvanceCheckpoint = { cp -> viewModel.simulateAdvanceCheckpoint(cp) },
              onDestinationReached = { viewModel.simulateDestinationReached() },
              onReturnToBase = { viewModel.simulateAdvanceCheckpoint("BASE_STATION") },
              onVerifyRfid = { orderId, rfidTag, onResult ->
                viewModel.simulateRfidScan(orderId, rfidTag) { success ->
                  onResult(success)
                }
              },
              onMarkAllNotificationsRead = { viewModel.markNotificationsAsRead() },
              onStartFullDemo = { viewModel.startFullDemoCuj() },
              onStopFullDemo = { viewModel.stopSimulation() }
            )
          }
        }
      }
    }
  }
}
