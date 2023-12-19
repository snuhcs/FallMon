package com.example.fallmon.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
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

    /**
     * sensors / views
     * text_square for debugging
     */
    private lateinit var sensorManager: SensorManager
    private var mAccelerometer: Sensor?= null

    /**
     * 30Hz Samples, total 75 * 3 (x,y,z) data for a window, 15 striding
     */
    private val SAMPLING_RATE: Int = 30
    private val WINDOW_SIZE: Int = 75
    private val WINDOW_STRIDE: Int = 15

    private var sensor_window = Array(WINDOW_SIZE) { Array(3) { 0.0f } }
    private var sensor_window_transpose = Array(3) { Array(WINDOW_SIZE) { 0.0f } }
    private var window_index: Int = 0

    /**
     * var, listener, binder for intent DetectedActivity & receiving isFinished
     */
    private var intented: Boolean = false
    private val pendingDetectionTime: Int = 300  //  seconds = value / 30 Hz
    private var afterDetectionTime: Int = pendingDetectionTime
    private var activityResultListener: ActivityResultListener? = null
    fun setActivityResultListener(listener: ActivityResultListener) {
        this.activityResultListener = listener
    }
    private val binder: LocalBinder = LocalBinder()

    /**
     * Constructor
     * set up textview, sensor
     */
    override fun onCreate() {
        Log.d("FallDetectionService", "Service Created")
        super.onCreate()
        setUpSensor()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Fall Detection Channel"
            val descriptionText = "Channel for Fall Detection Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("FallDetectionChannelId", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        notification()
    }


    /**
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

    private fun notification() {
        /*
         * run this code for maintain application to be run continuously
         */
        Log.d("FallDetectionService", "notification")
        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "ForegroundServiceChannel",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Create and show a notification for the foreground service
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "ForegroundServiceChannel")
            .setContentTitle("Foreground Service")
            .setContentText("Running...")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentIntent(pendingIntent)
            .build()

        // Start foreground service with notification
        startForeground(1234, notification)
    }

    /**
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
                afterDetectionTime += 1

                if (window_index % WINDOW_STRIDE == 0 && window_index >= WINDOW_SIZE && !intented) {
                    sensorWindowFulled()
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

        }
    }

    /**
     * transpose data to 3 * 75 for getting features
     * get scores by running model, intent DetectedActivity if fall detected
     */
    private fun sensorWindowFulled() {
        if(window_index % 75 == 0) {
            notification()
        }
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

        if(score[1] == score.max() && !intented && pendingDetectionTime < afterDetectionTime) {
            fallDetected(classificationResult)
        }

        /* For Debugging Date processing values
        val featureText = """${window_index}
            |score: ${score[0]} ${score[1]}
            |classification: ${classificationResult[0]} ${classificationResult[1]} ${classificationResult[2]} ${classificationResult[3]} ${classificationResult[4]}
            |x: ${features.filterIndexed{i, _ -> i % 3 == 0}.joinToString(limit=5, transform = {x-> "%.2f".format(x)})}
            |y: ${features.filterIndexed{i, _ -> i % 3 == 1}.joinToString(limit=5, transform = {x-> "%.2f".format(x)})}
            |z: ${features.filterIndexed{i, _ -> i % 3 == 2}.joinToString(limit=5, transform = {x-> "%.2f".format(x)})}
            |""".trimMargin()

         */


    }

    /**
     * Service calls this function when fall is detected even if background.
     * Popping up DetectedActivity, and alarm.
     */
    private fun fallDetected(classificationResult: DoubleArray) {

        afterDetectionTime = 0
        Log.d("Service fallDetected","Fall detected")

        /**
         * Create a notification to open DetectedActivity
         */
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "FallDetectionChannelId"

        val intent = Intent(this, DetectedActivity::class.java)
        intent.putExtra("classificationResult", classificationResult)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

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

    /**
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

    /**
     * when DetectedActivity creates/destroys, this function will be called for checking intent
     */
    fun notifyActivityFinished() {
        Log.d("FallDetectionService", "DetectedActivity Finished")
        activityResultListener?.onActivityFinished()
        intented = false
    }

    fun notifyActivityCreated() {
        Log.d("FallDetectionService", "DetectedActivity Created")
        activityResultListener?.onActivityCreated()
        intented = true
    }

    fun stopService() {
        Log.d("FallDetectionService", "Stopping Service...")
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (e:Exception) {

        }
        stopSelf()
    }

    override fun onDestroy() {
        Log.d("FallDetectionService", "Warning: FallDetectionService Destroyed")
        super.onDestroy()
        sensorManager.unregisterListener(sensorListener)
    }
}