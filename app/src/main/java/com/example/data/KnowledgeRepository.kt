package com.example.data

import com.example.model.KnowledgeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class KnowledgeRepository(private val dao: UltronKnowledgeDao) {

    val allKnowledge: Flow<List<KnowledgeEntity>> = dao.getAllKnowledge()

    fun getByCategory(category: String): Flow<List<KnowledgeEntity>> = dao.getByCategory(category)

    suspend fun ensureDatabasePopulated() = withContext(Dispatchers.IO) {
        val count = dao.getCount()
        if (count == 0) {
            dao.insertAll(PreloadedKnowledge.initialData)
        }
    }

    suspend fun searchOfflineKnowledge(query: String): KnowledgeEntity? = withContext(Dispatchers.IO) {
        val cleanedQuery = query.trim().lowercase()
        if (cleanedQuery.isBlank()) return@withContext null

        // 1. Direct search in Room
        val directResults = dao.searchKnowledge(cleanedQuery)
        if (directResults.isNotEmpty()) {
            return@withContext directResults.first()
        }

        // 2. Tokenize words and find best match across keywords
        val words = cleanedQuery.split(" ", "?", "!", ".", ",", "-", "'")
            .filter { it.length > 2 && it !in listOf("what", "when", "where", "which", "who", "whom", "whose", "why", "how", "tell", "about", "the", "and", "is", "are", "can", "you", "give", "please") }

        for (word in words) {
            val partialResults = dao.searchKnowledge(word)
            if (partialResults.isNotEmpty()) {
                return@withContext partialResults.first()
            }
        }

        null
    }

    suspend fun searchAllMatching(query: String): List<KnowledgeEntity> = withContext(Dispatchers.IO) {
        dao.searchKnowledge(query.trim().lowercase())
    }

    suspend fun addKnowledge(entity: KnowledgeEntity) = withContext(Dispatchers.IO) {
        dao.insert(entity)
    }
}
