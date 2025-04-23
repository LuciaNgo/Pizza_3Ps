package com.example.pizza3ps.activity

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.pizza3ps.R
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Arrays


class LogInActivity : AppCompatActivity() {
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val REQ_ONE_TAP = 2
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordToggle: ImageView
    private lateinit var logInButton: Button
    private lateinit var signUpButton: TextView
    private lateinit var backButton: ImageView
    private lateinit var facebookButton: ImageView
    private lateinit var googleButton: ImageView
    private var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        FacebookSdk.sdkInitialize(getApplicationContext())
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User đã đăng nhập trước đó, tự động vào màn hình chính
            if (currentUser.email == "adminpizza3ps@gmail.com")
                goToManagementHome()
            else
                goToDashboard()
        }

        auth = FirebaseAuth.getInstance()

        logInButton = findViewById(R.id.log_in_button)
        emailEditText = findViewById(R.id.email_input)
        passwordEditText = findViewById(R.id.password_input)
        backButton = findViewById(R.id.back_button)
        signUpButton = findViewById(R.id.sign_up_text)
        facebookButton = findViewById(R.id.facebook_button)
        googleButton = findViewById(R.id.google_button)
        passwordToggle = findViewById(R.id.password_toggle_icon)


        backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)

            startActivity(intent)
        }

        logInButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter full information!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("Email", email)

            Log.d("Email_pass", password)
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        if (email == "adminpizza3ps@gmail.com") {
                            goToManagementHome()
                        }
                        else {
                            goToDashboard()
                        }
                    } else {
                        Toast.makeText(this, "Error: Incorrect password or email", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    Toast.makeText(this@LogInActivity, "Facebook login canceled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(exception: FacebookException) {
                    Toast.makeText(this@LogInActivity, "Facebook login error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            })

        facebookButton.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        }

        setupOneTapSignIn()

        googleButton.setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this) { result ->
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent.intentSender, REQ_ONE_TAP,
                            null, 0, 0, 0
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e("OneTap", "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener(this) { e ->
                    Log.e("OneTap", e.localizedMessage ?: "One Tap Sign-In failed")
                }
        }
    }

    private fun setupOneTapSignIn() {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.google_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .setAutoSelectEnabled(true)
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager?.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            val ggCredential =
                                GoogleAuthProvider.getCredential(idToken, null)

                            signInWithCredential(ggCredential)
                        }
                        else -> {
                            Toast.makeText(this, "\nNo ID token!", Toast.LENGTH_SHORT).show()

                        }
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this, "\n${e.message.toString()}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleFacebookAccessToken(token: com.facebook.AccessToken) {
        val credential = com.google.firebase.auth.FacebookAuthProvider.getCredential(token.token)
        signInWithCredential(credential)
    }


    fun signInWithCredential(firebaseCredential: AuthCredential){
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
//                    Toast.makeText(this, "\nsignInWithCredential:success", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    val userId = user?.uid.toString()

                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("Users").document(userId)

                    userRef.get()
                        .addOnSuccessListener { document ->
                            Log.d("OneTap", "Checking user profile in Firestore")

                            if (document.exists()) {
                                goToDashboard()
                            } else {
                                // Chưa có profile → chuyển qua màn hình tạo profile
                                startActivity(Intent(this, AddProfileActivity::class.java))
                                finish()
                            }
                        }
                } else {
                    Toast.makeText(this, "\n${task.exception.toString()}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun saveUserToSharedPreferences(context: Context, user: FirebaseUser) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString("uid", user.uid)
            putString("displayName", user.displayName)
            putString("email", user.email)
            apply()
        }
    }

    fun getUserFromSharedPreferences(context: Context): Map<String, String?> {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return mapOf(
            "uid" to prefs.getString("uid", null),
            "displayName" to prefs.getString("displayName", null),
            "email" to prefs.getString("email", null),
        )
    }

    private fun goToDashboard() {
        Toast.makeText(this, "Log in successful!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToManagementHome() {
        Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, ManagementHomeActivity::class.java))
        finish()
    }
}