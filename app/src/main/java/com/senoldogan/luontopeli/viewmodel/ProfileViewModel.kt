package com.senoldogan.luontopeli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senoldogan.luontopeli.data.remote.firebase.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    val currentUser = authManager.authStateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = authManager.currentUser
    )

    val currentUserId: StateFlow<String?> = currentUser.map { it?.uid }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = authManager.currentUserId
    )

    val isSignedIn: StateFlow<Boolean> = currentUser.map { it != null }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = authManager.isSignedIn
    )

    fun signIn() {
        viewModelScope.launch {
            authManager.signInAnonymously()
        }
    }

    fun signOut() {
        authManager.signOut()
    }
}
