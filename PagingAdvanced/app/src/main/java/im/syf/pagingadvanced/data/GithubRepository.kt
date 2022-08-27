package im.syf.pagingadvanced.data

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import im.syf.pagingadvanced.api.GithubService
import im.syf.pagingadvanced.data.GithubPagingSource.Companion.NETWORK_PAGE_SIZE
import im.syf.pagingadvanced.repo.Repo
import kotlinx.coroutines.flow.Flow

/**
 * Repository class that works with local and remote data sources.
 */
class GithubRepository(
    private val service: GithubService,
) {
    /**
     * Search repositories whose names match the query, exposed as a stream of data that will emit
     * every time we get more data from the network.
     */
    fun getSearchResultStream(query: String): Flow<PagingData<Repo>> {
        Log.d("GithubRepository", "New query: $query")

        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { GithubPagingSource(service, query) }
        ).flow
    }
}
