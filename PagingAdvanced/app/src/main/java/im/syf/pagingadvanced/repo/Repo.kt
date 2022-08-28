package im.syf.pagingadvanced.repo

data class Repo(
    val id: Long,
    val fullName: String,
    val url: String,
    val stars: Int,
    val forks: Int,
    val description: String?,
    val language: String?
)
