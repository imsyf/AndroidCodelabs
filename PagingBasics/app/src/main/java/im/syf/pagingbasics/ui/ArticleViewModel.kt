package im.syf.pagingbasics.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import im.syf.pagingbasics.data.Article
import im.syf.pagingbasics.data.ArticleRepository
import kotlinx.coroutines.flow.Flow

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
    val items: Flow<PagingData<Article>> = Pager(
        config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
        pagingSourceFactory = { repository.articlePagingSource() }
    )
        .flow
        .cachedIn(viewModelScope)

    companion object {
        private const val ITEMS_PER_PAGE = 50
    }
}
