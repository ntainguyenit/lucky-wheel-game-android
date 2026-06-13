package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ------------------ ENTITIES ------------------

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val avatarId: Int, // index for avatar selection
    val balance: Double, // current game coins/wallet balance
    val referralCode: String,
    val referredBy: String? = null,
    val lastDailyClaimed: Long = 0L,
    val consecutiveDailyDays: Int = 0,
    val totalReferralsCount: Int = 0,
    val isLoggedIn: Boolean = false,
    val referralBonusClaimed: Double = 0.0
)

@Entity(tableName = "bet_history")
data class BetHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val animalSelected: String, // Rabbit, Cat, Dog, Sheep, Panda, Bear, Tiger, Lion
    val betAmount: Double,
    val targetMultiplier: Double,
    val winningAnimal: String,
    val isWin: Boolean,
    val netPayout: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val winningAnimal: String,
    val multiplier: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_settings")
data class AdminSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val rtpRatio: Float = 0.70f, // 70% RTP target. User wins/losses obey this.
    val isRiggedMode: String = "NORMAL", // "NORMAL", "FORCE_WIN", "FORCE_MIN_LOSS", "REDUCE_WINS"
    val countdownSeconds: Int = 15,
    val bonusMultiplierEnabled: Boolean = true,
    val minWithdrawalLimit: Double = 100.0,
    val maxDepositLimit: Double = 5000.0
)

@Entity(tableName = "transaction_logs")
data class TransactionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val type: String, // "DEPOSIT", "WITHDRAWAL", "REFERRAL_BONUS", "DAILY_CLAIM"
    val amount: Double,
    val status: String, // "PENDING", "COMPLETED", "FAILED"
    val timestamp: Long = System.currentTimeMillis()
)

// ------------------ DAOS ------------------

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveUser(): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY balance DESC LIMIT 20")
    fun getLeaderboardFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE referralCode = :code LIMIT 1")
    suspend fun getUserByReferralCode(code: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>
    
    @Query("UPDATE users SET balance = balance + :amount WHERE id = :userId")
    suspend fun creditBalance(userId: Int, amount: Double)

    @Query("UPDATE users SET balance = balance - :amount WHERE id = :userId")
    suspend fun debitBalance(userId: Int, amount: Double)
}

@Dao
interface BetHistoryDao {
    @Query("SELECT * FROM bet_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT 50")
    fun getBetsForUserFlow(userId: Int): Flow<List<BetHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBet(bet: BetHistoryEntity)

    @Query("DELETE FROM bet_history")
    suspend fun clearHistory()
}

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM game_sessions ORDER BY timestamp DESC LIMIT 20")
    fun getLastSessionsFlow(): Flow<List<GameSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSessionEntity)

    @Query("DELETE FROM game_sessions")
    suspend fun clearSessions()
}

@Dao
interface AdminSettingsDao {
    @Query("SELECT * FROM admin_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<AdminSettingsEntity?>

    @Query("SELECT * FROM admin_settings WHERE id = 1")
    suspend fun getSettings(): AdminSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AdminSettingsEntity)
}

@Dao
interface TransactionLogDao {
    @Query("SELECT * FROM transaction_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<TransactionLogEntity>>

    @Query("SELECT * FROM transaction_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getLogsForUserFlow(userId: Int): Flow<List<TransactionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TransactionLogEntity)

    @Query("UPDATE transaction_logs SET status = :status WHERE id = :id")
    suspend fun updateLogStatus(id: Int, status: String)
}

// ------------------ DATABASE ------------------

@Database(
    entities = [
        UserEntity::class,
        BetHistoryEntity::class,
        GameSessionEntity::class,
        AdminSettingsEntity::class,
        TransactionLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun betHistoryDao(): BetHistoryDao
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun adminSettingsDao(): AdminSettingsDao
    abstract fun transactionLogDao(): TransactionLogDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: android.content.Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "mix_spin_game_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
