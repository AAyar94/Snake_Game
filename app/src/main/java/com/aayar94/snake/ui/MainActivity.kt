package com.aayar94.snake.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.aayar94.snake.core.theme.SnakeTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val game = SnakeGame(lifecycleScope, applicationContext)

        setContent {
            SnakeTheme {
                val systemUiController = rememberSystemUiController()
                systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
                systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)
                Snake(game = game)
            }
        }
    }
}