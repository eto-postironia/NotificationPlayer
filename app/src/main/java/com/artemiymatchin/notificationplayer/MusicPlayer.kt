package com.artemiymatchin.notificationplayer

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.MutableLiveData

import java.io.File
import kotlin.random.Random

object MusicPlayer {

    private lateinit var mediaPlayer: MediaPlayer
    public val isLooped: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private var isPaused = true

    private var musicFolder = "/storage/emulated/0/NotificationPlayer"
    private lateinit var currentTrackName: String

    private var trackList = ArrayList<String>()

    private fun refreshTrackList() {
        trackList.clear()
        val directory = File(musicFolder)
        val fList = directory.listFiles() ?: return

        for (file in fList) {
            if (file.isFile && file.name.endsWith(".m4a")) { // TODO: Change to mp3 after testing
                trackList.add(file.name)
            }
        }
    }


    private fun setNextTrack() {
        refreshTrackList()
        val currentTrackIndex = trackList.indexOf(currentTrackName)

        currentTrackName = if (currentTrackIndex == trackList.size - 1)
            trackList[0]
        else
            trackList[currentTrackIndex + 1]
    }


    fun startPlayer(context: Context) {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }

        refreshTrackList()
        if (trackList.size == 0) {
            Toast.makeText(
                context,
                context.getString(R.string.no_tracks_msg),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        currentTrackName = trackList[0]
        isLooped.value = true

        mediaPlayer.apply {
            setDataSource(context, Uri.parse("$musicFolder/$currentTrackName"))
            prepare()
            setOnCompletionListener {
                if (!isLooping) {
                    setNextTrack()
                }
                reset()
                setDataSource(context, Uri.parse("$musicFolder/$currentTrackName"))
                prepare()
                start()
            }
        }
    }


    fun playPauseTrack() {
        if (isPaused) {
            isPaused = false
            mediaPlayer.apply { start() }
        } else {
            isPaused = true
            mediaPlayer.apply { pause() }
        }
    }


    fun playRandomTrack(context: Context) {
        refreshTrackList()
        val randomTrackID = Random.nextInt(trackList.size)
        currentTrackName = trackList[randomTrackID]

        isPaused = false
        mediaPlayer.apply {
            reset()
            setDataSource(context, Uri.parse("$musicFolder/$currentTrackName"))
            prepare()
            start()
        }
    }


    fun loopTrack() {
        mediaPlayer.isLooping = !mediaPlayer.isLooping
    }

}