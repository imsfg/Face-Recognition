package com.faceattend.app.util

import android.content.Context
import com.faceattend.app.data.UserRole

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    val role: UserRole?
        get() = prefs.getString(KEY_ROLE, null)?.let(UserRole::valueOf)

    /** Row id of the signed-in staff member; meaningless for admin sessions. */
    val staffId: Long
        get() = prefs.getLong(KEY_STAFF_ID, -1L)

    fun signInAsAdmin() = prefs.edit().putString(KEY_ROLE, UserRole.ADMIN.name).apply()

    fun signInAsStaff(staffId: Long) = prefs.edit()
        .putString(KEY_ROLE, UserRole.STAFF.name)
        .putLong(KEY_STAFF_ID, staffId)
        .apply()

    fun signOut() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_ROLE = "role"
        const val KEY_STAFF_ID = "staff_id"
    }
}
