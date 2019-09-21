package com.example.musicplayer.activities

import android.Manifest
import android.content.Intent
import android.content.ServiceConnection
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
import com.example.musicplayer.model.MusicService
import com.example.musicplayer.model.Song
import com.example.musicplayer.viewmodel.SongAdapter
import kotlinx.android.synthetic.main.activity_main.*
import android.content.ComponentName
import android.content.Context
import com.example.musicplayer.model.MusicService.MusicBinder
import android.os.IBinder
import android.support.v4.app.SupportActivity
import android.support.v4.app.SupportActivity.ExtraData
import android.support.v4.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.Menu
import android.view.MenuItem


class MainActivity : AppCompatActivity(), SongAdapter.ItemClicked {

    private var songs: ArrayList<Song> = ArrayList()
    private lateinit var songAdapter: SongAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private var musicService: MusicService ?= null
    private var playIntent: Intent ?= null
    private var musicBound: Boolean = false

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

    override fun onStart() {
        super.onStart()

        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_end -> {
                stopService(playIntent)
                musicService = null
                System.exit(0)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicBinder

            musicService = binder.service
            musicService!!.setSongList(songs)
            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

    override fun onItemClicked(index: Int) {
        musicService!!.setSong(index)
        musicService!!.playSong()
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

    override fun onDestroy() {
        stopService(playIntent)
        musicService = null
        super.onDestroy()
    }
}
