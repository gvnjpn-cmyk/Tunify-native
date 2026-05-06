package com.tunifyx.app.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.tunifyx.app.R
import com.tunifyx.app.databinding.FragmentHomeBinding
import com.tunifyx.app.ui.MainViewModel
import com.tunifyx.app.ui.adapter.*
import com.tunifyx.app.ui.search.SearchFragment
import com.tunifyx.app.ui.showTrackMenu
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()

    private lateinit var trendingAdapter: GridTrackAdapter
    private lateinit var recentAdapter:   GridTrackAdapter
    private lateinit var quickAdapter:    QuickPickAdapter
    private lateinit var moodAdapter:     MoodAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentHomeBinding.bind(view)

        b.tvGreeting.text = greeting()

        // Quick picks — 2-col grid
        quickAdapter = QuickPickAdapter { track, list, idx -> vm.play(list, idx) }
        b.rvQuickPicks.layoutManager = GridLayoutManager(requireContext(), 2)
        b.rvQuickPicks.adapter = quickAdapter

        // Moods — 2-col grid
        moodAdapter = MoodAdapter { mood ->
            // Navigate to search with mood query
            val frag = SearchFragment.newInstance(mood.query)
            parentFragmentManager.beginTransaction()
                .replace(R.id.navHostFragment, frag)
                .commit()
        }
        b.rvMoods.layoutManager = GridLayoutManager(requireContext(), 2)
        b.rvMoods.adapter = moodAdapter

        // Trending — horizontal
        trendingAdapter = GridTrackAdapter { track, list, idx -> vm.play(list, idx) }
        b.rvTrending.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        b.rvTrending.adapter = trendingAdapter

        // Recently played — horizontal
        recentAdapter = GridTrackAdapter { track, list, idx -> vm.play(list, idx) }
        b.rvRecent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        b.rvRecent.adapter = recentAdapter

        // Observe trending
        viewLifecycleOwner.lifecycleScope.launch {
            vm.trending.collectLatest { trendingAdapter.submitList(it) }
        }

        // Observe history
        viewLifecycleOwner.lifecycleScope.launch {
            vm.history.collectLatest { history ->
                quickAdapter.submitList(history.take(6))
                recentAdapter.submitList(history)
                b.tvRecentLabel.visibility = if (history.isEmpty()) View.GONE else View.VISIBLE
                b.rvRecent.visibility      = if (history.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        // Swipe to refresh
        b.swipeRefresh.setColorSchemeColors(
            requireContext().getColor(R.color.accent_green)
        )
        b.swipeRefresh.setOnRefreshListener {
            vm.loadHome()
            viewLifecycleOwner.lifecycleScope.launch {
                kotlinx.coroutines.delay(1500)
                b.swipeRefresh.isRefreshing = false
            }
        }

        // See all
        b.tvSeeAll.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.navHostFragment, SearchFragment())
                .commit()
        }

        vm.loadHome()
    }

    private fun greeting() = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..10  -> "Selamat pagi ☀️"
        in 11..14 -> "Selamat siang 🌤"
        in 15..17 -> "Selamat sore 🌆"
        in 18..20 -> "Selamat malam 🌙"
        else       -> "Malam ini dengerin apa? 🎵"
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
