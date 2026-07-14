package com.example.caloriestrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.caloriestrack.data.CaloriesRepository
import com.example.caloriestrack.data.CaloriesTrackDatabase
import com.example.caloriestrack.ui.analysis.AnalysisScreen
import com.example.caloriestrack.ui.goals.GoalsScreen
import com.example.caloriestrack.ui.history.HistoryScreen
import com.example.caloriestrack.ui.products.ProductsScreen
import com.example.caloriestrack.ui.theme.CaloriesTrackTheme
import com.example.caloriestrack.ui.today.TodayScreen
import com.example.caloriestrack.ui.weight.WeightScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CaloriesTrackTheme {
                CaloriesTrackApp()
            }
        }
    }
}

@Composable
fun CaloriesTrackApp() {
    var selectedSection by remember { mutableStateOf(AppSection.Today) }
    val context = LocalContext.current
    val repository = remember(context) {
        CaloriesRepository.fromDatabase(CaloriesTrackDatabase.getDatabase(context))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                AppSection.entries.forEach { section ->
                    NavigationBarItem(
                        selected = selectedSection == section,
                        onClick = { selectedSection = section },
                        icon = { Text(section.icon) },
                        label = { Text(section.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedSection) {
            AppSection.Today -> {
                TodayScreen(
                    repository = repository,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppSection.Products -> {
                ProductsScreen(
                    repository = repository,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppSection.History -> {
                HistoryScreen(
                    repository = repository,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppSection.Analysis -> {
                AnalysisScreen(
                    repository = repository,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppSection.Goals -> {
                GoalsScreen(
                    repository = repository,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppSection.Weight -> {
                WeightScreen(
                    repository = repository,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

private enum class AppSection(
    val label: String,
    val icon: String
) {
    Today("Today", "T"),
    Products("Products", "P"),
    History("History", "H"),
    Analysis("Analysis", "A"),
    Goals("Goals", "G"),
    Weight("Weight", "W")
}

@Preview(showBackground = true)
@Composable
fun CaloriesTrackAppPreview() {
    CaloriesTrackTheme {
        CaloriesTrackApp()
    }
}
