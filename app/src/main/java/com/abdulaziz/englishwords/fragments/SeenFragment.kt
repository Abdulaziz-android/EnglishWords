package com.abdulaziz.englishwords.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.abdulaziz.englishwords.R
import com.abdulaziz.englishwords.adapter.SeenAdapter
import com.abdulaziz.englishwords.database.SeenItem
import com.abdulaziz.englishwords.database.WordDatabase
import com.abdulaziz.englishwords.databinding.FragmentSeenBinding


class SeenFragment : Fragment() {

    lateinit var binding:FragmentSeenBinding
    lateinit var database: WordDatabase
    lateinit var adapter:SeenAdapter
    private var list:ArrayList<SeenItem>?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_seen, container, false)

        database = WordDatabase.getInstance(requireContext())

        list = arrayListOf()
        if (database.seenDao().isExist()){
            list = database.seenDao().getAllItems() as ArrayList<SeenItem>
            binding.clearBtn.setOnClickListener {
                database.seenDao().deleteAll(list!!)
                list = arrayListOf()
                adapter = SeenAdapter(list!!)
                binding.rv.adapter = adapter
            }
        }
        if (list!!.isNotEmpty()){
            list!!.reverse()
        }
        adapter = SeenAdapter(list!!)
        binding.rv.adapter = adapter


        return binding.root
    }

}