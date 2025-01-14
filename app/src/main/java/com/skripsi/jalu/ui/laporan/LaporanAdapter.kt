package com.skripsi.jalu.ui.laporan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skripsi.jalu.data.LaporanResponse
import com.skripsi.jalu.databinding.ItemLaporanBinding

class LaporanAdapter(private val onItemClick: (String) -> Unit) :
    ListAdapter<LaporanResponse, LaporanAdapter.LaporanViewHolder>(LaporanDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaporanViewHolder {
        val binding = ItemLaporanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LaporanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LaporanViewHolder, position: Int) {
        val laporan = getItem(position)
        holder.bind(laporan)
    }

    inner class LaporanViewHolder(private val binding: ItemLaporanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(laporan: LaporanResponse) {
            binding.titleTextView.text = "Beikut adalah Hasil Prediksinya , Ayo Cek Detailnya Sekarang"
            binding.dateTextView.text = laporan.tanggal

            val words = laporan.serapan_words_info.take(3)
            binding.word1Button.apply {
                text = words.getOrNull(0)?.kata ?: ""
                visibility = if (words.size > 0) View.VISIBLE else View.GONE
            }
            binding.word2Button.apply {
                text = words.getOrNull(1)?.kata ?: ""
                visibility = if (words.size > 1) View.VISIBLE else View.GONE
            }
            binding.moreWordsButton.apply {
                text = if (laporan.serapan_words_info.size > 2) "+${laporan.serapan_words_info.size - 2}" else ""
                visibility = if (laporan.serapan_words_info.size > 2) View.VISIBLE else View.GONE
            }

            binding.detailButton.setOnClickListener {
                onItemClick(laporan.id)
            }
        }
    }

    class LaporanDiffCallback : DiffUtil.ItemCallback<LaporanResponse>() {
        override fun areItemsTheSame(oldItem: LaporanResponse, newItem: LaporanResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LaporanResponse, newItem: LaporanResponse): Boolean {
            return oldItem == newItem
        }
    }
}