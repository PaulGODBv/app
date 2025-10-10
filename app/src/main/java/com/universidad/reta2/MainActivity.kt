package com.universidad.reta2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.universidad.reta2.ui.navigation.NavGraph
import com.universidad.reta2.ui.theme.Reta2Theme
import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.local.mappers.UserStatsMapper
import javax.inject.Inject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Reta2App()
        }
    }
}


class StatsInitializer @Inject constructor(
    private val userStatsDao: UserStatsDao
) {
    suspend fun initializeUserStats(username: String) {
        val existingStats = userStatsDao.getUserStatsSync(username)
        if (existingStats == null) {
            val initialStats = UserStatsMapper.createInitialStats(username)
            userStatsDao.updateUserStats(initialStats)
        }
    }
}

@Composable
fun Reta2App(){
    Reta2Theme{
        Surface (
            modifier=Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ){
            val navController=rememberNavController()

            NavGraph(navController=navController)
        }
    }
}