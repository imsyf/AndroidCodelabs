package im.syf.pagingadvanced.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import im.syf.pagingadvanced.api.GithubService
import im.syf.pagingadvanced.api.RepoDto
import im.syf.pagingadvanced.data.ext.toRepoEntity
import im.syf.pagingadvanced.db.RemoteKeysEntity
import im.syf.pagingadvanced.db.RepoDatabase
import im.syf.pagingadvanced.db.RepoEntity
import java.io.IOException
import retrofit2.HttpException

@OptIn(ExperimentalPagingApi::class)
class GithubRemoteMediator(
    private val query: String,
    private val service: GithubService,
    private val database: RepoDatabase,
) : RemoteMediator<Int, RepoEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RepoEntity>
    ): MediatorResult {
        val page: Int = when (loadType) {
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)

                /**
                 * If remoteKeys is null, that means the refresh result is not in the database yet.
                 * We can return Success with endOfPaginationReached = false because Paging
                 * will call this method again if RemoteKeys becomes non-null.
                 *
                 * If remoteKeys is NOT NULL but its nextKey is null, that means we've reached
                 * the end of pagination for append.
                 */
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)

                nextKey
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)

                /**
                 * If remoteKeys is null, that means the refresh result is not in the database yet.
                 */
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)

                prevKey
            }
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: GITHUB_STARTING_PAGE_INDEX
            }
        }

        val apiQuery = query + IN_QUALIFIER

        try {
            val apiResponse = service.searchRepos(apiQuery, page, state.config.pageSize)

            val repos = apiResponse.items.map(RepoDto::toRepoEntity)
            val endOfPaginationReached = repos.isEmpty()

            database.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().clearRemoteKeys()
                    database.reposDao().clearRepos()
                }

                val prevKey = if (page == GITHUB_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = repos.map {
                    RemoteKeysEntity(
                        repoId = it.id,
                        prevKey,
                        nextKey,
                    )
                }

                database.remoteKeysDao().insertAll(keys)
                database.reposDao().insertAll(repos)
            }

            return MediatorResult.Success(endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, RepoEntity>
    ): RemoteKeysEntity? {
        /**
         * Get the first page that was retrieved, that contained items.
         * From that first page, get the first item.
         */
        return state.pages.firstOrNull { it.data.isNotEmpty() }
            ?.data?.firstOrNull()
            ?.let { repo ->
                database.remoteKeysDao().remoteKeysRepoId(repo.id)
            }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, RepoEntity>
    ): RemoteKeysEntity? {
        /**
         * Get the last page that was retrieved, that contained items.
         * From that last page, get the last item.
         */
        return state.pages.lastOrNull { it.data.isNotEmpty() }
            ?.data?.lastOrNull()
            ?.let { repo ->
                database.remoteKeysDao().remoteKeysRepoId(repo.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, RepoEntity>
    ): RemoteKeysEntity? {
        /**
         * The paging library is trying to load data after the anchor position
         * Get the item closest to the anchor position
         */
        return state.anchorPosition
            ?.let { position ->
                state.closestItemToPosition(position)
                    ?.id
                    ?.let { repoId ->
                        database.remoteKeysDao().remoteKeysRepoId(repoId)
                    }
            }
    }

    companion object {
        const val GITHUB_STARTING_PAGE_INDEX = 1
        const val NETWORK_PAGE_SIZE = 30
        const val IN_QUALIFIER = "in:name,description"
    }
}
