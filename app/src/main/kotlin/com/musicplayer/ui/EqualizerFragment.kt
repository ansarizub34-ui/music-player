package com.musicplayer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.musicplayer.databinding.FragmentEqualizerBinding

class EqualizerFragment : Fragment() {
    private var _binding: FragmentEqualizerBinding? = null
    private val binding get() = _binding!!

    private val equalizerPresets = mapOf(
        "Normal" to floatArrayOf(0f, 0f, 0f, 0f, 0f),
        "Bass Boost" to floatArrayOf(5f, 3f, 0f, -2f, -4f),
        "Treble Boost" to floatArrayOf(-4f, -2f, 0f, 3f, 5f),
        "Acoustic" to floatArrayOf(4f, 3f, 1f, 2f, 3f),
        "Pop" to floatArrayOf(-2f, 4f, 5f, 3f, -1f)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEqualizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEqualizer()
    }

    private fun setupEqualizer() {
        // Setup preset buttons
        binding.normalButton.setOnClickListener { applyPreset("Normal") }
        binding.bassBoostButton.setOnClickListener { applyPreset("Bass Boost") }
        binding.trebleBoostButton.setOnClickListener { applyPreset("Treble Boost") }
        binding.acousticButton.setOnClickListener { applyPreset("Acoustic") }
        binding.popButton.setOnClickListener { applyPreset("Pop") }

        // Setup frequency sliders
        setupSlider(binding.slider60Hz, 60)
        setupSlider(binding.slider250Hz, 250)
        setupSlider(binding.slider1kHz, 1000)
        setupSlider(binding.slider4kHz, 4000)
        setupSlider(binding.slider16kHz, 16000)
    }

    private fun setupSlider(seekBar: SeekBar, frequency: Int) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val value = (progress - 50) / 10f
                    // Apply equalizer settings to audio
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun applyPreset(presetName: String) {
        val preset = equalizerPresets[presetName] ?: return
        binding.slider60Hz.progress = (preset[0] * 10 + 50).toInt()
        binding.slider250Hz.progress = (preset[1] * 10 + 50).toInt()
        binding.slider1kHz.progress = (preset[2] * 10 + 50).toInt()
        binding.slider4kHz.progress = (preset[3] * 10 + 50).toInt()
        binding.slider16kHz.progress = (preset[4] * 10 + 50).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
