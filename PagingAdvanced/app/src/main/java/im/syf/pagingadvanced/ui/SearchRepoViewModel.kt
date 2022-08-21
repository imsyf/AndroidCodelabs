package im.syf.pagingadvanced.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import im.syf.pagingadvanced.data.GithubRepository
import im.syf.pagingadvanced.data.RepoSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SearchRepoViewModel(
    private val repository: GithubRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /**
     * Stream of immutable states representative of the UI.
     */
    val state: LiveData<UiState>

    /**
     * Processor of side effects from the UI which in turn feedback into [UiState]
     */
    val accept: (UiAction) -> Unit

    init {
        val queryLiveData = MutableLiveData(savedStateHandle[LAST_SEARCH_QUERY] ?: DEFAULT_QUERY)

        state = queryLiveData
            .distinctUntilChanged()
            .switchMap { q ->
                liveData {
                    val uiState: LiveData<UiState> = repository.getSearchResultStream(q)
                        .map { UiState(q, it) }
                        .asLiveData(Dispatchers.Main)
                    emitSource(uiState)
                }
            }

        accept = { action ->
            when (action) {
                is UiAction.Scroll -> if (action.shouldFetchMore) {
                    val immutableQuery = queryLiveData.value
                    if (immutableQuery != null) {
                        viewModelScope.launch {
                            repository.requestMore(immutableQuery)
                        }
                    }
                }
                is UiAction.Search -> queryLiveData.postValue(action.query)
            }
        }
    }

    override fun onCleared() {
        savedStateHandle[LAST_SEARCH_QUERY] = state.value?.query
        super.onCleared()
    }

    sealed class UiAction {
        data class Search(val query: String) : UiAction()
        data class Scroll(
            val visibleItemCount: Int,
            val lastVisibleItemPosition: Int,
            val totalItemCount: Int,
        ) : UiAction() {
            val shouldFetchMore =
                visibleItemCount + lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount
        }
    }

    data class UiState(
        val query: String,
        val searchResult: RepoSearchResult,
    )

    companion object {
        private const val VISIBLE_THRESHOLD = 5
        private const val LAST_SEARCH_QUERY = "last_search_query"
        private const val DEFAULT_QUERY = "Android"
    }
}
