package com.senoldogan.luontopeli.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senoldogan.luontopeli.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentUserId by viewModel.currentUserId.collectAsState()
    val isSignedIn by viewModel.isSignedIn.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Käyttäjäprofiili") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Käyttäjä ID:",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = currentUserId ?: "Ei kirjautunut",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isSignedIn) {
                Button(onClick = { viewModel.signOut() }) {
                    Text("Kirjaudu ulos")
                }
            } else {
                Button(onClick = { viewModel.signIn() }) {
                    Text("Kirjaudu sisään")
                }
            }
        }
    }
}
