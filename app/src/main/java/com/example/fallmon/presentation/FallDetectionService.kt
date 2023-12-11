package com.example.fallmon.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fallmon.R

class FallDetectionService : Service() {

    /*
     * sensors / views
     * text_square for debugging
     */
    private lateinit var sensorManager: SensorManager
    private var mAccelerometer: Sensor?= null

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
     * var, listener, binder for intent DetectedActivity & receiving isFinished
     */
    private var intented: Boolean = false
    private var activityResultListener: ActivityResultListener? = null
    fun setActivityResultListener(listener: ActivityResultListener) {
        this.activityResultListener = listener
    }
    private val binder = LocalBinder()

    /*
     * Constructor
     * set up textview, sensor
     */
    override fun onCreate() {
        super.onCreate()
        setUpSensor()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Fall Detection Channel"
            val descriptionText = "Channel for Fall Detection Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("FallDetectionChannelId", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /*
     * Set up sensor when the app starts
     * sampling period = 1000000 (us) / SAMPLING_RATE
     */
    private fun setUpSensor(){
        sensorManager = getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager.registerListener(
            sensorListener,
            mAccelerometer,
            1000000 / SAMPLING_RATE,
            1000000 / SAMPLING_RATE
        )
    }

    /*
     * Run at every sensor samples data
     * Save sensor values in window, run sensorWindowFulled() when 75 * 3 data is ready.
     */
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                // Process accelerometer data
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

                if (window_index % WINDOW_STRIDE == 0 && window_index >= WINDOW_SIZE) {
                    sensorWindowFulled()
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

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
            fallDetected(classificationResult)
        }

        val featureText = """${window_index}
            |score: ${score[0]} ${score[1]}
            |classification: ${classificationResult[0]} ${classificationResult[1]} ${classificationResult[2]} ${classificationResult[3]} ${classificationResult[4]}
            |x: ${features.filterIndexed{i, _ -> i % 3 == 0}.joinToString(limit=5, transform = {x-> "%.2f".format(x)})}
            |y: ${features.filterIndexed{i, _ -> i % 3 == 1}.joinToString(limit=5, transform = {x-> "%.2f".format(x)})}
            |z: ${features.filterIndexed{i, _ -> i % 3 == 2}.joinToString(limit=5, transform = {x-> "%.2f".format(x)})}
            |""".trimMargin()


    }

    /*
     * Service calls this function when fall is detected even if background.
     * Popping up DetectedActivity, and alarm.
     */
    private fun fallDetected(classificationResult: DoubleArray) {

        Log.d("Service fallDetected","Fall detected")

        intented = true

        /*
         * Create a notification to open DetectedActivity
         */
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "FallDetectionChannelId"

        val intent = Intent(this, DetectedActivity::class.java)
        intent.putExtra("classificationResult", classificationResult)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val pendingIntent = if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE); //Activity를 시작하는 인텐트 생성
        }else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Fall Detected!")
            .setContentText("Tap to view details")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)

        // Call DetectedActivity
        startActivity(intent)
    }

    /*
     * Binder for Activities to bind this service
     */
    inner class LocalBinder : Binder() {
        fun getService(): FallDetectionService {
            return this@FallDetectionService
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    /*
     * when DetectedActivity destroys, this function will be called
     */
    fun notifyActivityFinished() {
        Log.d("FallDetectionService", "DetectedActivity Finished")
        activityResultListener?.onActivityFinished()
        intented = false
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorListener)
    }
}