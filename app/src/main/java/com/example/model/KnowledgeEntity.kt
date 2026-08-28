package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "knowledge_base")
data class KnowledgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String, // SCIENCE, LORE, TECH, HISTORY, SYSTEM, WORLD, CALCULATION
    val title: String,
    val keywords: String, // Comma separated keywords for rapid offline search
    val question: String,
    val answer: String,
    val confidence: Float = 1.0f
)
