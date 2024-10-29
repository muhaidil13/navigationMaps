package com.example.tugas_maps.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tugas_maps.MainViewModel
import com.mapbox.maps.MapView


@Composable
fun HomeScreen(mainViewModel: MainViewModel){
    val mapView = MapView(LocalContext.current)
    Scaffold {values ->
        Box(modifier = Modifier.fillMaxSize().padding(values)){
            Column(modifier = Modifier
                .fillMaxWidth()
            ){
                Button(onClick = {
                }) {
                    Text(text = "Clik Me")
                }

            }
        }



    }
}