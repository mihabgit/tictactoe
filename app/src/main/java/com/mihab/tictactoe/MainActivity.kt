package com.mihab.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mihab.tictactoe.ui.theme.TicTacToeTheme
import com.mihab.tictactoe.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToeTheme {
                com.mihab.tictactoe.ui.screens.GameScreen(viewModel = vm)
            }
        }
    }

}
