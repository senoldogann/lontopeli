package com.senoldogan.luontopeli.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Map : Screen("map", "Map", Icons.Default.Map)
    object Discover : Screen("discover", "Discover", Icons.Default.Search)
    object Stats : Screen("stats", "Stats", Icons.Default.BarChart)
    object Profile : Screen("profile", "Profiili", Icons.Default.Person)
}
