

package com.chandan.instagramck

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_up.email_signup
import kotlinx.android.synthetic.main.activity_sign_up.fullname_signup
import kotlinx.android.synthetic.main.activity_sign_up.password_signup
import kotlinx.android.synthetic.main.activity_sign_up.signin_link_btn
import kotlinx.android.synthetic.main.activity_sign_up.signup_btn
import kotlinx.android.synthetic.main.activity_sign_up.username_signup
import kotlinx.android.synthetic.main.activity_signin.signup_link_btn
import java.util.Locale

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        signin_link_btn.setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
        }
        signup_btn.setOnClickListener {
            CreateAccount()
        }
    }

    private fun CreateAccount() {
        val fullName=fullname_signup.text.toString()
        val userName=username_signup.text.toString()
        val email=email_signup.text.toString()
        val password=password_signup.text.toString()

        when{
            TextUtils.isEmpty(fullName) -> Toast.makeText(this,"full name is required",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this,"username is required",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this,"email is required",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this,"password name is required",Toast.LENGTH_LONG).show()

            else->{
                //progress bar
                val progressDialog=ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("signup")
                progressDialog.setMessage("please wait")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                // Firebase authentication
                val mAuth: FirebaseAuth= FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener{ task ->
                        if (task.isSuccessful){
                            saveUserInfo(userName,fullName,email,progressDialog)

                        }
                        else{
                            val message=task.exception!!.toString()
                            Toast.makeText(this, "Error:$message",Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }

    }

    // real time database data save
    private fun saveUserInfo(
        userName: String,
        fullName: String,
        email: String,
        progressDialog: ProgressDialog
    ) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserId
        userMap["fullname"] = fullName.lowercase(Locale.getDefault())
        userMap["username"] = userName.lowercase(Locale.getDefault())
        userMap["email"] = email
        userMap["bio"] = "i am using clone app"
        userMap["image"] =
            "https://firebasestorage.googleapis.com/v0/b/instagram-clone-427c7.appspot.com/o/Default%20images%2Fprofile.png?alt=media&token=5775d984-b99a-4c1f-a310-97e2b33cfc92"
        usersRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account has been created successfully", Toast.LENGTH_LONG)
                        .show()

                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(currentUserId)
                            .child("Following").child(currentUserId)
                            .setValue(true)


                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "Error:$message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()

                }

            }

    }
}