package io.attractions.maplayr.sample.ui.stableid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.attractions.maplayr.sample.data.model.StableIdAttraction
import io.attractions.maplayr.sample.data.model.StableIdAttractionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StableIdViewModel : ViewModel() {

    val attractions: StateFlow<Set<StableIdAttraction>> = StableIdAttractionManager.attractions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
            initialValue = StableIdAttractionManager.snapshot()
        )

}
