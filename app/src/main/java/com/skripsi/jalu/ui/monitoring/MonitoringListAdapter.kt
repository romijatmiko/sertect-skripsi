package com.skripsi.jalu.ui.monitoring

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skripsi.jalu.R
import com.skripsi.jalu.data.MonitoringData

class MonitoringListAdapter(
    private val onPredictClick: (MonitoringData) -> Unit
) : RecyclerView.Adapter<MonitoringListAdapter.ViewHolder>() {
    private var monitoringList: List<MonitoringData> = emptyList()
    private var selectedMonitoring: MonitoringData? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJudulLaporan: TextView = view.findViewById(R.id.tvJudulLaporan)
        val tvTanggalLaporan: TextView = view.findViewById(R.id.tvTanggalLaporan)
        val btnPrediksi: Button = view.findViewById(R.id.btnPrediksi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_monitoring, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val monitoring = monitoringList[position]
        holder.tvJudulLaporan.text = monitoring.judulMonitoring
        holder.tvTanggalLaporan.text = monitoring.tanggalMonitoring
        holder.btnPrediksi.setOnClickListener {
            selectedMonitoring = monitoring
            onPredictClick(monitoring)
        }
    }

    override fun getItemCount() = monitoringList.size

    fun updateData(newList: List<MonitoringData>) {
        monitoringList = newList
        notifyDataSetChanged()
    }

    fun getSelectedMonitoring(): MonitoringData? {
        return selectedMonitoring
    }
}
