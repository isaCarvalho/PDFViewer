package com.example.pdfviewer.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.pdfviewer.R
import java.lang.ClassCastException

class PageDialogFragment : DialogFragment()
{
    private lateinit var listener : PageDialogListener

    interface PageDialogListener
    {
        fun onDialogPositiveClick(dialogFragment: DialogFragment, nPage : Int)
        fun onDialogNegativeClick(dialogFragment: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as PageDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + "must implement PageDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            val mView = inflater.inflate(R.layout.layout_input, null)

            builder.setTitle(R.string.goToPage)
                .setView(mView)
                .setIcon(R.drawable.ic_search_black_24dp)
                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                    val nPage = mView.findViewById<EditText>(R.id.goToPageTxt).text.toString().toInt()

                    listener.onDialogPositiveClick(this, nPage)
                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, which ->
                    listener.onDialogNegativeClick(this)
                })

            builder.create()
        }!!
    }
}