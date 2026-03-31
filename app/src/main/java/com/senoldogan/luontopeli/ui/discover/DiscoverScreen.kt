package com.senoldogan.luontopeli.ui.discover

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.senoldogan.luontopeli.data.local.entity.NatureSpot
import com.senoldogan.luontopeli.viewmodel.DiscoverViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val natureSpots by viewModel.natureSpots.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Omat Luontolöydöt") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(natureSpots) { spot ->
                NatureSpotItem(spot = spot)
            }
        }
    }
}

@Composable
fun NatureSpotItem(spot: NatureSpot) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Kuvan näyttäminen Coil-kirjastolla
            // Ensisijaisesti paikallinen polku, fallbackina Firebase URL
            AsyncImage(
                model = spot.imageLocalPath ?: spot.imageFirebaseUrl,
                contentDescription = spot.plantLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = spot.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    // Synkronointitila (Vk 6)
                    StatusIcon(synced = spot.synced)
                }

                Text(
                    text = "Tunnistettu: ${spot.plantLabel ?: "Määrittämätön"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (spot.confidence != null) {
                    Text(
                        text = "Varmuus: ${(spot.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatTimestamp(spot.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun StatusIcon(synced: Boolean) {
    val color = if (synced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val text = if (synced) "Synkronoitu" else "Vain paikallinen"
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
