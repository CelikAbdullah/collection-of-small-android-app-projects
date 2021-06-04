package celik.abdullah.authentication.di
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import celik.abdullah.authentication.database.AuthDatabase
import celik.abdullah.authentication.network.AuthApi
import celik.abdullah.authentication.repository.AuthRepository
import celik.abdullah.authentication.utils.Const.USER_CREDENTIALS
import celik.abdullah.authentication.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Singleton


@Module
@InstallIn(ActivityRetainedComponent::class)
object AppModule {


    @ActivityRetainedScoped
    @Provides
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return EncryptedSharedPreferences.create(
            USER_CREDENTIALS,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @ActivityRetainedScoped
    @Provides
    fun provideSharedPrefsEditor(
        sharedPreferences: SharedPreferences
    ): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    @ActivityRetainedScoped
    @Provides
    fun provideSessionManager(sharedPreferences: SharedPreferences,
                              sharedPrefsEditor: SharedPreferences.Editor) : SessionManager = SessionManager(sharedPreferences, sharedPrefsEditor)

    @ActivityRetainedScoped
    @Provides
    fun provideAuthRepository(service: AuthApi,
                              database: AuthDatabase,
                              sessionManager: SessionManager): AuthRepository = AuthRepository(service, database, sessionManager)
}