package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.KnowledgeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [KnowledgeEntity::class], version = 1, exportSchema = false)
abstract class UltronDatabase : RoomDatabase() {
    abstract fun knowledgeDao(): UltronKnowledgeDao

    companion object {
        @Volatile
        private var INSTANCE: UltronDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): UltronDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UltronDatabase::class.java,
                    "ultron_knowledge.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch(Dispatchers.IO) {
                            INSTANCE?.knowledgeDao()?.insertAll(PreloadedKnowledge.initialData)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
