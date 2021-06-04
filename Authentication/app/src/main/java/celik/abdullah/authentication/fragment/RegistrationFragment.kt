package celik.abdullah.authentication.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import celik.abdullah.authentication.R
import celik.abdullah.authentication.databinding.FragmentRegistrationBinding
import celik.abdullah.authentication.utils.Const.showErrorDialog
import celik.abdullah.authentication.utils.Const.showNetworkIssuesDialog
import celik.abdullah.authentication.utils.EventObserver
import celik.abdullah.authentication.viewmodel.RegistrationFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint

/*
* The UI controller that deals with the registration process
* */
@AndroidEntryPoint
class RegistrationFragment : Fragment() {
    private lateinit var binding: FragmentRegistrationBinding
    private val registrationFragmentViewModel by viewModels<RegistrationFragmentViewModel>()
    private val navController by lazy {findNavController()}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        disableRegistration()        // disable registration request until required fields are set
        setupInputListeners()       // set listeners for the input fields
        setupObservers()            // set observers for navigation and making the registration request

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = registrationFragmentViewModel
        }

        // Inflate the layout for this fragment
        return binding.root
    }


    // listeners for the input fields
    private fun setupInputListeners() {
        binding.registerEmail.editText?.doAfterTextChanged {
            binding.registerEmail.error = null
            registrationFragmentViewModel.registrationDataChanged(
                binding.registerEmail.editText?.text.toString(),
                binding.registerUsername.editText?.text.toString(),
                binding.registerPassword.editText?.text.toString()
            )
        }
        binding.registerUsername.editText?.doAfterTextChanged {
            binding.registerUsername.error = null
            registrationFragmentViewModel.registrationDataChanged(
                binding.registerEmail.editText?.text.toString(),
                binding.registerUsername.editText?.text.toString(),
                binding.registerPassword.editText?.text.toString()
            )
        }
        binding.registerPassword.editText?.doAfterTextChanged {
            binding.registerPassword.error = null
            registrationFragmentViewModel.registrationDataChanged(
                binding.registerEmail.editText?.text.toString(),
                binding.registerUsername.editText?.text.toString(),
                binding.registerPassword.editText?.text.toString()
            )
        }
    }

    // enable sending of the credentials for registration
    private fun enableRegistration(){
        binding.register.apply{
            isClickable = true
            setTextColor(ContextCompat.getColor(this.context, R.color.black))
        }
    }

    // disable sending of the credentials for registration
    private fun disableRegistration(){
        binding.register.apply {
            isClickable = false
            setTextColor(ContextCompat.getColor(this.context, R.color.silver))
        }
    }

    private fun setupObservers() {
        registrationFragmentViewModel.apply {

            // triggers the registration procedure
            startRegistration.observe(viewLifecycleOwner, EventObserver{

                // make the progressbar visible
                binding.loading.visibility = View.VISIBLE

                // pass the credentials so that registration can begin
                registrationFragmentViewModel.register(
                    binding.registerUsername.editText?.text.toString(),
                    binding.registerEmail.editText?.text.toString(),
                    binding.registerPassword.editText?.text.toString()
                )
            })

            // listens for type errors made by the user during the registration
            registrationFormState.observe(viewLifecycleOwner, Observer{ registrationFormState ->

                // return
                if (registrationFormState == null) {
                    return@Observer
                }

                // if given input form data is okay, then enable registration
                if(registrationFormState.isDataValid) {
                    if(registrationFormState.emailError == null) binding.registerEmail.error = null
                    if(registrationFormState.usernameError == null) binding.registerUsername.error = null
                    if(registrationFormState.passwordError == null) binding.registerPassword.error = null
                    enableRegistration()
                }
                // otherwise, prevent the user from sending the credentials
                else {
                    disableRegistration()
                }
                // inform the user that the email is wrong
                registrationFormState.emailError?.let{invalidEmail ->
                    binding.registerEmail.error = getString(invalidEmail)
                }
                // inform the user that the username is wrong
                registrationFormState.usernameError?.let{invalidUsername ->
                    binding.registerUsername.error = getString(invalidUsername)
                }
                // inform the user that the password is wrong
                registrationFormState.passwordError?.let{invalidPassword->
                    binding.registerPassword.error = getString(invalidPassword)
                }
            })

            // listen for the result of the registration process
            registrationResult.observe(viewLifecycleOwner, Observer{ registerResult ->
                registerResult  ?: return@Observer

                // scenario I: our network request was a success
                // if registration procedure was successful,
                // then store the user data into Room and ask the user if he wants to save his/her
                // credentials for a future login
                registerResult.success?.let {
                    binding.loading.visibility = View.GONE
                    // save the user data with its token to the local database
                    storeUserDataLocally(it.user)
                    dialogSaveUserCredentialsOrNot(
                        email = it.user.userEmail,
                        username = it.user.userName,
                        password = binding.registerPassword.editText?.text.toString(),
                        token = it.token
                    ).show()
                }
                // scenario II: our network request did not work because of a specific error
                // notify the user that an error has been occurred during registration
                // e.g. if the username already exists, then tell it to the user so he/she can choose another username,
                //      if the email already exists, then tell it to the user so he/she can choose another email address
                //      etc.
                registerResult.error?.let {errorMessage ->
                    binding.loading.visibility = View.GONE
                    showErrorDialog(requireContext(), errorMessage).show()
                }

                // scenario III: our network request did not work because the user has no internet
                // notify the user that we have a problem with the internet connection
                // e.g. when the smartphone might be in airplane-mode or the user has no access to the internet somehow
                registerResult.networkError?.let { networkErrorMessage ->
                    binding.loading.visibility = View.GONE
                    showNetworkIssuesDialog(requireContext(), networkErrorMessage).show()
                }
            })
        }
    }

    /*
    * Function that creates a dialog to let the user decide if he/she wants to save his/her credentials.
    * */
    private fun dialogSaveUserCredentialsOrNot(email:String, username:String, password:String, token:String): AlertDialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply{
            setMessage("Do you want to save your credentials?")
            setPositiveButton("Yes, save them.", DialogInterface.OnClickListener{ _, _ ->
                // save user credentials
                registrationFragmentViewModel.saveUserDataIntoEncryptedSharedPreferences(
                    email = email,
                    username=username,
                    password=password,
                    token =token
                )

                // navigate to ProfileFragment after registration
                navController.navigate(RegistrationFragmentDirections.actionRegistrationFragmentToProfileFragment())
            })
            setNegativeButton("No, thanks.", DialogInterface.OnClickListener{ dialog, _ ->
                dialog.cancel()
                // navigate to ProfileFragment after registration
                navController.navigate(RegistrationFragmentDirections.actionRegistrationFragmentToProfileFragment())
            })
        }
        return builder.create()
    }
}