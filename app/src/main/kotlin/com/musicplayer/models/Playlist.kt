package com.musicplayer.models

data class Playlist(
    val id: String,
    val name: String,
    val songs: MutableList<Song> = mutableListOf()
) {
    fun addSong(song: Song) {
        songs.add(song)
    }

    fun removeSong(song: Song) {
        songs.remove(song)
    }
}
