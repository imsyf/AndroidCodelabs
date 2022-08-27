package im.syf.pagingadvanced.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import im.syf.pagingadvanced.api.GithubService
import im.syf.pagingadvanced.api.RepoDto
import im.syf.pagingadvanced.repo.Repo
import im.syf.pagingadvanced.repo.toRepo
import java.io.IOException
import retrofit2.HttpException

class GithubPagingSource(
    private val service: GithubService,
    private val query: String,
) : PagingSource<Int, Repo>() {

    /**
     * The refresh key is used for subsequent refresh calls to PagingSource.load
     * after the initial load
     */
    override fun getRefreshKey(state: PagingState<Int, Repo>): Int? {
        /**
         * We need to get the previous key (or next key if previous is null) of the page
         * that was closest to the most recently accessed index.
         *
         * Anchor position is the most recently accessed index
         */
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> {
        val position = params.key ?: GITHUB_STARTING_PAGE_INDEX
        val apiQuery = query + IN_QUALIFIER

        return try {
            val response = service.searchRepos(apiQuery, position, params.loadSize)
            val repos = response.items.map(RepoDto::toRepo)

            LoadResult.Page(
                data = repos,
                prevKey = if (position == GITHUB_STARTING_PAGE_INDEX) null else position - 1,
                nextKey = if (repos.isEmpty()) {
                    null
                } else {
                    position + (params.loadSize / NETWORK_PAGE_SIZE)
                }
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    companion object {
        const val GITHUB_STARTING_PAGE_INDEX = 1
        const val NETWORK_PAGE_SIZE = 30
        const val IN_QUALIFIER = "in:name,description"
    }
}
