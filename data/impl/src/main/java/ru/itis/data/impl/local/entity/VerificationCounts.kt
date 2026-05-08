package ru.itis.data.impl.local.entity

import androidx.room.ColumnInfo

data class VerificationCounts(
    @ColumnInfo(name = "confirmCount")
    val confirmCount: Long = 0,

    @ColumnInfo(name = "disputeCount")
    val disputeCount: Long = 0
)