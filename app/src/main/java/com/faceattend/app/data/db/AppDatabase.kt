package com.faceattend.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.faceattend.app.data.entities.AttendanceRecord
import com.faceattend.app.data.entities.Staff

@Database(
    entities = [Staff::class, AttendanceRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun staffDao(): StaffDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: build(context.applicationContext).also { instance = it }
        }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "faceattend.db")
                .addCallback(seedCallback)
                .build()

        // Demo staff so the app is usable immediately after install; their login
        // passwords live in com.faceattend.app.data.Credentials.
        private val seedCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()
                listOf(
                    "EMP001" to "Aarav Sharma",
                    "EMP002" to "Priya Nair"
                ).forEach { (employeeId, name) ->
                    db.execSQL(
                        "INSERT INTO staff (employeeId, name, faceImagePath, embeddingJson, createdAt) " +
                            "VALUES (?, ?, NULL, NULL, ?)",
                        arrayOf(employeeId, name, now)
                    )
                }
            }
        }
    }
}
