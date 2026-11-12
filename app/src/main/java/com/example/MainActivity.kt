package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.EcoMapDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.EcoMapTheme
import com.example.ui.viewmodel.EcoMapDetailViewModel
import com.example.ui.viewmodel.EcoMapListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoMapTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EcoMapApp()
                }
            }
        }
    }
}

@Composable
fun EcoMapApp() {
    val listViewModel: EcoMapListViewModel = viewModel()
    val detailViewModel: EcoMapDetailViewModel = viewModel()

    var selectedMapId by remember { mutableStateOf<String?>(null) }

    if (selectedMapId != null) {
        BackHandler {
            selectedMapId = null
        }
        EcoMapDetailScreen(
            ecomapId = selectedMapId!!,
            viewModel = detailViewModel,
            onBackClick = { selectedMapId = null }
        )
    } else {
        HomeScreen(
            viewModel = listViewModel,
            onMapSelected = { id ->
                selectedMapId = id
            }
        )
    }
}
