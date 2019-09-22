package com.example.musicplayer.controllers

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import android.os.PowerManager
import android.os.Binder
import android.util.Log
import com.example.musicplayer.R
import com.example.musicplayer.activities.MainActivity
import com.example.musicplayer.model.Song
import java.lang.Exception


class MusicService : Service(),
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var songs: ArrayList<Song>
    private var songPosition: Int = 0
    private var songTitle: String = ""
    private val NOTIFY_ID = 1

    private val musicBind = MusicBinder()

    override fun onCreate() {
        super.onCreate()

        songPosition = 0
        mediaPlayer = MediaPlayer()

        initMediaPlayer()
    }

    fun initMediaPlayer() {
        mediaPlayer.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)

        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnErrorListener(this)
    }

    fun setSongList(songs: ArrayList<Song>) {
        this.songs = songs
    }

    fun setSong(songIndex: Int) {
        songPosition = songIndex
    }

    fun playSong() {
        mediaPlayer.reset()

        val playSong: Song = songs[songPosition]
        val url: String = playSong.url

        songTitle = playSong.title

        try {
            mediaPlayer.setDataSource(url)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }

        mediaPlayer.prepareAsync()
    }

    fun playPrev() {
        songPosition.dec()
        if (songPosition < 0) {
            songPosition = songs.size - 1
        }
        playSong()
    }

    fun playNext() {
        songPosition.inc()
        if (songPosition > songs.size) {
            songPosition = 0
        }
        playSong()
    }

    fun getPosition(): Int {
        return mediaPlayer.currentPosition
    }

    fun getDuration(): Int {
        return mediaPlayer.duration
    }

    fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    fun pausePlayer() {
        mediaPlayer.pause()
    }

    fun seek(position: Int) {
        mediaPlayer.seekTo(position)
    }

    fun go() {
        mediaPlayer.start()
    }

    inner class MusicBinder : Binder() {
        internal val service: MusicService
            get() = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return musicBind
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mediaPlayer.stop()
        mediaPlayer.release()
        return false
    }


    override fun onPrepared(mp: MediaPlayer) {
        mp.start()

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = Notification.Builder(this)

        builder.setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_play)
            .setTicker(songTitle)
            .setOngoing(true)
            .setContentTitle("Playing")
        .setContentText(songTitle)

        val notification = builder.build()

        startForeground(NOTIFY_ID, notification)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {

    }

    override fun onDestroy() {
        stopForeground(true)
    }
}