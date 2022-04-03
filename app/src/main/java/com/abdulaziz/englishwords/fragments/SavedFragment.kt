package com.abdulaziz.englishwords.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.abdulaziz.englishwords.R
import com.abdulaziz.englishwords.adapter.SaveAdapter
import com.abdulaziz.englishwords.database.WordDatabase
import com.abdulaziz.englishwords.database.WordEntity
import com.abdulaziz.englishwords.databinding.FragmentSavedBinding

class SavedFragment : Fragment() {

    lateinit var binding:FragmentSavedBinding
    lateinit var database: WordDatabase
    private var list:ArrayList<WordEntity>?=null
    lateinit var adapter: SaveAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_saved, container, false)

        list = arrayListOf()
        database = WordDatabase.getInstance(binding.root.context)
        list = database.wordDao().getAllSavedWords() as ArrayList<WordEntity>
        checkList()
        adapter = SaveAdapter(list!!, object : SaveAdapter.OnClickListener{
            override fun onSaveCardClickListener(wordEntity: WordEntity, position:Int) {
                wordEntity.save = false
                database.wordDao().updateWord(wordEntity)
                list!!.remove(wordEntity)
                binding.rv.adapter?.notifyItemRemoved(position)
                binding.rv.adapter?.notifyItemRangeChanged(position, list!!.size - 1)
                checkList()
            }
        })

        binding.rv.adapter = adapter

        binding.rv.addItemDecoration(DividerItemDecoration(binding.rv.context,
            DividerItemDecoration.VERTICAL))

        return binding.root
    }

    private fun checkList() {
        if (list!!.isEmpty()){
            binding.rv.visibility = View.GONE
            binding.notFoundLayout.visibility = View.VISIBLE
        }
    }

}