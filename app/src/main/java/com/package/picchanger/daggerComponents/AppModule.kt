package com.package.picchhanger

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import com.package.picchhanger.repository.StorageRepository

@Module
object AppModule {
    @Provides
    @Singleton
    fun provideContentResolver(context: Context): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideStorageRepository(contentResolver: ContentResolver): StorageRepository =
        StorageRepository(contentResolver)
}
