package com.dicoding.asclepius.view

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.asclepius.databinding.ItemHistoryBinding
import com.dicoding.asclepius.entity.PredictionHistory


class HistoryAdapter : ListAdapter<PredictionHistory, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {

        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(history: PredictionHistory) {

            Glide.with(binding.imageView.context)
                .load(Uri.parse(history.imageUri))
                .into(binding.imageView)


            binding.textViewResult.text =
                if (history.label == "Cancer") "Cancer Detected" else "No Cancer"
            binding.textViewConfidence.text = "Confidence: ${history.confidenceScore}"
        }
        }

    class HistoryDiffCallback : DiffUtil.ItemCallback<PredictionHistory>() {
        override fun areItemsTheSame(oldItem: PredictionHistory, newItem: PredictionHistory): Boolean {
            return oldItem.imageUri == newItem.imageUri
        }

        override fun areContentsTheSame(oldItem: PredictionHistory, newItem: PredictionHistory): Boolean {
            return oldItem == newItem
        }
    }
}
