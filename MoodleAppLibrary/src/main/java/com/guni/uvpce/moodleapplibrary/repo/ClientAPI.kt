package com.guni.uvpce.moodleapplibrary.repo

import android.app.AlertDialog
import android.content.Context
import org.json.JSONObject

class ClientAPI {
    companion object{
        fun showErrorBox(context: Context, title: String, msg: String, negB:String = "", posB:String = "", cancelable: Boolean = true){
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setTitle(title)
            alertDialogBuilder
                .setMessage(msg)
                .setCancelable(cancelable)
                .setNegativeButton(negB) { dialog, id ->
                    dialog.cancel()
                }
                .setPositiveButton(posB) { dialog, id ->
                    dialog.cancel()
                }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

}
