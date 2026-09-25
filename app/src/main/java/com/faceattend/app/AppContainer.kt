package com.faceattend.app

import android.content.Context
import com.faceattend.app.data.db.AppDatabase
import com.faceattend.app.data.repository.AttendanceRepository
import com.faceattend.app.data.repository.StaffRepository
import com.faceattend.app.face.FaceRecognizer
import com.faceattend.app.util.ImageFileStore
import com.faceattend.app.util.LocationHelper
import com.faceattend.app.util.SessionManager

/** Manual DI — a DI framework would be more machinery than this app needs. */
class AppContainer(context: Context) {
    private val database = AppDatabase.get(context)

    val staffRepository = StaffRepository(database.staffDao())
    val attendanceRepository = AttendanceRepository(database.attendanceDao())
    val sessionManager = SessionManager(context)
    val imageFileStore = ImageFileStore(context)
    val locationHelper = LocationHelper(context)

    // Lazy: loading the FaceNet model costs ~23 MB, so defer it until a capture screen opens.
    val faceRecognizer: FaceRecognizer by lazy { FaceRecognizer(context) }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as FaceAttendApp).container
