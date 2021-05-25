package celik.abdullah.pagingwithnetworkanddatabase.di

import android.content.Context
import androidx.room.Room
import celik.abdullah.pagingwithnetworkanddatabase.database.ItemDatabase
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
    fun provideDatabase(@ApplicationContext context: Context) : ItemDatabase {
        return Room.databaseBuilder(context.applicationContext,
            ItemDatabase::class.java,
            "item-database")
            .fallbackToDestructiveMigration()
            .build()
    }
}