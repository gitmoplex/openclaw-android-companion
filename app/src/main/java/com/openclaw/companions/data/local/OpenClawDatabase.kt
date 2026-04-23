package com.openclaw.companions.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.openclaw.companions.data.local.dao.MessageDao
import com.openclaw.companions.data.local.entity.MessageEntity

@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class OpenClawDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
