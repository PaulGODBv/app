package com.universidad.reta2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.universidad.reta2.ui.navigation.BottomNavigationBar
import com.universidad.reta2.ui.navigation.NavGraph
import com.universidad.reta2.ui.navigation.Screen
import com.universidad.reta2.ui.theme.Reta2Theme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.currentBackStackEntryAsState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent()
        }
    }
}


@Composable
fun AppContent() {
    Reta2Theme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomNav = currentRoute in listOf(
            Screen.Home.route,
            Screen.Competencies.route,
            Screen.Progress.route,
            Screen.Profile.route
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                bottomBar = {
                    if (showBottomNav) {
                        BottomNavigationBar(navController = navController)
                    }
                }
            ) { innerPadding ->
                NavGraph(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
