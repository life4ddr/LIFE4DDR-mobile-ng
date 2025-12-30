package com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel

import com.perrigogames.life4ddr.nextgen.util.Destination

sealed class PlacementListEvent {
    data class Navigate(
        val destination: Destination,
        val popExisting: Boolean
    ) : PlacementListEvent()
}