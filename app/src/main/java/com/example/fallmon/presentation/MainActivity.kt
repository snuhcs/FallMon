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
import android.util.Log
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
import com.example.fallmon.presentation.Model
typealias FeatureExtractor = (Array<Float>) -> Float

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

    // type alias
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
                sensor_window_transpose[i][j] = sensor_window[j][i]

        // features used in classification model
        // redundant calculations(average, standardDeviation) exist
        val average:FeatureExtractor = {v -> v.average().toFloat()}
        val standardDeviation:FeatureExtractor = {v -> FMath.standardDeviation(v, average(v))}
        val rootMinSquare:FeatureExtractor = {v -> FMath.rootMeanSquare(v)}
        val maxAmplitude:FeatureExtractor = {v -> FMath.maxAmplitude(v)}
        val minAmplitude:FeatureExtractor = {v -> FMath.minAmplitude(v)}
        val median:FeatureExtractor = {v -> FMath.median(v)}
        val nzc:FeatureExtractor = {v -> FMath.nzc(v)}
        val skewness:FeatureExtractor = {v -> FMath.skewness(v, average(v), standardDeviation(v))}
        val kurtosis:FeatureExtractor = {v -> FMath.kurtosis(v, average(v), standardDeviation(v))}
        val percentile1:FeatureExtractor = {v -> FMath.percentile_1(v)}
        val percentile3:FeatureExtractor = {v -> FMath.percentile_3(v)}
        val freqAverage:FeatureExtractor = {v -> FMath.frequencySpectrum(v).average().toFloat()}
        val freqMedian:FeatureExtractor = {v -> FMath.median(v)}
        val zero:FeatureExtractor = {_ -> 0.0F}

        val featureExtractors :Array<FeatureExtractor> = arrayOf(average, standardDeviation,
            rootMinSquare, maxAmplitude, minAmplitude, median, nzc, skewness, kurtosis, percentile1,
            percentile3, freqAverage, freqMedian, zero, zero)
        assert(featureExtractors.size == 15)
        val features: Array<Float> = featureExtractors.map{f -> sensor_window_transpose.map{v -> f(v)}}.flatten().toTypedArray()
        val score = Model.score(features.map {t -> t.toDouble()}.toDoubleArray())
        val featureText = """${window_index}
            |score: ${score[0]} ${score[1]}
            |x: ${features.filterIndexed{i, _ -> i % 3 == 0}.joinToString(limit=5, transform = {x-> "%.2f".format(x)})}
            |y: ${features.filterIndexed{i, _ -> i % 3 == 1}.joinToString(limit=5, transform = {x-> "%.2f".format(x)})}
            |z: ${features.filterIndexed{i, _ -> i % 3 == 2}.joinToString(limit=5, transform = {x-> "%.2f".format(x)})}
            |""".trimMargin()
        Log.d("score", score.joinToString { x -> x.toString() })
        Log.d("Features", featureText)
        text_square.text = featureText
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