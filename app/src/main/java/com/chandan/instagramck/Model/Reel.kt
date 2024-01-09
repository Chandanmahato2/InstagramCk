package com.chandan.instagramck.Model

class Reel {
    private var reelid:String=""
    private var reelvideo:String=""
    private var publisher:String=""
    private var content:String=""

    constructor()
    constructor(reelid: String, reelvideo: String, publisher: String, content: String) {
        this.reelid = reelid
        this.reelvideo = reelvideo
        this.publisher = publisher
        this.content = content
    }

    fun getReelid():String{
        return reelid
    }
    fun setReelid(reelid: String)
    {
        this.reelid=reelid
    }
    fun getReelvideo():String{
        return reelvideo
    }
    fun setReelvideo(reelvideo: String)
    {
        this.reelvideo=reelvideo
    }
    fun getPublisher():String{
        return publisher
    }
    fun setPublisher(publisher: String){
        this.publisher=publisher
    }
    fun getContent():String{
        return content
    }
    fun setContent(content: String)
    {
        this.content=content
    }


}



