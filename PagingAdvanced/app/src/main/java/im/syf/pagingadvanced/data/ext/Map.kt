package im.syf.pagingadvanced.data.ext

import im.syf.pagingadvanced.api.RepoDto
import im.syf.pagingadvanced.db.RepoEntity
import im.syf.pagingadvanced.repo.Repo

fun RepoDto.toRepoEntity(): RepoEntity = RepoEntity(
    id,
    name,
    fullName = full_name,
    url = html_url,
    stars = stargazers_count,
    forks = forks_count,
    description,
    language,
)

fun RepoEntity.toRepo(): Repo = Repo(
    id,
    fullName,
    url,
    stars,
    forks,
    description,
    language,
)
