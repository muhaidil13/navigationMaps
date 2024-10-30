package com.example.tugas_maps.ui.components

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tugas_maps.data.model.Marker
import com.mapbox.geojson.Point
import java.io.ByteArrayOutputStream

@Composable
fun MarkerDialog(location: Point, onDismis:() -> Unit, onAddMarker: (Marker) -> Unit ) {
    val context = LocalContext.current
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }
    val selectedBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) { bitmap->
        bitmap?.let {
            val baos = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            Log.d("testMapbox" ,"${data}")
            selectedBitmap.value = bitmap
        }
        
    }
    var name by remember {
        mutableStateOf("")
    }
    var isWisata by remember {
        mutableStateOf(true)
    }

    val text = remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = {onDismis() },
        confirmButton = {
            Button(
                onClick = {
                    val markerType = if (isWisata) {
                        Marker.WisataMarker(locationName =  name, location =  location, type= "wisata", id = null)
                    } else {
                        Marker.KulinerMarker(locationName =  name, location= location, type =  "kuliner",id = null)
                    }
                    onAddMarker(markerType)
                },
                enabled = name.isNotBlank()) {
                Text(text = "Add Marker")
            }},
        title = { Text(text = "Add Location Marker",style = MaterialTheme.typography.titleLarge)},
        icon = { Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add") },
        text = {
            Column(modifier = Modifier.padding(2.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "Location Name") })
                OutlinedTextField(
                    value = text.value,
                    onValueChange = { text.value = it },
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = { Text("Enter your text") },
                    placeholder = { Text("Write something...") },
                    maxLines = Int.MAX_VALUE,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    )
                )
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
                    RadioButton(selected = isWisata, onClick = { isWisata = true })
                    Text(text = "Wisata", color = if (isWisata) Color.Blue else Color.Black)
                    RadioButton(selected = !isWisata, onClick = { isWisata = false })
                    Text(text = "Kuliner", color = if (!isWisata) Color.Blue else Color.Black)
                }

                Row {
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "Open Galery ", fontSize = 12.sp)
                    }
                    Button(onClick = {
                        cameraLauncher.launch()
                    }) {
                        Text(text = "Open Camera", fontSize = 12.sp)
                    }
                }
//                Row {
//                    selectedBitmap.value?.let { bitmap ->
//                        Image(bitmap = bitmap.asImageBitmap() , contentDescription = "", modifier = Modifier.size(50.dp))
//                    }
//                }

            }
        }
    )
}