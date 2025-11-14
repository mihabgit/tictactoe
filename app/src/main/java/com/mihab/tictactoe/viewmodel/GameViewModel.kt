package com.mihab.tictactoe.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mihab.tictactoe.model.*
import com.mihab.tictactoe.utils.PreferencesManager
import com.mihab.tictactoe.utils.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentPlayer = MutableStateFlow(Player.X)
    val currentPlayer: StateFlow<Player> get() = _currentPlayer

    private val prefs = PreferencesManager(application)
    enum class Difficulty { EASY, HARD }

    private var _singlePlayer = mutableStateOf(true)
    val singlePlayer: State<Boolean> get() = _singlePlayer

    private var _difficulty = mutableStateOf(Difficulty.HARD)
    val difficulty: State<Difficulty> get() = _difficulty

    private val _board = MutableStateFlow(Array(3) { Array(3) { CellState.EMPTY } })
    val board: StateFlow<Array<Array<CellState>>> = _board

    private val _result = MutableStateFlow(GameResult.ONGOING)
    val result: StateFlow<GameResult> = _result

    private val _winningLine = MutableStateFlow<GameLogic.WinningLine?>(null)
    val winningLine: StateFlow<GameLogic.WinningLine?> = _winningLine

    private val _xScore = MutableStateFlow(0)
    val xScore: StateFlow<Int> = _xScore

    private val _oScore = MutableStateFlow(0)
    val oScore: StateFlow<Int> = _oScore

    private val _soundOn = MutableStateFlow(true)
    val soundOn: StateFlow<Boolean> get() = _soundOn

    init {
        // Load saved preferences
        viewModelScope.launch {
            prefs.soundEnabled.collect { _soundOn.value = it }
        }
        viewModelScope.launch {
            prefs.xScore.combine(prefs.oScore) { x, o -> x to o }.collect {
                _xScore.value = it.first
                _oScore.value = it.second
            }
        }
    }

    fun toggleSound() {
        val newValue = !_soundOn.value
        _soundOn.value = newValue
        viewModelScope.launch { prefs.setSoundEnabled(newValue) }
        SoundManager.isSoundOn = newValue   // ✅ keep SoundManager in sync
    }
    fun makeMove(i: Int, j: Int) {
        if (_board.value[i][j] != CellState.EMPTY || _result.value != GameResult.ONGOING) return

        val player = _currentPlayer.value  // get the actual player from StateFlow
        _board.value[i][j] = if (player == Player.X) CellState.X else CellState.O

        checkAndProgress()
    }
    private fun checkAndProgress() {
        val r = GameLogic.checkResult(_board.value)
        _result.value = r

        _winningLine.value = if (r == GameResult.X_WINS || r == GameResult.O_WINS)
            GameLogic.getWinningLine(_board.value)
        else null

        when (r) {
            GameResult.X_WINS -> _xScore.value += 1
            GameResult.O_WINS -> _oScore.value += 1
            else -> {}
        }

        // Save score updates
        viewModelScope.launch { prefs.updateScore(_xScore.value, _oScore.value) }

        if (r == GameResult.ONGOING) {
            _currentPlayer.value = if (_currentPlayer.value == Player.X) Player.O else Player.X

            if (_singlePlayer.value && _currentPlayer.value == Player.O) {
                aiMove()
            }

        }
    }

    fun resetGame() {
        _board.value = Array(3) { Array(3) { CellState.EMPTY } }
        _result.value = GameResult.ONGOING
        _winningLine.value = null   // ✅ Hide winning line
        _currentPlayer.value = Player.X   // Correct way to reset the current player
    }

    fun resetScores() {
        _xScore.value = 0
        _oScore.value = 0
        viewModelScope.launch { prefs.resetScores() }
        resetGame()
    }

    fun setDifficulty(d: Difficulty) {
        _difficulty.value = d
        restartMatch()
    }

    fun toggleMode() {
        _singlePlayer.value = !_singlePlayer.value
        restartMatch()
    }

    fun resetBoard() {
        _board.value = GameLogic.createEmptyBoard()
        _currentPlayer.value = Player.X
        _result.value = GameResult.ONGOING
        resetGame()
    }

    fun restartMatch() {
        _xScore.value = 0
        _oScore.value = 0
        resetBoard()
    }

    private fun aiMove() {
        viewModelScope.launch {
            delay(500L) // Small delay for natural gameplay (half second)

            val boardCopy = _board.value.map { it.copyOf() }.toTypedArray()

            when (_difficulty.value) {
                Difficulty.EASY -> {
                    val emptyCells = mutableListOf<Pair<Int, Int>>()
                    for (i in 0..2)
                        for (j in 0..2)
                            if (boardCopy[i][j] == CellState.EMPTY)
                                emptyCells.add(i to j)

                    if (emptyCells.isNotEmpty()) {
                        val (r, c) = emptyCells.random(Random)
                        GameLogic.makeMove(boardCopy, r, c, Player.O)
                        _board.value = boardCopy
                        checkAndProgress()
                    }
                }

                Difficulty.HARD -> {
                    val best = GameLogic.findBestMove(boardCopy)
                    if (best != null) {
                        GameLogic.makeMove(boardCopy, best.first, best.second, Player.O)
                        _board.value = boardCopy
                        checkAndProgress()
                    }
                }
            }
        }
    }

}
