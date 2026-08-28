package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.data.dao.HospitalDao
import com.example.data.entity.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sealed class representing the discrete states of local user authentication.
 */
sealed class AuthState {
  data class Unauthenticated(val lastSelectedRole: UserRole = UserRole.NURSE) : AuthState()
  data object Authenticating : AuthState()
  data class Authenticated(
    val user: UserEntity,
    val sessionTimestamp: Long = System.currentTimeMillis()
  ) : AuthState()
  data class AuthError(
    val message: String,
    val selectedRole: UserRole
  ) : AuthState()
}

/**
 * Permission specifications enforced across Nurse and Pharmacist roles.
 */
data class RolePermissions(
  val canCreatePrescriptionOrder: Boolean,
  val canApprovePrescriptionQueue: Boolean,
  val canLoadMedicineCargo: Boolean,
  val canDispatchRobot: Boolean,
  val canVerifyBedsideRfid: Boolean,
  val canManagePharmacyInventory: Boolean,
  val canViewPatientHistory: Boolean,
  val canMonitorRobotTelemetry: Boolean,
  val canControlObstacleOverride: Boolean
) {
  companion object {
    fun forRole(role: UserRole): RolePermissions {
      return when (role) {
        UserRole.NURSE -> RolePermissions(
          canCreatePrescriptionOrder = true,
          canApprovePrescriptionQueue = false,
          canLoadMedicineCargo = false,
          canDispatchRobot = false,
          canVerifyBedsideRfid = true,
          canManagePharmacyInventory = false,
          canViewPatientHistory = true,
          canMonitorRobotTelemetry = true,
          canControlObstacleOverride = true
        )
        UserRole.PHARMACIST -> RolePermissions(
          canCreatePrescriptionOrder = false,
          canApprovePrescriptionQueue = true,
          canLoadMedicineCargo = true,
          canDispatchRobot = true,
          canVerifyBedsideRfid = false,
          canManagePharmacyInventory = true,
          canViewPatientHistory = true,
          canMonitorRobotTelemetry = true,
          canControlObstacleOverride = true
        )
      }
    }
  }
}

/**
 * Local Authentication State Manager.
 * Handles credential verification, role-based session lifecycle, local persistence,
 * and role distinction between Nurse and Pharmacist.
 */
class LocalAuthStateManager(
  private val context: Context,
  private val hospitalDao: HospitalDao
) {
  private val prefs: SharedPreferences = context.getSharedPreferences(
    PREFS_AUTH_NAME,
    Context.MODE_PRIVATE
  )

  private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated())
  val authState: StateFlow<AuthState> = _authState.asStateFlow()

  private val _currentUser = MutableStateFlow<UserEntity?>(null)
  val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

  private val _currentRole = MutableStateFlow<UserRole?>(null)
  val currentRole: StateFlow<UserRole?> = _currentRole.asStateFlow()

  // Default fallback staff credentials for immediate testing
  private val defaultStaffRoster = listOf(
    UserEntity(
      userId = "NURSE-001",
      name = "Sarah Jenkins, RN",
      username = "nurse_sarah",
      passwordHash = "nurse123",
      role = UserRole.NURSE,
      ward = "Ward 3",
      staffBadgeId = "STAFF-RFID-8829"
    ),
    UserEntity(
      userId = "NURSE-002",
      name = "David Chen, RN",
      username = "nurse_david",
      passwordHash = "nurse123",
      role = UserRole.NURSE,
      ward = "ICU Ward",
      staffBadgeId = "STAFF-RFID-4412"
    ),
    UserEntity(
      userId = "PHARM-001",
      name = "Dr. Arjun Mehta, PharmD",
      username = "pharm_arjun",
      passwordHash = "pharm123",
      role = UserRole.PHARMACIST,
      ward = "Central Pharmacy",
      staffBadgeId = "PHARM-RFID-0011"
    ),
    UserEntity(
      userId = "PHARM-002",
      name = "Dr. Emily Watson, RPh",
      username = "pharm_emily",
      passwordHash = "pharm123",
      role = UserRole.PHARMACIST,
      ward = "Central Pharmacy",
      staffBadgeId = "PHARM-RFID-0022"
    )
  )

  init {
    restoreSessionIfRemembered()
  }

  /**
   * Restores persisted session if "Remember Me" was enabled.
   */
  private fun restoreSessionIfRemembered() {
    val isRemembered = prefs.getBoolean(KEY_REMEMBER_ME, false)
    val savedUsername = prefs.getString(KEY_SAVED_USERNAME, null)
    val savedRoleName = prefs.getString(KEY_SAVED_ROLE, null)

    if (isRemembered && !savedUsername.isNullOrEmpty() && !savedRoleName.isNullOrEmpty()) {
      val role = runCatching { UserRole.valueOf(savedRoleName) }.getOrDefault(UserRole.NURSE)
      val matchedUser = defaultStaffRoster.firstOrNull { it.username == savedUsername && it.role == role }
        ?: UserEntity(
          userId = if (role == UserRole.NURSE) "NURSE-001" else "PHARM-001",
          name = if (role == UserRole.NURSE) "Sarah Jenkins, RN" else "Dr. Arjun Mehta, PharmD",
          username = savedUsername,
          passwordHash = if (role == UserRole.NURSE) "nurse123" else "pharm123",
          role = role,
          ward = if (role == UserRole.NURSE) "Ward 3" else "Central Pharmacy",
          staffBadgeId = if (role == UserRole.NURSE) "STAFF-RFID-8829" else "PHARM-RFID-0011"
        )
      _currentUser.value = matchedUser
      _currentRole.value = role
      _authState.value = AuthState.Authenticated(matchedUser)
    } else {
      _authState.value = AuthState.Unauthenticated(UserRole.NURSE)
      _currentUser.value = null
      _currentRole.value = null
    }
  }

  /**
   * Authenticates user against local database or fallback staff roster.
   */
  suspend fun login(
    usernameInput: String,
    passwordInput: String,
    targetRole: UserRole,
    rememberMe: Boolean = false
  ): Result<UserEntity> {
    _authState.value = AuthState.Authenticating
    val cleanUsername = usernameInput.trim()
    val cleanPassword = passwordInput.trim()

    if (cleanUsername.isEmpty() || cleanPassword.isEmpty()) {
      val errorMsg = "Staff ID / Username and Password cannot be empty."
      _authState.value = AuthState.AuthError(errorMsg, targetRole)
      return Result.failure(IllegalArgumentException(errorMsg))
    }

    // Check Room DAO first
    var user: UserEntity? = null
    try {
      user = hospitalDao.getUserByUsername(cleanUsername)
    } catch (_: Exception) {
      // Fallback
    }

    // Fallback to roster if not in DB yet
    if (user == null) {
      user = defaultStaffRoster.firstOrNull {
        it.username.equals(cleanUsername, ignoreCase = true)
      }
    }

    // Generic fallback for demo if password matches role convention
    if (user == null) {
      if (targetRole == UserRole.NURSE && cleanPassword == "nurse123") {
        user = UserEntity(
          userId = "NURSE-001",
          name = if (cleanUsername.contains("sarah", ignoreCase = true)) "Sarah Jenkins, RN" else "Ward Nurse ($cleanUsername)",
          username = cleanUsername,
          passwordHash = "nurse123",
          role = UserRole.NURSE,
          ward = "Ward 3",
          staffBadgeId = "STAFF-RFID-8829"
        )
      } else if (targetRole == UserRole.PHARMACIST && cleanPassword == "pharm123") {
        user = UserEntity(
          userId = "PHARM-001",
          name = if (cleanUsername.contains("arjun", ignoreCase = true)) "Dr. Arjun Mehta, PharmD" else "Pharmacist ($cleanUsername)",
          username = cleanUsername,
          passwordHash = "pharm123",
          role = UserRole.PHARMACIST,
          ward = "Central Pharmacy",
          staffBadgeId = "PHARM-RFID-0011"
        )
      }
    }

    if (user == null || user.passwordHash != cleanPassword) {
      val errorMsg = "Invalid username or password for ${targetRole.name}. Please check demo credentials."
      _authState.value = AuthState.AuthError(errorMsg, targetRole)
      return Result.failure(IllegalAccessException(errorMsg))
    }

    // Role verification
    if (user.role != targetRole) {
      val errorMsg = "Account '$cleanUsername' is assigned to ${user.role.name} role, not ${targetRole.name}."
      _authState.value = AuthState.AuthError(errorMsg, targetRole)
      return Result.failure(IllegalStateException(errorMsg))
    }

    // Persist if remember me is enabled
    prefs.edit().apply {
      putBoolean(KEY_REMEMBER_ME, rememberMe)
      if (rememberMe) {
        putString(KEY_SAVED_USERNAME, user.username)
        putString(KEY_SAVED_ROLE, user.role.name)
      } else {
        remove(KEY_SAVED_USERNAME)
        remove(KEY_SAVED_ROLE)
      }
      apply()
    }

    _currentUser.value = user
    _currentRole.value = user.role
    _authState.value = AuthState.Authenticated(user)
    return Result.success(user)
  }

  /**
   * Fast role switch for simulation / multi-role demonstration.
   */
  fun switchRole(targetRole: UserRole) {
    val switchedUser = defaultStaffRoster.firstOrNull { it.role == targetRole }
      ?: UserEntity(
        userId = if (targetRole == UserRole.NURSE) "NURSE-001" else "PHARM-001",
        name = if (targetRole == UserRole.NURSE) "Sarah Jenkins, RN" else "Dr. Arjun Mehta, PharmD",
        username = if (targetRole == UserRole.NURSE) "nurse_sarah" else "pharm_arjun",
        passwordHash = if (targetRole == UserRole.NURSE) "nurse123" else "pharm123",
        role = targetRole,
        ward = if (targetRole == UserRole.NURSE) "Ward 3" else "Central Pharmacy",
        staffBadgeId = if (targetRole == UserRole.NURSE) "STAFF-RFID-8829" else "PHARM-RFID-0011"
      )

    _currentUser.value = switchedUser
    _currentRole.value = targetRole
    _authState.value = AuthState.Authenticated(switchedUser)
  }

  /**
   * Clears session and returns to login screen.
   */
  fun logout() {
    val lastRole = _currentRole.value ?: UserRole.NURSE
    prefs.edit().apply {
      putBoolean(KEY_REMEMBER_ME, false)
      remove(KEY_SAVED_USERNAME)
      remove(KEY_SAVED_ROLE)
      apply()
    }
    _currentUser.value = null
    _currentRole.value = null
    _authState.value = AuthState.Unauthenticated(lastRole)
  }

  fun clearError() {
    val current = _authState.value
    if (current is AuthState.AuthError) {
      _authState.value = AuthState.Unauthenticated(current.selectedRole)
    }
  }

  fun getPermissions(): RolePermissions {
    val role = _currentRole.value ?: UserRole.NURSE
    return RolePermissions.forRole(role)
  }

  fun isNurse(): Boolean = _currentRole.value == UserRole.NURSE
  fun isPharmacist(): Boolean = _currentRole.value == UserRole.PHARMACIST

  companion object {
    private const val PREFS_AUTH_NAME = "hospital_auth_prefs"
    private const val KEY_REMEMBER_ME = "key_remember_me"
    private const val KEY_SAVED_USERNAME = "key_saved_username"
    private const val KEY_SAVED_ROLE = "key_saved_role"
  }
}
