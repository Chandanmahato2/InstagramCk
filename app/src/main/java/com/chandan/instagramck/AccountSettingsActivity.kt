package com.chandan.instagramck


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.chandan.instagramck.Model.User
import com.chandan.instagramck.databinding.ActivityAccountSettingsBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.bio_profile_frags
import kotlinx.android.synthetic.main.activity_account_settings.close_profile_btn
import kotlinx.android.synthetic.main.activity_account_settings.delete_account_btn_profile_frags
import kotlinx.android.synthetic.main.activity_account_settings.fullname_profile_frag
import kotlinx.android.synthetic.main.activity_account_settings.logout_btn
import kotlinx.android.synthetic.main.activity_account_settings.profile_image_view_frag
import kotlinx.android.synthetic.main.activity_account_settings.upload_image_text_btn
import kotlinx.android.synthetic.main.activity_account_settings.username_profile_frag

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImg:Uri
    private lateinit var dialog: AlertDialog.Builder
    private lateinit var binding: ActivityAccountSettingsBinding
    private var checker =""
    private var myUrl =""
    private var storageProfilePicRef:StorageReference?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser=FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef=FirebaseStorage.getInstance().reference.child("Profile Pictures")
        binding= ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog=AlertDialog.Builder(this).setMessage("Updating").setCancelable(false)

        database= FirebaseDatabase.getInstance()
        storage= FirebaseStorage.getInstance()
        binding.changeImageTextBtn.setOnClickListener {
            val intent=Intent()
            intent.action=Intent.ACTION_GET_CONTENT
            intent.type="image/*"
            startActivityForResult(intent,1)

        }
        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent= Intent(this@AccountSettingsActivity, SigninActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

       binding.saveInfoProfileBtn.setOnClickListener {
            if (checker=="clicked")
            {

            }
            else{
                updateUserInfoOnly()
            }
        }
        close_profile_btn.setOnClickListener {
            val intent=Intent(this@AccountSettingsActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        userInfo()
        upload_image_text_btn.setOnClickListener {
            if (userprofilepicturedefault()==null
            ){
                Toast.makeText(this, "Please select image first.", Toast.LENGTH_LONG).show()
            }else{
                uploadImage()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data!=null){
            if (data.data!=null){
                selectedImg=data.data!!
                binding.profileImageViewFrag.setImageURI(selectedImg)
                profile_image_view_frag.setImageURI(selectedImg)
            }
        }
    }


    private fun updateUserInfoOnly() {
        when {
            fullname_profile_frag.text.toString()=="" -> {
                Toast.makeText(this, "Please enter Full Name First", Toast.LENGTH_LONG)
                    .show()
            }
            username_profile_frag.text.toString() =="" -> {
                Toast.makeText(this, "Please enter username First", Toast.LENGTH_LONG)
                    .show()
            }
            bio_profile_frags.text.toString() =="" -> {
                Toast.makeText(this, "Please enter Bio First", Toast.LENGTH_LONG)
                    .show()
            }
            else -> {
                val userRef= FirebaseDatabase.getInstance().getReference().child("users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] =fullname_profile_frag.text.toString().lowercase()
                userMap["username"] =username_profile_frag.text.toString().lowercase()
                userMap["bio"] = bio_profile_frags.text.toString().lowercase()

                userRef.child(firebaseUser.uid).updateChildren(userMap)
                Toast.makeText(this, "Account Information has been updated successfully", Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(this@AccountSettingsActivity,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun userprofilepicturedefault(){
        val userRef= FirebaseDatabase.getInstance().reference.child("users")
            .child(firebaseUser.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    val user = pO.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profile_image_view_frag)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun userInfo(){
        val userRef= FirebaseDatabase.getInstance().reference.child("users")
            .child(firebaseUser.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    val user = pO.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profile_image_view_frag)
                    username_profile_frag.setText(user!!.getUsername())
                    fullname_profile_frag.setText(user!!.getFullname())
                    bio_profile_frags.setText(user!!.getBio())
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun uploadImage() {
                dialog = AlertDialog.Builder(this).setMessage("Uploading..").setCancelable(false)
                dialog.show()
                val fileRef = storageProfilePicRef!!.child(firebaseUser!!.uid + ".jpg")
                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(selectedImg!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()
                        val ref = FirebaseDatabase.getInstance().reference.child("users")
                        val userMap = HashMap<String, Any>()
                        userMap["image"] = myUrl
                        ref.child(firebaseUser.uid).updateChildren(userMap)
                        Toast.makeText(
                            this,
                            "Account Information has been updated successfully.",
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Failed.", Toast.LENGTH_LONG).show()
                    }
                })
    }

}



