package im.syf.pagingadvanced.repo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import im.syf.pagingadvanced.databinding.SeparatorViewItemBinding

class SeparatorViewHolder(
    private val binding: SeparatorViewItemBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(text: String): Unit = with(binding) {
        separatorDescription.text = text
    }

    companion object {
        fun create(parent: ViewGroup): SeparatorViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = SeparatorViewItemBinding.inflate(inflater, parent, false)
            return SeparatorViewHolder(binding)
        }
    }
}
