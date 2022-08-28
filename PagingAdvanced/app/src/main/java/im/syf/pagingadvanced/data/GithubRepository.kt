package im.syf.pagingadvanced.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import im.syf.pagingadvanced.api.GithubService
import im.syf.pagingadvanced.data.GithubRemoteMediator.Companion.NETWORK_PAGE_SIZE
import im.syf.pagingadvanced.db.RepoDatabase
import im.syf.pagingadvanced.db.RepoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository class that works with local and remote data sources.
 */
class GithubRepository(
    private val service: GithubService,
    private val database: RepoDatabase,
) {
    /**
     * Search repositories whose names match the query, exposed as a stream of data that will emit
     * every time we get more data from the network.
     */
    @OptIn(ExperimentalPagingApi::class)
    fun getSearchResultStream(query: String): Flow<PagingData<RepoEntity>> {
        Log.d("GithubRepository", "New query: $query")

        val dbQuery = "%${query.replace(' ', '%')}%"

        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false,
            ),
            remoteMediator = GithubRemoteMediator(query, service, database),
            pagingSourceFactory = { database.reposDao().reposByName(dbQuery) }
        ).flow
    }
}
