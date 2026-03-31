package com.senoldogan.luontopeli.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senoldogan.luontopeli.viewmodel.StatsViewModel
import com.senoldogan.luontopeli.viewmodel.WalkViewModel

/**
 * StatsScreen – näyttää askeleet ja gyroskooppidatan (Vk 2).
 */
@Composable
fun StatsScreen(
    statsViewModel: StatsViewModel = hiltViewModel(),
    walkViewModel: WalkViewModel = hiltViewModel()
) {
    val totalSteps by statsViewModel.totalSteps.collectAsState()
    val activeSession by walkViewModel.activeSession.collectAsState()
    
    // Reaaliaikaiset lukemat sessiosta
    val currentSteps by walkViewModel.steps.collectAsState()
    val currentDistance by walkViewModel.distance.collectAsState()
    val gyroData by walkViewModel.gyroData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Kävely ja Liike",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Elinikäiset tilastot
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Askeleet yhteensä", style = MaterialTheme.typography.labelLarge)
                Text("$totalSteps", style = MaterialTheme.typography.displayMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Aktiivinen sessio tai aloitus
        if (activeSession != null) {
            ActiveSessionCard(
                steps = currentSteps,
                distance = currentDistance,
                gyroX = gyroData[0],
                gyroY = gyroData[1],
                gyroZ = gyroData[2],
                onStop = { walkViewModel.stopWalk() }
            )
        } else {
            Button(
                onClick = { walkViewModel.startWalk() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aloita uusi kävely")
            }
        }
    }
}

@Composable
fun ActiveSessionCard(
    steps: Int,
    distance: Float,
    gyroX: Float,
    gyroY: Float,
    gyroZ: Float,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Aktiivinen kävely", style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Askeleet", style = MaterialTheme.typography.labelSmall)
                    Text("$steps", style = MaterialTheme.typography.headlineMedium)
                }
                Column {
                    Text("Matka (m)", style = MaterialTheme.typography.labelSmall)
                    Text("%.1f".format(distance), style = MaterialTheme.typography.headlineMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Gyroskooppidata (Vk 2 vaatimus)
            Text("Liikeanturi (Gyroscope):", style = MaterialTheme.typography.labelMedium)
            Text("X: %.2f".format(gyroX), style = MaterialTheme.typography.bodySmall)
            Text("Y: %.2f".format(gyroY), style = MaterialTheme.typography.bodySmall)
            Text("Z: %.2f".format(gyroZ), style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Lopeta kävely ja tallenna")
            }
        }
    }
}
