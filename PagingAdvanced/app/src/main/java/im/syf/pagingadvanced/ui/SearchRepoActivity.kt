package im.syf.pagingadvanced.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import im.syf.pagingadvanced.SearchRepoApp
import im.syf.pagingadvanced.databinding.ActivitySearchRepoBinding
import im.syf.pagingadvanced.repo.Repo
import im.syf.pagingadvanced.repo.ReposAdapter
import im.syf.pagingadvanced.repo.ReposLoadStateAdapter
import im.syf.pagingadvanced.ui.SearchRepoViewModel.UiAction
import im.syf.pagingadvanced.ui.SearchRepoViewModel.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SearchRepoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySearchRepoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: SearchRepoViewModel by viewModels {
            viewModelFactory {
                initializer {
                    val app = application as SearchRepoApp
                    val savedStateHandle = createSavedStateHandle()
                    SearchRepoViewModel(app.githubRepository, savedStateHandle)
                }
            }
        }

        binding.list.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        binding.bindState(
            uiState = viewModel.state,
            pagingData = viewModel.pagingDataFlow,
            uiActions = viewModel.accept
        )
    }

    /**
     * Binds the [UiState] provided by the [SearchRepoViewModel] to the UI,
     * and allows the UI to feed back user actions to it.
     */
    private fun ActivitySearchRepoBinding.bindState(
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Repo>>,
        uiActions: (UiAction) -> Unit,
    ) {
        val reposAdapter = ReposAdapter()
        list.adapter = reposAdapter.withLoadStateHeaderAndFooter(
            header = ReposLoadStateAdapter { reposAdapter.retry() },
            footer = ReposLoadStateAdapter { reposAdapter.retry() }
        )

        bindSearch(
            uiState = uiState,
            onQueryChanged = uiActions,
        )

        bindList(
            adapter = reposAdapter,
            uiState = uiState,
            pagingData = pagingData,
            onScrollChanged = uiActions,
        )
    }

    private fun ActivitySearchRepoBinding.bindSearch(
        uiState: StateFlow<UiState>,
        onQueryChanged: (UiAction.Search) -> Unit,
    ) {
        searchRepo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateRepoListFromInput(onQueryChanged)
                true
            } else {
                false
            }
        }

        searchRepo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateRepoListFromInput(onQueryChanged)
                true
            } else {
                false
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                uiState
                    .map { it.query }
                    .distinctUntilChanged()
                    .collect(searchRepo::setText)
            }
        }
    }

    private fun ActivitySearchRepoBinding.updateRepoListFromInput(
        onQueryChanged: (UiAction.Search) -> Unit,
    ) {
        val query = searchRepo.text.trim().toString()
        if (query.isNotEmpty()) {
            onQueryChanged(UiAction.Search(query))
        }
    }

    private fun ActivitySearchRepoBinding.bindList(
        adapter: ReposAdapter,
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Repo>>,
        onScrollChanged: (UiAction.Scroll) -> Unit,
    ) {
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) onScrollChanged(UiAction.Scroll(uiState.value.query))
            }
        })

        val notLoading = adapter.loadStateFlow
            // Only emit when REFRESH LoadState for the paging source changes.
            .distinctUntilChangedBy { it.source.refresh }
            // Only react to cases where REFRESH completes i.e., NotLoading.
            .map { it.source.refresh is LoadState.NotLoading }

        val hasNotScrolledForCurrentSearch = uiState
            .map { it.hasNotScrolledForCurrentSearch }
            .distinctUntilChanged()

        val shouldScrollToTop = combine(
            notLoading,
            hasNotScrolledForCurrentSearch,
            Boolean::and
        ).distinctUntilChanged()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pagingData.collectLatest(adapter::submitData)
            }

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                shouldScrollToTop.collect { shouldScroll ->
                    if (shouldScroll) list.scrollToPosition(0)
                }
            }
        }
    }
}
