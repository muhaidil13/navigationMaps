package com.example.tugas_maps.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.tugas_maps.MainViewModel
import com.example.tugas_maps.data.model.Marker

@Composable
fun WisataScreen(navController: NavController, mainViewModel: MainViewModel){
    val wisataMarker = mainViewModel.markers.observeAsState(emptyList()).value.filter { it is Marker.WisataMarker }
    Scaffold {values ->
        LazyColumn (modifier = Modifier.padding(values)){
            items(wisataMarker){
                Button(onClick = {
                    navController.navigate("map/locationName/${it.location.latitude()}/${it.location.longitude()}")
                }) {
                    Text("Hello")
                }
            }
        }


    }
}