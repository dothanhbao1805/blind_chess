package ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import board.Board
import board.rememberBoard
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import pieces.Piece

@Composable
fun GameScreen(
    board: Board,
) {
    val navigator = LocalNavigator.currentOrThrow
    var gameKey by remember { mutableStateOf(0) }
    val board = rememberBoard(key = gameKey)

    if (board.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Game Over!") },
            text = {
                val winnerText = if (board.winner == Piece.Color.White) "Trắng" else "Đen"
                Text("$winnerText thắng!")
            },
            confirmButton = {
                Button(onClick = { gameKey++ }) {  // tăng key → rememberBoard tạo Board mới
                    Text("Chơi lại")
                }
            },
            dismissButton = {
                Button(onClick = { navigator.popUntilRoot() }) {
                    Text("Thoát")
                }
            }
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Row {
                IconButton(onClick = { navigator.popUntilRoot() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
                IconButton(onClick = { board.save() }) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null
                    )
                }
            }

            BoardUi(
                board = board,
                modifier = Modifier.weight(1f)
            )
        }
    }
}