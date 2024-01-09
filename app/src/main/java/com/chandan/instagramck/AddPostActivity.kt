package com.chandan.instagramck

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.chandan.instagramck.databinding.ActivityAddPostBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_post.Content_add_fragment
import kotlinx.android.synthetic.main.activity_add_post.image_view_add_fragmet_image
import kotlinx.android.synthetic.main.activity_add_post.upload_image_add_fragment

class AddPostActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImg: Uri
    private lateinit var dialog: AlertDialog.Builder
    private lateinit var binding:   ActivityAddPostBinding
    private var myUrl =""
    private var storagePostPicRef: StorageReference?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        firebaseUser=FirebaseAuth.getInstance().currentUser!!
        storagePostPicRef=FirebaseStorage.getInstance().reference.child("Post Pictures")
        binding= ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database= FirebaseDatabase.getInstance()
        storage= FirebaseStorage.getInstance()
        binding.selectImageAddFragment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 2)
        }

        upload_image_add_fragment.setOnClickListener {
                uploadpost()
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data!=null){
            if (data.data!=null){
                selectedImg=data.data!!
                binding.imageViewAddFragmetImage.setImageURI(selectedImg)
                image_view_add_fragmet_image.setImageURI(selectedImg)
            }
        }
    }
    private fun uploadpost() {
        dialog=AlertDialog.Builder(this).setMessage("Uploading..").setCancelable(false)
        dialog.show()
        val fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString()+ ".jpg")
        var uploadTask: StorageTask<*>
        uploadTask = fileRef.putFile(selectedImg!!)
        uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
            if (!task.isSuccessful)
            {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation fileRef.downloadUrl
        }).addOnCompleteListener (OnCompleteListener<Uri> { task ->
            if (task.isSuccessful)
            {
                val downloadUrl = task.result
                myUrl = downloadUrl.toString()

                val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                val postId=ref.push().key

                val userMap = HashMap<String, Any>()
                userMap["postid"]=postId!!
                userMap["description"]=Content_add_fragment.text.toString().lowercase()
                userMap["publisher"]= FirebaseAuth.getInstance().currentUser!!.uid
                userMap["postimage"] = myUrl

                ref.child(postId).updateChildren(userMap)

                Toast.makeText(this, "Post updated successfully", Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(this@AddPostActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else
            {
                Toast.makeText(this, "failed", Toast.LENGTH_LONG)
                    .show()
            }
        } )
    }
}