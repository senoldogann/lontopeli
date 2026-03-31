package com.senoldogan.luontopeli.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.senoldogan.luontopeli.ui.map.MapScreen
import com.senoldogan.luontopeli.ui.discover.DiscoverScreen
import com.senoldogan.luontopeli.ui.stats.StatsScreen
import com.senoldogan.luontopeli.ui.profile.ProfileScreen
import com.senoldogan.luontopeli.camera.CameraScreen
import com.senoldogan.luontopeli.viewmodel.CameraViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LuontopeliNavHost(navController: NavHostController, paddingValues: PaddingValues) {
    // Jaettu CameraViewModel navigoinnin välillä (jos tarpeen)
    val cameraViewModel: CameraViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Map.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        // Karttanäkymä (Vk 3)
        composable(Screen.Map.route) { 
            MapScreen(onNavigateToCamera = { navController.navigate("camera") }) 
        }

        // Löydöt-näkymä (Vk 4 & 6)
        composable(Screen.Discover.route) { 
            DiscoverScreen() 
        }

        // Tilastot-näkymä (Vk 2)
        composable(Screen.Stats.route) { 
            StatsScreen() 
        }

        // Profiili-näkymä (Vk 6 valinnainen)
        composable(Screen.Profile.route) {
            ProfileScreen()
        }

        // Kamera-näkymä (Vk 4 & 5)
        composable("camera") { 
            CameraScreen(
                viewModel = cameraViewModel,
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
    }
}
