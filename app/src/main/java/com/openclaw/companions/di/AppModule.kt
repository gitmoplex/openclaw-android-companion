package com.openclaw.companions.di

import android.content.Context
import androidx.room.Room
import com.openclaw.companions.data.local.OpenClawDatabase
import com.openclaw.companions.data.remote.WebSocketService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(WebSockets)
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }

    @Provides
    @Singleton
    fun provideWebSocketService(client: HttpClient): WebSocketService {
        return WebSocketService(client)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OpenClawDatabase {
        return Room.databaseBuilder(
            context,
            OpenClawDatabase::class.java,
            "openclaw.db"
        ).build()
    }

    @Provides
    fun provideMessageDao(database: OpenClawDatabase) = database.messageDao()
}
