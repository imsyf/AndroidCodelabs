package im.syf.pagingbasics.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import im.syf.pagingbasics.PagingBasicsApp
import im.syf.pagingbasics.databinding.ActivityArticleBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleBinding

    private val viewModel: ArticleViewModel by viewModels {
        viewModelFactory {
            initializer {
                val app = application as PagingBasicsApp
                ArticleViewModel(app.articleRepository)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val articleAdapter = ArticleAdapter()

        with(binding.list) {
            adapter = articleAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        lifecycleScope.launch {
            /**
             * Repeat on the STARTED lifecycle because an Activity may be PAUSED
             * but still visible on the screen, for example in a multi window app
             */
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.items.collectLatest {
                        articleAdapter.submitData(it)
                    }
                }

                launch {
                    articleAdapter.loadStateFlow.collect {
                        binding.prependProgress.isVisible = it.source.prepend is LoadState.Loading
                        binding.appendProgress.isVisible = it.source.append is LoadState.Loading
                    }
                }
            }
        }
    }
}
