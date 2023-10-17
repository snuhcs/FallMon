/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.fallmon.presentation

import android.annotation.SuppressLint
import android.content.Context.*
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
    private var sensor_window = Array(WINDOW_SIZE) { Array(3) { 0.0f } }
    private var sensor_window_transpose = Array(3) { Array(WINDOW_SIZE) { 0.0f } }
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
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
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

    @SuppressLint("SetTextI18n")
    private fun sensorWindowFulled() {

        sensor_window_transpose = Array(3) { Array(WINDOW_SIZE) { 0.0f } }

        for(i: Int in 0 until 3)
            for(j: Int in 0 until WINDOW_SIZE)
                sensor_window_transpose[i][j] = (i+1)*(j+1).toFloat()

        val xAverage = sensor_window_transpose[0].average().toFloat()
        val yAverage = sensor_window_transpose[1].average().toFloat()
        val zAverage = sensor_window_transpose[2].average().toFloat()

        val xStandardDeviation = FMath.standardDeviation(sensor_window_transpose[0], xAverage)
        val yStandardDeviation = FMath.standardDeviation(sensor_window_transpose[1], yAverage)
        val zStandardDeviation = FMath.standardDeviation(sensor_window_transpose[2], zAverage)

        val xRootMeanSquare = FMath.rootMeanSquare(sensor_window_transpose[0])
        val yRootMeanSquare = FMath.rootMeanSquare(sensor_window_transpose[1])
        val zRootMeanSquare = FMath.rootMeanSquare(sensor_window_transpose[2])

        val xMaxAmplitude = FMath.maxAmplitude(sensor_window_transpose[0])
        val yMaxAmplitude = FMath.maxAmplitude(sensor_window_transpose[1])
        val zMaxAmplitude = FMath.maxAmplitude(sensor_window_transpose[2])

        val xMinAmplitude = FMath.minAmplitude(sensor_window_transpose[0])
        val yMinAmplitude = FMath.minAmplitude(sensor_window_transpose[1])
        val zMinAmplitude = FMath.minAmplitude(sensor_window_transpose[2])

        val xMedian = FMath.median(sensor_window_transpose[0])
        val yMedian = FMath.median(sensor_window_transpose[1])
        val zMedian = FMath.median(sensor_window_transpose[2])

        val xNZC = FMath.nzc(sensor_window_transpose[0])
        val yNZC = FMath.nzc(sensor_window_transpose[1])
        val zNZC = FMath.nzc(sensor_window_transpose[2])

        val xSkewness = FMath.skewness(sensor_window_transpose[0], xAverage, xStandardDeviation)
        val ySkewness = FMath.skewness(sensor_window_transpose[1], yAverage, yStandardDeviation)
        val zSkewness = FMath.skewness(sensor_window_transpose[2], zAverage, zStandardDeviation)

        val xKurtosis = FMath.kurtosis(sensor_window_transpose[0], xAverage, xStandardDeviation)
        val yKurtosis = FMath.kurtosis(sensor_window_transpose[1], yAverage, yStandardDeviation)
        val zKurtosis = FMath.kurtosis(sensor_window_transpose[2], zAverage, zStandardDeviation)

        val xPercentile1 = FMath.percentile1(sensor_window_transpose[0])
        val yPercentile1 = FMath.percentile1(sensor_window_transpose[1])
        val zPercentile1 = FMath.percentile1(sensor_window_transpose[2])

        val xPercentile3 = FMath.percentile3(sensor_window_transpose[0])
        val yPercentile3 = FMath.percentile3(sensor_window_transpose[1])
        val zPercentile3 = FMath.percentile3(sensor_window_transpose[2])

        val xFreq = FMath.frequencySpectrum(sensor_window_transpose[0])
        val yFreq = FMath.frequencySpectrum(sensor_window_transpose[1])
        val zFreq = FMath.frequencySpectrum(sensor_window_transpose[2])

        val xFreqAverage = xFreq.average().toFloat()
        val yFreqAverage = yFreq.average().toFloat()
        val zFreqAverage = zFreq.average().toFloat()

        val xFreqMedian = FMath.median(xFreq)
        val yFreqMedian = FMath.median(yFreq)
        val zFreqMedian = FMath.median(zFreq)

        // below two functions would be fixed
        val xEntropy = FMath.entropy(xFreq)
        val yEntropy = FMath.entropy(yFreq)
        val zEntropy = FMath.entropy(zFreq)

        val xEnergy = FMath.energy(xFreq)
        val yEnergy = FMath.energy(yFreq)
        val zEnergy = FMath.energy(zFreq)

        text_square.text = """${window_index}
            |x: ${xAverage}, ${xStandardDeviation}, ${xRootMeanSquare}, ${xMaxAmplitude}, ${xMinAmplitude}, ${xMedian}, ${xNZC}, ${xSkewness}, ${xKurtosis}, ${xPercentile1}, ${xPercentile3}, ${xFreqAverage}, ${xFreqMedian}, ${xEntropy}, ${xEnergy}
            |""".trimMargin()
        /*
        text_square.text = """${window_index}
            |x: ${xAverage}, ${xStandardDeviation}, ${xRootMeanSquare}, ${xMaxAmplitude}, ${xMinAmplitude}, ${xMedian}, ${xNZC}, ${xSkewness}, ${xKurtosis}, ${xPercentile1}, ${xPercentile3}, ${xFreqAverage}, ${xFreqMedian}, ${xEntropy}, ${xEnergy}
            |y: ${yAverage}, ${yStandardDeviation}, ${yRootMeanSquare}, ${yMaxAmplitude}, ${yMinAmplitude}, ${yMedian}, ${yNZC}, ${ySkewness}, ${yKurtosis}, ${yPercentile1}, ${yPercentile3}, ${yFreqAverage}, ${yFreqMedian}, ${yEntropy}, ${yEnergy}
            |z: ${zAverage}, ${zStandardDeviation}, ${zRootMeanSquare}, ${zMaxAmplitude}, ${zMinAmplitude}, ${zMedian}, ${zNZC}, ${zSkewness}, ${zKurtosis}, ${zPercentile1}, ${zPercentile3}, ${zFreqAverage}, ${zFreqMedian}, ${zEntropy}, ${zEnergy}
            |""".trimMargin()

         */
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