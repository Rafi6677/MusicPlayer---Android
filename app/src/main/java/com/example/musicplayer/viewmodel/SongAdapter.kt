package com.example.musicplayer.viewmodel

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.musicplayer.R
import com.example.musicplayer.model.Song

class SongAdapter(context: Context, val songs: ArrayList<Song>): RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    private var activityItem: ItemClicked = context as ItemClicked

    interface ItemClicked {
        fun onItemClicked(index: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var songName: TextView = itemView.findViewById(R.id.songName_TextView)
        var artistName: TextView = itemView.findViewById(R.id.artistName_TextView)

        init {
            itemView.setOnClickListener {
                activityItem.onItemClicked(songs.indexOf(it.tag as Song))
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.songs_row, viewGroup, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.itemView.tag = songs[i]

        val song: Song = songs[i]

        viewHolder.songName.text = song.title
        viewHolder.artistName.text = song.artist
    }

}