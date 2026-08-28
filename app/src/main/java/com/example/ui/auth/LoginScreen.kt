package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.MedEmeraldTertiary
import com.example.ui.theme.MedNavySecondary
import com.example.ui.theme.MedTealPrimary

@Composable
fun LoginScreen(
  onLoginSuccess: (username: String, pass: String, role: UserRole, rememberMe: Boolean) -> Unit,
  errorMessage: String? = null,
  isLoading: Boolean = false
) {
  var selectedRole by remember { mutableStateOf(UserRole.NURSE) }
  var username by remember { mutableStateOf("nurse_sarah") }
  var password by remember { mutableStateOf("nurse123") }
  var passwordVisible by remember { mutableStateOf(false) }
  var rememberMe by remember { mutableStateOf(true) }

  val focusManager = LocalFocusManager.current

  val primaryRoleColor by animateColorAsState(
    targetValue = if (selectedRole == UserRole.NURSE) MedTealPrimary else Color(0xFF7C3AED),
    animationSpec = tween(300),
    label = "roleColor"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0B1329))
      .testTag("login_screen"),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .verticalScroll(rememberScrollState())
        .padding(vertical = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Hospital Brand Header
      Box(
        modifier = Modifier
          .size(68.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              colors = listOf(primaryRoleColor, Color(0xFF1E293B))
            )
          )
          .border(2.dp, primaryRoleColor.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (selectedRole == UserRole.NURSE) Icons.Default.LocalHospital else Icons.Default.LocalPharmacy,
          contentDescription = "Hospital Cross",
          tint = Color.White,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Autonomous Medicine Delivery Robot",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
      ) {
        Icon(
          imageVector = Icons.Default.PrecisionManufacturing,
          contentDescription = null,
          tint = primaryRoleColor,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "ESP32 Motor Controller • RFID RC522 Locker Security",
          fontSize = 11.sp,
          color = Color(0xFF94A3B8),
          fontFamily = FontFamily.Monospace
        )
      }

      // Authentication Card
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("login_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF162036))
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "Staff Access Portal",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "Select your medical department role to continue",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Role Switcher Segmented Tabs
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF0B1329))
              .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
              .padding(4.dp)
          ) {
            // Nurse Tab
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(9.dp))
                .background(if (selectedRole == UserRole.NURSE) MedTealPrimary else Color.Transparent)
                .clickable {
                  selectedRole = UserRole.NURSE
                  username = "nurse_sarah"
                  password = "nurse123"
                }
                .padding(vertical = 10.dp)
                .testTag("role_tab_nurse"),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.MedicalServices,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Nurse",
                  fontSize = 13.sp,
                  fontWeight = if (selectedRole == UserRole.NURSE) FontWeight.Bold else FontWeight.Medium,
                  color = Color.White
                )
              }
            }

            // Pharmacist Tab
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(9.dp))
                .background(if (selectedRole == UserRole.PHARMACIST) Color(0xFF7C3AED) else Color.Transparent)
                .clickable {
                  selectedRole = UserRole.PHARMACIST
                  username = "pharm_arjun"
                  password = "pharm123"
                }
                .padding(vertical = 10.dp)
                .testTag("role_tab_pharmacist"),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.LocalPharmacy,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Pharmacist",
                  fontSize = 13.sp,
                  fontWeight = if (selectedRole == UserRole.PHARMACIST) FontWeight.Bold else FontWeight.Medium,
                  color = Color.White
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Role Capability Banner
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = if (selectedRole == UserRole.NURSE) Color(0xFF0F2E3D) else Color(0xFF2C194D)
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (selectedRole == UserRole.NURSE) Icons.Default.Nfc else Icons.Default.Badge,
                contentDescription = null,
                tint = if (selectedRole == UserRole.NURSE) Color(0xFF38BDF8) else Color(0xFFA78BFA),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = if (selectedRole == UserRole.NURSE) "Ward & Bedside Privileges" else "Dispensary & Robot Dispatch Privileges",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = if (selectedRole == UserRole.NURSE)
                    "Request prescriptions, track transit, unlock RC522 bedside locker."
                  else
                    "Verify Rx queue, load robot cargo, dispatch R01, manage catalog.",
                  fontSize = 10.sp,
                  color = Color(0xFFCBD5E1),
                  lineHeight = 13.sp
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Username field
          OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Staff ID / Username", color = Color(0xFF94A3B8)) },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = primaryRoleColor)
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("username_input"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedBorderColor = primaryRoleColor,
              unfocusedBorderColor = Color(0xFF334155),
              focusedContainerColor = Color(0xFF0B1329),
              unfocusedContainerColor = Color(0xFF0B1329)
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Password field
          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color(0xFF94A3B8)) },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = primaryRoleColor)
            },
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = if (passwordVisible) "Hide password" else "Show password",
                  tint = Color(0xFF94A3B8)
                )
              }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
              focusManager.clearFocus()
              onLoginSuccess(username, password, selectedRole, rememberMe)
            }),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("password_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedBorderColor = primaryRoleColor,
              unfocusedBorderColor = Color(0xFF334155),
              focusedContainerColor = Color(0xFF0B1329),
              unfocusedContainerColor = Color(0xFF0B1329)
            )
          )

          // Remember Me Checkbox
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = rememberMe,
              onCheckedChange = { rememberMe = it },
              modifier = Modifier.testTag("remember_me_checkbox"),
              colors = CheckboxDefaults.colors(
                checkedColor = primaryRoleColor,
                uncheckedColor = Color(0xFF64748B),
                checkmarkColor = Color.White
              )
            )
            Text(
              text = "Remember this device session",
              fontSize = 12.sp,
              color = Color(0xFFCBD5E1),
              modifier = Modifier.clickable { rememberMe = !rememberMe }
            )
          }

          // Error Message Display
          AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            errorMessage?.let { error ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 8.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF451A1A))
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.ErrorOutline,
                  contentDescription = null,
                  tint = Color(0xFFF87171),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = error,
                  color = Color(0xFFFCA5A5),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Submit Login Button
          Button(
            onClick = {
              focusManager.clearFocus()
              onLoginSuccess(username, password, selectedRole, rememberMe)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("login_submit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = primaryRoleColor),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Authenticating Staff...", color = Color.White, fontWeight = FontWeight.Bold)
            } else {
              Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Log In as ${if (selectedRole == UserRole.NURSE) "Nurse Staff" else "Pharmacist"}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // One-Tap Quick Demo Credentials Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF162036).copy(alpha = 0.85f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.SmartToy,
              contentDescription = null,
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Quick Demo Credentials (Tap to Instant Login)",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF38BDF8)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Preset Nurse 1
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF0B1329))
              .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
              .clickable {
                selectedRole = UserRole.NURSE
                username = "nurse_sarah"
                password = "nurse123"
                onLoginSuccess("nurse_sarah", "nurse123", UserRole.NURSE, rememberMe)
              }
              .padding(10.dp)
              .testTag("quick_nurse_btn"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("👩‍⚕️ Sarah Jenkins, RN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
              Text("Ward 3 • nurse_sarah / nurse123", fontSize = 10.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
            }
            Text("Login as Nurse →", fontSize = 11.sp, color = MedTealPrimary, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Preset Pharmacist
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF0B1329))
              .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
              .clickable {
                selectedRole = UserRole.PHARMACIST
                username = "pharm_arjun"
                password = "pharm123"
                onLoginSuccess("pharm_arjun", "pharm123", UserRole.PHARMACIST, rememberMe)
              }
              .padding(10.dp)
              .testTag("quick_pharm_btn"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("💊 Dr. Arjun Mehta, PharmD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
              Text("Central Pharmacy • pharm_arjun / pharm123", fontSize = 10.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
            }
            Text("Login as Pharmacist →", fontSize = 11.sp, color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
