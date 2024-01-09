package com.chandan.instagramck

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chandan.instagramck.Adapter.ReelAdapter
import com.chandan.instagramck.Model.Reel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ReelFragment : Fragment() {
    private var reelAdapter: ReelAdapter?=null
    private var reelList:MutableList<Reel>?=null
    private var followingList:MutableList<Reel>?=null



    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_reel, container, false)

        var recyclerView: RecyclerView? = null
        recyclerView=view.findViewById(R.id.recycler_view_reel)
        val linearLayoutManager= LinearLayoutManager(context)
        linearLayoutManager.reverseLayout=true
        linearLayoutManager.stackFromEnd=true
        recyclerView.layoutManager=linearLayoutManager

        reelList=ArrayList()
        reelAdapter=context?.let { ReelAdapter(it,reelList as ArrayList<Reel>) }
        recyclerView.adapter=reelAdapter

        checkFollowing()

        return view
    }



    private fun checkFollowing() {
        followingList=ArrayList()
        val followingRef= FirebaseDatabase.getInstance().reference.
        child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    (followingList as ArrayList<String>).clear()
                    for (snapshot in snapshot.children)
                    {
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }
                    retrieveFollowingReels()
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
    private fun retrieveFollowingReels() {
        val reelsRef= FirebaseDatabase.getInstance().reference.child("Reels")
        reelsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reelList?.clear()
                for (snapshot in snapshot.children)
                {
                    val reel=snapshot.getValue(Reel::class.java)

                    for (id in (followingList as ArrayList<String>))
                    {
                        if (reel!!.getPublisher()==id){
                            reelList!!.add(reel)
                        }
                        reelAdapter!!.notifyDataSetChanged()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}


