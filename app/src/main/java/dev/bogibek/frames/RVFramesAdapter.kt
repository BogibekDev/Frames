package dev.bogibek.frames

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RVFramesAdapter(var list: ArrayList<Frame>) :
    RecyclerView.Adapter<RVFramesAdapter.RvFramesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvFramesViewHolder {
        val inflater =
            LayoutInflater.from(parent.context).inflate(R.layout.item_frame, parent, false)
        return RvFramesViewHolder(inflater.rootView)
    }

    override fun onBindViewHolder(holder: RvFramesViewHolder, position: Int) {
        holder.img.setImageBitmap(list[position].imgBitmap)
        holder.tvTime.text = list[position].time
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class RvFramesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.img)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
    }
}