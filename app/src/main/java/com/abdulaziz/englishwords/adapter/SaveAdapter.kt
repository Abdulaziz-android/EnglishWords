package com.abdulaziz.englishwords.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulaziz.englishwords.database.WordEntity
import com.abdulaziz.englishwords.databinding.ItemSaveBinding

class SaveAdapter(val list: List<WordEntity>, val listener: OnClickListener) : RecyclerView.Adapter<SaveAdapter.SearchVH>() {



    inner class SearchVH(private val itemBinding: ItemSaveBinding): RecyclerView.ViewHolder(itemBinding.root){
        @SuppressLint("SetTextI18n")
        fun onBind(wordEntity: WordEntity, position: Int){
            itemBinding.wordTv.text = wordEntity.word
            itemBinding.definitionTv.text = "Definition: ${wordEntity.definition}"
            itemBinding.exampleTv.text = "Example: ${wordEntity.example}"
            itemBinding.numberTv.text = (position.plus(1)).toString()

            itemBinding.saveCard.setOnClickListener {
                listener.onSaveCardClickListener(wordEntity, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchVH {
        return SearchVH(ItemSaveBinding.inflate(LayoutInflater.from(parent.context), parent, false))
      }

    override fun onBindViewHolder(holder: SearchVH, position: Int) {
        holder.onBind(list[position], position)
    }

    override fun getItemCount(): Int = list.size

    interface OnClickListener{
        fun onSaveCardClickListener(wordEntity: WordEntity, position: Int)
    }
}