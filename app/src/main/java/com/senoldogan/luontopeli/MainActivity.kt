package com.senoldogan.luontopeli

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.senoldogan.luontopeli.ui.theme.LuontopeliTheme
import androidx.navigation.compose.rememberNavController
import com.senoldogan.luontopeli.ui.navigation.LuontopeliBottomBar
import com.senoldogan.luontopeli.ui.navigation.LuontopeliNavHost
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LuontopeliTheme {
                PermissionWrapper {
                    LuontopeliApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionWrapper(content: @Composable () -> Unit) {
    val permissions = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    // Activity Recognition tarvitaan API 29+ (Week 2 vaatimus)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
    }

    val permissionState = rememberMultiplePermissionsState(permissions)

    if (permissionState.allPermissionsGranted) {
        content()
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Sovellus tarvitsee luvat toimiakseen",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Kameraa tarvitaan bitkien tunnistukseen ve sijaintia haritalla liikkumiseen.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                Text("Myönnä luvat")
            }
        }
    }
}

@Composable
fun LuontopeliApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            LuontopeliBottomBar(navController = navController)
        }
    ) { innerPadding ->
        LuontopeliNavHost(
            navController = navController,
            paddingValues = innerPadding
        )
    }
}