package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "backlight_config")
data class BacklightConfigEntity(
    @PrimaryKey val id: Int = 1,
    val filePath: String = "/sys/class/leds/button-backlight/brightness",
    val activeValue: Int = 1,
    val inactiveValue: Int = 0,
    val isDeactivated: Boolean = false
)

@Entity(tableName = "command_logs")
data class CommandLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val actionType: String, // "ENABLE", "DISABLE", "PATH_CHANGE", "ROOT_CHECK", "CUSTOM_CHANGE"
    val command: String,
    val isSuccess: Boolean,
    val details: String
)

@Dao
interface BacklightDao {
    @Query("SELECT * FROM backlight_config WHERE id = 1")
    fun getConfig(): Flow<BacklightConfigEntity?>

    @Query("SELECT * FROM backlight_config WHERE id = 1")
    suspend fun getConfigDirect(): BacklightConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: BacklightConfigEntity)
}

@Dao
interface LogDao {
    @Query("SELECT * FROM command_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<CommandLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CommandLogEntity)

    @Query("DELETE FROM command_logs")
    suspend fun clearLogs()
}

@Database(entities = [BacklightConfigEntity::class, CommandLogEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun backlightDao(): BacklightDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "backlight_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
