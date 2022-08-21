package im.syf.pagingadvanced.repo

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import im.syf.pagingadvanced.R
import im.syf.pagingadvanced.databinding.ViewRepoItemBinding

class RepoViewHolder(
    private val binding: ViewRepoItemBinding,
) : RecyclerView.ViewHolder(binding.root) {

    private var currentRepo: Repo? = null

    init {
        itemView.setOnClickListener {
            currentRepo?.url?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                it.context.startActivity(intent)
            }
        }
    }

    fun bind(repo: Repo?): Unit = with(binding) {
        val resources = itemView.resources

        repoDescription.isVisible = repo?.description?.isNotEmpty() ?: false
        repoLanguage.isVisible = repo?.language?.isNotEmpty() ?: false

        if (repo == null) {
            repoName.text = resources.getString(R.string.loading)
            repoStars.text = resources.getString(R.string.unknown)
            repoForks.text = resources.getString(R.string.unknown)
        } else {
            currentRepo = repo
            repoName.text = repo.fullName
            repoStars.text = repo.stars.toString()
            repoForks.text = repo.forks.toString()
            repoDescription.text = repo.description
            repoLanguage.text = resources.getString(R.string.language, repo.language)
        }
    }

    companion object {
        fun create(parent: ViewGroup): RepoViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ViewRepoItemBinding.inflate(inflater, parent, false)
            return RepoViewHolder(binding)
        }
    }
}
