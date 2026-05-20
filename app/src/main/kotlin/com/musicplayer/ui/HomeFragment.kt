package com.musicplayer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.musicplayer.R
import com.musicplayer.databinding.FragmentHomeBinding
import com.musicplayer.viewmodels.MusicPlayerViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MusicPlayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MusicPlayerViewModel::class.java)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.currentSong.observe(viewLifecycleOwner) { song ->
            if (song != null) {
                binding.songTitle.text = song.title
                binding.artistName.text = song.artist
                binding.albumArt.load(song.albumArtPath) {
                    crossfade(true)
                    placeholder(R.drawable.ic_music_placeholder)
                    error(R.drawable.ic_music_placeholder)
                }
            }
        }

        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            binding.playButton.setImageResource(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )
        }

        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = progress
            binding.currentTime.text = formatTime(progress)
        }

        viewModel.duration.observe(viewLifecycleOwner) { duration ->
            binding.progressBar.max = duration
            binding.totalTime.text = formatTime(duration)
        }
    }

    private fun setupListeners() {
        binding.playButton.setOnClickListener {
            if (viewModel.isPlaying.value == true) {
                viewModel.pause()
            } else {
                viewModel.play()
            }
        }

        binding.nextButton.setOnClickListener {
            viewModel.next()
        }

        binding.previousButton.setOnClickListener {
            viewModel.previous()
        }

        binding.shuffleButton.setOnClickListener {
            viewModel.toggleShuffle()
        }

        binding.repeatButton.setOnClickListener {
            viewModel.toggleRepeat()
        }

        binding.volumeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setVolume(progress / 100f)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.seek(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun formatTime(ms: Int): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
