package com.alex.newnotes.ui.main

import android.graphics.Color
import android.graphics.Paint

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.alex.newnotes.R
import com.alex.newnotes.data.database.Note
import com.alex.newnotes.databinding.NoteItemBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NoteAdapter(private val onItemClick: ItemClickListener) : Adapter<NoteAdapter.NoteHolder>() {
    val differ = AsyncListDiffer(this, NoteDiffUtilCallback())

    interface ItemClickListener {
        fun onItemClick(note: Note)
    }

    fun updateList(list: List<Note>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        return NoteHolder(
            NoteItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        holder.bind(differ.currentList[position])
        holder.itemView.animation =
            AnimationUtils.loadAnimation(holder.itemView.context, R.anim.main_rv_anim)
    }

    override fun getItemCount() = differ.currentList.size

    class NoteDiffUtilCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }

    inner class NoteHolder(private val binding: NoteItemBinding) : ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                differ.currentList[absoluteAdapterPosition]?.let { onItemClick.onItemClick(it) }
            }
        }

        fun bind(note: Note) {
            binding.apply {
                tvItemTitle.text = note.title
                itemCard.setCardBackgroundColor(Color.parseColor(note.color))
                if (note.uri != null) itemImage.visibility = View.VISIBLE
                if (note.completeBy?.isNotEmpty() == true) {
                    val completeBy = LocalDateTime.parse(
                        note.completeBy,
                        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                    )
                    if (LocalDateTime.now().isAfter(completeBy)) note.warning = true
                }
                if (note.completed) {
                    note.warning = false
                }
                if (note.warning) {
                    imWarning.visibility = View.VISIBLE
                    val warningAnim =
                        AnimationUtils.loadAnimation(imWarning.context, R.anim.warning_anim)
                    imWarning.startAnimation(warningAnim)
                } else imWarning.visibility = View.GONE
                if (note.completed) {
                    imDone.visibility = View.VISIBLE
                    tvItemTitle.paintFlags =
                        tvItemTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    imDone.visibility = View.GONE
                    tvItemTitle.paintFlags =
                        tvItemTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
        }
    }
}




