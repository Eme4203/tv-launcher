package com.tvlauncher

import android.content.ComponentName
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppEntry(val label: String, val component: ComponentName, val info: ResolveInfo)

class AppAdapter(
    private val pm: PackageManager,
    private val onClick: (AppEntry) -> Unit,
    private val onLongClick: (AppEntry) -> Unit
) : RecyclerView.Adapter<AppAdapter.VH>() {

    private var items: List<AppEntry> = emptyList()

    fun submit(newItems: List<AppEntry>) {
        // Evita perder el foco si la lista no cambió.
        if (items.map { it.component } == newItems.map { it.component }) return
        items = newItems
        @Suppress("NotifyDataSetChanged")
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = items[position]
        holder.label.text = app.label

        val ai = app.info.activityInfo
        val banner = ai.loadBanner(pm) ?: ai.applicationInfo.loadBanner(pm)
        if (banner != null) {
            holder.icon.setPadding(0, 0, 0, 0)
            holder.icon.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.icon.setImageDrawable(banner)
        } else {
            val p = (14 * holder.itemView.resources.displayMetrics.density).toInt()
            holder.icon.setPadding(p, p, p, p)
            holder.icon.scaleType = ImageView.ScaleType.FIT_CENTER
            holder.icon.setImageDrawable(app.info.loadIcon(pm))
        }

        holder.itemView.setOnClickListener { onClick(app) }
        holder.itemView.setOnLongClickListener { onLongClick(app); true }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val label: TextView = view.findViewById(R.id.label)

        init {
            view.setOnFocusChangeListener { v, focused ->
                val s = if (focused) 1.12f else 1f
                v.animate().scaleX(s).scaleY(s).setDuration(120).start()
                v.translationZ = if (focused) 8f else 0f
            }
        }
    }
}
