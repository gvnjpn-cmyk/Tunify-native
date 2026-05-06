package com.tunifyx.app.ui.library

import android.os.Bundle
import android.view.*
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tunifyx.app.R
import com.tunifyx.app.databinding.FragmentLibraryBinding
import com.tunifyx.app.ui.MainViewModel
import com.tunifyx.app.ui.adapter.TrackAdapter
import com.tunifyx.app.ui.showTrackMenu
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _b: FragmentLibraryBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentLibraryBinding.bind(view)

        val historyAdapter = TrackAdapter(
            onPlay = { _, list, idx -> vm.play(list, idx) },
            onMore = { track, v -> showTrackMenu(requireContext(), track, v, vm) }
        )
        b.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        b.rvHistory.adapter = historyAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            vm.history.collectLatest { history ->
                historyAdapter.submitList(history)
                b.tvEmpty.visibility =
                    if (history.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.currentTrack.collectLatest { track ->
                historyAdapter.setActiveId(track?.videoId ?: "")
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
