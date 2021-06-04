package celik.abdullah.authentication.di

import android.content.Context
import androidx.room.Room
import celik.abdullah.authentication.database.AuthDatabase
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
    fun provideDatabase(@ApplicationContext context: Context) : AuthDatabase {
        return Room.databaseBuilder(context.applicationContext,
            AuthDatabase::class.java,
            "auth-database")
            .fallbackToDestructiveMigration()
            .build()
    }
}