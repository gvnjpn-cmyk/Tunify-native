package com.tunifyx.app.ui.search

import android.os.Bundle
import android.text.*
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tunifyx.app.R
import com.tunifyx.app.databinding.FragmentSearchBinding
import com.tunifyx.app.ui.MainViewModel
import com.tunifyx.app.ui.adapter.TrackAdapter
import com.tunifyx.app.ui.showTrackMenu
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _b: FragmentSearchBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()
    private lateinit var adapter: TrackAdapter

    companion object {
        fun newInstance(query: String = "") = SearchFragment().apply {
            arguments = bundleOf("query" to query)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentSearchBinding.bind(view)

        adapter = TrackAdapter(
            onPlay = { _, list, idx -> vm.play(list, idx) },
            onMore = { track, v  -> showTrackMenu(requireContext(), track, v, vm) }
        )
        b.rvResults.layoutManager = LinearLayoutManager(requireContext())
        b.rvResults.adapter = adapter

        // Pre-fill query from arguments (mood shortcuts)
        val preQuery = arguments?.getString("query") ?: ""
        if (preQuery.isNotEmpty()) {
            b.etSearch.setText(preQuery)
            vm.search(preQuery)
        }

        // Debounced text watcher
        b.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                vm.search(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Observe
        viewLifecycleOwner.lifecycleScope.launch {
            vm.searchResults.collectLatest { results ->
                adapter.submitList(results)
                val hasResults = results.isNotEmpty()
                b.layoutPlayAll.visibility = if (hasResults) View.VISIBLE else View.GONE
                b.layoutEmpty.visibility   =
                    if (!hasResults && b.etSearch.text.isNullOrBlank()) View.VISIBLE else View.GONE
                b.tvResultCount.text = "${results.size} hasil"
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.searchLoading.collectLatest { loading ->
                b.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.searchError.collectLatest { err ->
                b.tvError.text       = err ?: ""
                b.tvError.visibility = if (err != null) View.VISIBLE else View.GONE
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.currentTrack.collectLatest { adapter.setActiveId(it?.videoId ?: "") }
        }

        b.btnPlayAll.setOnClickListener {
            val list = adapter.currentList
            if (list.isNotEmpty()) vm.play(list, 0)
        }
        b.btnAddAll.setOnClickListener {
            vm.addManyToQueue(adapter.currentList)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
