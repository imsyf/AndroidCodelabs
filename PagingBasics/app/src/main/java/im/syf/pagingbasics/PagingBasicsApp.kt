package im.syf.pagingbasics

import android.app.Application
import im.syf.pagingbasics.data.ArticleRepository

class PagingBasicsApp : Application() {

    /**
     * Shared instance of [ArticleRepository]
     */
    val articleRepository: ArticleRepository by lazy {
        ArticleRepository()
    }
}
