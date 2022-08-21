package im.syf.pagingadvanced.data

import android.util.Log
import im.syf.pagingadvanced.api.GithubService
import im.syf.pagingadvanced.api.RepoDto
import im.syf.pagingadvanced.repo.toRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository class that works with local and remote data sources.
 */
class GithubRepository(
    private val service: GithubService,
) {

    // Keep the list of all results received
    private val inMemoryCache = mutableSetOf<RepoDto>()

    // Shared flow of results, which allows us to broadcast updates so
    // the subscriber will have the latest data
    private val searchResults = MutableSharedFlow<RepoSearchResult>(replay = 1)

    // Keep the last requested page. When the request is successful, increment the page number
    private var lastRequestedPage = GITHUB_STARTING_PAGE_INDEX

    // Avoid triggering multiple requests in the same time
    private var isRequestInProgress = false

    /**
     * Search repositories whose names match the query, exposed as a stream of data that will emit
     * every time we get more data from the network.
     */
    suspend fun getSearchResultStream(query: String): Flow<RepoSearchResult> {
        Log.d("GithubRepository", "New query: $query")
        lastRequestedPage = 1
        inMemoryCache.clear()
        requestAndSaveData(query)

        return searchResults
    }

    suspend fun requestMore(query: String) {
        if (isRequestInProgress) return
        val successful = requestAndSaveData(query)
        if (successful) {
            lastRequestedPage++
        }
    }

    suspend fun retry(query: String) {
        if (isRequestInProgress) return
        requestAndSaveData(query)
    }

    private suspend fun requestAndSaveData(query: String): Boolean {
        isRequestInProgress = true
        var successful = false

        val apiQuery = query + IN_QUALIFIER
        try {
            val response = service.searchRepos(apiQuery, lastRequestedPage, NETWORK_PAGE_SIZE)
            Log.d("GithubRepository", "response $response")
            val repos = response.items
            inMemoryCache.addAll(repos)
            val reposByName = reposByName(query)
            searchResults.emit(RepoSearchResult.Success(reposByName.map(RepoDto::toRepo)))
            successful = true
        } catch (exception: IOException) {
            searchResults.emit(RepoSearchResult.Error(exception))
        } catch (exception: HttpException) {
            searchResults.emit(RepoSearchResult.Error(exception))
        }
        isRequestInProgress = false
        return successful
    }

    private fun reposByName(query: String): List<RepoDto> {
        // From the in memory cache select only the repos whose name or description matches
        // the query. Then order the results.
        return inMemoryCache.filter {
            it.name.contains(query, true) ||
                (it.description != null && it.description.contains(query, true))
        }.sortedWith(compareByDescending<RepoDto> { it.stargazers_count }.thenBy { it.name })
    }

    companion object {
        private const val GITHUB_STARTING_PAGE_INDEX = 1
        private const val NETWORK_PAGE_SIZE = 30
        private const val IN_QUALIFIER = "in:name,description"
    }
}
