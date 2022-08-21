package im.syf.pagingadvanced.ui

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import im.syf.pagingadvanced.SearchRepoApp
import im.syf.pagingadvanced.data.RepoSearchResult
import im.syf.pagingadvanced.databinding.ActivitySearchRepoBinding
import im.syf.pagingadvanced.repo.ReposAdapter
import im.syf.pagingadvanced.ui.SearchRepoViewModel.UiAction
import im.syf.pagingadvanced.ui.SearchRepoViewModel.UiState

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
            uiActions = viewModel.accept
        )
    }

    /**
     * Binds the [UiState] provided by the [SearchRepoViewModel] to the UI,
     * and allows the UI to feed back user actions to it.
     */
    private fun ActivitySearchRepoBinding.bindState(
        uiState: LiveData<UiState>,
        uiActions: (UiAction) -> Unit,
    ) {
        val reposAdapter = ReposAdapter()
        list.adapter = reposAdapter

        bindSearch(
            uiState = uiState,
            onQueryChanged = uiActions,
        )

        bindList(
            adapter = reposAdapter,
            uiState = uiState,
            onScrollChanged = uiActions,
        )
    }

    private fun ActivitySearchRepoBinding.bindSearch(
        uiState: LiveData<UiState>,
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

        uiState
            .map(UiState::query)
            .distinctUntilChanged()
            .observe(this@SearchRepoActivity, searchRepo::setText)
    }

    private fun ActivitySearchRepoBinding.updateRepoListFromInput(
        onQueryChanged: (UiAction.Search) -> Unit,
    ) {
        val query = searchRepo.text.trim().toString()
        if (query.isNotEmpty()) {
            list.scrollToPosition(0)
            onQueryChanged(UiAction.Search(query))
        }
    }

    private fun ActivitySearchRepoBinding.bindList(
        adapter: ReposAdapter,
        uiState: LiveData<UiState>,
        onScrollChanged: (UiAction.Scroll) -> Unit,
    ) {
        setupScrollListener(onScrollChanged)

        uiState
            .map(UiState::searchResult)
            .distinctUntilChanged()
            .observe(this@SearchRepoActivity) {
                when (it) {
                    is RepoSearchResult.Error -> {
                        Toast.makeText(
                            this@SearchRepoActivity,
                            "\uD83D\uDE28 Wooops ${it.error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is RepoSearchResult.Success -> {
                        showEmptyList(it.data.isEmpty())
                        adapter.submitList(it.data)

                        Log.d("blah", "bindList(${it.data.size}):")
                        for ((i, e) in it.data.withIndex()) {
                            Log.d("blah", "$i. ${e.fullName}")
                        }
                    }
                }
            }
    }

    private fun ActivitySearchRepoBinding.showEmptyList(isEmpty: Boolean) {
        empty.isVisible = isEmpty
        list.isVisible = !isEmpty
    }

    private fun ActivitySearchRepoBinding.setupScrollListener(
        onScrollChanged: (UiAction.Scroll) -> Unit
    ) {
        val layoutManager = list.layoutManager as LinearLayoutManager
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val totalItemCount = layoutManager.itemCount
                val visibleItemCount = layoutManager.childCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                onScrollChanged(
                    UiAction.Scroll(visibleItemCount, lastVisibleItem, totalItemCount)
                )
            }
        })
    }
}
