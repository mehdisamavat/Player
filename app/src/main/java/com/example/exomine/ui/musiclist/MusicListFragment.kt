package com.example.exomine.ui.musiclist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.exomine.R
import com.example.exomine.databinding.FragmentMusicListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicListFragment : Fragment() {

    companion object {
        fun newInstance() = MusicListFragment()
    }

    private val viewModel: MusicListViewModel by viewModels()

    lateinit var binding: FragmentMusicListBinding

    private val listAdapter = MediaItemAdapter { clickedItem ->
        viewModel.playMedia(clickedItem.id)
        findNavController().navigate(MusicListFragmentDirections.actionMusicListFragmentToDetailMusicFragment(clickedItem.id))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel

        viewModel.mediaItems.observe(viewLifecycleOwner) { list ->
            listAdapter.submitList(list)
        }
        binding.list.adapter = listAdapter

    }


}