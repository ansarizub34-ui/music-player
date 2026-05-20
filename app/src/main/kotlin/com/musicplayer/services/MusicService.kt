package com.musicplayer.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.musicplayer.models.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicService : Service() {
    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var currentSong: Song? = null
    private var isPlaying = false
    private var currentPosition = 0
    private var playlist = mutableListOf<Song>()
    private var currentIndex = 0

    private var onPlaybackStateChangeListener: ((Boolean) -> Unit)? = null
    private var onSongChangeListener: ((Song?) -> Unit)? = null
    private var onProgressListener: ((Int) -> Unit)? = null

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        startProgressUpdate()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun setPlaylist(songs: List<Song>) {
        playlist = songs.toMutableList()
    }

    fun playSong(song: Song) {
        try {
            currentSong = song
            mediaPlayer?.apply {
                reset()
                setDataSource(song.path)
                prepare()
                start()
            }
            isPlaying = true
            onPlaybackStateChangeListener?.invoke(isPlaying)
            onSongChangeListener?.invoke(currentSong)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            isPlaying = true
            onPlaybackStateChangeListener?.invoke(isPlaying)
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPlaying = false
            onPlaybackStateChangeListener?.invoke(isPlaying)
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        isPlaying = false
        onPlaybackStateChangeListener?.invoke(isPlaying)
    }

    fun next() {
        if (playlist.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % playlist.size
            playSong(playlist[currentIndex])
        }
    }

    fun previous() {
        if (playlist.isNotEmpty()) {
            currentIndex = if (currentIndex > 0) currentIndex - 1 else playlist.size - 1
            playSong(playlist[currentIndex])
        }
    }

    fun seek(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun isPlaying(): Boolean = isPlaying

    fun getCurrentSong(): Song? = currentSong

    fun setOnPlaybackStateChangeListener(listener: (Boolean) -> Unit) {
        onPlaybackStateChangeListener = listener
    }

    fun setOnSongChangeListener(listener: (Song?) -> Unit) {
        onSongChangeListener = listener
    }

    fun setOnProgressListener(listener: (Int) -> Unit) {
        onProgressListener = listener
    }

    private fun startProgressUpdate() {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                if (isPlaying && mediaPlayer != null) {
                    onProgressListener?.invoke(mediaPlayer!!.currentPosition)
                }
                kotlinx.coroutines.delay(500)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}
