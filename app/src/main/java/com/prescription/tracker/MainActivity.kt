package com.prescription.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.prescription.tracker.ui.navigation.NavGraph
import com.prescription.tracker.ui.theme.PrescriptionTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val medicationId = intent?.getLongExtra("medication_id", -1L) ?: -1L

        setContent {
            PrescriptionTrackerTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    startMedicationId = if (medicationId > 0) medicationId else null
                )
            }
        }
    }
}
