package dev.nucleus.scheduleit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.nucleus.scheduleit.di.createAndroidAppGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val graph = createAndroidAppGraph(applicationContext)

        setContent {
            App(graph)
        }
    }
}
