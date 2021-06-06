package celik.abdullah.authentication.utils

import android.content.SharedPreferences
import android.util.Log
import celik.abdullah.authentication.utils.Const.USER_EMAIL
import celik.abdullah.authentication.utils.Const.USER_NAME
import celik.abdullah.authentication.utils.Const.USER_PASSWORD
import celik.abdullah.authentication.utils.Const.USER_TOKEN
import javax.inject.Inject

/*
* The SessionManager's role is to provide a cache for the user's data.
* That way, we can grab the user's data such as email, username etc. directly.
* Note: We could also store important user credentials like the password or token
*       into Room but that would be in cleartext. Room does not come up with encrypting data before
*       storing. You have to encrypt them yourself before passing them to Room. I did not want to this
*       for this app. Nevertheless, it is recommended to encrypt such data before saving it.
*       Therefore, I have chosen EncryptSharedPreferences which saves the credentials in an encrypted
*       manner for us.
* */
class SessionManager @Inject constructor(private val sharedPreferences: SharedPreferences,
                                         private val sharedPrefsEditor: SharedPreferences.Editor) {

    // sets the token to null
    fun clearToken(): SharedPreferences.Editor = sharedPrefsEditor.putString(USER_TOKEN, null)

    // used to save the new password after it is changed
    fun saveNewPasswordIntoSharedPrefs(newPassword:String) = sharedPrefsEditor.putString(USER_PASSWORD, newPassword).apply()

    /*
    * For a quick lookup, we store the credentials into EncryptedSharedPreferences which is a more
    * secure version of SharedPreferences. As far as I understood, EncryptedSharedPreferences encrypts
    * the keys & values for us. That way, we do not expose the token and the password.
    * You can read more here: https://developer.android.com/topic/security/data
    * */
    fun saveUserCredentialsIntoEncryptedSharedPrefs(email: String, username:String, password:String, token:String) =
        sharedPrefsEditor.apply{
            putString(USER_EMAIL, email)
            putString(USER_NAME, username)
            putString(USER_PASSWORD, password)
            putString(USER_TOKEN, token)
            apply()
        }


    // methods used to grab the credentials of the user
    // note that the user will be asked to store them
    fun retrieveUserTokenFromEncryptSharedPrefs(): String? = sharedPreferences.getString(USER_TOKEN, null)
    fun retrieveUserNameFromEncryptSharedPrefs() : String?  = sharedPreferences.getString(USER_NAME, null)
    fun retrieveUserPasswordFromEncryptSharedPrefs() :  String?  = sharedPreferences.getString(USER_PASSWORD, null)
}