package com.sowmya.security.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class LocationViewModel : ViewModel() {
    // Mutable state to store user location
    var userLocation by mutableStateOf<LatLng?>(null)
        private set

    // Function to update user location
    fun updateLocation(latLng: LatLng) {
        userLocation = latLng
    }
}
