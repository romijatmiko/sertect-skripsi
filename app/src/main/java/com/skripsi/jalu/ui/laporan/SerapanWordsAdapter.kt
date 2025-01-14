package com.skripsi.jalu.ui.laporan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skripsi.jalu.R
import com.skripsi.jalu.data.SerapanWordInfo
import com.skripsi.jalu.databinding.ItemDetailKataSerapanBinding


class SerapanWordsAdapter : ListAdapter<SerapanWordInfo, SerapanWordsAdapter.WordViewHolder>(SerapanWordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ItemDetailKataSerapanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WordViewHolder(private val binding: ItemDetailKataSerapanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(wordInfo: SerapanWordInfo) {
            binding.wordTitle.text = wordInfo.kata
            binding.wordDetail.text = "Asal Kata: ${wordInfo.bentuk_kata}\nAsal Bahasa: ${wordInfo.asal_kata}"
            binding.arti.text = "Artinya: ${wordInfo.arti}"
            binding.wordExplanation.text = "Sumber: ${wordInfo.sumber}"


            binding.detailContainer.visibility = View.GONE
            binding.iconDropdown.setOnClickListener {
                binding.detailContainer.visibility = if (binding.detailContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                binding.iconDropdown.setImageResource(if (binding.detailContainer.visibility == View.VISIBLE) R.drawable.arrow_up_fill else R.drawable.arrow_down_fill)
            }
        }
    }

    class SerapanWordDiffCallback : DiffUtil.ItemCallback<SerapanWordInfo>() {
        override fun areItemsTheSame(oldItem: SerapanWordInfo, newItem: SerapanWordInfo): Boolean {
            return oldItem.kata == newItem.kata
        }

        override fun areContentsTheSame(oldItem: SerapanWordInfo, newItem: SerapanWordInfo): Boolean {
            return oldItem == newItem
        }
    }
}
