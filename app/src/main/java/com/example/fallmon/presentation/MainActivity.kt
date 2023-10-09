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

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt


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

        var xAverage = sensor_window_transpose[0].average().toFloat()
        var yAverage = sensor_window_transpose[1].average().toFloat()
        var zAverage = sensor_window_transpose[2].average().toFloat()

        var xStandardDeviation = standardDeviation(sensor_window_transpose[0], xAverage)
        var yStandardDeviation = standardDeviation(sensor_window_transpose[1], yAverage)
        var zStandardDeviation = standardDeviation(sensor_window_transpose[2], zAverage)

        var xRootMeanSquare = rootMeanSquare(sensor_window_transpose[0])
        var yRootMeanSquare = rootMeanSquare(sensor_window_transpose[1])
        var zRootMeanSquare = rootMeanSquare(sensor_window_transpose[2])

        var xMaxAmplitude = maxAmplitude(sensor_window_transpose[0])
        var yMaxAmplitude = maxAmplitude(sensor_window_transpose[1])
        var zMaxAmplitude = maxAmplitude(sensor_window_transpose[2])

        var xMinAmplitude = minAmplitude(sensor_window_transpose[0])
        var yMinAmplitude = minAmplitude(sensor_window_transpose[1])
        var zMinAmplitude = minAmplitude(sensor_window_transpose[2])

        var xMedian = median(sensor_window_transpose[0])
        var yMedian = median(sensor_window_transpose[1])
        var zMedian = median(sensor_window_transpose[2])

        var xNZC = nzc(sensor_window_transpose[0])
        var yNZC = nzc(sensor_window_transpose[1])
        var zNZC = nzc(sensor_window_transpose[2])

        var xSkewness = skewness(sensor_window_transpose[0], xAverage, xStandardDeviation)
        var ySkewness = skewness(sensor_window_transpose[1], yAverage, yStandardDeviation)
        var zSkewness = skewness(sensor_window_transpose[2], zAverage, zStandardDeviation)

        var xKurtosis = kurtosis(sensor_window_transpose[0], xAverage, xStandardDeviation)
        var yKurtosis = kurtosis(sensor_window_transpose[1], yAverage, yStandardDeviation)
        var zKurtosis = kurtosis(sensor_window_transpose[2], zAverage, zStandardDeviation)

        var xPercentile1 = percentile_1(sensor_window_transpose[0])
        var yPercentile1 = percentile_1(sensor_window_transpose[1])
        var zPercentile1 = percentile_1(sensor_window_transpose[2])

        var xPercentile3 = percentile_3(sensor_window_transpose[0])
        var yPercentile3 = percentile_3(sensor_window_transpose[1])
        var zPercentile3 = percentile_3(sensor_window_transpose[2])

        text_square.text = """${window_index}
            |x: ${xAverage}, ${xStandardDeviation}, ${xRootMeanSquare}, ${xMaxAmplitude}, ${xMinAmplitude}, ${xMedian}, ${xNZC}, ${xSkewness}, ${xKurtosis}, ${xPercentile1}, ${xPercentile3}
            |y: ${yAverage}, ${yStandardDeviation}, ${yRootMeanSquare}, ${yMaxAmplitude}, ${yMinAmplitude}, ${yMedian}, ${yNZC}, ${ySkewness}, ${yKurtosis}, ${yPercentile1}, ${yPercentile3}
            |z: ${zAverage}, ${zStandardDeviation}, ${zRootMeanSquare}, ${zMaxAmplitude}, ${zMinAmplitude}, ${zMedian}, ${zNZC}, ${zSkewness}, ${zKurtosis}, ${zPercentile1}, ${zPercentile3}
            |""".trimMargin()
    }

    private fun standardDeviation(array: Array<Float>, average: Float): Float {
        var variance: Float = 0.0f
        for(f in array) variance += abs(f - average)
        return sqrt(variance)
    }

    private fun rootMeanSquare(array: Array<Float>): Float {
        var sumSquare: Float = 0.0f
        for(f in array) sumSquare += f*f
        return sqrt(sumSquare / WINDOW_SIZE)
    }

    private fun maxAmplitude(array: Array<Float>): Float {
        var maxAmp: Float = 0.0f
        for(f in array) maxAmp = max(maxAmp, abs(f))
        return maxAmp
    }

    private fun minAmplitude(array: Array<Float>): Float {
        var minAmp: Float = Float.MAX_VALUE
        for(f in array) minAmp = min(minAmp, abs(f))
        return minAmp
    }

    private fun median(array: Array<Float>): Float {
        val sortedArray = array.sorted()
        return sortedArray[WINDOW_SIZE / 2]
    }

    private fun percentile_1(array: Array<Float>): Float {
        val sortedArray = array.sorted()
        return sortedArray[WINDOW_SIZE / 4]
    }

    private fun percentile_3(array: Array<Float>): Float {
        val sortedArray = array.sorted()
        return sortedArray[WINDOW_SIZE * 3 / 4]
    }

    private fun nzc(array: Array<Float>): Float {
        var signArray = Array<Float>(WINDOW_SIZE) { 0.0f }
        for(i: Int in 0 until WINDOW_SIZE)
            if(array[i] > 0) signArray[i] = 1.0f
            else if(array[i] < 0) signArray[i] = -1.0f
            // else signArray[i] = 0.0f // don't have to modify

        var sum = 0.0f
        for(i: Int in 1 until WINDOW_SIZE)
            sum += abs(signArray[i] - signArray[i-1])
        return sum
    }

    private fun skewness(array: Array<Float>, average: Float, standardDeviation: Float): Float {
        val sumCubedDiff = array.sumOf { (it - average).toDouble().pow(3.0) }
        val skewness = (sumCubedDiff / WINDOW_SIZE) / standardDeviation.toDouble().pow(3.0)
        return skewness.toFloat()
    }

    private fun kurtosis(array: Array<Float>, average: Float, standardDeviation: Float): Float {
        val sumFourthPowerDiff = array.sumOf { (it - average).toDouble().pow(4.0) }
        val skewness = (sumFourthPowerDiff / WINDOW_SIZE) / standardDeviation.toDouble().pow(4.0) - 3.0
        return skewness.toFloat()
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