package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.KnowledgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UltronKnowledgeDao {
    @Query("SELECT * FROM knowledge_base ORDER BY category, title ASC")
    fun getAllKnowledge(): Flow<List<KnowledgeEntity>>

    @Query("SELECT * FROM knowledge_base WHERE category = :category ORDER BY title ASC")
    fun getByCategory(category: String): Flow<List<KnowledgeEntity>>

    @Query("""
        SELECT * FROM knowledge_base 
        WHERE title LIKE '%' || :query || '%' 
           OR keywords LIKE '%' || :query || '%' 
           OR question LIKE '%' || :query || '%' 
           OR answer LIKE '%' || :query || '%'
        ORDER BY 
           CASE 
               WHEN title LIKE '%' || :query || '%' THEN 1
               WHEN keywords LIKE '%' || :query || '%' THEN 2
               WHEN question LIKE '%' || :query || '%' THEN 3
               ELSE 4 
           END
        LIMIT 20
    """)
    suspend fun searchKnowledge(query: String): List<KnowledgeEntity>

    @Query("SELECT COUNT(*) FROM knowledge_base")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<KnowledgeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: KnowledgeEntity)
}
