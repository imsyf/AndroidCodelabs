package im.syf.pagingadvanced.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repos")
data class RepoEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val fullName: String,
    val url: String,
    val stars: Int,
    val forks: Int,
    val description: String?,
    val language: String?
)
