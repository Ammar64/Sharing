package com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter.viewHolders

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.R
import com.ammar.sharing.common.utils.Utils
import com.ammar.sharing.nativebackend.TransferOperation
import com.ammar.sharing.nativebackend.TransferStatus
import java.util.Locale
import kotlin.math.roundToInt

class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var fileNameTV: TextView = itemView.findViewById(R.id.TV_SharedFileName)
    var stopB: AppCompatImageButton = itemView.findViewById(R.id.B_StopSharing)
    var operationTV: TextView = itemView.findViewById(R.id.TV_OperationType)
    var transferInfoTV: TextView = itemView.findViewById(R.id.TV_FileTransferInfo)
    var fileProgressPB: ProgressBar = itemView.findViewById(R.id.PB_SharedFileProgress)
    var fileProgressTV: TextView = itemView.findViewById(R.id.TV_SharedFileProgress)

    private val handler = Handler(Looper.getMainLooper())

    fun setup(operation: TransferOperation) {
        setFileName(operation.itemName)
        setFileTransferInfo(operation)
        handler.post { setProgress(operation) }
        setOperationText(operation)
        setClickListeners(operation)
    }

    private fun setOperationText(operation: TransferOperation) {
        val opType = operation.transferType
        val operationText: String?
        val ctx = itemView.context
        val username = operation.userName

        when (opType) {
            TransferOperation.DOWNLOAD -> operationText =
                when (operation.status) {
                    TransferStatus.Completed -> ctx.getString(
                        R.string.sending_to_user_done,
                        username,
                        Utils.getFormattedTime(operation.timeToComplete!!.inWholeMilliseconds)
                    )
                    TransferStatus.Failed -> ctx.getString(
                        R.string.sending_to_user_stopped, username
                    )
                    else -> ctx.getString(R.string.sending_to_user, username)
                }

            TransferOperation.UPLOAD -> operationText =
                when(operation.status) {
                    TransferStatus.Completed -> ctx.getString(
                        R.string.receiving_from_user_done,
                        username,
                        Utils.getFormattedTime(operation.timeToComplete!!.inWholeMilliseconds)
                    )
                    TransferStatus.Failed -> ctx.getString(
                        R.string.receiving_from_user_stopped, username
                    )
                    else -> ctx.getString(R.string.receiving_from_user, username)
                }

            else -> throw RuntimeException("UnknownOP")
        }
        operationTV.text = operationText
    }

    private fun setFileName(fileName: String?) {
        fileNameTV.text = fileName
    }

    private fun setFileTransferInfo(operation: TransferOperation) {
        when (operation.status) {
            TransferStatus.Completed -> {
                transferInfoTV.setText(R.string.completed)
                return
            }

            TransferStatus.Failed -> {
                transferInfoTV.setText(R.string.stopped)
                return
            }

            else -> {}
        }

        val loaded = Utils.getFormattedSize(operation.progress)
        val total = Utils.getFormattedSize(operation.total)
        val bytesPerSecond = Utils.getFormattedSize(operation.speed.toLong())

        transferInfoTV.text = String.format(
            Locale.ENGLISH,
            "%s / %s   (%s/S)",
            loaded,
            total,
            bytesPerSecond
        )
    }

    private fun setProgress(operation: TransferOperation) {
        if (operation.status != TransferStatus.InProgress) {
            stopB.setImageResource(R.drawable.icon_minus)
            fileProgressTV.text = ""
            fileProgressTV.visibility = View.INVISIBLE

            fileProgressPB.isIndeterminate = false
            fileProgressPB.progress = 100
            fileProgressPB.setPaddingRelative(0, 0, 0, 0)

            val c = when (operation.status) {
                TransferStatus.Completed -> Color.GREEN
                TransferStatus.Failed -> Color.RED
                else -> throw RuntimeException("Invalid progress status. progress is " + operation.progress)
            }
            DrawableCompat.setTint(fileProgressPB.progressDrawable, c)
            return
        } else stopB.setImageResource(R.drawable.icon_x)

        // todo: we will support indeterminate downloads later
        if (true) {
            val percentage =
                (operation.progress.toDouble() / operation.total.toDouble() * 100.0).roundToInt()

            fileProgressTV.visibility = View.VISIBLE
            fileProgressTV.text = String.format(Locale.ENGLISH, "%d%%", percentage)
            DrawableCompat.setTint(fileProgressPB.progressDrawable, Color.CYAN)
            fileProgressPB.isIndeterminate = false
            fileProgressPB.progress = percentage
            fileProgressPB.setPaddingRelative(0, 0, Utils.dpToPx(8.0f).toInt(), 0)
        } else {
            //            fileProgressTV.setVisibility(View.INVISIBLE);
//            fileProgressPB.setPaddingRelative(0, 0, 0, 0);
//            DrawableCompat.setTint(fileProgressPB.getProgressDrawable(), Color.CYAN);
//
//            if (manager.getLoaded() == ProgressManager.COMPLETED) {
//                fileProgressPB.setIndeterminate(false);
//                fileProgressPB.setProgress(100);
//            } else {
//                fileProgressPB.setProgress(0);
//                fileProgressPB.setIndeterminate(true);
//            }
        }
    }

    private fun setClickListeners(operation: TransferOperation) {
        if (operation.transferType == TransferOperation.UPLOAD && operation.status == TransferStatus.Completed) {
            itemView.isClickable = true
            itemView.setFocusable(true)
            itemView.setOnClickListener { _: View? ->
                // todo: supported clicking on upload items when they complete
            }
        } else {
            itemView.isClickable = false
            itemView.setFocusable(false)
            itemView.setOnClickListener(null)
        }

        stopB.setOnClickListener { _: View? ->
            // support stopping
        }
    }
}
