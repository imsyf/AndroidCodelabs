package im.syf.pagingbasics.data

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Immutable model class for an article.
 */
data class Article(
    val id: Int,
    val title: String,
    val description: String,
    val created: LocalDateTime,
) {
    val createdText: String = articleDateFormatter.format(created)

    companion object {
        private val articleDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    }
}
