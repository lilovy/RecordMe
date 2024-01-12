package com.lilovy.recordme

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lilovy.recordme.databinding.BottomSheetBinding
import java.io.File


class BottomSheet(
    private var dirPath: String,
    private var filename: String,
    private var listener: OnClickListener
) : BottomSheetDialogFragment() {

    interface OnClickListener {
        fun onCancelClicked()
        fun onOkClicked(filePath: String, filename: String)
    }


    private lateinit var binding: BottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetBinding.inflate(layoutInflater)
        val editText = binding.filenameInput

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        filename = filename.split(".mp3")[0]
        editText.setText(filename)

        showKeyboard(editText)

        binding.okBtn.setOnClickListener {
            hideKeyboard(binding.root)

            val updatedFilename = editText.text.toString()
            if(updatedFilename != filename){
                val newFile = File("$dirPath$updatedFilename.mp3")
                File(dirPath+filename).renameTo(newFile)
            }

            dismiss()

            listener.onOkClicked("$dirPath$updatedFilename.mp3", updatedFilename)
        }

        binding.cancelBtn.setOnClickListener {
            hideKeyboard(binding.root)
            File(dirPath+filename).delete()

            dismiss()

            listener.onCancelClicked()
        }

        return binding.root

    }

    private fun showKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

}