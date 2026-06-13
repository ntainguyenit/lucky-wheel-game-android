package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class GameRoundState {
    BETTING,  // Users place bets
    SPINNING, // Wheel/Spotlight is rotating
    SETTLED   // Result declared, matching payouts credited
}

data class AnimalConfig(
    val name: String,
    val multiplier: Double,
    val iconEmoji: String,
    val hexColor: String
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GameDatabase.getDatabase(application)
    val repository = GameRepository(db)

    // Constants & Game rules
    val animalsList = listOf(
        AnimalConfig("Rabbit", 5.0, "🐰", "#FF8DA1"), // 0
        AnimalConfig("Cat", 5.0, "🐱", "#FFC785"),    // 45 deg
        AnimalConfig("Dog", 5.0, "🐶", "#7CE2A6"),    // 90 deg
        AnimalConfig("Sheep", 5.0, "🐑", "#A2DEFF"),  // 135 deg
        AnimalConfig("Panda", 10.0, "🐼", "#FFC93C"), // 180 deg
        AnimalConfig("Bear", 15.0, "🐻", "#FF9F43"),  // 225 deg
        AnimalConfig("Tiger", 25.0, "🐯", "#EE5A24"), // 270 deg
        AnimalConfig("Lion", 45.0, "🦁", "#F53B57")   // 315 deg
    )

    val chipOptions = listOf(10, 100, 500, 1000, 10000)

    // ------------------ UI STATE FLOWS ------------------

    val activeUser = repository.activeUserFlow
    val leaderboard = repository.leaderboardFlow
    val listSessions = repository.lastSessionsFlow
    val adminSettings = repository.adminSettingsFlow
    val listLogs = repository.allLogsFlow

    private val _currentPlayerBets = MutableStateFlow<Map<String, Double>>(emptyMap())
    val currentPlayerBets: StateFlow<Map<String, Double>> = _currentPlayerBets.asStateFlow()

    private val _otherPlayersBets = MutableStateFlow<Map<String, Double>>(emptyMap())
    val otherPlayersBets: StateFlow<Map<String, Double>> = _otherPlayersBets.asStateFlow()

    private val _gameState = MutableStateFlow(GameRoundState.BETTING)
    val gameState: StateFlow<GameRoundState> = _gameState.asStateFlow()

    private val _countdown = MutableStateFlow(15)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private val _selectedChip = MutableStateFlow(100)
    val selectedChip: StateFlow<Int> = _selectedChip.asStateFlow()

    private val _highlightedIndex = MutableStateFlow(0) // Used for spinning wheel graphics
    val highlightedIndex: StateFlow<Int> = _highlightedIndex.asStateFlow()

    private val _recentWinnings = MutableSharedFlow<Pair<String, Double>>(replay = 1)
    val recentWinnings = _recentWinnings.asSharedFlow()

    private val _isMusicOn = MutableStateFlow(true)
    val isMusicOn = _isMusicOn.asStateFlow()

    private val _activeViewers = MutableStateFlow(Random.nextInt(12, 45))
    val activeViewers = _activeViewers.asStateFlow()

    private val _lastBetsPlaced = MutableStateFlow<Map<String, Double>>(emptyMap()) // for Repeat button
    val lastBetsPlaced = _lastBetsPlaced.asStateFlow()

    private var gameLoopJob: Job? = null

    init {
        startBettingSessionLoop()
    }

    // ------------------ GAME CONTROL METHODS ------------------

    fun setMusicEnabled(enabled: Boolean) {
        _isMusicOn.value = enabled
    }

    fun selectChipValue(value: Int) {
        _selectedChip.value = value
    }

    /**
     * Places a bet on a selected animal for the active user.
     */
    fun placeBet(animalName: String) {
        if (_gameState.value != GameRoundState.BETTING) return

        viewModelScope.launch {
            val user = repository.getActiveUser() ?: return@launch
            val betSize = _selectedChip.value.toDouble()

            if (user.balance >= betSize) {
                // Deduct balance and record bet state
                val debitSuccess = repository.placeBet(user.id, animalName, betSize)
                if (debitSuccess) {
                    val currentMap = _currentPlayerBets.value.toMutableMap()
                    currentMap[animalName] = (currentMap[animalName] ?: 0.0) + betSize
                    _currentPlayerBets.value = currentMap
                }
            }
        }
    }

    /**
     * Repeat last betting configuration.
     */
    fun repeatLastBets() {
        if (_gameState.value != GameRoundState.BETTING) return
        val lastBetMap = _lastBetsPlaced.value
        if (lastBetMap.isEmpty()) return

        viewModelScope.launch {
            val user = repository.getActiveUser() ?: return@launch
            val totalCost = lastBetMap.values.sum()
            if (user.balance >= totalCost) {
                for ((animal, amount) in lastBetMap) {
                    val ok = repository.placeBet(user.id, animal, amount)
                    if (ok) {
                        val currentMap = _currentPlayerBets.value.toMutableMap()
                        currentMap[animal] = (currentMap[animal] ?: 0.0) + amount
                        _currentPlayerBets.value = currentMap
                    }
                }
            }
        }
    }

    /**
     * Simple reactive scheduler that loops between BETTING, SPINNING, SETTLED
     */
    private fun startBettingSessionLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (true) {
                // PHASE 1: BETTING WINDOW
                _gameState.value = GameRoundState.BETTING
                _currentPlayerBets.value = emptyMap()
                _otherPlayersBets.value = emptyMap()
                
                val currentConfig = repository.getAdminSettings()
                var secCountdown = currentConfig.countdownSeconds
                
                while (secCountdown > 0) {
                    _countdown.value = secCountdown
                    delay(1000L)
                    secCountdown--
                    
                    // Simulate random multiplayer chips flying to make app feel alive!
                    simulateOtherPlayersActions()
                }

                _countdown.value = 0
                delay(500L)

                // PHASE 2: SPINNING WHEEL
                _gameState.value = GameRoundState.SPINNING
                
                // Determine layout result index
                val winningIndex = calculateWinningIndex()
                val winningAnimalConfig = animalsList[winningIndex]

                // Animate wheel spotlight revolutions - custom decelerating delay loops
                var currentSpinIndex = _highlightedIndex.value
                val fullSpins = 4 // number of cycles
                val totalSteps = (fullSpins * 8) + (winningIndex - currentSpinIndex + 8) % 8
                
                for (step in 1..totalSteps) {
                    currentSpinIndex = (currentSpinIndex + 1) % 8
                    _highlightedIndex.value = currentSpinIndex
                    
                    // Progressive slow down formula
                    val stepDelay = if (step > totalSteps - 8) {
                        100L + (100L * (8 - (totalSteps - step)))
                    } else {
                        50.0 + (step * 0.8)
                    }
                    delay(stepDelay.toLong())
                }

                _highlightedIndex.value = winningIndex

                // PHASE 3: SETTLE OUTCOME
                _gameState.value = GameRoundState.SETTLED
                resolveBetPayouts(winningAnimalConfig)

                // Cache actual bets for repeat button if applicable
                if (_currentPlayerBets.value.isNotEmpty()) {
                    _lastBetsPlaced.value = _currentPlayerBets.value
                }

                // Randomize viewers counter slightly
                _activeViewers.value = maxOf(5, _activeViewers.value + Random.nextInt(-3, 4))

                delay(4000L) // Wait 4 seconds for victory celebratory state
            }
        }
    }

    private fun simulateOtherPlayersActions() {
        if (Random.nextFloat() > 0.4f) return
        val randAnimal = animalsList.random().name
        val randChip = chipOptions.random().toDouble()
        val currentOthers = _otherPlayersBets.value.toMutableMap()
        currentOthers[randAnimal] = (currentOthers[randAnimal] ?: 0.0) + randChip
        _otherPlayersBets.value = currentOthers
    }

    /**
     * Determines which index wins, respecting Admin configuration criteria (RTP, Rig options)
     */
    private suspend fun calculateWinningIndex(): Int {
        val settings = repository.getAdminSettings()
        val playerBets = _currentPlayerBets.value
        
        // If player made no bet, resolve 100% randomly
        if (playerBets.isEmpty()) {
            return Random.nextInt(0, 8)
        }

        // Apply RIGGED/RTP logic
        val outcomeRoll = Random.nextFloat() // 0.0f to 1.0f

        return when (settings.isRiggedMode) {
            "FORCE_WIN" -> {
                // Pick any animal that the user has bet on (preferably with the highest bet size)
                val winningBets = playerBets.filter { it.value > 0.0 }
                if (winningBets.isNotEmpty()) {
                    val selectedName = winningBets.keys.random()
                    animalsList.indexOfFirst { it.name == selectedName }.coerceAtLeast(0)
                } else {
                    Random.nextInt(0, 8)
                }
            }
            "FORCE_MIN_LOSS", "REDUCE_WINS" -> {
                // Find animals with ZERO bets placed on them
                val unselectedAnimals = animalsList.filter { it.name !in playerBets.keys }
                if (unselectedAnimals.isNotEmpty()) {
                    val selectedAnimal = unselectedAnimals.random()
                    animalsList.indexOf(selectedAnimal).coerceAtLeast(0)
                } else {
                    // Pick the animal with the lowest potential payout
                    var bestIndex = 0
                    var minPayout = Double.MAX_VALUE
                    for ((index, item) in animalsList.withIndex()) {
                        val betOnThis = playerBets[item.name] ?: 0.0
                        val payout = betOnThis * item.multiplier
                        if (payout < minPayout) {
                            minPayout = payout
                            bestIndex = index
                        }
                    }
                    bestIndex
                }
            }
            else -> {
                // Normal RTP distribution
                // settings.rtpRatio decides if we let the user win.
                if (outcomeRoll <= settings.rtpRatio) {
                    // Allow normal statistical spin
                    Random.nextInt(0, 8)
                } else {
                    // Counteract user wins to lower RTP: Pick a non-bet index or low-paying index
                    val unselected = animalsList.filter { it.name !in playerBets.keys }
                    if (unselected.isNotEmpty()) {
                        animalsList.indexOf(unselected.random()).coerceAtLeast(0)
                    } else {
                        Random.nextInt(0, 8)
                    }
                }
            }
        }
    }

    private suspend fun resolveBetPayouts(winningAnimal: AnimalConfig) {
        val user = repository.getActiveUser() ?: return
        val playerBets = _currentPlayerBets.value
        
        val userWagerOnWin = playerBets[winningAnimal.name] ?: 0.0
        val isUserWinner = userWagerOnWin > 0.0
        val finalMultiplier = winningAnimal.multiplier
        val netPayout = userWagerOnWin * finalMultiplier

        // Record session history
        repository.recordGameSession(winningAnimal.name, finalMultiplier)

        // Save Bet history logs
        for ((animal, wgr) in playerBets) {
            val wonThisItem = animal == winningAnimal.name
            val itemPayout = if (wonThisItem) wgr * finalMultiplier else 0.0
            
            repository.insertBetHistory(
                BetHistoryEntity(
                    userId = user.id,
                    animalSelected = animal,
                    betAmount = wgr,
                    targetMultiplier = if (wonThisItem) finalMultiplier else 0.0,
                    winningAnimal = winningAnimal.name,
                    isWin = wonThisItem,
                    netPayout = itemPayout
                )
            )
        }

        // If player won, credit back to database wallet
        if (isUserWinner && netPayout > 0.0) {
            repository.depositCoins(user.id, netPayout)
            _recentWinnings.emit(winningAnimal.name to netPayout)
        } else {
            _recentWinnings.emit(winningAnimal.name to 0.0)
        }
    }

    // ------------------ WALLET & MANAGEMENT FUNCTIONS ------------------

    fun depositFunds(amount: Double, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.getActiveUser()
            if (user != null) {
                val success = repository.depositCoins(user.id, amount)
                onFinished(success)
            } else {
                onFinished(false)
            }
        }
    }

    fun withdrawFunds(amount: Double, onFinished: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getActiveUser()
            if (user == null) {
                onFinished("Please login first.")
                return@launch
            }
            if (user.balance < amount) {
                onFinished("Insufficient balance!")
                return@launch
            }
            val settings = repository.getAdminSettings()
            if (amount < settings.minWithdrawalLimit) {
                onFinished("Minimum withdrawal limit is ${settings.minWithdrawalLimit} coins.")
                return@launch
            }

            val success = repository.withdrawCoins(user.id, amount)
            if (success) {
                onFinished("SUCCESS")
            } else {
                onFinished("Transaction processing failed.")
            }
        }
    }

    fun registerOrLoginUsername(username: String, avatarSelection: Int, referral: String?, onLoggedIn: () -> Unit) {
        viewModelScope.launch {
            if (username.trim().length >= 3) {
                repository.loginOrRegisterUser(username.trim(), avatarSelection, referral)
                onLoggedIn()
            }
        }
    }

    fun claimDailyReward(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getActiveUser()
            if (user == null) {
                onResult("LOGIN_REQUIRED")
                return@launch
            }
            val gift = repository.claimDailyBonus(user.id)
            if (gift != null) {
                onResult("SUCCESS:$gift")
            } else {
                onResult("ALREADY_CLAIMED")
            }
        }
    }

    fun updateAdminToggles(rtp: Float, rawRigMode: String, secs: Int) {
        viewModelScope.launch {
            val config = repository.getAdminSettings()
            repository.saveAdminSettings(
                config.copy(
                    rtpRatio = rtp.coerceIn(0f, 1f),
                    isRiggedMode = rawRigMode,
                    countdownSeconds = secs.coerceIn(5, 60)
                )
            )
        }
    }

    fun forceSetAnyUserBalance(targetUser: UserEntity, newBalance: Double) {
        viewModelScope.launch {
            repository.updateUser(targetUser.copy(balance = newBalance))
        }
    }
}
