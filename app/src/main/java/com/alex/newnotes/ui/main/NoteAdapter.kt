package com.alex.newnotes.ui.main

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.alex.newnotes.R
import com.alex.newnotes.data.database.Note
import com.alex.newnotes.databinding.RcItemBinding
import java.util.Collections

class NoteAdapter(private val onItemClick: ItemClickListener) : Adapter<NoteAdapter.NoteHolder>() {
    val differ = AsyncListDiffer(this, NoteDiffUtilCallback())

    interface ItemClickListener {
        fun onItemClick(note: Note)
    }

//    var modList = differ.currentList.toMutableList()

    fun updateList(list: List<Note>) {
//        modList = list.toMutableList()
//        differ.submitList(modList)
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        return NoteHolder(
            RcItemBinding.inflate(
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

    inner class NoteHolder(private val binding: RcItemBinding) : ViewHolder(binding.root) {
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

//    fun onItemMove(from: Int, to: Int): Boolean {
//        if (from < to) {
//            for (i in from until to) {
//                Collections.swap(modList, i, i + 1)
//            }
//        } else {
//            for (i in from downTo to + 1) {
//                Collections.swap(modList, i, i - 1)
//            }
//        }
//        notifyItemMoved(from, to)
//        return true
//    }

}




