package com.example

import android.app.Application
import com.example.data.KnowledgeRepository
import com.example.data.UltronDatabase
import com.example.network.OpenRouterClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UltronApplication : Application() {

    lateinit var database: UltronDatabase
        private set

    lateinit var repository: KnowledgeRepository
        private set

    override fun onCreate() {
        super.onCreate()
        OpenRouterClient.init(this)
        database = UltronDatabase.getDatabase(this)
        repository = KnowledgeRepository(database.knowledgeDao())

        // Ensure database preloading in background
        CoroutineScope(Dispatchers.IO).launch {
            repository.ensureDatabasePopulated()
        }
    }
}
