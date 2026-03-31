package com.senoldogan.luontopeli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senoldogan.luontopeli.data.repository.NatureSpotRepository
import com.senoldogan.luontopeli.data.local.entity.NatureSpot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val natureSpotRepository: NatureSpotRepository
) : ViewModel() {

    private val _natureSpots = MutableStateFlow<List<NatureSpot>>(emptyList())
    val natureSpots: StateFlow<List<NatureSpot>> = _natureSpots.asStateFlow()

    init {
        viewModelScope.launch {
            natureSpotRepository.allSpots.collect { spots ->
                _natureSpots.value = spots
            }
        }
    }
}
