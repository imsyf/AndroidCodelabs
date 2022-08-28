package im.syf.pagingadvanced

import android.app.Application
import im.syf.pagingadvanced.api.GithubService
import im.syf.pagingadvanced.data.GithubRepository
import im.syf.pagingadvanced.db.RepoDatabase

class SearchRepoApp : Application() {

    val githubRepository: GithubRepository by lazy {
        GithubRepository(
            GithubService.create(),
            RepoDatabase.getInstance(this),
        )
    }
}
