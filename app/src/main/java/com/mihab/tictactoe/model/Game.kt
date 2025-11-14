package com.mihab.tictactoe.model

import kotlin.math.max
import kotlin.math.min

enum class Player { X, O }
enum class CellState { EMPTY, X, O }
enum class GameResult {
    ONGOING, DRAW, X_WINS, O_WINS
}

typealias Board = Array<Array<CellState>>

object GameLogic {
    fun createEmptyBoard(): Board = Array(3) { Array(3) { CellState.EMPTY } }

    fun makeMove(board: Board, row: Int, col: Int, player: Player): Boolean {
        if (board[row][col] != CellState.EMPTY) return false
        board[row][col] = if (player == Player.X) CellState.X else CellState.O
        return true
    }

    fun checkResult(board: Board): GameResult {
        // rows & cols
        for (i in 0..2) {
            if (board[i][0] != CellState.EMPTY &&
                board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return if (board[i][0] == CellState.X) GameResult.X_WINS else GameResult.O_WINS
            }
            if (board[0][i] != CellState.EMPTY &&
                board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return if (board[0][i] == CellState.X) GameResult.X_WINS else GameResult.O_WINS
            }
        }
        // diagonals
        if (board[0][0] != CellState.EMPTY &&
            board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return if (board[0][0] == CellState.X) GameResult.X_WINS else GameResult.O_WINS
        }
        if (board[0][2] != CellState.EMPTY &&
            board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return if (board[0][2] == CellState.X) GameResult.X_WINS else GameResult.O_WINS
        }
        // draw?
        val anyEmpty = board.any { row -> row.any { it == CellState.EMPTY } }
        return if (!anyEmpty) GameResult.DRAW else GameResult.ONGOING
    }

    // -----------------------
    // Minimax for "Hard" AI
    // -----------------------
    // We will treat AI (O) as the maximizing player.
    // Scores:
    //  +10 - depth => O wins (prefer faster wins)
    //  -10 + depth => X wins  (prefer slower losses)
    //   0 => draw

    private fun evaluate(board: Board, depth: Int): Int {
        return when (checkResult(board)) {
            GameResult.O_WINS -> 10 - depth
            GameResult.X_WINS -> -10 + depth
            GameResult.DRAW, GameResult.ONGOING -> 0
        }
    }

    private fun availableMoves(board: Board): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) for (j in 0..2) if (board[i][j] == CellState.EMPTY) moves.add(i to j)
        return moves
    }

    // minimax returns best score for the current player perspective (isMaximizing = AI/O)
    private fun minimax(board: Board, depth: Int, isMaximizing: Boolean): Int {
        val result = checkResult(board)
        if (result != GameResult.ONGOING) {
            return evaluate(board, depth)
        }

        val moves = availableMoves(board)
        if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for ((r, c) in moves) {
                board[r][c] = CellState.O
                val score = minimax(board, depth + 1, false)
                board[r][c] = CellState.EMPTY
                bestScore = max(bestScore, score)
            }
            return bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for ((r, c) in moves) {
                board[r][c] = CellState.X
                val score = minimax(board, depth + 1, true)
                board[r][c] = CellState.EMPTY
                bestScore = min(bestScore, score)
            }
            return bestScore
        }
    }

    /**
     * Returns best move for AI (O) using minimax.
     * If board is full or game over, returns null.
     */
    fun findBestMove(board: Board): Pair<Int, Int>? {
        if (checkResult(board) != GameResult.ONGOING) return null

        var bestScore = Int.MIN_VALUE
        var bestMove: Pair<Int, Int>? = null

        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == CellState.EMPTY) {
                    board[i][j] = CellState.O
                    val score = minimax(board, 0, false)
                    board[i][j] = CellState.EMPTY

                    if (score > bestScore) {
                        bestScore = score
                        bestMove = i to j
                    }
                }
            }
        }
        return bestMove
    }

    data class WinningLine(val start: Pair<Int, Int>, val end: Pair<Int, Int>)

    fun getWinningLine(board: Board): WinningLine? {
        // Rows
        for (i in 0..2) {
            if (board[i][0] != CellState.EMPTY &&
                board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return WinningLine(i to 0, i to 2)
            }
        }
        // Columns
        for (j in 0..2) {
            if (board[0][j] != CellState.EMPTY &&
                board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
                return WinningLine(0 to j, 2 to j)
            }
        }
        // Diagonals
        if (board[0][0] != CellState.EMPTY &&
            board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return WinningLine(0 to 0, 2 to 2)
        }
        if (board[0][2] != CellState.EMPTY &&
            board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return WinningLine(0 to 2, 2 to 0)
        }
        return null
    }

}
