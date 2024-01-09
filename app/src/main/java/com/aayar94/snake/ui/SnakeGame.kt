package com.aayar94.snake.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random


data class State(
    val food: Pair<Int, Int>,
    val snake: List<Pair<Int, Int>>,
    var scoreState: Int,
    var highScore: Int
)

class SnakeGame(private val scope: CoroutineScope, context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("SnakePrefs", Context.MODE_PRIVATE)
    private val savedHighScore = sharedPreferences.getInt("high_score", 0)
    private val mutex = Mutex()
    private val mutableState =
        MutableStateFlow(
            State(
                food = Pair(5, 5),
                snake = listOf(Pair(7, 7)),
                scoreState = 0,
                highScore = savedHighScore
            )
        )
    val state: Flow<State> = mutableState


    var move = Pair(1, 0)
        set(value) {
            scope.launch {
                mutex.withLock {
                    field = value
                }
            }
        }

    init {
        scope.launch {
            var snakeLength = 4

            while (true) {
                delay(150)
                mutableState.update {
                    val newPosition = it.snake.first().let { poz ->
                        mutex.withLock {
                            Pair(
                                (poz.first + move.first + BOARD_SIZE) % BOARD_SIZE,
                                (poz.second + move.second + BOARD_SIZE) % BOARD_SIZE
                            )
                        }
                    }

                    if (newPosition == it.food) {
                        snakeLength++
                        it.scoreState = it.scoreState + 1
                        val editor = sharedPreferences.edit()
                        if (it.scoreState >= savedHighScore) {
                            editor.putInt("high_score", it.scoreState)
                            editor.apply()
                        }
                    }

                    if (it.snake.contains(newPosition)) {
                        snakeLength = 4
                    }

                    it.copy(
                        food = if (newPosition == it.food) Pair(
                            Random.nextInt(BOARD_SIZE),
                            Random.nextInt(BOARD_SIZE),
                        )
                        else it.food,
                        snake = listOf(newPosition) + it.snake.take(snakeLength - 1),
                    )
                }
            }
        }

    }

    companion object {
        const val BOARD_SIZE = 16

    }
}

@Composable
fun Snake(game: SnakeGame) {
    val state = game.state.collectAsState(initial = null)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()
    ) {
        Text(
            modifier = Modifier.padding(top = 4.dp), text = "High Score: ${state.value?.highScore}",
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = "Score: ${state.value?.scoreState}",
            color = MaterialTheme.colorScheme.primary
        )
        state.value?.let {
            Board(it)
        }
        Buttons {
            game.move = it
        }
    }

}

@Composable
fun Buttons(onDirectionChange: (Pair<Int, Int>) -> Unit) {
    val buttonSize = Modifier.size(64.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp).background(MaterialTheme.colorScheme.background)) {
        Button(onClick = { onDirectionChange(Pair(0, -1)) }, modifier = buttonSize) {
            Icon(Icons.Default.KeyboardArrowUp, null)
        }
        Row {
            Button(onClick = { onDirectionChange(Pair(-1, 0)) }, modifier = buttonSize) {
                Icon(Icons.Default.KeyboardArrowLeft, null)
            }
            Spacer(modifier = buttonSize)
            Button(onClick = { onDirectionChange(Pair(1, 0)) }, modifier = buttonSize) {
                Icon(Icons.Default.KeyboardArrowRight, null)
            }
        }
        Button(onClick = { onDirectionChange(Pair(0, 1)) }, modifier = buttonSize) {
            Icon(Icons.Default.KeyboardArrowDown, null)
        }
    }
}

@Composable
fun Board(state: State) {
    BoxWithConstraints(Modifier.padding(16.dp).background(MaterialTheme.colorScheme.background)) {
        val tileSize = maxWidth / SnakeGame.BOARD_SIZE

        Box(
            Modifier
                .size(maxWidth)
                .border(3.dp, MaterialTheme.colorScheme.primary)
        )

        Box(
            Modifier
                .offset(x = tileSize * state.food.first, y = tileSize * state.food.second)
                .size(tileSize)
                .background(
                    MaterialTheme.colorScheme.primary, CircleShape
                )
        )

        state.snake.forEach {
            Box(
                modifier = Modifier
                    .offset(x = tileSize * it.first, y = tileSize * it.second)
                    .size(tileSize)
                    .background(
                        MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}