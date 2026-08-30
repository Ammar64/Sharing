package com.ammar.sharing.nativebackend

import androidx.annotation.Keep
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Keep
data class TransferOperation(
    val itemName: String,
    val progress: Long,
    val total: Long,  // speed. bytes per milliseconds

    val speed: Double,
    val avgSpeed: Double,

    val expectedRemainingTime: Duration,

    val status: TransferStatus,
    val err: String?, // always null unless the rust TransferStatus enum is the Failed variant

    val timeToComplete: Duration?, // the total time the operation took to complete

    val transferType: Int,  // 0 for download, 1 for upload
    val userName: String
) {
    @Keep
    companion object {
        const val DOWNLOAD = 0;
        const val UPLOAD = 1;

        @JvmStatic
        @Suppress("unused")
        fun newUsingIntDuration(
            itemName: String,
            progress: Long,
            total: Long,

            speed: Double,
            avgSpeed: Double,

            expectedRemainingTime: Long,

            status: Int,
            err: String?,

            timeToComplete: Long,

            transferType: Int,
            userName: String
        ) = TransferOperation(
            itemName,
            progress,
            total,
            speed,
            avgSpeed,
            expectedRemainingTime.seconds,
            TransferStatus.fromInt(status),
            err,
            if (timeToComplete != -1L) timeToComplete.seconds else null,
            transferType,
            userName
        )
    }
}

enum class TransferStatus {
    InProgress,
    Completed,
    CancelledByUser,
    Failed;

    companion object {
        fun fromInt(value: Int): TransferStatus {
            return entries.getOrElse(value) { throw RuntimeException() }
        }
    }
}