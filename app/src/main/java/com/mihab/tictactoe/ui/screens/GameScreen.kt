package com.mihab.tictactoe.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihab.tictactoe.VibrationUtils
import com.mihab.tictactoe.model.CellState
import com.mihab.tictactoe.model.GameLogic
import com.mihab.tictactoe.model.GameResult
import com.mihab.tictactoe.utils.SoundManager
import com.mihab.tictactoe.viewmodel.GameViewModel


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val board by viewModel.board.collectAsState()
    val result by viewModel.result.collectAsState()
    val currentPlayer by viewModel.currentPlayer.collectAsState()
    val xScore by viewModel.xScore.collectAsState()
    val oScore by viewModel.oScore.collectAsState()
    val context = LocalContext.current

    val soundOn by viewModel.soundOn.collectAsState()

    val winningLine = viewModel.winningLine.collectAsState().value
    val winningCells = winningLine?.let { getWinningCells(it) } ?: emptyList()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Tic Tac Toe",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSound() }) {
                        Icon(
                            imageVector = if (soundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (soundOn) "Sound On" else "Sound Off",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1E1E2F)
                )
            )
        },
        containerColor = Color(0xFF12121C)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ✅ Game Controls (Mode + Difficulty)
            GameControls(viewModel = viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Scoreboard
            val singlePlayer = viewModel.singlePlayer.value

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (singlePlayer) "You (X): $xScore" else "Player X: $xScore",
                    color = Color(0xFF90CAF9),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (singlePlayer) "AI: $oScore" else "Player O: $oScore",
                    color = Color(0xFFF48FB1),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Game board
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    board.forEachIndexed { i, row ->
                        Row {
                            row.forEachIndexed { j, cellState ->
                                val isWinningCell = winningCells.any { it.first == i && it.second == j }
                                Cell(
                                    state = cellState,
                                    isWinningCell = isWinningCell,
                                    onClick = {
                                        viewModel.makeMove(i, j)
                                        if (soundOn) SoundManager.playMove(context)
                                        VibrationUtils.vibrate(context)
                                    }
                                )
                            }
                        }
                    }
                }

            }


            Spacer(modifier = Modifier.height(24.dp))

            // ✅ Game result message
            when (result) {
                GameResult.X_WINS -> Text("X Wins!", color = Color(0xFF90CAF9), fontWeight = FontWeight.Bold)
                GameResult.O_WINS -> Text("O Wins!", color = Color(0xFFF48FB1), fontWeight = FontWeight.Bold)
                GameResult.DRAW -> Text("It's a Draw!", color = Color.Gray, fontWeight = FontWeight.Bold)
                else -> Text("Current: ${currentPlayer.name}", color = Color.White)
            }

            // Play win sound & vibration
            LaunchedEffect(result) {
                if (result == GameResult.X_WINS || result == GameResult.O_WINS) {
                    if (soundOn) SoundManager.playWin(context)
                    VibrationUtils.vibrate(context, 120L)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ Reset buttons
            Row {
                Button(onClick = { viewModel.resetBoard() }) {
                    Text("Reset Board")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(onClick = { viewModel.restartMatch() }) {
                    Text("Restart Match")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.resetScores() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
            ) {
                Text("Reset Score", color = Color.White)
            }

        }
    }
}

@Composable
fun Cell(
    state: CellState,
    isWinningCell: Boolean,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }

    // Animate move bounce
    LaunchedEffect(state) {
        if (state != CellState.EMPTY) {
            scale.snapTo(0.8f)
            scale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // 🎨 Colors
    val baseColor = Color(0xFF2C2C3E)
    val xGlow = Color(0xFF00FF99)
    val oGlow = Color(0xFF00B3FF)

    val targetGlowColor =
        if (isWinningCell) {
            when (state) {
                CellState.X -> xGlow
                CellState.O -> oGlow
                else -> baseColor
            }
        } else baseColor

    // Background color animation
    val glowColor by animateColorAsState(
        targetValue = targetGlowColor,
        animationSpec = tween(600),
        label = "glowColor"
    )

    // Soft halo glow animation
    val glowAlpha by animateFloatAsState(
        targetValue = if (isWinningCell) 0.35f else 0f,
        animationSpec = tween(800),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .size(90.dp)
            .padding(4.dp)
            .scale(scale.value)
            .background(
                color = baseColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = state == CellState.EMPTY) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 🔥 Halo glow layer
        if (isWinningCell) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = targetGlowColor.copy(alpha = glowAlpha),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }

        // Icon (X / O)
        Text(
            text = when (state) {
                CellState.X -> "X"
                CellState.O -> "O"
                else -> ""
            },
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        )
    }
}



@Composable
fun WinningLineOverlay(
    line: GameLogic.WinningLine?,
    cellSize: Dp
) {
    if (line == null) return

    val progress by animateFloatAsState(
        targetValue = if (line != null) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 600),
        label = "lineAnim"
    )

    Canvas(
        modifier = Modifier
            .size(cellSize * 3 + 24.dp)
            .padding(4.dp)
    ) {
        val (start, end) = line

        // ✅ Convert dp → px correctly here
        val cell = cellSize.toPx() + 8.dp.toPx()

        val startX = start.second * cell + cell / 2
        val startY = start.first * cell + cell / 2
        val endX = end.second * cell + cell / 2
        val endY = end.first * cell + cell / 2

        val drawEndX = startX + (endX - startX) * progress
        val drawEndY = startY + (endY - startY) * progress

        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF00FF99), Color(0xFF00B3FF))
            ),
            start = Offset(startX, startY),
            end = Offset(drawEndX, drawEndY),
            strokeWidth = 10f,
            cap = StrokeCap.Round,
            alpha = 0.9f
        )
    }
}


@Composable
fun GameControls(viewModel: GameViewModel) {
    val singlePlayer = viewModel.singlePlayer.value
    val difficulty = viewModel.difficulty.value

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // --- Game Mode ---
        Text("Game Mode", color = Color.LightGray, modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.Center) {
            ModeButton(
                text = "Single Player",
                isSelected = singlePlayer,
                onClick = { if (!singlePlayer) viewModel.toggleMode() }
            )
            Spacer(Modifier.width(8.dp))
            ModeButton(
                text = "Two Player",
                isSelected = !singlePlayer,
                onClick = { if (singlePlayer) viewModel.toggleMode() }
            )
        }

        // --- Difficulty (only visible in Single Player mode) ---
        if (singlePlayer) {
            Spacer(Modifier.height(16.dp))
            Text("Difficulty", color = Color.LightGray, modifier = Modifier.padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                ModeButton(
                    text = "Easy",
                    isSelected = (difficulty == GameViewModel.Difficulty.EASY),
                    onClick = { viewModel.setDifficulty(GameViewModel.Difficulty.EASY) }
                )
                Spacer(Modifier.width(8.dp))
                ModeButton(
                    text = "Hard",
                    isSelected = (difficulty == GameViewModel.Difficulty.HARD),
                    onClick = { viewModel.setDifficulty(GameViewModel.Difficulty.HARD) }
                )
            }
        }
    }
}

@Composable
fun ModeButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF4CAF50) else Color(0xFF2C2C3C),
            contentColor = if (isSelected) Color.White else Color.LightGray
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

fun getWinningCells(line: GameLogic.WinningLine): List<Pair<Int, Int>> {
    val (start, end) = line
    val cells = mutableListOf<Pair<Int, Int>>()

    // Horizontal or vertical line
    if (start.first == end.first) { // same row
        for (col in start.second..end.second) {
            cells.add(start.first to col)
        }
    } else if (start.second == end.second) { // same column
        for (row in start.first..end.first) {
            cells.add(row to start.second)
        }
    } else { // diagonal
        val rowStep = if (end.first > start.first) 1 else -1
        val colStep = if (end.second > start.second) 1 else -1
        var r = start.first
        var c = start.second
        while (true) {
            cells.add(r to c)
            if (r == end.first && c == end.second) break
            r += rowStep
            c += colStep
        }
    }

    return cells
}

