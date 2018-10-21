package com.spaceapps.dovahkiin.thuum

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.spaceapps.dovahkiin.thuum.R.id.*
import com.spaceapps.dovahkiin.thuum.R.string.login
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.registerdialog.view.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.longToast
import org.jetbrains.anko.*

private val SMS_REQUEST_CODE=101
class MainActivity : Activity() {

    private val RC_SIGN_IN = 123
    var mAuth = FirebaseAuth.getInstance()!!
    private lateinit var dialog: ProgressDialog
    var user: FirebaseUser? = null

    override fun onStart(){
        super.onStart()
    }

    class userinfo(val name: String, val email: String)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = Slide()
            exitTransition = Slide()
        }
        setContentView(R.layout.activity_main)

        val view = View.inflate(this, R.layout.registerdialog, null)

        mAuth = FirebaseAuth.getInstance()
        loginbutton.setOnClickListener {
            if (isEmailValid(emaillogin.text.toString())) {
                login(emaillogin.text.toString(), passwordlogin.text.toString())
                val context: Context = this@MainActivity
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                //dialog = indeterminateProgressDialog(message = "Please wait a bit...",title="Logging in")
            } else {
                longToast("The Email entered is Invalid")
            }
        }
        registerbutton.setOnClickListener {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setView(view)
            val dialog: AlertDialog = builder.create()
            val newdialog: Dialog? = null
            dialog.show()
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            view.alertbuttonregister.setOnClickListener {
                //dialog.dismiss()
                if (isEmailValid(view.registeremail.text.toString())) {
                    val context: Context = this@MainActivity
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                    val dialog2 = indeterminateProgressDialog(message = "Creating new user...", title = "Registering")
                    mAuth.createUserWithEmailAndPassword(
                        view.registeremail.text.toString(),
                        view.registerpass.text.toString()
                    )
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                //view.dialogname
                                Log.i("task_successful", "New User created")
                                //val mD = FirebaseDatabase.getInstance().getReference("users")
                                dialog2.dismiss()
                                Toast.makeText(this@MainActivity, "New User Created", Toast.LENGTH_LONG).show()
                                user = mAuth.currentUser
                                user?.sendEmailVerification()?.addOnSuccessListener {
                                    longToast("Verification Email has been sent. Please verify your account.")
                                    Log.i("requeststatus", "Email Verification Sent")
                                }?.addOnFailureListener {
                                    Log.i("requeststatus", "Email Verification Not Sent")

                                }
                                dialog.dismiss()
                                login(view.registeremail.text.toString(), view.registerpass.text.toString())
                            } else {

                                Log.i("task_failed", "User Creation falied")
                                dialog2.dismiss()
                                Toast.makeText(this@MainActivity, "User Registration Failed", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    longToast("The Email entered is Invalid")
                }
            }

        }
    }

    private fun login(email: String, password: String){
        val context: Context= this@MainActivity
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)
        //val dialog = progressDialog(message="Please wait a bit...",title="Logging In")
        dialog = indeterminateProgressDialog(message = "Please wait a bit...",title="Logging in")
        mAuth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    Log.i("user_login","Logged In Successfully")
                    user= mAuth.currentUser!!
                    Toast.makeText(this@MainActivity,"Logged in as ${user!!.email.toString()}",Toast.LENGTH_LONG).show()
                    startActivity<OptionSelector>()
                    finish()

                }
                else{
                    Log.i("user_login","Login Failed")

                }
            }.addOnFailureListener {
                dialog.dismiss()
                longToast("Login failed. Email or Password may be incorrect or Internet may be unavailable")

            }

    }

    private fun isEmailValid(email: String): Boolean{
        if (email == null)
            return false
        else{
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

}
