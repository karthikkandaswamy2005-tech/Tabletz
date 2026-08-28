package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.repository.HospitalRepository
import com.example.ui.HospitalApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HospitalViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: HospitalViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        HospitalApp(viewModel = viewModel)
      }
    }
  }
}

