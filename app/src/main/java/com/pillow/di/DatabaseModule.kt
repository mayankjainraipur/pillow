package com.pillow.di

import android.content.Context
import com.pillow.data.db.PillowDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun providePillowDatabase(
        @ApplicationContext context: Context
    ): PillowDatabase {
        return PillowDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideNoteDao(database: PillowDatabase) = database.noteDao()

    @Singleton
    @Provides
    fun provideCategoryDao(database: PillowDatabase) = database.categoryDao()

    @Singleton
    @Provides
    fun provideTagDao(database: PillowDatabase) = database.tagDao()

    @Singleton
    @Provides
    fun provideVoiceMemoDao(database: PillowDatabase) = database.voiceMemoDao()

    @Singleton
    @Provides
    fun provideAttachmentDao(database: PillowDatabase) = database.attachmentDao()
}
