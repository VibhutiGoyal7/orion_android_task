package com.vibhuti.orion

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vibhuti.orion.ui.home.HomeRoute
import com.vibhuti.orion.ui.insights.InsightsRoute
import com.vibhuti.orion.ui.theme.OrionColors
import com.vibhuti.orion.ui.theme.OrionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The design is dark-only: light status/navigation bar icons regardless of system theme.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            OrionTheme {
                val nav = rememberNavController()
                Scaffold(containerColor = OrionColors.Background) { padding ->
                    // Only consume the top inset; the Insights list draws under the nav bar and pads for it.
                    NavHost(
                        nav,
                        startDestination = "insights",
                        modifier = Modifier.padding(top = padding.calculateTopPadding()),
                    ) {
                        composable("insights") { InsightsRoute() }
                        composable("home") { HomeRoute() }
                    }
                }
            }
        }
    }
}
