package celik.abdullah.authentication.fragment

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import celik.abdullah.authentication.R
import celik.abdullah.authentication.databinding.FragmentChangePasswordBinding
import celik.abdullah.authentication.utils.Const.showErrorDialog
import celik.abdullah.authentication.utils.Const.showNetworkIssuesDialog
import celik.abdullah.authentication.utils.EventObserver
import celik.abdullah.authentication.viewmodel.ChangePasswordFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint

/*
* The UI controller that helps a user to change his/her password.
* */
@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {
    private lateinit var binding: FragmentChangePasswordBinding
    private val changePasswordFragmentViewModel by viewModels<ChangePasswordFragmentViewModel>()
    private val navController by lazy{findNavController()}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = changePasswordFragmentViewModel
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        disableSending()        // disable sending until we have our required input
        setupObserver()         // observers for navigation & execution of network request
        setupInputListener()    // listeners for the two input fields
    }

    // handles user input (e.g. email & new pw)
    private fun setupInputListener() {
        binding.editTextEmail.editText?.doAfterTextChanged {
            binding.editTextEmail.error = null
            changePasswordFragmentViewModel.changePasswordFormDataChanged(
                binding.editTextEmail.editText?.text.toString(),
                binding.editTextNewPassword.editText?.text.toString())
        }
        binding.editTextNewPassword.editText?.doAfterTextChanged {
            binding.editTextNewPassword.error = null
            changePasswordFragmentViewModel.changePasswordFormDataChanged(
                binding.editTextEmail.editText?.text.toString(),
                binding.editTextNewPassword.editText?.text.toString())
        }
    }

    private fun setupObserver() {
        changePasswordFragmentViewModel.apply {

            // make the network request to change password
            changePasswordEvent.observe(viewLifecycleOwner, EventObserver{
                // make the progressbar visible
                binding.loading.visibility = View.VISIBLE

                // make the network request
                this.changePassword(
                    binding.editTextEmail.editText?.text.toString(),
                    binding.editTextNewPassword.editText?.text.toString())
            })

            changePasswordFormState.observe(viewLifecycleOwner, Observer { changePasswordFormState->
                // return
                if (changePasswordFormState == null) {
                    return@Observer
                }

                // if given input form data is okay, then enable registration
                if(changePasswordFormState.isDataValid) {
                    if(changePasswordFormState.emailError == null) binding.editTextEmail.error = null
                    if(changePasswordFormState.newPasswordError == null) binding.editTextNewPassword.error = null
                    enableSending()
                }
                // otherwise, prevent the user from sending the credentials
                else {
                    disableSending()
                }
                // inform the user that the email is wrong
                changePasswordFormState.emailError?.let{invalidEmail ->
                    binding.editTextEmail.error = getString(invalidEmail)
                }

                // inform the user that the password is wrong
                changePasswordFormState.newPasswordError?.let{invalidNewPassword->
                    binding.editTextNewPassword.error = getString(invalidNewPassword)
                }
            })

            changePasswordResult.observe(viewLifecycleOwner, Observer { changePasswordResult ->
                changePasswordResult  ?: return@Observer
                binding.loading.visibility = View.GONE

                // if everything is ok, then save the credentials & navigate back
                changePasswordResult.success?.let {newPassword ->
                    saveNewPasswordOrNotDialog(newPassword).show()
                }
                // notify the user that an error has been occurred
                changePasswordResult.error?.let {errorMessage ->
                    showErrorDialog(requireContext(), errorMessage).show()
                }

                // notify the user that a network error has been occured
                changePasswordResult.networkError?.let{networkError ->
                    showNetworkIssuesDialog(requireContext(), networkError).show()
                }
            })
        }
    }

    // enable sending
    private fun enableSending() =
        binding.changePassword.apply{
            isClickable = true
            setTextColor(ContextCompat.getColor(this.context, R.color.black))
        }


    // disable sending
    private fun disableSending() =
        binding.changePassword.apply {
            isClickable = false
            setTextColor(ContextCompat.getColor(this.context, R.color.silver))
        }

    private fun saveNewPasswordOrNotDialog(newPassword:String): AlertDialog{
        val builder = AlertDialog.Builder(requireContext())
        builder.apply{
            setMessage("Do you want to save your new password?")
            setPositiveButton("Yes, save it.", DialogInterface.OnClickListener{ _, _ ->
                // save the credentials for future login
                changePasswordFragmentViewModel.saveNewPassword(newPassword)
                // navigate back to where we were come from
                navController.navigateUp()
            })
            setNegativeButton("No, thanks.", DialogInterface.OnClickListener{ dialog, _ ->
                dialog.cancel()
                // navigate back to where we were come from
                navController.navigateUp()
            })
        }
        return builder.create()
    }
}