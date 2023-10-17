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
import com.example.fallmon.presentation.math.FallMonMath as FMath


class MainActivity : ComponentActivity(), SensorEventListener {

    private val SAMPLING_RATE: Int = 30
    private val WINDOW_SIZE: Int = 75
    private val WINDOW_STRIDE: Int = 15

    private lateinit var sensorManager: SensorManager
    private var mAccelerometer: Sensor ?= null
    private lateinit var text_square: TextView

    // sensor_window, window_index : for sliding window
    private var sensor_window = Array(WINDOW_SIZE, {Array<Float>(3, {0.0f})})
    private var sensor_window_transpose = Array(3, {Array<Float>(WINDOW_SIZE, {0.0f})})
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
            //text_square.text = "${window_index}  \nx: ${xAcceleration}  \ny:  ${yAcceleration}  \nz: ${zAcceleration}"

            sensor_window[window_index % WINDOW_SIZE][0] = xAcceleration
            sensor_window[window_index % WINDOW_SIZE][1] = yAcceleration
            sensor_window[window_index % WINDOW_SIZE][2] = zAcceleration

            // for easy calculate features. Erase this if has efficient way
            sensor_window_transpose[0][window_index % WINDOW_SIZE] = xAcceleration
            sensor_window_transpose[1][window_index % WINDOW_SIZE] = yAcceleration
            sensor_window_transpose[2][window_index % WINDOW_SIZE] = zAcceleration

            window_index += 1

            if(window_index % WINDOW_STRIDE == 0 && window_index >= WINDOW_SIZE) {
                sensorWindowFulled()
            }
        }
    }

    private fun sensorWindowFulled() {

        sensor_window_transpose = Array(3, {Array<Float>(WINDOW_SIZE, {0.0f})})

        for(i: Int in 0 until 3)
            for(j: Int in 0 until WINDOW_SIZE)
                sensor_window_transpose[i][j] = (i+1)*(j+1).toFloat()

        var xAverage = sensor_window_transpose[0].average().toFloat()
        var yAverage = sensor_window_transpose[1].average().toFloat()
        var zAverage = sensor_window_transpose[2].average().toFloat()

        var xStandardDeviation = FMath.standardDeviation(sensor_window_transpose[0], xAverage)
        var yStandardDeviation = FMath.standardDeviation(sensor_window_transpose[1], yAverage)
        var zStandardDeviation = FMath.standardDeviation(sensor_window_transpose[2], zAverage)

        var xRootMeanSquare = FMath.rootMeanSquare(sensor_window_transpose[0])
        var yRootMeanSquare = FMath.rootMeanSquare(sensor_window_transpose[1])
        var zRootMeanSquare = FMath.rootMeanSquare(sensor_window_transpose[2])

        var xMaxAmplitude = FMath.maxAmplitude(sensor_window_transpose[0])
        var yMaxAmplitude = FMath.maxAmplitude(sensor_window_transpose[1])
        var zMaxAmplitude = FMath.maxAmplitude(sensor_window_transpose[2])

        var xMinAmplitude = FMath.minAmplitude(sensor_window_transpose[0])
        var yMinAmplitude = FMath.minAmplitude(sensor_window_transpose[1])
        var zMinAmplitude = FMath.minAmplitude(sensor_window_transpose[2])

        var xMedian = FMath.median(sensor_window_transpose[0])
        var yMedian = FMath.median(sensor_window_transpose[1])
        var zMedian = FMath.median(sensor_window_transpose[2])

        var xNZC = FMath.nzc(sensor_window_transpose[0])
        var yNZC = FMath.nzc(sensor_window_transpose[1])
        var zNZC = FMath.nzc(sensor_window_transpose[2])

        var xSkewness = FMath.skewness(sensor_window_transpose[0], xAverage, xStandardDeviation)
        var ySkewness = FMath.skewness(sensor_window_transpose[1], yAverage, yStandardDeviation)
        var zSkewness = FMath.skewness(sensor_window_transpose[2], zAverage, zStandardDeviation)

        var xKurtosis = FMath.kurtosis(sensor_window_transpose[0], xAverage, xStandardDeviation)
        var yKurtosis = FMath.kurtosis(sensor_window_transpose[1], yAverage, yStandardDeviation)
        var zKurtosis = FMath.kurtosis(sensor_window_transpose[2], zAverage, zStandardDeviation)

        var xPercentile1 = FMath.percentile_1(sensor_window_transpose[0])
        var yPercentile1 = FMath.percentile_1(sensor_window_transpose[1])
        var zPercentile1 = FMath.percentile_1(sensor_window_transpose[2])

        var xPercentile3 = FMath.percentile_3(sensor_window_transpose[0])
        var yPercentile3 = FMath.percentile_3(sensor_window_transpose[1])
        var zPercentile3 = FMath.percentile_3(sensor_window_transpose[2])

        var xFreq = FMath.frequencySpectrum(sensor_window_transpose[0])
        var yFreq = FMath.frequencySpectrum(sensor_window_transpose[1])
        var zFreq = FMath.frequencySpectrum(sensor_window_transpose[2])

        var xFreqAverage = xFreq.average().toFloat()
        var yFreqAverage = yFreq.average().toFloat()
        var zFreqAverage = zFreq.average().toFloat()

        var xFreqMedian = FMath.median(xFreq)
        var yFreqMedian = FMath.median(yFreq)
        var zFreqMedian = FMath.median(zFreq)

        text_square.text = """${window_index}
            |x: ${xAverage}, ${xStandardDeviation}, ${xRootMeanSquare}, ${xMaxAmplitude}, ${xMinAmplitude}, ${xMedian}, ${xNZC}, ${xSkewness}, ${xKurtosis}, ${xPercentile1}, ${xPercentile3}, ${xFreqAverage}, ${xFreqMedian}
            |y: ${yAverage}, ${yStandardDeviation}, ${yRootMeanSquare}, ${yMaxAmplitude}, ${yMinAmplitude}, ${yMedian}, ${yNZC}, ${ySkewness}, ${yKurtosis}, ${yPercentile1}, ${yPercentile3}, ${yFreqAverage}, ${yFreqMedian}
            |z: ${zAverage}, ${zStandardDeviation}, ${zRootMeanSquare}, ${zMaxAmplitude}, ${zMinAmplitude}, ${zMedian}, ${zNZC}, ${zSkewness}, ${zKurtosis}, ${zPercentile1}, ${zPercentile3}, ${zFreqAverage}, ${zFreqMedian}
            |""".trimMargin()
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