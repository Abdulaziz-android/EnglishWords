package com.abdulaziz.englishwords.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulaziz.englishwords.database.SeenItem
import com.abdulaziz.englishwords.databinding.ItemSeenBinding

class SeenAdapter(val list: List<SeenItem>) : RecyclerView.Adapter<SeenAdapter.SeenVH>() {


    inner class SeenVH(private val itemBinding: ItemSeenBinding):RecyclerView.ViewHolder(itemBinding.root){
        fun onBind(item: SeenItem){
            itemBinding.wordTv.text = item.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeenVH {
        return SeenVH(ItemSeenBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: SeenVH, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size
}