package im.syf.pagingadvanced.api

import com.squareup.moshi.JsonClass

/**
 * Immutable model class for a Github repo that holds all the information about a repository.
 * Objects of this type are received from the Github API, therefore all the fields are written
 * in camel case.
 */
@JsonClass(generateAdapter = true)
data class RepoDto(
    val id: Long,
    val name: String,
    val full_name: String,
    val html_url: String,
    val description: String?,
    val stargazers_count: Int,
    val forks_count: Int,
    val language: String?
)
