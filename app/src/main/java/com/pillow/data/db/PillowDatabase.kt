package com.pillow.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pillow.data.db.dao.CategoryDao
import com.pillow.data.db.dao.NoteDao
import com.pillow.data.db.dao.TagDao
import com.pillow.data.db.dao.VoiceMemoDao
import com.pillow.data.db.entity.CategoryEntity
import com.pillow.data.db.entity.NoteEntity
import com.pillow.data.db.entity.NoteTagCrossRef
import com.pillow.data.db.entity.TagEntity
import com.pillow.data.db.entity.VoiceMemoEntity

@Database(
    entities = [
        NoteEntity::class,
        CategoryEntity::class,
        TagEntity::class,
        NoteTagCrossRef::class,
        VoiceMemoEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class PillowDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
    abstract fun voiceMemoDao(): VoiceMemoDao

    companion object {
        @Volatile
        private var instance: PillowDatabase? = null

        fun getInstance(context: Context): PillowDatabase {
            return instance ?: synchronized(this) {
                instance ?: createDatabase(context).also { instance = it }
            }
        }

        private fun createDatabase(context: Context): PillowDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                PillowDatabase::class.java,
                "pillow_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
