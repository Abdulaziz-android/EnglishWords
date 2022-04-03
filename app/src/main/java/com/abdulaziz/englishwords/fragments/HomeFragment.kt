package com.abdulaziz.englishwords.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context.*
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.abdulaziz.englishwords.R
import com.abdulaziz.englishwords.adapter.AutoCompleteAdapter
import com.abdulaziz.englishwords.adapter.WordAdapter
import com.abdulaziz.englishwords.database.SearchItem
import com.abdulaziz.englishwords.database.SeenItem
import com.abdulaziz.englishwords.database.WordDatabase
import com.abdulaziz.englishwords.database.WordEntity
import com.abdulaziz.englishwords.databinding.FragmentHomeBinding
import com.abdulaziz.englishwords.models.MyItem
import com.abdulaziz.englishwords.retrofit.ApiClient
import com.abdulaziz.englishwords.viewmodels.ConnectionLiveData
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.*
import java.util.*


class HomeFragment : Fragment(), PermissionListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: WordDatabase
    private var listWord: List<WordEntity>? = null
    private lateinit var tts: TextToSpeech
    private var currentWord: WordEntity? = null
    private lateinit var sPref: SharedPreferences
    private lateinit var conLiveData: ConnectionLiveData
    private var networkConnected = true
    private lateinit var adapter: AutoCompleteAdapter
    private lateinit var allItemAdapter: WordAdapter
    private var speechRecognizer:SpeechRecognizer?=null
    private var speechRecognizerIntent:Intent?=null
    private val RecordAudioRequestCode = 1

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_home, container, false)

        initialVars()
        loadData()
        onCardClickListeners()
        runSpeech()

        binding.autoCompleteText.isSingleLine = true

        return binding.root
    }

    private fun runSpeech() {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.RECORD_AUDIO)
            .withListener(this)
            .check()
        binding.micIv.setOnClickListener {
            Dexter.withContext(requireContext())
                .withPermission(Manifest.permission.RECORD_AUDIO)
                .withListener(this)
                .check()
        }
    }

    private fun initialVars() {
        sPref = binding.root.context.getSharedPreferences("shared", MODE_PRIVATE)
        database = WordDatabase.getInstance(binding.root.context)
        conLiveData = ConnectionLiveData(requireActivity().application)
        conLiveData.observe(requireActivity()) { isConnected ->
            networkConnected = isConnected
        }
        tts = TextToSpeech(requireContext()) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.UK
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onCardClickListeners() {

        binding.copyCard.setOnClickListener {
            val clipboard: ClipboardManager? =
                requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("label", binding.wordTv.text.toString())
            clipboard?.setPrimaryClip(clip)
        }

        binding.audioCard.setOnClickListener {
            if (networkConnected) {
                val player = MediaPlayer.create(requireContext(),
                    Uri.parse("https:${currentWord?.audio_link}"))
                if (player != null)
                    player.start()
                else {
                    tts.speak(currentWord?.word, TextToSpeech.QUEUE_FLUSH, null, "null")
                }
            } else {
                tts.speak(currentWord?.word, TextToSpeech.QUEUE_FLUSH, null, "null")
            }
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

        binding.wordOfDayTv.setOnClickListener {
            switchOneAndAll(false)
        }
        binding.allWordTv.setOnClickListener {
            switchOneAndAll(true)
        }

        binding.pasteIv.setOnClickListener {
            val clipboard: ClipboardManager? =
                requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?

            var pasteData = ""
            if (clipboard!!.primaryClipDescription!!
                    .hasMimeType(MIMETYPE_TEXT_PLAIN)){
                val item = clipboard.primaryClip!!.getItemAt(0)
                pasteData = item.text.toString()
            }
            if (pasteData.isNotEmpty())
                binding.autoCompleteText.setText(pasteData)
        }


        binding.refreshIv.setOnClickListener {
            val word = listWord?.random()
            loadWord(word!!)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadData() {
        listWord = database.wordDao().getAllWords()

        binding.foundWordsTv.text = "found ${listWord!!.size} words offline"

        //load item
        val lastWord = sPref.getString("last", "")
        currentWord = if (lastWord.equals("")) {
            listWord?.get(0)
        } else {
            database.wordDao().getWordByName(lastWord!!)
        }
        loadWord(currentWord!!)

        val itemList: ArrayList<MyItem> = arrayListOf()
        listWord!!.forEach {
            itemList.add(MyItem(it.word))
        }
        adapter = AutoCompleteAdapter(requireContext(), itemList)
        binding.autoCompleteText.setAdapter(adapter)
        binding.autoCompleteText.onItemClickListener = object : AdapterView.OnItemClickListener {
            @SuppressLint("SetTextI18n")
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val myItem = adapter.getItem(p2) as MyItem
                if (myItem.isExsist) {
                    val word = database.wordDao().getWordByName(myItem.name)
                    loadWord(word)
                    binding.autoCompleteText.text.clear()
                    switchOneAndAll(false)
                    saveSearchItem()
                } else {
                    fetchAndSaveWord(myItem.name)
                }
                hideKeyboard()
            }

        }


        allItemAdapter = WordAdapter(listWord!!, object : WordAdapter.OnItemClickListener {
            override fun onItemClick(wordEntity: WordEntity) {
                loadWord(wordEntity)
                switchOneAndAll(false)
            }

        })

        binding.rv.adapter = allItemAdapter
        binding.rv.addItemDecoration(DividerItemDecoration(binding.rv.context,
            DividerItemDecoration.VERTICAL))
    }

    private fun saveSearchItem() {
        if (database.itemDao().isExist()) {
            val allItems = database.itemDao().getAllItems()
            database.itemDao().insertItem(SearchItem(currentWord!!.word, position = allItems.size))
        }
        else {
            database.itemDao().insertItem(SearchItem(currentWord!!.word))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadWord(wordEntity: WordEntity) {
        currentWord = wordEntity
        binding.wordTv.text = currentWord!!.word
        if (wordEntity.save){
            binding.saveTv.setCompoundDrawablesWithIntrinsicBounds(0,  R.drawable.ic_baseline_bookmark_full,0, 0)
        }else{
            binding.saveTv.setCompoundDrawablesWithIntrinsicBounds(0,  R.drawable.ic_baseline_bookmark_border_24,0, 0)
        }
        binding.definitionTv.text = "Definition: ${currentWord!!.definition}"
        binding.exampleTv.text = "Example: ${currentWord!!.example}"
        sPref.edit().putString("last", wordEntity.word).apply()
        if (database.seenDao().isExist()) {
            val allItems = database.seenDao().getAllItems()
            database.seenDao().insertItem(SeenItem(currentWord!!.word, position = allItems.size))
        }
        else {
            database.seenDao().insertItem(SeenItem(currentWord!!.word))
        }
    }

    fun switchOneAndAll(isAll: Boolean) {
        if (isAll) {
            binding.oneItemConst.visibility = View.GONE
            binding.rvLinear.visibility = View.VISIBLE
            binding.wordOfDayTv.setTextColor(Color.parseColor("#A3FFFFFF"))
            binding.allWordTv.setTextColor(Color.WHITE)
        } else {
            binding.oneItemConst.visibility = View.VISIBLE
            binding.rvLinear.visibility = View.GONE
            binding.wordOfDayTv.setTextColor(Color.WHITE)
            binding.allWordTv.setTextColor(Color.parseColor("#A3FFFFFF"))
        }
        hideKeyboard()
    }


    private fun fetchAndSaveWord(str: String) {
        val apiService = ApiClient.apiService
        try {
            val ceh = CoroutineExceptionHandler { _, throwable ->
                throwable.printStackTrace()
            }
            GlobalScope.launch(Dispatchers.Main + ceh) {
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

                        word.phonetics?.forEach {
                            if (it.audio.isNotBlank()) {
                                audio = it.audio
                                return@forEach
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
                    loadWord(wordEntity)
                    loadData()
                    switchOneAndAll(false)
                    saveSearchItem()
                    binding.autoCompleteText.text.clear()
                } catch (e: Exception) {
                    Toast.makeText(binding.root.context, "Word not found!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {

        }
    }
    private fun setUpSpeech(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        speechRecognizerIntent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault())
        speechRecognizer!!.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {

            }

            override fun onBeginningOfSpeech() {
                binding.autoCompleteText.hint = "Listening..."
            }

            override fun onRmsChanged(p0: Float) {

            }

            override fun onBufferReceived(p0: ByteArray?) {

            }

            override fun onEndOfSpeech() {

            }

            override fun onError(p0: Int) {

            }

            override fun onResults(bundle: Bundle?) {
                binding.micIv.setImageResource(R.drawable.ic_baseline_mic_none_24)
                val data: ArrayList<String> =
                    bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>
                binding.autoCompleteText.setText(data[0])
            }

            override fun onPartialResults(p0: Bundle?) {

            }

            override fun onEvent(p0: Int, p1: Bundle?) {

            }

        })
    }

    fun hideKeyboard() {
        requireActivity().currentFocus?.let { view ->
            val imm =
                requireActivity().getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

        setUpSpeech()
        binding.micIv.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                speechRecognizer!!.stopListening()
            }
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                binding.micIv.setImageResource(R.drawable.ic_baseline_mic_black)
                speechRecognizer!!.startListening(speechRecognizerIntent!!)
            }
            false
        }
    }

    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

    }

    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
        Toast.makeText(requireContext(), "Sozlamalar orqali ruxsat bering va qaytadan kiring!", Toast.LENGTH_SHORT).show()
    }
}