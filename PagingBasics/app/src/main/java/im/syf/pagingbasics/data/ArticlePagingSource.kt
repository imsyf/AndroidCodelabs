package im.syf.pagingbasics.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import kotlin.math.max

class ArticlePagingSource : PagingSource<Int, Article>() {

    /**
     * The refresh key is used for the initial load of the next PagingSource, after invalidation
     */
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        /**
         * In our case we grab the item closest to the anchor position
         * then return its id - (state.config.pageSize / 2) as a buffer
         */
        val anchorPosition = state.anchorPosition ?: return null
        val article = state.closestItemToPosition(anchorPosition) ?: return null

        return ensureValidKey(key = article.id - (state.config.pageSize / 2))
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        // Start paging with the STARTING_KEY if this is the first load
        val start = params.key ?: STARTING_KEY
        // Load as many items as hinted by params.loadSize
        val range = start.until(start + params.loadSize)

        if (start != STARTING_KEY) delay(LOAD_DELAY_MILLIS)

        return LoadResult.Page(
            data = range.map {
                Article(
                    id = it, // Generate consecutive increasing numbers as the article id
                    title = "Article $it",
                    description = "This describes article $it",
                    created = firstArticleCreatedTime.minusDays(it.toLong())
                )
            },
            prevKey = when (start) {
                STARTING_KEY -> null
                else -> ensureValidKey(key = range.first - params.loadSize)
            },
            nextKey = range.last + 1,
        )
    }

    /**
     * Makes sure the paging key is never less than [STARTING_KEY]
     */
    private fun ensureValidKey(key: Int): Int = max(STARTING_KEY, key)

    companion object {
        private const val STARTING_KEY = 0
        private const val LOAD_DELAY_MILLIS = 3_000L
        private val firstArticleCreatedTime = LocalDateTime.now()
    }
}
