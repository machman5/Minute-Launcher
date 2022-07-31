package com.example.android.minutelauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun FavoriteApps(
    viewModel: LauncherViewModel = hiltViewModel()
) {
    val totalUsage by viewModel.getTotalUsage()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(totalUsage.toTimeUsed())
        }
        item {
            (1..5).forEach {
                Text("App $it", modifier = Modifier.padding(16.dp), fontSize = 23.sp)
            }
        }
    }
}