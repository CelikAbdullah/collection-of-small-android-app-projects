package celik.abdullah.pagingwithnetworkanddatabase.di

import androidx.paging.ExperimentalPagingApi
import celik.abdullah.pagingwithnetworkanddatabase.database.ItemDatabase
import celik.abdullah.pagingwithnetworkanddatabase.network.ItemApi
import celik.abdullah.pagingwithnetworkanddatabase.repository.ItemsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@ExperimentalPagingApi
@Module
@InstallIn(ActivityRetainedComponent::class)
object AppModule {
    @ActivityRetainedScoped
    @Provides
    fun provideItemsRepository(service: ItemApi, database: ItemDatabase): ItemsRepository = ItemsRepository(service, database)
}