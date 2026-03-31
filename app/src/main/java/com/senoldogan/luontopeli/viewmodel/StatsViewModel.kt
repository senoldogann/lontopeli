package com.senoldogan.luontopeli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senoldogan.luontopeli.data.local.entity.WalkSession
import com.senoldogan.luontopeli.data.repository.WalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val walkRepository: WalkRepository
) : ViewModel() {

    private val _walkSessions = MutableStateFlow<List<WalkSession>>(emptyList())
    val walkSessions: StateFlow<List<WalkSession>> = _walkSessions.asStateFlow()

    private val _totalSteps = MutableStateFlow(0)
    val totalSteps: StateFlow<Int> = _totalSteps.asStateFlow()

    init {
        viewModelScope.launch {
            walkRepository.getAllWalkSessions().collect { sessions ->
                _walkSessions.value = sessions
                _totalSteps.value = sessions.sumOf { it.stepCount }
            }
        }
    }
}
