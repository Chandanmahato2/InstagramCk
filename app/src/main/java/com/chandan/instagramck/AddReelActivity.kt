package com.chandan.instagramck

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.chandan.instagramck.databinding.ActivityAddReelBinding
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
import kotlinx.android.synthetic.main.activity_add_reel.reel_view_add_fragmet_image
import kotlinx.android.synthetic.main.activity_add_reel.upload_reel_add_fragment

class AddReelActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImg: Uri
    private lateinit var dialog: AlertDialog.Builder
    private lateinit var binding:   ActivityAddReelBinding
    private var myUrl =""
    private var storagePostPicRef: StorageReference?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reel)


        firebaseUser=FirebaseAuth.getInstance().currentUser!!
        storagePostPicRef=FirebaseStorage.getInstance().reference.child("Post Reels")
        binding= ActivityAddReelBinding.inflate(layoutInflater)
        setContentView(binding.root)


        database= FirebaseDatabase.getInstance()
        storage= FirebaseStorage.getInstance()
        binding.selectReelAddFragment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "video/*"
            startActivityForResult(intent, 4)
        }
        binding.reelViewAddFragmetImage.setOnPreparedListener {
            it.start()
        }
        upload_reel_add_fragment.setOnClickListener {
            uploadreels()
        }


    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data!=null){
            if (data.data!=null){
                selectedImg=data.data!!
                binding.reelViewAddFragmetImage.setVideoURI(selectedImg)
                reel_view_add_fragmet_image.setVideoURI(selectedImg)
            }
        }
    }
    private fun uploadreels() {
        dialog=AlertDialog.Builder(this).setMessage("Uploading..").setCancelable(false)
        dialog.show()
        val fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString()+ ".video")
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

                val ref = FirebaseDatabase.getInstance().reference.child("Reels")
                val reelId=ref.push().key

                val userMap = HashMap<String, Any>()
                userMap["reelid"]=reelId!!
                userMap["content"]=Content_add_fragment.text.toString().lowercase()
                userMap["publisher"]= FirebaseAuth.getInstance().currentUser!!.uid
                userMap["reelvideo"] = myUrl

                ref.child(reelId).updateChildren(userMap)

                Toast.makeText(this, "Post updated successfully", Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(this@AddReelActivity, MainActivity::class.java)
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



