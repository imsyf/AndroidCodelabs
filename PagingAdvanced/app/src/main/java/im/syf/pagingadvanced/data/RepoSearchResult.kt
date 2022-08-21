package im.syf.pagingadvanced.data

import im.syf.pagingadvanced.repo.Repo

/**
 * RepoSearchResult from a search, which contains List<RepoDto> holding query data,
 * and a String of network error state.
 */
sealed class RepoSearchResult {
    data class Success(val data: List<Repo>) : RepoSearchResult()
    data class Error(val error: Exception) : RepoSearchResult()
}
