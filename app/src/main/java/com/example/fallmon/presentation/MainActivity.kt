/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.fallmon.presentation

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.fallmon.R
import com.example.fallmon.presentation.retrofit.FallMonService
import com.example.fallmon.presentation.retrofit.dto.FallHistoryDTO
import com.example.fallmon.presentation.retrofit.dto.UserDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.UUID

interface ActivityResultListener {
    fun onActivityFinished()
    fun onActivityCreated()
}

class MainActivity : ComponentActivity(), ActivityResultListener {

    /*
     * text_square for debugging
     */
    private lateinit var text_square: TextView

    /*
     * intented : check if intented DetectedActivity
     * getActivityResult : to get result from DetectedActivity
     */
    private var intented: Boolean = false
    private val getActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK) {
            val confirmed = it.data?.getBooleanExtra("confirmed", false)
            intented = false
        }
    }


    private fun getOrSetSharedPreferences(preference:String, key: String, value: String): String{
        val userPreferences = getSharedPreferences(preference, MODE_PRIVATE)
        val prevVal = userPreferences.getString(key, "").toString()
        // if value has already set in sharedPreferences, just return value
        if (prevVal.isNotBlank()) return prevVal
        // else set key to value passed by argument
        val editor = userPreferences.edit()
        editor.putString(key, value)
        editor.apply()
        return value
    }

    /*
     *  initialize local user ID and PW
     *  TODO: save PW with secure way
     */
    private fun setupLocalUser(){
        val userID: String = getOrSetSharedPreferences("User", "ID", UUID.randomUUID().toString())
        val userPW: String = getOrSetSharedPreferences("User", "PW", UUID.randomUUID().toString())
    }

    /*
     * initialize remote user (server) ID and PW
     *
     */
    private fun setupRemoteUser(){
        val preferences = getSharedPreferences("User", MODE_PRIVATE)
        val userID = preferences.getString("ID", "") ?: ""
        val userPW = preferences.getString("PW", "") ?: ""
        Log.d("Main", "setup remote user $userID $userPW")
        request(userID, userPW)
    }

    private fun requestSucceeded(){
        Log.d("Main", "User Created")
    }

    private  fun requestFailed(){
        Log.d("Main", "User creation failed")
    }
    private fun request(userID: String, userPW: String){
        try{
            val retrofit = RetrofitClient.instance
            val api = retrofit.create(FallMonService::class.java)
            api.createUser(userID, userPW).enqueue(object :
                Callback<UserDTO> {
                override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                    Log.e("Retrofit", "Error: ${t.message}")
                    requestFailed()
                }
                override fun onResponse(
                    call: Call<UserDTO>,
                    response: Response<UserDTO>
                ) {
                    if(response.isSuccessful){
                        Log.d("Retrofit", "Success: response code ${response.code()}")
                        requestSucceeded()
                    }else{
                        Log.d("Retrofit", "Failed: response code ${response.code()}")
                        requestFailed()
                    }
                }
            })
        }catch(e:Exception){
            Log.e("Retrofit", "Error: ${e.message}")
            requestFailed()
        }
    }

    /*
     * Constructor
     * Start FallDetectionService
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text_square = findViewById(R.id.text_view1)
        setupLocalUser()
        setupRemoteUser()
        val fallDetectionService = FallDetectionService()
        fallDetectionService.setActivityResultListener(this)
        val serviceIntent = Intent(this, FallDetectionService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    override fun onActivityFinished() {
    }

    override fun onActivityCreated() {
    }
}
