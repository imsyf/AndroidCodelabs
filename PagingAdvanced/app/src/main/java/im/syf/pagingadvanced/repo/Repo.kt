package im.syf.pagingadvanced.repo

import im.syf.pagingadvanced.api.RepoDto

data class Repo(
    val fullName: String,
    val url: String,
    val stars: Int,
    val forks: Int,
    val description: String?,
    val language: String?
)

fun RepoDto.toRepo(): Repo = Repo(
    fullName = full_name,
    url = html_url,
    stars = stargazers_count,
    forks = forks_count,
    description,
    language
)
