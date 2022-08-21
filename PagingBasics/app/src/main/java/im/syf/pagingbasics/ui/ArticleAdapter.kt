package im.syf.pagingbasics.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import im.syf.pagingbasics.data.Article
import im.syf.pagingbasics.databinding.ViewArticleBinding
import im.syf.pagingbasics.ext.provideItemCallback

/**
 * Adapter for an [Article] [List].
 */
class ArticleAdapter : PagingDataAdapter<Article, ArticleViewHolder>(DIFFER) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ViewArticleBinding.inflate(inflater, parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    companion object {
        private val DIFFER = provideItemCallback<Article>(
            items = { old, new -> old.id == new.id },
            contents = { old, new -> old == new },
        )
    }
}
