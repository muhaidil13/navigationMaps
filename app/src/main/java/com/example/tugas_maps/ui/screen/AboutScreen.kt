package com.example.tugas_maps.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AboutScreen(){
    Scaffold {values ->
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(values)){
            Text(text = "About Screen")
        }

    }
}