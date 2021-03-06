package com.zaeem.myapplication.ui

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.zaeem.myapplication.R
import com.zaeem.myapplication.common.enums.ConversationsError
import com.zaeem.myapplication.common.extensions.enableErrorResettingOnTextChanged
import com.zaeem.myapplication.common.extensions.getErrorMessage
import com.zaeem.myapplication.common.extensions.lazyViewModel
import com.zaeem.myapplication.common.extensions.onSubmit
import com.zaeem.myapplication.common.injector
import com.zaeem.myapplication.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    val loginViewModel by lazyViewModel { injector.createLoginViewModel(application) }

    private val noInternetSnackBar by lazy {
        Snackbar.make(binding.loginCoordinatorLayout, R.string.no_internet_connection, Snackbar.LENGTH_INDEFINITE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loginViewModel.isLoading.observe(this) { isLoading ->
            showProgress(isLoading)
        }

        loginViewModel.isNetworkAvailable.observe(this) { isNetworkAvailable ->
            showNoInternetSnackbar(!isNetworkAvailable)
        }

        loginViewModel.onSignInError.observe(this) { signInError ->
            signInFailed(signInError)
        }

        loginViewModel.onSignInSuccess.observe(this) {
            signInSucceeded()
        }

        binding.usernameInputLayout.enableErrorResettingOnTextChanged()
        binding.passwordInputLayout.enableErrorResettingOnTextChanged()
        binding.passwordTv.onSubmit { signInPressed() }
        binding.signInBtn.setOnClickListener { signInPressed() }

        if (savedInstanceState == null) {
            showStartupSnackbarIfNeeded()
        }
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }

    private fun showProgress(show: Boolean) {
        Timber.d("showProgress: $show")
        binding.loginProgress.root.visibility = if (show) VISIBLE else GONE
        binding.loginLayout.visibility = if (show) GONE else VISIBLE
    }

    private fun signInPressed() {
        Timber.d("signInPressed")
        val identity = binding.usernameTv.text.toString()
        val password = binding.passwordTv.text.toString()

        loginViewModel.signIn(identity, password)
    }

    private fun goToConversationListScreen() {
        Timber.d("go to next screen")
        Timber.d("startActivity ConversationListActivity")
        startActivity(Intent(this, ConversationListActivity::class.java))
    }

    private fun signInSucceeded() = goToConversationListScreen()

    private fun signInFailed(error: ConversationsError) {
        when (error) {
            ConversationsError.EMPTY_USERNAME -> usernameInputLayout.error = getString(R.string.enter_username)

            ConversationsError.EMPTY_PASSWORD -> passwordInputLayout.error = getString(R.string.enter_password)

            ConversationsError.EMPTY_USERNAME_AND_PASSWORD -> {
                usernameInputLayout.error = getString(R.string.enter_username)
                passwordInputLayout.error = getString(R.string.enter_password)
            }

            ConversationsError.TOKEN_ACCESS_DENIED -> passwordInputLayout.error = getString(R.string.token_access_denied)

            ConversationsError.NO_INTERNET_CONNECTION,
            ConversationsError.TOKEN_ERROR -> showNoInternetDialog()

            else -> passwordInputLayout.error = getErrorMessage(error, R.string.sign_in_error)
        }
    }

    private fun showNoInternetDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.no_internet_dialog_title)
            .setMessage(R.string.no_internet_dialog_message)
            .setNegativeButton(R.string.close, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isAllCaps = false
        }

        dialog.show()
    }

    private fun showNoInternetSnackbar(show: Boolean) {
        Timber.d("showNoInternetSnackbar: $show")

        if (show) {
            noInternetSnackBar.show()
        } else {
            noInternetSnackBar.dismiss()
        }
    }

    private fun showStartupSnackbarIfNeeded() {
        val error = intent.getSerializableExtra(EXTRA_ERROR) as? ConversationsError ?: return
        if (error == ConversationsError.NO_STORED_CREDENTIALS) return

        showSnackbar(getErrorMessage(error))
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.loginCoordinatorLayout, message, Snackbar.LENGTH_LONG)
            .show()
    }

    companion object {

        private val EXTRA_ERROR = "EXTRA_ERROR"

        fun start(context: Context, error: ConversationsError = ConversationsError.NO_ERROR) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(EXTRA_ERROR, error)

            // Should always start LoginActivity with these flags
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
