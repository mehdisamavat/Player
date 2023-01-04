package com.example.exomine.ui.detailmusic

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.exomine.*
import com.example.exomine.databinding.FragmentDetailMusicBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.floor

@AndroidEntryPoint
class DetailMusicFragment : Fragment() {


    val args :DetailMusicFragmentArgs by navArgs()


    private  val viewModel: DetailMusicViewModel by viewModels()
    lateinit var binding: FragmentDetailMusicBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_music, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel=viewModel

        viewModel.nowPlayingMetaDataCompat.observe(viewLifecycleOwner){
            updateUI(view, it)
        }


        viewModel.mediaButtonRes.observe(viewLifecycleOwner) { res ->
            binding.mediaButton.setImageResource(res)
        }
        viewModel.mediaPosition.observe(viewLifecycleOwner) { pos ->
            binding.position.text =timestampToMSS( pos)
            binding.seekbar.progress=pos.toInt()
        }

        // Setup UI handlers for buttons
        binding.mediaButton.setOnClickListener {
            viewModel.nowPlayingMetaDataCompat.value?.let { viewModel.playMedia(it.id.toString()) }
        }

    }

    private fun updateUI(view: View, metadata: MediaMetadataCompat) = with(binding) {
        if (metadata.displayIconUri == Uri.EMPTY) {
            albumArt.setImageResource(R.drawable.ic_album_black_24dp)
        } else {
            Glide.with(view)
                .load(metadata.displayIconUri)
                .into(albumArt)
        }
        title.text = metadata.title?.trim()
        subtitle.text = metadata.displaySubtitle?.trim()
        duration.text = timestampToMSS(metadata.duration)
        seekbar.max= metadata.duration.toInt()
    }


    fun timestampToMSS( position: Long): String {
        val totalSeconds = floor(position / 1E3).toInt()
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds - (minutes * 60)
        return if (position < 0) "--:--"
        else  "$minutes:$remainingSeconds"
    }


}