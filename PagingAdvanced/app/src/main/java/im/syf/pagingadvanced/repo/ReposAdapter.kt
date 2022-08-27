package im.syf.pagingadvanced.repo

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import im.syf.pagingadvanced.ext.provideItemCallback

class ReposAdapter : PagingDataAdapter<Repo, RepoViewHolder>(DIFFER) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
        return RepoViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFFER = provideItemCallback<Repo>(
            items = { old, new -> old.fullName == new.fullName },
            contents = { old, new -> old == new }
        )
    }
}
