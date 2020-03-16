package com.qwert2603.coroutines_test

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity
data class Post(
    val userId: Long,
    @PrimaryKey val id: Long,
    val title: String,
    val body: String
)

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePosts(posts: List<Post>)

    @Query("SELECT * FROM Post WHERE userId=:userId ORDER BY id")
    fun flowPosts(userId: Long): Flow<List<Post>>
}

@Database(entities = [Post::class], exportSchema = false, version = 2)
abstract class LocalDB : RoomDatabase() {
    companion object {
        fun create(context: Context) = Room
            .databaseBuilder(context, LocalDB::class.java, "local.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    abstract val postDao: PostDao
}