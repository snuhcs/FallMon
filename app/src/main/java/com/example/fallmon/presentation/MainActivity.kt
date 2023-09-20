/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.fallmon.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.fallmon.R
import com.example.fallmon.presentation.theme.FallMonTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private val SAMPLING_RATE: Int = 30
    private val WINDOW_SIZE: Int = 75
    private val WINDOW_STRIDE: Int = 15

    private lateinit var sensorManager: SensorManager
    private var mAccelerometer: Sensor ?= null
    private lateinit var text_square: TextView

    // sensor_window, window_index : for sliding window
    private var sensor_window = Array(75, {Array<Float>(3, {0.0f})})
    private var window_index: Int = 0

    /* Constructor */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text_square = findViewById(R.id.text_view1)
        setUpSensor()
    }

    /* Set up sensor when the app starts */
    private fun setUpSensor(){
        sensorManager = getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also{
            sensorManager.registerListener(
                this,
                it,
                1000000 / SAMPLING_RATE,
                1000000 / SAMPLING_RATE
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            // Process accelerometer data
            val xAcceleration = event.values[0]
            val yAcceleration = event.values[1]
            val zAcceleration = event.values[2]
            text_square.text = "${window_index}  \nx: ${xAcceleration}  \ny:  ${yAcceleration}  \nz: ${zAcceleration}"

            sensor_window[window_index % WINDOW_SIZE][0] = xAcceleration
            sensor_window[window_index % WINDOW_SIZE][1] = yAcceleration
            sensor_window[window_index % WINDOW_SIZE][2] = zAcceleration
            window_index += 1

            if(window_index % WINDOW_SIZE == 0) sensorWindowFulled()
        }
    }

    private fun sensorWindowFulled() {
        text_square.text = "sensorWindowFulled"
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}

@Composable
fun SensorData(v1: Float, v2: Float, v3: Float) {
    FallMonTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.sensor_data, v1.toString(), v2.toString(), v3.toString())
            )
        }
    }
}


@Composable
fun WearApp() {
    FallMonTheme {
        /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
         * version of LazyColumn for wear devices with some added features. For more information,
         * see d.android.com/wear/compose.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center
        ) {
            SensorData(0f, 0f , 0f)
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}