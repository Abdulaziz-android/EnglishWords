package com.abdulaziz.englishwords.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.databinding.DataBindingUtil
import com.abdulaziz.englishwords.R
import com.abdulaziz.englishwords.databinding.WordAutocomplateRowBinding
import com.abdulaziz.englishwords.models.MyItem


class AutoCompleteAdapter(context: Context, wordList: ArrayList<MyItem>) :
    ArrayAdapter<MyItem>(context, 0, wordList) {

    var isWordExist = false
    val wordListFull = wordList
    var filtered: List<MyItem> = arrayListOf()

    override fun getCount(): Int = filtered.size

    override fun getItem(position: Int): MyItem = filtered[position]


    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemBinding =
            DataBindingUtil.inflate<WordAutocomplateRowBinding>(LayoutInflater.from(parent.context),
                R.layout.word_autocomplate_row,
                parent, false)
        val item: MyItem = getItem(position)
        itemBinding.wordTv.text = item.name
        if (!item.isExsist) itemBinding.downIv.visibility = View.VISIBLE

        return itemBinding.root
    }

    private val wordFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val result = FilterResults()
            isWordExist = false
            val suggestions: ArrayList<MyItem> = arrayListOf()
            if (constraint != null && wordListFull != null) {
                if (constraint.toString().isEmpty()) {
                    suggestions.addAll(wordListFull)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    wordListFull.forEachIndexed { index, it ->
                        if (it.name.lowercase().contains(filterPattern)) {
                            suggestions.add(it)
                            if (constraint.toString().lowercase() == it.name.lowercase()) {
                                isWordExist = true
                            }
                        }
                    }
                    if (!isWordExist) {
                        suggestions.add(0, MyItem(constraint.toString(), false))
                    }
                }

                result.values = suggestions
                result.count = suggestions.size
            }
            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            if (results != null && constraint.toString().isNotEmpty()) {
                if (results.values != null)
                    filtered = results.values as List<MyItem>
            }
            notifyDataSetInvalidated()
        }

        override fun convertResultToString(resultValue: Any): CharSequence {
            return (resultValue as MyItem).name
        }
    }

    override fun getFilter(): Filter {
        return wordFilter
    }

}