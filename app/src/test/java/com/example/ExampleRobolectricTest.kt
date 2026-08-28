package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.auth.AuthState
import com.example.data.auth.LocalAuthStateManager
import com.example.data.auth.RolePermissions
import com.example.data.model.UserRole
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Autonomous Medicine Delivery Robot", appName)
  }

  @Test
  fun `nurse login success and permissions`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context, TestScope())
    val authManager = LocalAuthStateManager(context, db.hospitalDao())

    val result = authManager.login("nurse_sarah", "nurse123", UserRole.NURSE, rememberMe = false)
    assertTrue(result.isSuccess)
    val user = result.getOrNull()
    assertNotNull(user)
    assertEquals(UserRole.NURSE, user?.role)
    assertTrue(authManager.isNurse())
    assertFalse(authManager.isPharmacist())

    val permissions = authManager.getPermissions()
    assertTrue(permissions.canCreatePrescriptionOrder)
    assertTrue(permissions.canVerifyBedsideRfid)
    assertFalse(permissions.canDispatchRobot)
    assertFalse(permissions.canManagePharmacyInventory)
  }

  @Test
  fun `pharmacist login success and permissions`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context, TestScope())
    val authManager = LocalAuthStateManager(context, db.hospitalDao())

    val result = authManager.login("pharm_arjun", "pharm123", UserRole.PHARMACIST, rememberMe = false)
    assertTrue(result.isSuccess)
    val user = result.getOrNull()
    assertNotNull(user)
    assertEquals(UserRole.PHARMACIST, user?.role)
    assertTrue(authManager.isPharmacist())
    assertFalse(authManager.isNurse())

    val permissions = authManager.getPermissions()
    assertFalse(permissions.canCreatePrescriptionOrder)
    assertTrue(permissions.canApprovePrescriptionQueue)
    assertTrue(permissions.canDispatchRobot)
    assertTrue(permissions.canManagePharmacyInventory)
  }

  @Test
  fun `login fails when role mismatch or wrong password`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context, TestScope())
    val authManager = LocalAuthStateManager(context, db.hospitalDao())

    // Attempt to log in nurse_sarah under PHARMACIST role
    val mismatchResult = authManager.login("nurse_sarah", "nurse123", UserRole.PHARMACIST, rememberMe = false)
    assertTrue(mismatchResult.isFailure)

    // Wrong password
    val wrongPassResult = authManager.login("nurse_sarah", "wrong_pass", UserRole.NURSE, rememberMe = false)
    assertTrue(wrongPassResult.isFailure)
  }

  @Test
  fun `logout resets authentication state`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context, TestScope())
    val authManager = LocalAuthStateManager(context, db.hospitalDao())

    authManager.login("nurse_sarah", "nurse123", UserRole.NURSE, rememberMe = false)
    assertTrue(authManager.authState.value is AuthState.Authenticated)

    authManager.logout()
    assertTrue(authManager.authState.value is AuthState.Unauthenticated)
    assertNull(authManager.currentUser.value)
  }
}

