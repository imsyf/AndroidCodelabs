package im.syf.pagingbasics.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.syf.pagingbasics.data.Article
import im.syf.pagingbasics.data.ArticleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the [ArticleActivity] screen.
 * The ViewModel works with the [ArticleRepository] to get the data.
 */
class ArticleViewModel(
    repository: ArticleRepository,
) : ViewModel() {

    /**
     * Stream of [Article]s for the UI
     */
    val items: StateFlow<List<Article>> = repository.articleStream
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = listOf(),
        )
}
