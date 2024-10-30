package com.example.tugas_maps.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tugas_maps.MainViewModel
import com.example.tugas_maps.data.model.Marker
import com.example.tugas_maps.ui.components.BottomMenu
import com.mapbox.maps.MapView

@Composable
fun WisataScreen(navController: NavController, mainViewModel: MainViewModel){

    val wisataMarker = mainViewModel.getListOfMarkerWisata()

    val scrollState = rememberScrollState()



    Scaffold(
        bottomBar ={
            BottomMenu(navController = navController)
        }
    ){values ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(values)
        ) {
            wisataMarker?.forEach { item ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(Color.LightGray),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Lokasi: ${item.locationName}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            Text(
                                text = "Kordinat Lokasi",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "latitude ${item.location.latitude()} longitude ${item.location.longitude()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Button(
                            onClick = {
                                navController.navigate("detail/${item.id}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp) // Padding between buttons
                        ) {
                            Text(text = "Lihat")
                        }
                    }
                }
            }
        }


    }
}