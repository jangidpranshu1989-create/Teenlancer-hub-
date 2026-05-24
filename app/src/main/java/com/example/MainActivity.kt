package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.TeenlancerRepository
import com.example.ui.TeenlancerViewModel
import com.example.ui.screens.TeenlancerApp
import com.example.ui.theme.TeenlancerHubTheme

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var repository: TeenlancerRepository
    private lateinit var viewModel: TeenlancerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize local Room Database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "teenlancer_hub_database"
        )
        .fallbackToDestructiveMigration(true)
        .build()

        val dao = database.teenlancerDao()
        repository = TeenlancerRepository(dao)

        // Instantiate ViewModel with custom repository factory
        val factory = TeenlancerViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TeenlancerViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            TeenlancerHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TeenlancerApp(viewModel = viewModel)
                }
            }
        }
    }
}

class TeenlancerViewModelFactory(private val repository: TeenlancerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeenlancerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeenlancerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
