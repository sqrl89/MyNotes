package com.alex.newnotes.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.RecyclerView
import com.alex.newnotes.R

abstract class SwipeCallbacks(val context: Context) : SimpleCallback(0, LEFT or RIGHT) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_note)
    private val doneIcon = ContextCompat.getDrawable(context, R.drawable.ic_done_check)
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
//                adapter.onItemMove(
//                    viewHolder.absoluteAdapterPosition,
//                    target.absoluteAdapterPosition
//                )
//                return true
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val p = Paint()
        var background = RectF(0F, 0F, 0F, 0F)
        val isCanceled = dX == 0f && !isCurrentlyActive
        if (isCanceled) {
            clearCanvas(
                c,
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false)
            return
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX > 0) {
                p.color = ContextCompat.getColor(context, R.color.mark_note_color)
                background = RectF(
                    itemView.left.toFloat(),
                    itemView.top.toFloat(),
                    itemView.left.toFloat() + dX + 35,
                    itemView.bottom.toFloat()
                )
                c.drawRoundRect(background, 20F,20F, p)
                val intrinsicWidth = doneIcon?.intrinsicWidth
                val intrinsicHeight = doneIcon?.intrinsicHeight
                val doneIconMargin = (itemHeight - intrinsicHeight!!) / 2
                val doneIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val doneIconLeft = itemView.left + doneIconMargin
                val doneIconRight = itemView.left + doneIconMargin + intrinsicWidth!!
                val doneIconBottom = doneIconTop + intrinsicHeight
                doneIcon?.setBounds(doneIconLeft, doneIconTop, doneIconRight, doneIconBottom)
                doneIcon?.draw(c)
            } else if (dX < 0) {
                p.color = ContextCompat.getColor(context, R.color.delete_note_color)
                background = RectF(
                    itemView.right.toFloat() + dX.toInt() - 35,
                    itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat()
                )
                c.drawRoundRect(background, 20F,20F, p)
                val intrinsicWidth = deleteIcon?.intrinsicWidth
                val intrinsicHeight = deleteIcon?.intrinsicHeight
                val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight!!) / 2
                val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
                val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth!!
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom = deleteIconTop + intrinsicHeight
                deleteIcon?.setBounds(
                    deleteIconLeft,
                    deleteIconTop,
                    deleteIconRight,
                    deleteIconBottom
                )
                deleteIcon?.draw(c)
            } else {
                background.set(0F, 0F, 0F, 0F)
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}