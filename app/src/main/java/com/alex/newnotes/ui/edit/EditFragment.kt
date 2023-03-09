package com.alex.newnotes.ui.edit

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ablanco.zoomy.Zoomy
import com.alex.newnotes.R
import com.alex.newnotes.R.anim.hide_action_button
import com.alex.newnotes.R.anim.show_action_button
import com.alex.newnotes.data.database.Note
import com.alex.newnotes.databinding.FragmentEditBinding
import com.alex.newnotes.utils.Const.ACTION
import com.alex.newnotes.utils.Const.DATE_TIME_PATTERN
import com.alex.newnotes.utils.Const.DEFAULT_COLOR
import com.alex.newnotes.utils.Const.PREFS_NEW_ID_KEY
import com.alex.newnotes.utils.Const.PREFS_NEW_ID_NAME
import com.alex.newnotes.utils.Const.SELECTED_COLOR
import com.alex.newnotes.utils.PictureManager
import com.alex.newnotes.utils.changeStatusBarColor
import com.alex.newnotes.utils.showDialog
import com.alex.newnotes.utils.showSnackbar
import com.bumptech.glide.Glide
import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Objects

@AndroidEntryPoint
class EditFragment : Fragment(R.layout.fragment_edit), TextToSpeech.OnInitListener {
    private val viewModel: EditViewModel by viewModels()
    private lateinit var binding: FragmentEditBinding
    private val viewBinding get() = binding
    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestPicturePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickSingleMediaLauncher: ActivityResultLauncher<Intent>
    private lateinit var resultSpeechLauncher: ActivityResultLauncher<Intent>
    private lateinit var pictureManager: PictureManager
    private lateinit var sharedPref: SharedPreferences
    private var textToSpeech: TextToSpeech? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditBinding.inflate(layoutInflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getInt(NOTE_KEY, -1).also {
            if (it != -1) it?.let { id -> viewModel.setNoteId(id) }
        }
        sharedPref = requireActivity().getSharedPreferences(PREFS_NEW_ID_NAME, MODE_PRIVATE)
        textToSpeech = TextToSpeech(requireContext(), this)
        setUi()
        setListeners()
        setFragmentListeners()
        onBackPressed()
        descriptionTextWatcher()

        pictureManager = PictureManager(
            requireActivity().activityResultRegistry, requireActivity().application
        ) { uri ->
            if (uri == null) return@PictureManager
            showImage(uri)
        }

        pickSingleMediaLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val uri = it.data?.data
                    showImage(uri!!)
                }
            }

        requestNotificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) reminderNotification()
                else showSnackbar(resources.getString(R.string.grant_notify_permission))
            }

        resultSpeechLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val data: Intent? = result.data
                if (result.resultCode == RESULT_OK && data != null) {
                    val res: ArrayList<String> =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                    viewModel.setDesc(
                        viewBinding.edDescription.text.toString() + Objects.requireNonNull(
                            res
                        )[0] + " "
                    )
                }
            }

        requestPicturePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) showDialog(TAG_GET_PICTURE, TAG_GET_PICTURE)
                else showSnackbar(resources.getString(R.string.grant_files_permission))
            }
    }

    private fun setUi() {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver, IntentFilter(BOTTOM_ACTION)
        )
        viewBinding.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.apply {
                        note.collectLatest {
                            it?.color?.let { color -> setSelectedColor(color) }
                            edTitle.setText(it?.title)
                            edDescription.setText(it?.content)
                            if (it?.creationDate?.isNotEmpty() == true) tvNewNote.text =
                                buildString {
                                    append(resources.getString(R.string.created))
                                    append(it.creationDate)
                                }
                            if (it?.completed == true) tvNewNote.text = buildString {
                                append(resources.getString(R.string.completed))
                                append(it.completionDate)
                            }
                            if (it?.uri?.isNotEmpty() == true) {
                                imageLayout.visibility = View.VISIBLE
                                Glide.with(this@EditFragment).load(it.uri).into(imMain)
                                Zoomy.Builder(requireActivity()).target(viewBinding.imMain)
                                    .register()
                            }
                            if (tmpUri.value != null) {
                                imageLayout.visibility = View.VISIBLE
                                Glide.with(this@EditFragment).load(tmpUri.value)
                                    .into(imMain)
                                Zoomy.Builder(requireActivity()).target(viewBinding.imMain)
                                    .register()
                            } else it?.uri?.let { it1 -> setTmpUri(it1) }
                            if (selectedColor.value?.isNotEmpty() == true) {
                                requireActivity().changeStatusBarColor(
                                    Color.parseColor(selectedColor.value),
                                    false
                                )
                                editContainer.setBackgroundColor(Color.parseColor(selectedColor.value))
                            }
                            if (it?.completeBy?.isNotEmpty() == true) {
                                tvCompleteBy.visibility = View.VISIBLE
                                tvCompleteBy.text = buildString {
                                    append(resources.getString(R.string.complete_by))
                                    append(it.completeBy)
                                    setByDateAndTime(it.completeBy!!)
                                }
                                checkExpiration(it)
                            }
                            if (it?.warning == true) tvCompleteBy.setTextColor(Color.RED)
                        }
                    }
                }
            }
        }
    }

    private fun showImage(uri: Uri) {
        viewBinding.imageLayout.visibility = View.VISIBLE
        Glide.with(this@EditFragment).load(uri).into(viewBinding.imMain)
        viewModel.setTmpUri(uri.toString())
    }

    private fun setListeners() {
        viewBinding.apply {
            setFabs()
            fbSave.setOnClickListener {
                viewModel.note.value?.let { updateNote(it) } ?: saveNote()
            }
            ibDelete.setOnClickListener {
                imageLayout.visibility = View.GONE
                viewModel.setTmpUri(DELETE_KEY)
            }
            ibEdit.setOnClickListener { editImage() }
            ibPlay.setOnClickListener { speakOut() }
            tvCompleteBy.setOnClickListener {
                tvCompleteBy.visibility = View.GONE
                viewModel.setByDateAndTime(DELETE_KEY)
                viewModel.note.value?.completeBy = null
                viewModel.note.value?.warning = false
            }
        }
    }

    private fun saveNote() {
        if (viewBinding.edTitle.text.isNullOrEmpty()) showSnackbar(resources.getString(R.string.fill_the_field))
        else {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    Note().apply {
                        title = viewBinding.edTitle.text.toString()
                        content = viewBinding.edDescription.text.toString()
                        uri = viewModel.tmpUri.value
                        color = viewModel.selectedColor.value ?: DEFAULT_COLOR
                        creationDate =
                            SimpleDateFormat(DATE_TIME_PATTERN, Locale.ROOT).format(Date())
                        completeBy = viewModel.byDateAndTime.value
                    }.also {
                        viewModel.onSaveClick(it)
                        createNotification()
                        setNewIdInPref()
                    }
                }
            }
        }
    }

    private fun setNewIdInPref() {
        var count = sharedPref.getInt(PREFS_NEW_ID_KEY, 0)
        count++
        requireActivity().getSharedPreferences(PREFS_NEW_ID_NAME, MODE_PRIVATE).edit().also {
            it.putInt(PREFS_NEW_ID_KEY, count)
        }.apply()
    }

    private fun updateNote(note: Note) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                note.apply {
                    title = viewBinding.edTitle.text.toString()
                    content = viewBinding.edDescription.text.toString()
                    color = when (viewModel.selectedColor.value) {
                        null -> note.color
                        else -> viewModel.selectedColor.value!!
                    }
                    uri = when (viewModel.tmpUri.value) {
                        DELETE_KEY -> null
                        null -> note.uri
                        else -> viewModel.tmpUri.value
                    }
                    completeBy = when (viewModel.byDateAndTime.value) {
                        DELETE_KEY -> null
                        null -> note.completeBy
                        else -> viewModel.byDateAndTime.value
                    }
                }.also {
                    createNotification()
                    viewModel.updateNoteAndClose(it)
                }
            }
        }
    }

    private fun setFragmentListeners() {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            when (bundle.getString(KEY_FOR_SOURCE)) {
                SOURCE_GALLERY -> {
                    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                        pickSingleMediaLauncher.launch(Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                            type = "image/*"
                        })
                    } else pictureManager.pickPhoto()
                }

                SOURCE_CAMERA -> pictureManager.takePhoto()
                CLOSE_KEY -> viewModel.onBackPressed()
                SAVE_KEY -> viewModel.note.value?.let { updateNote(it) } ?: saveNote()
            }
        }
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            activity?.resources?.apply {
                when (intent!!.getStringExtra(ACTION)) {
                    getString(R.string.blue) -> changeColor(intent)
                    getString(R.string.cyan) -> changeColor(intent)
                    getString(R.string.green) -> changeColor(intent)
                    getString(R.string.orange) -> changeColor(intent)
                    getString(R.string.purple) -> changeColor(intent)
                    getString(R.string.red) -> changeColor(intent)
                    getString(R.string.yellow) -> changeColor(intent)
                    getString(R.string.dark) -> changeColor(intent)
                    getString(R.string.indigo) -> changeColor(intent)
                }
            }
        }
    }

    private fun changeColor(intent: Intent) {
        viewModel.setSelectedColor(intent.getStringExtra(SELECTED_COLOR) ?: "")
        viewBinding.editContainer.setBackgroundColor(Color.parseColor(viewModel.selectedColor.value))
        requireActivity().changeStatusBarColor(
            Color.parseColor(viewModel.selectedColor.value),
            false
        )
    }

    private fun setFabs() {
        viewBinding.apply {
            var isAllFabsVisible = false
            val actionFabShow = AnimationUtils.loadAnimation(requireContext(), show_action_button)
            val actionFabHide = AnimationUtils.loadAnimation(requireContext(), hide_action_button)
            fbAction.apply {
                shrink()
                setOnClickListener {
                    isAllFabsVisible = if (!isAllFabsVisible) {
                        startAnimation(actionFabShow)
                        showExtFabItem(fbAddImage, tvAddImage)
                        showExtFabItem(fbAddColor, tvAddColor)
                        showExtFabItem(fbAddSpeech, tvAddSpeech)
                        showExtFabItem(fbAddReminder, tvAddReminder)
                        showExtFabItem(fbShare, tvShare)
                        true
                    } else {
                        startAnimation(actionFabHide)
                        hideExtFabItem(fbAddImage, tvAddImage)
                        hideExtFabItem(fbAddColor, tvAddColor)
                        hideExtFabItem(fbAddSpeech, tvAddSpeech)
                        hideExtFabItem(fbAddReminder, tvAddReminder)
                        hideExtFabItem(fbShare, tvShare)
                        shrink()
                        false
                    }
                }
            }
            fbAddColor.setOnClickListener {
                requireActivity().supportFragmentManager.let {
                    NoteBottomSheetFragment.newInstance(viewModel.note.value?.id).show(
                        it, TAG_BOTTOM_FRAGMENT
                    )
                }
            }
            fbAddImage.setOnClickListener {
                checkPermission()
            }
            fbAddSpeech.setOnClickListener { addSpeech() }
            fbAddReminder.setOnClickListener {
                if (edTitle.text.isNullOrEmpty()) showSnackbar(resources.getString(R.string.fill_the_field))
                else addReminder()
            }
            fbShare.setOnClickListener { share() }
        }
    }

    private fun showExtFabItem(fab: FloatingActionButton, textView: TextView) {
        val scaleUp =
            ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, RELATIVE_TO_SELF, 1.0f, RELATIVE_TO_SELF, 0.5f)
        scaleUp.duration = 250
        fab.show()
        textView.visibility = View.VISIBLE
        textView.startAnimation(scaleUp)
    }

    private fun hideExtFabItem(fab: FloatingActionButton, textView: TextView) {
        val scaleDown =
            ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, RELATIVE_TO_SELF, 1.0f, RELATIVE_TO_SELF, 0.5f)
        scaleDown.duration = 100
        fab.hide()
        textView.visibility = View.GONE
        textView.startAnimation(scaleDown)
    }

    private fun addSpeech() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        if (Locale.getDefault().language == "ru") {
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "ru-RU"
            )
        } else intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, resources.getString(R.string.speak))
        try {
            resultSpeechLauncher.launch(intent)
        } catch (e: Exception) {
            showSnackbar(" " + e.message)
        }
    }

    private fun share() {
        val intent = Intent()
        intent.apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "${viewBinding.edTitle.text}. ${viewBinding.edDescription.text}."
            )
            if (viewModel.tmpUri.value != null) putExtra(
                Intent.EXTRA_STREAM,
                viewModel.tmpUri.value?.toUri()
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, resources.getString(R.string.share)))
    }

    private fun speakOut() {
        val text = viewBinding.edDescription.text.toString()
        if (text.isNotEmpty()) {
            textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
            textToSpeech!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {
                    viewBinding.ibPlay.setImageResource(R.drawable.ic_stop)
                }

                override fun onDone(utteranceId: String) {
                    viewBinding.ibPlay.setImageResource(R.drawable.ic_play)
                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    super.onStop(utteranceId, interrupted)
                    viewBinding.ibPlay.setImageResource(R.drawable.ic_play)
                    textToSpeech!!.stop()
                }

                override fun onError(utteranceId: String?) {
                    showSnackbar(resources.getString(R.string.error))
                }
            })

        } else showSnackbar(resources.getString(R.string.description_is_empty))
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = Locale("ru")
            val result: Int = textToSpeech!!.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                showSnackbar(resources.getString(R.string.unsupported_language))
            else viewBinding.ibPlay.isEnabled = true
        } else showSnackbar(resources.getString(R.string.error))
    }

    private fun onBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                checkChanges()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun checkChanges() {
        viewBinding.apply {
            viewModel.note.apply {
                if (value == null) {
                    if (edTitle.text.isNotEmpty() ||
                        edDescription.text.isNotEmpty() ||
                        viewModel.tmpUri.value?.isNotEmpty() == true
                    ) showDialog(TAG_CLOSE_EDIT_FRAGMENT, TAG_CLOSE_EDIT_FRAGMENT)
                    else viewModel.onBackPressed()
                }
                if (value != null) {
                    if (edTitle.text.toString() != value?.title.toString() ||
                        edDescription.text.toString() != value?.content.toString() ||
                        viewModel.tmpUri.value != value?.uri ||
                        viewModel.selectedColor.value != value?.color ||
                        viewModel.byDateAndTime.value != value?.completeBy ||
                        viewModel.byDateAndTime.value == DELETE_KEY
                    ) showDialog(TAG_CLOSE_EDIT_FRAGMENT, TAG_CLOSE_EDIT_FRAGMENT)
                    else viewModel.onBackPressed()
                }
            }
        }
    }

    private fun editImage() {
        val intent = Intent(requireActivity(), DsPhotoEditorActivity::class.java)
        intent.apply {
            data = viewModel.tmpUri.value!!.toUri()
            putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "images")
            putExtra(
                DsPhotoEditorConstants.DS_MAIN_BACKGROUND_COLOR,
                resources.getColor(R.color.icon_foreground, null)
            )
            putExtra(
                DsPhotoEditorConstants.DS_TOOL_BAR_BACKGROUND_COLOR,
                resources.getColor(R.color.icon_background, null)
            )
            putExtra(
                DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE,
                arrayListOf(
                    DsPhotoEditorActivity.TOOL_WARMTH,
                    DsPhotoEditorActivity.TOOL_PIXELATE
                )
            )
        }
        pickSingleMediaLauncher.launch(intent)
    }

    private fun checkPermission() {
        return if (VERSION.SDK_INT >= VERSION_CODES.Q) {
            showDialog(TAG_GET_PICTURE, TAG_GET_PICTURE)
        } else {
            requestPicturePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun descriptionTextWatcher() {
        viewBinding.edDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                showPlayButton()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                showPlayButton()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                showPlayButton()
            }
        })
    }

    private fun showPlayButton() {
        if (viewBinding.edDescription.text.isNotEmpty())
            viewBinding.ibPlay.visibility = View.VISIBLE
        else viewBinding.ibPlay.visibility = View.GONE
    }

    private fun addReminder() {
        val currentDate = Calendar.getInstance()
        val date: Calendar = Calendar.getInstance()
        DatePickerDialog(
            requireActivity(), R.style.DatePickerDialog,
            { _, year, monthOfYear, dayOfMonth ->
                date.set(year, monthOfYear, dayOfMonth)
                TimePickerDialog(
                    requireActivity(), R.style.DatePickerDialog,
                    { _, hourOfDay, minute ->
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        date.set(Calendar.MINUTE, minute)
                        val byDateAndTime = "${String.format("%02d", dayOfMonth)}-${
                            String.format("%02d", monthOfYear + 1)
                        }-$year ${String.format("%02d", hourOfDay)}:${
                            String.format(
                                "%02d",
                                minute
                            )
                        }"
                        viewModel.setByDateAndTime(byDateAndTime)
                        viewBinding.tvCompleteBy.visibility = View.VISIBLE
                        viewBinding.tvCompleteBy.text = buildString {
                            append(resources.getString(R.string.complete_by))
                            append(viewModel.byDateAndTime.value)
                        }
                        viewModel.note.value?.warning = false
                        viewBinding.tvCompleteBy.setTextColor(Color.BLACK)
                    },
                    currentDate[Calendar.HOUR_OF_DAY],
                    currentDate[Calendar.MINUTE] + 1, true
                ).show()
            }, currentDate[Calendar.YEAR], currentDate[Calendar.MONTH],
            currentDate[Calendar.DATE]
        ).show()
    }

    private fun createNotification() {
        if (viewModel.byDateAndTime.value != null && viewModel.byDateAndTime.value != DELETE_KEY) {
            if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.SCHEDULE_EXACT_ALARM
                    ) == PackageManager.PERMISSION_GRANTED
                ) reminderNotification()
                else requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else reminderNotification()
        }
    }

    private fun reminderNotification() {
        val id = viewModel.note.value?.id ?: viewModel.noteId.value
        val title = viewBinding.edTitle.text.toString()
        val color = viewModel.note.value?.color ?: DEFAULT_COLOR
        viewModel.reminderNotification(id!!, title, color, viewModel.byDateAndTime.value!!)
    }

    override fun onPause() {
        super.onPause()
        viewModel.setTitle(viewBinding.edTitle.text.toString())
        viewModel.setDesc(viewBinding.edDescription.text.toString())
    }

    override fun onResume() {
        super.onResume()
        viewBinding.edTitle.setText(viewModel.title.value)
        viewBinding.edDescription.setText(viewModel.description.value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pictureManager = PictureManager(
            requireActivity().activityResultRegistry, requireActivity().application
        ) {}
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(
            broadcastReceiver
        )
        Zoomy.unregister(viewBinding.imMain)
        if (textToSpeech != null) {
            textToSpeech!!.stop()
            textToSpeech!!.shutdown()
        }
    }

    companion object {
        const val REQUEST_KEY = "request_key"
        const val KEY_FOR_SOURCE = "key_for_source"
        const val SOURCE_CAMERA = "camera"
        const val SOURCE_GALLERY = "gallery"
        const val DELETE_KEY = "delete"
        const val NOTE_KEY = "note_id"
        const val TAG_BOTTOM_FRAGMENT = "bottom_fragment"
        const val BOTTOM_ACTION = "bottom_sheet_action"
        const val TAG_GET_PICTURE = "get_picture"
        const val SAVE_KEY = "save"
        const val CLOSE_KEY = "close"
        const val TAG_CLOSE_EDIT_FRAGMENT = "close_fragment"

        fun newInstance(note: Note) = EditFragment().apply {
            arguments = Bundle().apply {
                putInt(NOTE_KEY, note.id)
            }
        }
    }
}