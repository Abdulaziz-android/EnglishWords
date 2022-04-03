package com.abdulaziz.englishwords.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.content.Intent
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.abdulaziz.englishwords.databinding.ItemDialogWelcomeBinding
import com.abdulaziz.englishwords.R
import com.abdulaziz.englishwords.MainActivity
import com.abdulaziz.englishwords.WelcomeActivity
import com.abdulaziz.englishwords.database.WordDatabase
import com.abdulaziz.englishwords.database.WordEntity
import com.abdulaziz.englishwords.databinding.FragmentPagerBinding
import com.abdulaziz.englishwords.models.Word
import com.abdulaziz.englishwords.retrofit.ApiClient
import com.abdulaziz.englishwords.util.NetworkHelper
import com.abdulaziz.englishwords.viewmodels.ConnectionLiveData
import kotlinx.coroutines.*

private const val ARG_PARAM1 = "param1"

class PagerFragment : Fragment() {

    private var param1: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
        }
    }

    private lateinit var binding: FragmentPagerBinding
    private lateinit var database: WordDatabase
    private lateinit var connectionLiveData: ConnectionLiveData
    private lateinit var networkHelper: NetworkHelper
    private lateinit var alertDialog: AlertDialog

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_pager, container, false)
        database = WordDatabase.getInstance(binding.root.context)

        setUpDialog(container)

        networkHelper = NetworkHelper(binding.root.context)
        connectionLiveData = ConnectionLiveData(requireActivity().application)
        connectionLiveData.observe(requireActivity()) { isConnect ->
            if (param1 == 3) {
                if (isConnect) {
                        loadData()
                        alertDialog.dismiss()
                } else {
                    alertDialog.show()
                }
            }
        }

        binding.progressBar.scaleY = 3f
        when (param1) {
            0 -> {
                binding.textview.text = "Welcome English Words app"
            }
            1 -> {
                binding.textview.text = "Learn a new word every day"
            }
            2 -> {
                binding.textview.text = "‘Recent’ list to easily review looked-up words"
            }
            3 -> {
                if (networkHelper.isNetworkConnected()) {
                    loadData()
                }else alertDialog.show()
            }
        }


        return binding.root
    }

    private fun setUpDialog(container: ViewGroup?) {
        alertDialog = AlertDialog.Builder(binding.root.context, R.style.SheetDialog).create()
        val itemDialog = DataBindingUtil.inflate<ItemDialogWelcomeBinding>(
            layoutInflater,
            R.layout.item_dialog_welcome,
            container,
            false
        )
        alertDialog.setView(itemDialog.root)
        alertDialog.setCancelable(false)
    }

    @SuppressLint("SetTextI18n")
    private fun loadData() {
        binding.apply {
            textview.text = "Please wait!"
            progressBar.visibility = View.VISIBLE
            progressBar.progress = 0
            fetchWordsAndSave()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchWordsAndSave() {
        val stringList = resources.getStringArray(R.array.words)
        binding.progressBar.max = stringList.size
        val apiService = ApiClient.apiService
        val ceh = CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }
        GlobalScope.launch(Dispatchers.IO + ceh) {

            stringList.forEachIndexed { index, str ->
                try {
                    val async = async { apiService.getWord(str) }
                    val words = async.await()

                    var name = ""
                    var definition = ""
                    var example = ""
                    var phonetic = ""
                    var audio = ""
                    words.forEach { word ->
                        if (name.isEmpty() && word.word.isNotEmpty()) name = word.word

                        if (phonetic.isEmpty() && word.phonetic.isNotEmpty()) phonetic = word.phonetic

                        word.phonetics?.forEach www@ {
                            if (it.audio.isNotBlank()) {
                                audio = it.audio
                                return@www
                            }
                        }
                        word.meanings?.forEach {
                            it.definitions.forEach { def ->
                                if (def.definition != null && def.definition.isNotEmpty())
                                    definition = def.definition
                                if (def.example != null) example = def.example
                            }
                        }
                    }
                    val wordEntity = WordEntity(name, phonetic, audio, definition, example)

                    database.wordDao().insertWord(wordEntity)
                    binding.progressBar.progress = index
                    if (index == stringList.size - 1) {
                        binding.progressBar.progress = binding.progressBar.max
                        binding.textview.text = "Finish!"
                        (activity as WelcomeActivity).finish()
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                    }
                } catch (e: Exception) {
                    if (networkHelper.isNetworkConnected() && index!=0) {
                        binding.progressBar.progress = binding.progressBar.max
                        binding.textview.text = "Finish!"
                        (activity as WelcomeActivity).finish()
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                    }else {
                        (activity as WelcomeActivity).recreate()
                    }
                }
            }

        }
    }

    override fun onStop() {
        super.onStop()
        connectionLiveData.removeObservers(requireActivity())
    }


    companion object {

        @JvmStatic
        fun newInstance(param1: Int) =
            PagerFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }
}