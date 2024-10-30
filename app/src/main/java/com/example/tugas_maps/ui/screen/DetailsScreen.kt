package com.example.tugas_maps.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tugas_maps.MainViewModel
import com.example.tugas_maps.ui.components.BottomMenu

@Composable
fun DetailScreen(idLocation: String, mainViewModel: MainViewModel, navController: NavController){
    mainViewModel.getMarkersById(idLocation)
    val marker by mainViewModel.marker.collectAsState()

    Scaffold(
        bottomBar = { BottomMenu(navController = navController)}
    ){innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(8.dp)
            .fillMaxSize()){
            if(marker == null){
                Text(text = "Lokasi Tidak Tersedia", style = MaterialTheme.typography.titleLarge, )
            }
            Text(text = "${marker?.locationName}", style = MaterialTheme.typography.titleLarge, )
            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Opsi Navigasi", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(10.dp))
            Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                Button(onClick = {
                    mainViewModel.updatestatususeCustomLocation(false)
                    navController.navigate("map/${marker?.locationName}/${marker?.location!!.latitude()}/${marker?.location!!.longitude()}")
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)) {
                    Text(text = "Use My Location", style = MaterialTheme.typography.labelLarge)
                }
                Button(onClick = {
                    mainViewModel.updatestatususeCustomLocation(true)
                    navController.navigate("map/${marker?.locationName}/${marker?.location!!.latitude()}/${marker?.location!!.longitude()}")

                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)) {
                    Text(text = "Custom Location", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Deskripsi", style = MaterialTheme.typography.labelLarge,)
            Spacer(modifier = Modifier.height(200.dp))

            Text(text = "Foto Dll", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(100.dp))
            Text(text = "${marker?.type}", style = MaterialTheme.typography.labelLarge)

        }

    }
}