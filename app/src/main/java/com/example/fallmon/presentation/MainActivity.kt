/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.fallmon.presentation

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.fallmon.R
import com.example.fallmon.presentation.theme.FallMonTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    /*
     * sensors / views
     * text_square for debugging
     */
    private lateinit var sensorManager: SensorManager
    private var mAccelerometer: Sensor ?= null
    private lateinit var text_square: TextView

    /*
     * 30Hz Samples, total 75 * 3 (x,y,z) data for a window, 15 striding
     */
    private val SAMPLING_RATE: Int = 30
    private val WINDOW_SIZE: Int = 75
    private val WINDOW_STRIDE: Int = 15

    private var sensor_window = Array(WINDOW_SIZE) { Array(3) { 0.0f } }
    private var sensor_window_transpose = Array(3) { Array(WINDOW_SIZE) { 0.0f } }
    private var window_index: Int = 0

    /*
     * intented : check if intented DetectedActivity
     * getActivityResult : to get result from DetectedActivity
     */
    private var intented: Boolean = false
    private val getActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK) {
            val confirmed = it.data?.getBooleanExtra("confirmed", false)
        }
    }

    /*
     * Constructor
     * set up textview, sensor
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text_square = findViewById(R.id.text_view1)
        setUpSensor()
    }

    /*
     * Set up sensor when the app starts
     * sampling period = 1000000 (us) / SAMPLING_RATE
     */
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

    /*
     * Run at every sensor samples data
     * Save sensor values in window, run sensorWindowFulled() when 75 * 3 data is ready.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            val xAcceleration = event.values[0]
            val yAcceleration = event.values[1]
            val zAcceleration = event.values[2]

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

    /*
     * transpose data to 3 * 75 for getting features
     * get scores by running model, intent DetectedActivity if fall detected
     */
    private fun sensorWindowFulled() {
        sensor_window_transpose = Array(3, {Array<Float>(WINDOW_SIZE, {0.0f})})

        for(i: Int in 0 until 3)
            for(j: Int in 0 until WINDOW_SIZE)
                sensor_window_transpose[i][j] = sensor_window[j][i]

        val featureExtractors :Array<FeatureExtractor> = arrayOf(Feature.average, Feature.standardDeviation,
            Feature.rootMinSquare, Feature.maxAmplitude, Feature.minAmplitude, Feature.median, Feature.nzc, Feature.skewness, Feature.kurtosis, Feature.percentile1,
            Feature.percentile3, Feature.freqAverage, Feature.freqMedian, Feature.freqEntropy, Feature.freqEnergy)
        assert(featureExtractors.size == 15)
        val features: Array<Float> = featureExtractors.map{f -> sensor_window_transpose.map{v -> f(v)}}.flatten().toTypedArray()
        val score = Model.score(features.map {t -> t.toDouble()}.toDoubleArray())
        val classificationResult = ClassificationModel.score(features.map{t -> t.toDouble()}.toDoubleArray())

        if(score[1] == score.max() && !intented) {
            intented = true

            val intent = Intent(this, DetectedActivity::class.java)
            intent.putExtra("classificationResult", classificationResult)
            getActivityResult.launch(intent)
        }

        val featureText = """${window_index}
            |score: ${score[0]} ${score[1]}
            |classification: ${classificationResult[0]} ${classificationResult[1]} ${classificationResult[2]} ${classificationResult[3]} ${classificationResult[4]}
            |x: ${features.filterIndexed{i, _ -> i % 3 == 0}.joinToString(limit=5, transform = {x-> "%.2f".format(x)})}
            |y: ${features.filterIndexed{i, _ -> i % 3 == 1}.joinToString(limit=5, transform = {x-> "%.2f".format(x)})}
            |z: ${features.filterIndexed{i, _ -> i % 3 == 2}.joinToString(limit=5, transform = {x-> "%.2f".format(x)})}
            |""".trimMargin()

        text_square.text = featureText

    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}

/*
 * below functions will be removed if unnecessary
 */

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
fun WearApp(greetingName: String) {
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
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}