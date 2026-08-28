package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    UserEntity::class,
    PatientEntity::class,
    MedicineEntity::class,
    OrderEntity::class,
    OrderItemEntity::class,
    RobotStatusEntity::class,
    DeliveryLogEntity::class,
    NotificationEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun hospitalDao(): HospitalDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "hospital_robot_database"
        ).addCallback(AppDatabaseCallback(scope))
         .fallbackToDestructiveMigration()
         .build()
        INSTANCE = instance
        instance
      }
    }
  }

  private class AppDatabaseCallback(
    private val scope: CoroutineScope
  ) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
      super.onCreate(db)
      INSTANCE?.let { database ->
        scope.launch(Dispatchers.IO) {
          populateInitialDatabase(database.hospitalDao())
        }
      }
    }
  }
}

suspend fun populateInitialDatabase(dao: HospitalDao) {
  // Users
  val initialUsers = listOf(
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
    )
  )
  dao.insertUsers(initialUsers)

  // Patients
  val initialPatients = listOf(
    PatientEntity("P1024", "Ravi Kumar", "Ward 3", "B-12", 45, "Post-op Recovery", "2026-08-26"),
    PatientEntity("P1025", "Ananya Sharma", "Ward 3", "B-04", 32, "Acute Bronchitis", "2026-08-27"),
    PatientEntity("P1026", "Vikram Patel", "ICU Ward", "ICU-02", 58, "Cardiac Observation", "2026-08-25"),
    PatientEntity("P1027", "Meera Nair", "Ward 5", "B-09", 28, "Gastroenteritis", "2026-08-27"),
    PatientEntity("P1028", "Rajesh Gupta", "Ward 3", "B-15", 62, "Type 2 Diabetes / Wound Care", "2026-08-24")
  )
  dao.insertPatients(initialPatients)

  // Medicines from requirements
  val initialMedicines = listOf(
    MedicineEntity("MED01", "Paracetamol 500 mg", "500 mg", "tablet", 2.0, 500, "Analgesic"),
    MedicineEntity("MED02", "Amoxicillin 500 mg", "500 mg", "capsule", 8.0, 320, "Antibiotic"),
    MedicineEntity("MED03", "Cetirizine 10 mg", "10 mg", "tablet", 3.0, 240, "Antihistamine"),
    MedicineEntity("MED04", "Azithromycin 500 mg", "500 mg", "tablet", 12.0, 150, "Antibiotic"),
    MedicineEntity("MED05", "Pantoprazole 40 mg", "40 mg", "tablet", 5.0, 400, "Antacid / PPI"),
    MedicineEntity("MED06", "Ibuprofen 400 mg", "400 mg", "tablet", 4.0, 280, "NSAID"),
    MedicineEntity("MED07", "Metformin 500 mg", "500 mg", "tablet", 3.0, 350, "Antidiabetic"),
    MedicineEntity("MED08", "ORS (Oral Rehydration)", "Standard", "packet", 20.0, 180, "Electrolyte"),
    MedicineEntity("MED09", "Insulin Glargine", "100 IU/ml", "vial", 150.0, 60, "Diabetes Injectable"),
    MedicineEntity("MED10", "Cefixime 200 mg", "200 mg", "tablet", 10.0, 200, "Antibiotic")
  )
  dao.insertMedicines(initialMedicines)

  // Robots
  val initialRobots = listOf(
    RobotStatusEntity(
      robotId = "R01",
      robotName = "MedBot Alpha (R01)",
      currentStatus = RobotState.IDLE,
      currentOrderId = "",
      destinationWard = "",
      destinationBed = "",
      currentCheckpoint = "PHARMACY",
      batteryPercent = 96,
      hasObstacle = false,
      obstacleMessage = "",
      ultrasonicDistanceCm = 150.0f,
      rfidReaderActive = true,
      isHardwareConnected = false
    ),
    RobotStatusEntity(
      robotId = "R02",
      robotName = "MedBot Beta (R02)",
      currentStatus = RobotState.IDLE,
      currentOrderId = "",
      destinationWard = "",
      destinationBed = "",
      currentCheckpoint = "PHARMACY",
      batteryPercent = 88,
      hasObstacle = false,
      obstacleMessage = "",
      ultrasonicDistanceCm = 180.0f,
      rfidReaderActive = true,
      isHardwareConnected = false
    )
  )
  dao.insertRobots(initialRobots)

  // Past Delivered Orders for Patient History and Billing demonstration
  val pastOrder1 = OrderEntity(
    orderId = "MED-2026-00098",
    patientId = "P1024",
    patientName = "Ravi Kumar",
    ward = "Ward 3",
    bed = "B-12",
    nurseId = "NURSE-001",
    nurseName = "Sarah Jenkins, RN",
    priority = OrderPriority.NORMAL,
    totalAmount = 40.0,
    requestTime = System.currentTimeMillis() - 86400000L * 2,
    requestTimeString = "26 Aug 2026, 10:15 AM",
    status = OrderStatus.DELIVERED,
    assignedRobotId = "R01",
    currentCheckpoint = "WARD_3",
    rfidTagVerified = "STAFF-RFID-8829",
    completionTimeString = "26 Aug 2026, 10:28 AM"
  )
  dao.insertOrder(pastOrder1)
  dao.insertOrderItems(listOf(
    OrderItemEntity(0, "MED-2026-00098", "MED01", "Paracetamol 500 mg", "500 mg", 10, 2.0, 20.0),
    OrderItemEntity(0, "MED-2026-00098", "MED08", "ORS (Oral Rehydration)", "Standard", 1, 20.0, 20.0)
  ))

  val pastOrder2 = OrderEntity(
    orderId = "MED-2026-00105",
    patientId = "P1024",
    patientName = "Ravi Kumar",
    ward = "Ward 3",
    bed = "B-12",
    nurseId = "NURSE-001",
    nurseName = "Sarah Jenkins, RN",
    priority = OrderPriority.NORMAL,
    totalAmount = 80.0,
    requestTime = System.currentTimeMillis() - 86400000L,
    requestTimeString = "27 Aug 2026, 02:40 PM",
    status = OrderStatus.DELIVERED,
    assignedRobotId = "R01",
    currentCheckpoint = "WARD_3",
    rfidTagVerified = "STAFF-RFID-8829",
    completionTimeString = "27 Aug 2026, 02:55 PM"
  )
  dao.insertOrder(pastOrder2)
  dao.insertOrderItems(listOf(
    OrderItemEntity(0, "MED-2026-00105", "MED02", "Amoxicillin 500 mg", "500 mg", 10, 8.0, 80.0)
  ))

  // Initial Notifications
  dao.insertNotification(
    NotificationEntity(
      recipientRole = "ALL",
      title = "System Ready",
      message = "Hospital Autonomous Medicine Delivery Robot Network is online.",
      type = "SYSTEM",
      formattedTime = "Today, 08:00 AM"
    )
  )
}
