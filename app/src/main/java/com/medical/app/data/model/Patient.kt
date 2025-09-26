package com.medical.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val dateOfBirth: Date,
    val gender: String,
    val phoneNumber: String,
    val email: String? = null,
    val address: String? = null,
    val bloodType: String? = null,
    val allergies: String? = null,
    val notes: String? = null,
    val photoUrl: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    val fullName: String
        get() = "$firstName $lastName"
}
