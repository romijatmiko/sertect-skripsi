package com.skripsi.jalu.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skripsi.jalu.databinding.ItemNotificationBinding
import com.skripsi.jalu.data.NotificationItem

class NotificationAdapter(private var notifications: List<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: NotificationItem) {
            binding.notificationMessage.text = notification.judulNotification
            binding.notificationTime.text = notification.tanggalNotification
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<NotificationItem>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}