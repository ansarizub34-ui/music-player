package com.musicplayer.viewmodels

import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.models.Song
import com.musicplayer.models.Playlist
import com.musicplayer.services.MusicService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicPlayerViewModel : ViewModel() {
    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> = _currentSong

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _progress = MutableLiveData(0)
    val progress: LiveData<Int> = _progress

    private val _duration = MutableLiveData(0)
    val duration: LiveData<Int> = _duration

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _shuffle = MutableLiveData(false)
    val shuffle: LiveData<Boolean> = _shuffle

    private val _repeat = MutableLiveData(RepeatMode.REPEAT_OFF)
    val repeat: LiveData<RepeatMode> = _repeat

    private var musicService: MusicService? = null

    enum class RepeatMode {
        REPEAT_OFF, REPEAT_ONE, REPEAT_ALL
    }

    fun setMusicService(service: MusicService) {
        musicService = service
        service.setOnPlaybackStateChangeListener { isPlayingState ->
            _isPlaying.value = isPlayingState
        }
        service.setOnSongChangeListener { song ->
            _currentSong.value = song
            _duration.value = service.getDuration()
        }
        service.setOnProgressListener { position ->
            _progress.value = position
        }
    }

    fun loadSongs(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val songsList = mutableListOf<Song>()
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
            )
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val song = Song(
                        id = cursor.getLong(0),
                        title = cursor.getString(1),
                        artist = cursor.getString(2),
                        album = cursor.getString(3),
                        duration = cursor.getLong(4),
                        path = cursor.getString(5),
                        albumArtPath = "content://media/external/audio/albumart/${cursor.getLong(6)}"
                    )
                    songsList.add(song)
                }
            }

            _songs.postValue(songsList)
            musicService?.setPlaylist(songsList)
        }
    }

    fun playSong(song: Song) {
        musicService?.playSong(song)
    }

    fun play() {
        musicService?.play()
    }

    fun pause() {
        musicService?.pause()
    }

    fun stop() {
        musicService?.stop()
    }

    fun next() {
        musicService?.next()
    }

    fun previous() {
        musicService?.previous()
    }

    fun seek(position: Int) {
        musicService?.seek(position)
    }

    fun setVolume(volume: Float) {
        musicService?.setVolume(volume)
    }

    fun toggleShuffle() {
        _shuffle.value = !(_shuffle.value ?: false)
    }

    fun toggleRepeat() {
        val current = _repeat.value ?: RepeatMode.REPEAT_OFF
        _repeat.value = when (current) {
            RepeatMode.REPEAT_OFF -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE -> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_OFF
        }
    }

    fun createPlaylist(name: String) {
        val newPlaylist = Playlist(System.currentTimeMillis().toString(), name)
        val currentList = _playlists.value?.toMutableList() ?: mutableListOf()
        currentList.add(newPlaylist)
        _playlists.value = currentList
    }

    fun addSongToPlaylist(playlist: Playlist, song: Song) {
        playlist.addSong(song)
    }
}
