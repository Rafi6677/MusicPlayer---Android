package com.example.musicplayer.controllers

import android.content.Context
import android.widget.MediaController
import android.icu.lang.UCharacter.GraphemeClusterBreak.T

class MusicController(context: Context) : MediaController(context) {

    override fun hide() {}

}