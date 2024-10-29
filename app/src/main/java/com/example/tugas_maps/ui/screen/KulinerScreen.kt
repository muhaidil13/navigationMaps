package com.example.tugas_maps.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tugas_maps.MainViewModel
import com.example.tugas_maps.data.model.Marker
import com.google.android.gms.location.FusedLocationProviderClient
import androidx.compose.runtime.livedata.observeAsState


@Composable
fun KulinerScreen(navController: NavController, mainViewModel: MainViewModel){
    val kulinerMarkers = mainViewModel.markers.observeAsState(emptyList()).value.filter { it is Marker.KulinerMarker }

    Scaffold {values ->

        LazyColumn(modifier = Modifier.fillMaxWidth().padding(values), contentPadding = PaddingValues(16.dp)){
            items(kulinerMarkers){
                Button(
                    onClick = {
                        navController.navigate("map/locationName/${it.location.latitude()}/${it.location.longitude()}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp) // Padding between buttons
                ) {
                    Text(text = "Go to ${it.locationName}")
                }
            }
        }

    }
}
