package com.chandan.instagramck

import   android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chandan.instagramck.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.*






class ProfileFragment : Fragment() {
    private lateinit var profileId:String
    private lateinit var firebaseUser: FirebaseUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        //edit profile button convert into follow and following
        firebaseUser=FirebaseAuth.getInstance().currentUser!!
        val pref=context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)
        if (pref!=null)
        {
            this.profileId= pref.getString("profileId","none").toString()
        }
        if (profileId == firebaseUser.uid)
        {
            view.edit_account_settings_btn.text="Edit Profile"
        }
        else if (profileId != firebaseUser.uid)
        {
            checkFollowAndFollowigButton()
        }
        view.edit_account_settings_btn.setOnClickListener {
           val getButtonText=view.edit_account_settings_btn.text.toString()
            when{
                getButtonText=="Edit Profile"->startActivity(Intent(context,AccountSettingsActivity::class.java))

                getButtonText=="Follow"-> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(it1.toString())
                            .child("Following").child(profileId).setValue(true)
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(profileId)
                            .child("Followers").child(it1.toString()).setValue(true)
                    }
                }
                getButtonText=="Following"-> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(it1.toString())
                            .child("Following").child(profileId).removeValue()
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(profileId)
                            .child("Followers").child(it1.toString()).removeValue()
                    }
                }
            }

        }

        getFollowers()
        getFollowings()
        userInfo()

        //for profile downside image


        return view
    }



    private fun checkFollowAndFollowigButton() {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString())
                .child("Following")
        }
        if (followingRef!=null)
        {
            followingRef.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(pO: DataSnapshot) {
                    if (pO.child(profileId).exists())
                    {
                        view?.edit_account_settings_btn?.text  ="Following"
                    }
                    else{
                        view?.edit_account_settings_btn?.text  ="Follow"
                    }
                }

                override fun onCancelled(pO: DatabaseError) {
                }
            })
        }
    }
//edit profile button convert into follow and following

    private fun getFollowers()
    {
        val followersRef= FirebaseDatabase.getInstance().reference.child("Follow").
        child(profileId).child("Followers")

        followersRef.addValueEventListener(object :ValueEventListener
        {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists())
                {
                    view?.total_followers?.text= pO.childrenCount.toString()
                }
            }

            override fun onCancelled(pO: DatabaseError) {

            }
        })

    }
    private fun getFollowings()
    {
        val followersRef= FirebaseDatabase.getInstance().reference.child("Follow")
            .child(profileId).child("Following")

        followersRef.addValueEventListener(object :ValueEventListener
        {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists())
                {
                    view?.total_following?.text= pO.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }
    private fun userInfo(){
        val userRef=FirebaseDatabase.getInstance().getReference().child("users").child(profileId.toString())
        userRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists())
                {
                    val user=pO.getValue (User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(view?.pro_image_profile_frag)
                    view?.profile_fragment_username?.text=user!!.getUsername()
                    view?.full_name_profile_frag?.text=user!!.getFullname()
                    view?.bio_profile_frag?.text=user!!.getBio()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

   override fun onStop() {
        super.onStop()
        val pref= context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref= context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref= context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }
}