package im.syf.pagingbasics.data

/**
 * Repository class that mimics fetching [Article] instances from an asynchronous source.
 */
class ArticleRepository {

    /**
     * Should always return a brand new PagingSource as PagingSource instances are not reusable.
     */
    fun articlePagingSource(): ArticlePagingSource = ArticlePagingSource()
}
