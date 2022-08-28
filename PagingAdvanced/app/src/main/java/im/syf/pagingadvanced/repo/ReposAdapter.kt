package im.syf.pagingadvanced.repo

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import im.syf.pagingadvanced.R
import im.syf.pagingadvanced.ext.provideItemCallback
import im.syf.pagingadvanced.ui.SearchRepoViewModel.UiModel
import im.syf.pagingadvanced.ui.SearchRepoViewModel.UiModel.RepoItem
import im.syf.pagingadvanced.ui.SearchRepoViewModel.UiModel.SeparatorItem

class ReposAdapter : PagingDataAdapter<UiModel, RecyclerView.ViewHolder>(DIFFER) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.view_repo_item -> RepoViewHolder.create(parent)
            else -> SeparatorViewHolder.create(parent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RepoItem -> R.layout.view_repo_item
            is SeparatorItem -> R.layout.separator_view_item
            null -> throw UnsupportedOperationException("Unknown view")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        when (item) {
            is RepoItem -> (holder as RepoViewHolder).bind(item.repo)
            is SeparatorItem -> (holder as SeparatorViewHolder).bind(item.description)
        }
    }

    companion object {
        private val DIFFER = provideItemCallback<UiModel>(
            items = { old, new ->
                (
                    old is RepoItem && new is RepoItem &&
                        old.repo.fullName == new.repo.fullName
                    ) ||
                    (
                        old is SeparatorItem && new is SeparatorItem &&
                            old.description == new.description
                        )
            },
            contents = { old, new -> old == new }
        )
    }
}
