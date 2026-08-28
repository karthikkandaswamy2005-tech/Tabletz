package com.example.data.model

enum class UserRole {
  NURSE,
  PHARMACIST
}

enum class OrderPriority {
  LOW,
  NORMAL,
  URGENT;

  val displayName: String
    get() = when (this) {
      LOW -> "Low Priority"
      NORMAL -> "Normal Priority"
      URGENT -> "Urgent Priority"
    }
}

enum class OrderStatus {
  REQUEST_RECEIVED,
  PHARMACIST_ACCEPTED,
  MEDICINE_LOADED,
  ROBOT_DISPATCHED,
  EN_ROUTE,
  DESTINATION_REACHED,
  RFID_VERIFIED,
  DELIVERED,
  CANCELLED;

  val displayName: String
    get() = when (this) {
      REQUEST_RECEIVED -> "Request Received"
      PHARMACIST_ACCEPTED -> "Accepted"
      MEDICINE_LOADED -> "Medicine Loaded"
      ROBOT_DISPATCHED -> "Robot Dispatched"
      EN_ROUTE -> "En Route"
      DESTINATION_REACHED -> "Arrived / Waiting RFID"
      RFID_VERIFIED -> "RFID Verified ✓"
      DELIVERED -> "Delivered ✓"
      CANCELLED -> "Cancelled"
    }
}

enum class RobotState {
  IDLE,
  MEDICINE_LOADED,
  DISPATCHED,
  EN_ROUTE,
  OBSTACLE_DETECTED,
  WAITING,
  DESTINATION_REACHED,
  RFID_VERIFICATION_REQUIRED,
  DELIVERED,
  ERROR,
  RETURNING;

  val displayName: String
    get() = when (this) {
      IDLE -> "Idle in Pharmacy"
      MEDICINE_LOADED -> "Medicine Loaded"
      DISPATCHED -> "Dispatched"
      EN_ROUTE -> "En Route"
      OBSTACLE_DETECTED -> "⚠ Obstacle Detected"
      WAITING -> "Waiting"
      DESTINATION_REACHED -> "Destination Reached"
      RFID_VERIFICATION_REQUIRED -> "RFID Verification Required"
      DELIVERED -> "Delivered Successfully"
      ERROR -> "Robot Error"
      RETURNING -> "Returning to Pharmacy"
    }
}
