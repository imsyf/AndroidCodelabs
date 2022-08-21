package im.syf.pagingadvanced.api

import com.squareup.moshi.JsonClass

/**
 * Data class to hold repo responses from searchRepo API calls.
 */
@JsonClass(generateAdapter = true)
data class RepoSearchResponseDto(
    val total_count: Int = 0,
    val items: List<RepoDto> = emptyList(),
    val next_page: Int? = null
)
