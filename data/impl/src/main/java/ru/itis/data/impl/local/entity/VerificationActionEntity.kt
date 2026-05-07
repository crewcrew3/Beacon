package ru.itis.data.impl.local.entity

import androidx.room.*

@Entity(
    tableName = "verification_actions",
    indices = [
        Index(value = ["incident_id", "user_id"], unique = true) // один голос от пользователя на инцидент
    ],
    foreignKeys = [
        ForeignKey(
            entity = IncidentEntity::class,
            parentColumns = ["id"],
            childColumns = ["incident_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class VerificationActionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "incident_id")
    val incidentId: Long,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "action_type")
    val actionType: String, // "CONFIRM" или "DISPUTE"

    @ColumnInfo(name = "timestamp_millis")
    val timestampMillis: Long = System.currentTimeMillis()
)