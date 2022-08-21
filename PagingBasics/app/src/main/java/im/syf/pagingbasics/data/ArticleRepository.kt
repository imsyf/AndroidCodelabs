package im.syf.pagingbasics.data

import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository class that mimics fetching [Article] instances from an asynchronous source.
 */
class ArticleRepository {

    private val firstArticleCreatedTime = LocalDateTime.now()

    /**
     * Exposed singular stream of [Article] instances.
     */
    val articleStream: Flow<List<Article>> = flowOf(
        List(500) {
            Article(
                id = it,
                title = "Article $it",
                description = "This describes article $it",
                created = firstArticleCreatedTime.minusDays(it.toLong()),
            )
        }
    )
}
