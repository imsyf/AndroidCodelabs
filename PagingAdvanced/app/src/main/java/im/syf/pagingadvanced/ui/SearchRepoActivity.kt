package im.syf.pagingadvanced.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import im.syf.pagingadvanced.SearchRepoApp
import im.syf.pagingadvanced.databinding.ActivitySearchRepoBinding
import im.syf.pagingadvanced.repo.ReposAdapter
import im.syf.pagingadvanced.repo.ReposLoadStateAdapter
import im.syf.pagingadvanced.ui.SearchRepoViewModel.UiAction
import im.syf.pagingadvanced.ui.SearchRepoViewModel.UiModel
import im.syf.pagingadvanced.ui.SearchRepoViewModel.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
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
        pagingData: Flow<PagingData<UiModel>>,
        uiActions: (UiAction) -> Unit,
    ) {
        val reposAdapter = ReposAdapter()
        val header = ReposLoadStateAdapter { reposAdapter.retry() }
        list.adapter = reposAdapter.withLoadStateHeaderAndFooter(
            header,
            footer = ReposLoadStateAdapter { reposAdapter.retry() }
        )

        bindSearch(
            uiState = uiState,
            onQueryChanged = uiActions,
        )

        bindList(
            header = header,
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
        header: ReposLoadStateAdapter,
        adapter: ReposAdapter,
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<UiModel>>,
        onScrollChanged: (UiAction.Scroll) -> Unit,
    ) {
        retryButton.setOnClickListener { adapter.retry() }

        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) onScrollChanged(UiAction.Scroll(uiState.value.query))
            }
        })

        val notLoading = adapter.loadStateFlow
            .asRemotePresentation()
            // Only react to cases where REFRESH completes
            .map { it == RemotePresentationState.PRESENTED }

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

        lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadState ->
                val isListEmpty =
                    loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0

                header.loadState = loadState.mediator
                    ?.refresh
                    ?.takeIf { it is LoadState.Error && adapter.itemCount > 0 }
                    ?: loadState.prepend

                empty.isVisible = isListEmpty
                // Only show the list if refresh succeeds, either from the the local db or the remote.
                list.isVisible = loadState.source.refresh is LoadState.NotLoading ||
                    loadState.mediator?.refresh is LoadState.NotLoading

                // Show loading spinner during initial load or refresh.
                progressBar.isVisible = loadState.mediator?.refresh is LoadState.Loading
                // Show the retry state if initial load or refresh fails.
                retryButton.isVisible = loadState.mediator?.refresh is LoadState.Error &&
                    adapter.itemCount == 0

                // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error

                errorState?.let {
                    Toast.makeText(
                        this@SearchRepoActivity,
                        "\uD83D\uDE28 Wooops ${it.error}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun Flow<CombinedLoadStates>.asRemotePresentation(): Flow<RemotePresentationState> =
        scan(RemotePresentationState.INITIAL) { state, loadState ->
            when (state) {
                RemotePresentationState.INITIAL -> when (loadState.mediator?.refresh) {
                    LoadState.Loading -> RemotePresentationState.REMOTE_LOADING
                    else -> state
                }
                RemotePresentationState.REMOTE_LOADING -> when (loadState.source.refresh) {
                    LoadState.Loading -> RemotePresentationState.SOURCE_LOADING
                    else -> state
                }
                RemotePresentationState.SOURCE_LOADING -> when (loadState.source.refresh) {
                    is LoadState.NotLoading -> RemotePresentationState.PRESENTED
                    else -> state
                }
                RemotePresentationState.PRESENTED -> when (loadState.mediator?.refresh) {
                    LoadState.Loading -> RemotePresentationState.REMOTE_LOADING
                    is LoadState.Error -> TODO()
                    else -> state
                }
            }
        }
}
