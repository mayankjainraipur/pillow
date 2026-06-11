package com.pillow.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pillow.data.db.dao.AttachmentDao
import com.pillow.data.db.dao.CategoryDao
import com.pillow.data.db.dao.NoteDao
import com.pillow.data.db.dao.TagDao
import com.pillow.data.db.dao.VoiceMemoDao
import com.pillow.data.db.entity.AttachmentEntity
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
        VoiceMemoEntity::class,
        AttachmentEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class PillowDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
    abstract fun voiceMemoDao(): VoiceMemoDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        @Volatile
        private var instance: PillowDatabase? = null

        /**
         * v4 → v5: adds the `is_favorite` / `is_shared` flags to notes and the new
         * `attachments` table. A real migration (not destructive) so existing notes
         * and their backups survive the upgrade.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN is_shared INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS attachments (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        note_id INTEGER NOT NULL,
                        file_path TEXT NOT NULL,
                        type TEXT NOT NULL,
                        mime_type TEXT NOT NULL DEFAULT '',
                        display_name TEXT NOT NULL DEFAULT '',
                        created_at INTEGER NOT NULL,
                        FOREIGN KEY(note_id) REFERENCES notes(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attachments_note_id ON attachments(note_id)")
            }
        }

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
                .addMigrations(MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
