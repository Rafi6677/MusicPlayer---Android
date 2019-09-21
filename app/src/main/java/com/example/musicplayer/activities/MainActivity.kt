package com.example.musicplayer.activities

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.Toast
import com.example.musicplayer.R
import com.example.musicplayer.model.Song
import com.example.musicplayer.viewmodel.SongAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var songs: ArrayList<Song> = ArrayList()
    private lateinit var songAdapter: SongAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
        loadSongs()

        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        songList_RecyclerView.layoutManager = layoutManager
        songList_RecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        songList_RecyclerView.adapter = songAdapter
    }


    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 123)
                return
            }
        } else {
            loadSongs()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            123 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadSongs()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT)
                        .show()
                    checkPermission()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun loadSongs() {
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection: String = MediaStore.Audio.Media.IS_MUSIC + "!=0"
        val cursor: Cursor = contentResolver.query(uri, null, selection, null, null) as Cursor

        if (cursor.moveToFirst()) {
            do {
                val name: String = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val artist: String = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val url: String = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))

                val song = Song(name, artist, url)
                songs.add(song)
            } while (cursor.moveToNext())
        }

        cursor.close()

        songs.sortBy {
            it.title
        }

        songsButton.text = "Utwory (${songs.size})"

        songAdapter = SongAdapter(this, songs)
    }
}
