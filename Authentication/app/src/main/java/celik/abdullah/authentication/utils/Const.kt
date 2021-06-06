package celik.abdullah.authentication.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import celik.abdullah.authentication.R

object Const {
    const val BASE_URL = "http://192.168.2.104:8000/"
    const val USER_TOKEN = "celik.abdullah.authentication.USER_TOKEN"
    const val USER_PASSWORD = "celik.abdullah.authentication.USER_PASSWORD"
    const val USER_EMAIL = "celik.abdullah.authentication.USER_EMAIL"
    const val USER_NAME = "celik.abdullah.authentication.USER_NAME"
    const val USER_CREDENTIALS = "celik.abdullah.authentication.USER_CREDENTIALS"

    fun showErrorDialog(context: Context, error: String?) : AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.apply{
            if(error==null){
                setMessage("An error has been occurred. Please try it again.")
            }
            else{
                setMessage(error)
            }
            setTitle("Error")
        }
        return builder.create()
    }

    fun showNetworkIssuesDialog(context: Context, networkErrorString: Int): AlertDialog {
        val builder: AlertDialog.Builder = context.let { AlertDialog.Builder(it) }
        builder.setMessage(networkErrorString)?.setTitle(R.string.network_error_dialog)

        return builder.create()
    }

    fun showInvalidDataDialog(context: Context, invalidDataMessage:Int): AlertDialog {
        val builder: AlertDialog.Builder = context.let { AlertDialog.Builder(it) }
        builder.setMessage(invalidDataMessage)?.setTitle(R.string.unknown_error_dialog)

        return builder.create()
    }

    fun logoutDialog(context: Context, message:Int): AlertDialog{
        val builder: AlertDialog.Builder = context.let { AlertDialog.Builder(it) }
        builder.setMessage(message)?.setTitle(R.string.logout_dialog_title)

        return builder.create()
    }

}