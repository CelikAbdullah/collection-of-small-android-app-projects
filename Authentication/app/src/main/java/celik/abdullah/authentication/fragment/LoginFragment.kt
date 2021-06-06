package celik.abdullah.authentication.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import celik.abdullah.authentication.R
import celik.abdullah.authentication.databinding.FragmentLoginBinding
import celik.abdullah.authentication.utils.Const.logoutDialog
import celik.abdullah.authentication.utils.Const.showErrorDialog
import celik.abdullah.authentication.utils.Const.showInvalidDataDialog
import celik.abdullah.authentication.utils.Const.showNetworkIssuesDialog
import celik.abdullah.authentication.utils.EventObserver
import celik.abdullah.authentication.viewmodel.LoginFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint

/*
* The UI controller that deals with the login process
* */
@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var binding : FragmentLoginBinding
    private val loginFragmentViewModel by viewModels<LoginFragmentViewModel>()
    private val navController by lazy{findNavController()}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = loginFragmentViewModel
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        makeLogOutTextVisibleOrNot()
        retrieveSavedUserCredentials()
        setupObservers()
        setupInputListeners()
    }

    private fun setupObservers() {
        loginFragmentViewModel.apply {
            startLogin.observe(viewLifecycleOwner, EventObserver{
                // make the progressbar visible
                binding.loading.visibility = View.VISIBLE

                // pass the credentials so that the login can begin
                this.login(
                    binding.loginUsername.editText?.text.toString(),
                    binding.loginPassword.editText?.text.toString()
                )
            })

            startLogout.observe(viewLifecycleOwner, EventObserver{
                // make the progressbar visible
                binding.loading.visibility = View.VISIBLE
                // start logout request
                this.logout()
            })

            // navigate to RegistrationFragment
            registerEvent.observe(viewLifecycleOwner, EventObserver{
                // navigate to RegistrationFragment
                navController.navigate(LoginFragmentDirections.actionLogingFragmentToRegistrationFragment())

            })

            // navigate to ForgotPasswordFragment
            forgotPasswordEvent.observe(viewLifecycleOwner, EventObserver{
                // navigate to ChangePasswordFragment
                navController.navigate(LoginFragmentDirections.actionLogingFragmentToChangePasswordFragment())
            })

            // listen for type errors made by the user during the login
            loginFormState.observe(viewLifecycleOwner, Observer { loginFormState ->
                if (loginFormState == null) return@Observer

                // enable the login after the given input data is validated
                if(loginFormState.isDataValid) {
                    if(loginFormState.emailError == null) binding.loginUsername.error = null
                    if(loginFormState.passwordError == null) binding.loginPassword.error = null

                    enableLogin()
                }
                // otherwise, disable login
                else {
                    disableLogin()
                }

                // notify the user that the given email is wrong
                loginFormState.emailError?.let{
                    binding.loginUsername.error = getString(it)
                }

                // notify the user that the given password is wrong
                loginFormState.passwordError?.let{
                    binding.loginPassword.error = getString(it)
                }
            })

            // handles the possible results of our login request
            loginResult.observe(viewLifecycleOwner, Observer { loginResult ->
                loginResult ?: return@Observer

                // scenario I: our network request was a success
                // if login procedure was successful,
                // then store the user data into Room and ask the user if he wants to save his/her
                // credentials for a future login
                loginResult.success?.let {
                    binding.loading.visibility = View.GONE
                    // store user data into the local database
                    storeUserDataLocally(it.user)

                    dialogSaveUserCredentialsOrNot(
                        email=it.user.userEmail,
                        username=it.user.userName,
                        password = binding.loginPassword.editText?.text.toString(),
                        token= it.token
                    ).show()

                }

                // scenario II: our network request did not work because of a specific error
                // notify the user that an error has been occurred during login
                // e.g. if the username is wrong, then inform the user that the typed username might be wrong
                //      if the password is wrong, then inform the user that the typed password might be wrong
                loginResult.error?.let{errorMessage->
                    binding.loading.visibility = View.GONE
                    showErrorDialog(requireContext(), errorMessage).show()
                }

                // scenario III: our network request did not work because the user has no internet
                // notify the user that we have a problem with the internet connection
                // e.g. when the smartphone might be in airplane-mode or the user has no access to the internet somehow
                loginResult.networkError?.let { networkErrorMessage ->
                    binding.loading.visibility = View.GONE
                    showNetworkIssuesDialog(requireContext(), networkErrorMessage).show()
                }

                // scenario IV: something bad happened
                loginResult.invalidData?.let{ invalidDataMessage ->
                    binding.loading.visibility = View.GONE
                    showInvalidDataDialog(requireContext(), invalidDataMessage).show()
                }
            })

            // handles the possible results of our logout request
            logoutResult.observe(viewLifecycleOwner, Observer{ logoutResult ->
                logoutResult ?: return@Observer

                // scenario I: our network request was a success
                logoutResult.success?.let{ successMessage ->
                    // let the progressbar disappear
                    binding.loading.visibility = View.GONE
                    // inform the user that everything is fine
                    logoutDialog(requireContext(), successMessage).show()
                    // clear the token
                    loginFragmentViewModel.clearToken()
                }
                // scenario II: our network request did not work because of a specific error
                logoutResult.error?.let{ errorMessage ->
                    // let the progressbar disappear
                    binding.loading.visibility = View.GONE
                    // show error via dialog
                    showErrorDialog(requireContext(), errorMessage).show()
                }
                // scenario III: our network request did not work because the user has no internet
                logoutResult.networkError?.let{ networkErrorMessage ->
                    // let the progressbar disappear
                    binding.loading.visibility = View.GONE
                    // show error via dialog
                    showNetworkIssuesDialog(requireContext(), networkErrorMessage).show()
                }

                // scenario IV: something bad happened
                logoutResult.invalidData?.let{ invalidDataMessage ->
                    binding.loading.visibility = View.GONE
                    showInvalidDataDialog(requireContext(), invalidDataMessage).show()
                }
            })
        }
    }

    // listeners for the two input fields (email & password)
    private fun setupInputListeners() {
        binding.apply {
            // handling email input
            loginUsername.editText?.doAfterTextChanged {
                binding.loginUsername.error = null
                loginFragmentViewModel.loginDataChanged(
                    binding.loginUsername.editText?.text.toString(),
                    binding.loginPassword.editText?.text.toString())
            }

            // handling password input
            loginPassword.editText?.doAfterTextChanged {
                binding.loginPassword.error = null
                loginFragmentViewModel.loginDataChanged(
                    binding.loginUsername.editText?.text.toString(),
                    binding.loginPassword.editText?.text.toString())
            }
        }
    }

    private fun enableLogin() =
        binding.login.apply{
            isClickable = true
            setTextColor(ContextCompat.getColor(this.context, R.color.black))
        }


    // disable the login request
    private fun disableLogin() =
        binding.login.apply {
            isClickable = false
            setTextColor(ContextCompat.getColor(this.context, R.color.silver))
        }

    // decides whether to show the Logout text or not
    private fun makeLogOutTextVisibleOrNot(){
        if(loginFragmentViewModel.checkIfTokenExists()){
            if(binding.logout.isGone){
                binding.logout.visibility = View.VISIBLE
            }
        }
        else{
            if(binding.logout.isVisible){
                binding.logout.visibility = View.GONE
            }
        }
    }

    /**
     * If the user has accepted to save his credentials before (e.g. after a previous login or registration process),
     * then there is no need to let him type them again.
     * We restore the credentials and fill the EditText inputs.
     * */
    private fun retrieveSavedUserCredentials() {
        val username : String? = loginFragmentViewModel.retrieveUsernameFromEncryptedSharedPrefs()
        val password: String ? = loginFragmentViewModel.retrievePasswordFromEncryptedSharedPrefs()

        // if there are no credentials stored, then disableLogin()
        if(username.isNullOrBlank() && password.isNullOrBlank()){
            disableLogin()
        }
        else{
            username?.let {emailString->
                password?.let{ pw ->
                    binding.apply{
                        // make them first empty
                        loginUsername.editText?.setText("")
                        loginPassword.editText?.setText("")
                        // then, fill the EditText inputs
                        loginUsername.editText?.setText(emailString)
                        loginPassword.editText?.setText(pw)
                    }

                    // validate the input data
                    loginFragmentViewModel.loginDataChanged(
                        binding.loginUsername.editText?.text.toString(),
                        binding.loginPassword.editText?.text.toString()
                    )
                }
            }

            // ... and enable login after some validation
            activateLogin()
        }
    }

    // if both fields are validated (not empty) then enable login request
    // disable otherwise
    private fun activateLogin(){
        if(validateLoginFields() ) enableLogin()
        else disableLogin()
    }

    // check if the login fields are not empty
    private fun validateLoginFields() : Boolean =
        !TextUtils.isEmpty(binding.loginUsername.editText?.text) && !TextUtils.isEmpty(binding.loginPassword.editText?.text)

    private fun dialogSaveUserCredentialsOrNot(email:String, username:String, password:String, token:String): AlertDialog{
        val builder = AlertDialog.Builder(requireContext())
        builder.apply{
            setMessage("Do you want to save your credentials?")
            setPositiveButton("Yes, save them.", DialogInterface.OnClickListener{ _, _ ->
                // save user credentials
                loginFragmentViewModel.saveUserDataIntoEncryptedSharedPreferences(
                    email = email,
                    username=username,
                    password=password,
                    token =token
                )

                // navigate to ProfileFragment after registration
                navController.navigate(LoginFragmentDirections.actionLogingFragmentToProfileFragment())
            })
            setNegativeButton("No, thanks", DialogInterface.OnClickListener{ dialog,_ ->
                dialog.cancel()
                // navigate to ProfileFragment after registration
                navController.navigate(LoginFragmentDirections.actionLogingFragmentToProfileFragment())
            })
        }
        return builder.create()
    }
}