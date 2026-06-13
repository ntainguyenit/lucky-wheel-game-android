package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class GameRepository(private val db: GameDatabase) {

    private val userDao = db.userDao()
    private val betHistoryDao = db.betHistoryDao()
    private val gameSessionDao = db.gameSessionDao()
    private val adminSettingsDao = db.adminSettingsDao()
    private val transactionLogDao = db.transactionLogDao()

    // Flows for Reactive UI Updates
    val activeUserFlow: Flow<UserEntity?> = userDao.getActiveUserFlow()
    val leaderboardFlow: Flow<List<UserEntity>> = userDao.getLeaderboardFlow()
    val lastSessionsFlow: Flow<List<GameSessionEntity>> = gameSessionDao.getLastSessionsFlow()
    val adminSettingsFlow: Flow<AdminSettingsEntity?> = adminSettingsDao.getSettingsFlow()
    val allLogsFlow: Flow<List<TransactionLogEntity>> = transactionLogDao.getAllLogsFlow()

    suspend fun getActiveUser(): UserEntity? {
        return userDao.getActiveUser()
    }

    suspend fun getAllUsers(): List<UserEntity> {
        return userDao.getAllUsers()
    }

    suspend fun getAdminSettings(): AdminSettingsEntity {
        var settings = adminSettingsDao.getSettings()
        if (settings == null) {
            settings = AdminSettingsEntity()
            adminSettingsDao.saveSettings(settings)
        }
        return settings
    }

    suspend fun saveAdminSettings(settings: AdminSettingsEntity) {
        adminSettingsDao.saveSettings(settings)
    }

    /**
     * Attempts login or registration of a user profile. Handles session exclusion.
     */
    suspend fun loginOrRegisterUser(username: String, avatarId: Int, referralCodeToUse: String?): UserEntity {
        // Log out all existing users to enforce single session offline style
        val allUsers = userDao.getAllUsers()
        for (u in allUsers) {
            if (u.isLoggedIn) {
                userDao.updateUser(u.copy(isLoggedIn = false))
            }
        }

        // Try to fetch user by name
        var user = userDao.getUserByUsername(username)
        if (user == null) {
            // Register new user
            val generatedReferral = "REF${Random.nextInt(100000, 999999)}"
            val startingBalance = 1000.0 // Give some initial free play coins!
            
            var referralBonus = 0.0
            var parentReferralUser: UserEntity? = null
            
            if (!referralCodeToUse.isNullOrBlank()) {
                parentReferralUser = userDao.getUserByReferralCode(referralCodeToUse.trim().uppercase())
                if (parentReferralUser != null) {
                    referralBonus = 200.0 // Referral signup bonus
                }
            }

            user = UserEntity(
                username = username,
                avatarId = avatarId,
                balance = startingBalance + referralBonus,
                referralCode = generatedReferral,
                referredBy = parentReferralUser?.referralCode,
                consecutiveDailyDays = 1,
                lastDailyClaimed = System.currentTimeMillis(),
                isLoggedIn = true
            )
            
            val userId = userDao.insertUser(user).toInt()
            user = user.copy(id = userId)

            // Log starting gift
            transactionLogDao.insertLog(
                TransactionLogEntity(
                    userId = userId,
                    type = "DEPOSIT",
                    amount = startingBalance,
                    status = "COMPLETED"
                )
            )

            // If referred, handle referral bonus logger
            if (parentReferralUser != null) {
                transactionLogDao.insertLog(
                    TransactionLogEntity(
                        userId = userId,
                        type = "REFERRAL_BONUS",
                        amount = referralBonus,
                        status = "COMPLETED"
                    )
                )

                // Reward parent user as well
                val updatedParent = parentReferralUser.copy(
                    balance = parentReferralUser.balance + 300.0,
                    totalReferralsCount = parentReferralUser.totalReferralsCount + 1
                )
                userDao.updateUser(updatedParent)
                
                transactionLogDao.insertLog(
                    TransactionLogEntity(
                        userId = parentReferralUser.id,
                        type = "REFERRAL_BONUS",
                        amount = 300.0,
                        status = "COMPLETED"
                    )
                )
            }
        } else {
            // Login existing user
            user = user.copy(isLoggedIn = true, avatarId = avatarId)
            userDao.updateUser(user)
        }

        // Initialize admin settings if non-existent
        getAdminSettings()

        return user
    }

    /**
     * Updates an arbitrary user entity (e.g. for admin settings).
     */
    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    /**
     * Performs coin credit (deposit).
     */
    suspend fun depositCoins(userId: Int, amount: Double): Boolean {
        if (amount <= 0) return false
        userDao.creditBalance(userId, amount)
        transactionLogDao.insertLog(
            TransactionLogEntity(
                userId = userId,
                type = "DEPOSIT",
                amount = amount,
                status = "COMPLETED"
            )
        )
        return true
    }

    /**
     * Performs coin debit (withdrawal request).
     */
    suspend fun withdrawCoins(userId: Int, amount: Double): Boolean {
        val user = userDao.getActiveUser() ?: return false
        if (amount <= 0 || user.balance < amount) return false
        
        userDao.debitBalance(userId, amount)
        transactionLogDao.insertLog(
            TransactionLogEntity(
                userId = userId,
                type = "WITHDRAWAL",
                amount = amount,
                status = "COMPLETED"
            )
        )
        return true
    }

    /**
     * Claim daily bonus reward.
     */
    suspend fun claimDailyBonus(userId: Int): Double? {
        val user = userDao.getActiveUser() ?: return null
        val curTime = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        
        val diff = curTime - user.lastDailyClaimed
        if (diff < oneDayMillis && user.lastDailyClaimed != 0L) {
            // Already claimed within 24hr or calendar window
            // Return null indicating cooled down
            return null
        }

        // Calculate consecutive streak days
        val isConsecutive = diff <= 2 * oneDayMillis
        val nextStreak = if (isConsecutive && user.lastDailyClaimed != 0L) {
            user.consecutiveDailyDays % 7 + 1
        } else {
            1
        }

        // Base reward is 50. Scale by streak up to 350
        val rewardAmount = nextStreak * 50.0

        val updatedUser = user.copy(
            balance = user.balance + rewardAmount,
            lastDailyClaimed = curTime,
            consecutiveDailyDays = nextStreak
        )
        userDao.updateUser(updatedUser)

        transactionLogDao.insertLog(
            TransactionLogEntity(
                userId = userId,
                type = "DAILY_CLAIM",
                amount = rewardAmount,
                status = "COMPLETED"
            )
        )

        return rewardAmount
    }

    /**
     * Record a specific bet for auditing.
     */
    suspend fun placeBet(userId: Int, animal: String, amount: Double): Boolean {
        if (amount <= 0) return false
        val user = userDao.getActiveUser() ?: return false
        if (user.balance < amount) return false

        userDao.debitBalance(userId, amount)
        return true
    }

    /**
     * Injects a recorded history.
     */
    suspend fun insertBetHistory(bet: BetHistoryEntity) {
        betHistoryDao.insertBet(bet)
    }

    /**
     * Resolves game sessions, credits winning transactions, saves session histories.
     */
    suspend fun recordGameSession(winningAnimal: String, multiplier: Double) {
        gameSessionDao.insertSession(
            GameSessionEntity(
                winningAnimal = winningAnimal,
                multiplier = multiplier
            )
        )
    }

    suspend fun adminClearData() {
        betHistoryDao.clearHistory()
        gameSessionDao.clearSessions()
        // Reset balances of all registered players or delete and create a fresh state
    }
}
