package im.syf.pagingadvanced

import android.app.Application
import im.syf.pagingadvanced.api.GithubService
import im.syf.pagingadvanced.data.GithubRepository

class SearchRepoApp : Application() {

    val githubRepository: GithubRepository by lazy {
        GithubRepository(GithubService.create())
    }
}
