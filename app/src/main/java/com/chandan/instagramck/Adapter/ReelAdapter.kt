package com.chandan.instagramck.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.chandan.instagramck.Model.Reel
import com.chandan.instagramck.Model.User
import com.chandan.instagramck.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ReelAdapter (private val mContext: Context,
private val mReel:List<Reel>): RecyclerView.Adapter<ReelAdapter.ViewHolder>() {
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_reel_layout, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return mReel.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val reel = mReel[position]
        holder.content.text = reel.getContent()
        setVideoUrl(reel,holder)
        publisherInfo(holder.profileImage, holder.userName, holder.publisher, reel.getPublisher())


    }

    private fun setVideoUrl(reel: Reel, holder: ViewHolder) {
     //   holder.progressBar.visibility=View.VISIBLE
        val videoUrl:String=reel.getReelvideo()
        val videoUri=Uri.parse(videoUrl)
        holder.reelvideo.setVideoURI(videoUri)
        holder.reelvideo.requestFocus()
        holder.reelvideo.setOnPreparedListener{mediaPlayer ->
            mediaPlayer.start()
        }
    /*    holder.reelvideo.setOnInfoListener (MediaPlayer.OnInfoListener { mp, what, extra ->

            when(what){
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START->{
                    holder.progressBar.visibility=View.VISIBLE
                    return@OnInfoListener true

                }
                MediaPlayer.MEDIA_INFO_BUFFERING_START->{
                    holder.progressBar.visibility=View.VISIBLE
                    return@OnInfoListener true


                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END->{
                    holder.progressBar.visibility=View.GONE
                    return@OnInfoListener true

                }
            }

            false

        })*/
        holder.reelvideo.setOnCompletionListener { mediaPlayer->
            mediaPlayer.start()
        }

    }


    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView
        var reelvideo: VideoView
        var userName: TextView
        var publisher: TextView
        var content: TextView
       // var progressBar: ProgressBar

        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_reel)
            reelvideo = itemView.findViewById(R.id.reel_image_home)
            userName = itemView.findViewById(R.id.user_name_reel)
            publisher = itemView.findViewById(R.id.publisher)
            content = itemView.findViewById(R.id.content)
         //   progressBar=itemView.findViewById(R.id.progressBar)

        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun publisherInfo(
        profileImage: CircleImageView,
        userName: TextView,
        publisher: TextView,
        publisherID: String
    ) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(publisherID)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profileImage)
                    userName.text = user!!.getUsername()
                    publisher.text = user!!.getFullname()


                }

            }


            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}



