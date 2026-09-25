package com.faceattend.app.data

/**
 * Dummy auth, as permitted by the assignment. Staff sign in with their employee ID; the
 * shared demo password below stands in for a real identity provider.
 */
object Credentials {
    const val ADMIN_USERNAME = "admin"
    const val ADMIN_PASSWORD = "admin123"
    const val STAFF_PASSWORD = "staff123"
}

enum class UserRole { ADMIN, STAFF }
