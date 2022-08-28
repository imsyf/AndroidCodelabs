package im.syf.pagingadvanced.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RepoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repos: List<RepoEntity>)

    @Query(
        """
        SELECT * FROM repos
        WHERE name LIKE :query OR description LIKE :query
        ORDER BY stars DESC, name ASC
        """
    )
    fun reposByName(query: String): PagingSource<Int, RepoEntity>

    @Query("DELETE FROM repos")
    suspend fun clearRepos()
}
