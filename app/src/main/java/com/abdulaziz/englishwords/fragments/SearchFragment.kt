package com.abdulaziz.englishwords.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.abdulaziz.englishwords.R
import com.abdulaziz.englishwords.adapter.WordAdapter
import com.abdulaziz.englishwords.database.SeenItem
import com.abdulaziz.englishwords.database.WordDatabase
import com.abdulaziz.englishwords.database.WordEntity
import com.abdulaziz.englishwords.databinding.FragmentSearchBinding
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : Fragment() {

    lateinit var binding: FragmentSearchBinding
    private val TAG = "SearchFragment"
    lateinit var database: WordDatabase
    lateinit var adapter: WordAdapter
    private var currentWord: WordEntity? = null
    lateinit var tts: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_search, container, false)

        database = WordDatabase.getInstance(binding.root.context)


        tts = TextToSpeech(requireContext()) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.UK)
            }
        }
        var list: ArrayList<WordEntity> = arrayListOf()
        if (database.itemDao().isExist()) {
            val allItems = database.itemDao().getAllItems()
            allItems.forEach {
                val word = database.wordDao().getWordByName(it.name)
                list.add(word)
            }
            if (list.isEmpty()){
                binding.dataLayout.visibility = View.GONE
                binding.notFoundLayout.visibility = View.VISIBLE
            }

            list.reverse()
            loadWord(list.first())
            Log.d(TAG, "onCreateView: $list")
            adapter = WordAdapter(list, object : WordAdapter.OnItemClickListener {
                override fun onItemClick(wordEntity: WordEntity) {
                    loadWord(wordEntity)
                }
            })
            binding.rv.adapter = adapter
            binding.rv.addItemDecoration(DividerItemDecoration(binding.rv.context,
                DividerItemDecoration.VERTICAL))

            onClickListeners()
        }else{
            binding.dataLayout.visibility = View.GONE
            binding.notFoundLayout.visibility = View.VISIBLE
        }

        return binding.root
    }

    private fun onClickListeners() {

        binding.copyCard.setOnClickListener {
            val clipboard: ClipboardManager? =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("label", binding.wordTv.text.toString())
            clipboard?.setPrimaryClip(clip)
        }

        binding.soundIv.setOnClickListener {
            tts.speak(currentWord?.word, TextToSpeech.QUEUE_FLUSH, null, "null")
        }

        binding.saveCard.setOnClickListener {
            if (currentWord?.save!!) {
            binding.saveTv.setCompoundDrawablesWithIntrinsicBounds(0,
                R.drawable.ic_baseline_bookmark_border_24,
                0,
                0)
            currentWord?.save = false
            database.wordDao().updateWord(currentWord!!)
        }else{
            binding.saveTv.setCompoundDrawablesWithIntrinsicBounds(0,
                R.drawable.ic_baseline_bookmark_full,
                0,
                0)
            currentWord?.save = true
            database.wordDao().updateWord(currentWord!!)
        }
        }

        binding.shareCard.setOnClickListener {
            val myText = "Word: ${currentWord?.word}\n" +
                    "Definition: ${currentWord?.definition}\n" +
                    "Example: ${currentWord?.example}\n" +
                    "AudioLink: ${currentWord?.audio_link}"
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, myText)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        binding.fontCard.setOnClickListener {
            if (binding.wordTv.text.toString().first().isLowerCase()){
                var toString = binding.wordTv.text.toString().uppercase()
                binding.wordTv.text = toString
            }
            else{
                var toString = binding.wordTv.text.toString().lowercase()
                binding.wordTv.text = toString
            }
        }

    }

    private fun loadWord(wordEntity: WordEntity) {
        binding.wordTv.text = wordEntity.word

        if (wordEntity.save){
            binding.saveTv.setCompoundDrawablesWithIntrinsicBounds(0,  R.drawable.ic_baseline_bookmark_full,0, 0)
        }else{
            binding.saveTv.setCompoundDrawablesWithIntrinsicBounds(0,  R.drawable.ic_baseline_bookmark_border_24,0, 0)
        }
        currentWord = wordEntity


        if (database.seenDao().isExist()) {
            val allItems = database.seenDao().getAllItems()
            database.seenDao().insertItem(SeenItem(currentWord!!.word, position = allItems.size))
        }
        else {
            database.seenDao().insertItem(SeenItem(currentWord!!.word))
        }
    }

}