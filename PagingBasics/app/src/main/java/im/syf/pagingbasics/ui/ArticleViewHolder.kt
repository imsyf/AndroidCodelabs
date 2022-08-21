package im.syf.pagingbasics.ui

import androidx.recyclerview.widget.RecyclerView
import im.syf.pagingbasics.data.Article
import im.syf.pagingbasics.databinding.ViewArticleBinding

/**
 * View Holder for a [Article] RecyclerView list item.
 */
class ArticleViewHolder(
    private val binding: ViewArticleBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(article: Article) = with(binding) {
        title.text = article.title
        description.text = article.description
        created.text = article.createdText
    }
}
