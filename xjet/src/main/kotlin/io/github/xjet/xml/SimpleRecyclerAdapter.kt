package io.github.xjet.xml

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Minimal RecyclerView base adapter for the XML flavor. Subclass it, provide a
 * layout + holder, and it handles paging-ish data mutations and item clicks.
 */
abstract class SimpleRecyclerAdapter<T, VH : RecyclerView.ViewHolder>(
    layoutId: Int,
    inflater: LayoutInflater,
    private val onItemClick: ((item: T, position: Int) -> Unit)? = null,
) : RecyclerView.Adapter<VH>() {

    private val layoutId = layoutId
    private val inflater = inflater
    private val items = mutableListOf<T>()

    fun setData(list: List<T>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun addData(list: List<T>) {
        val start = items.size
        items.addAll(list)
        notifyItemRangeInserted(start, list.size)
    }

    fun clearData() {
        items.clear()
        notifyDataSetChanged()
    }

    fun item(position: Int): T = items[position]
    fun data(): List<T> = items.toList()

    final override fun getItemCount(): Int = items.size

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view: View = inflater.inflate(layoutId, parent, false)
        val holder = onCreateHolder(parent, view)
        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onItemClick?.invoke(items[pos], pos)
        }
        return holder
    }

    final override fun onBindViewHolder(holder: VH, position: Int) {
        onBind(holder, items[position], position)
    }

    abstract fun onCreateHolder(parent: ViewGroup, view: View): VH
    abstract fun onBind(holder: VH, item: T, position: Int)
}
