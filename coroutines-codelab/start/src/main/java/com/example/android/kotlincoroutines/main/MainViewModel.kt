/*
 * Copyright (C) 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.kotlincoroutines.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.kotlincoroutines.util.ScanIpAddress
import com.example.android.kotlincoroutines.util.singleArgViewModelFactory
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.*


/**
 * MainViewModel designed to store and manage UI-related data in a lifecycle conscious way. This
 * allows data to survive configuration changes such as screen rotations. In addition, background
 * work such as fetching network results can continue through configuration changes and deliver
 * results after the new Fragment or Activity is available.
 *
 * @param repository the data source this ViewModel will fetch results from.
 */

//const val URL_SCREENSHOT = "http://172.16.18.211:8080/image/"
//const val URL_BLUE = "http://172.16.18.211:8080/blue/"
//const val URL_RED = "http://172.16.18.211:8080/red/"

const val ZIYI_COMPUTER_MAC = "00:10:f3:6b:2e:da"
const val ZIHAN_COMPUTER_MAC_1 = "34:e6:d7:17:b7:3a"
const val ZIHAN_COMPUTER_MAC_2 = "00:13:3b:99:31:af"

//const val URL_SCREENSHOT = "http://${ZIYI_COMPUTER}:8080//image/"


class MainViewModel(private val repository: TitleRepository) : ViewModel() {
     val TAG = "MainViewModel"
    companion object {
        /**
         * Factory for creating [MainViewModel]
         *
         * @param arg the repository to pass to [MainViewModel]
         */
        val FACTORY = singleArgViewModelFactory(::MainViewModel)
    }
    private var macIPTable = mutableMapOf<String, String>()
    /**
     * Request a snackbar to display a string.
     *
     * This variable is private because we don't want to expose MutableLiveData
     *
     * MutableLiveData allows anyone to set a value, and MainViewModel is the only
     * class that should be setting values.
     */
    private val _snackBar = MutableLiveData<String?>()

    /**
     * Request a snackbar to display a string.
     */
    val snackbar: LiveData<String?>
        get() = _snackBar

    private val _scanningProgress = MutableLiveData<Int>()

    /**
     * Request a snackbar to display a string.
     */
    val scanningProgress: LiveData<Int>
        get() = _scanningProgress

    /**
     * Update title text via this LiveData
     */
    val title = repository.title

    private val _spinner = MutableLiveData<Boolean>(false)

    /**
     * Show a loading spinner if true
     */
    val spinner: LiveData<Boolean>
        get() = _spinner

    /**
     * Count of taps on the screen
     */
    private var tapCount = 0

    /**
     * LiveData with formatted tap count.
     */


    private val _waitingStatus = MutableLiveData<String>("")

    val waitingStatus: LiveData<String>
        get() = _waitingStatus

    /**
     * LiveData with formatted tap count.
     */
    private val _imageUrl = MutableLiveData<String>()

    /**
     * Public view of tap live data.
     */
    val imageUrl: LiveData<String>
        get() = _imageUrl

    /**
     * LiveData with formatted tap count.
     */
    private val _currentComputerOwner = MutableLiveData<String>()

    /**
     * Public view of tap live data.
     */
    val currentComputerOwner: LiveData<String>
        get() = _currentComputerOwner

    /**
     * LiveData with formatted tap count.
     */
    private val _currentComputerName = MutableLiveData<String>()

    /**
     * Public view of tap live data.
     */
    val currentComputerName: LiveData<String>
        get() = _currentComputerName




    private val _connectStatus = MutableLiveData<String>("kids computer disconnected")

    /**
     * Public view of tap live data.
     */
    val connectStatus: LiveData<String>
        get() = _connectStatus

    private var isContinueScanScreenshot = false



   data class  HomeComputerStatus(
       var owner: String,
       var computerName: String = "Unknown",
       var osType: String,
       var macAddrss: String,
       var ipAddress: String,
       var isOnLine: Boolean,
       var alive: Boolean = false,
       var portNumber: Int = 8080

   )

    private var kidsComputerStatusList = mutableListOf<HomeComputerStatus>()
    private var isCheckingPortDone = false
    private  var isPortOpen = false

    private fun initKidsComputerStatus(){
        val zihan1 = HomeComputerStatus(
                owner = "ZIHAN",
                osType = "LINUX",
            computerName = "Zihan Linux",
                macAddrss = ZIHAN_COMPUTER_MAC_1,
                ipAddress = "192.168.106.231",
                isOnLine = false

        )

        kidsComputerStatusList.add(zihan1)

        val zihan2 = HomeComputerStatus(
                owner = "ZIHAN",
                osType = "WINDOWS",
                computerName = "Zihan Windows",
                macAddrss = ZIHAN_COMPUTER_MAC_1,
                ipAddress = "192.168.106.119",
                isOnLine = false
        )
        kidsComputerStatusList.add(zihan2)


        val zihan3 = HomeComputerStatus(
            owner = "ZIHAN",
            osType = "WINDOWS",
            computerName = "Zihan Windows",
            macAddrss = ZIHAN_COMPUTER_MAC_1,
            ipAddress = "192.168.106.115",
            portNumber = 8088,
            isOnLine = false
        )
        kidsComputerStatusList.add(zihan3)


        val ziyi = HomeComputerStatus(
                owner = "ZIYI",
                osType = "LINUX",
            computerName = "Ziyi Linux",
                macAddrss = ZIYI_COMPUTER_MAC,
                ipAddress = "192.168.106.221",
                isOnLine = false
        )
        kidsComputerStatusList.add(ziyi)

        val ziyi_from_office = HomeComputerStatus(
            owner = "ZIYI",
            osType = "LINUX",
            computerName = "Ziyi Linux",
            macAddrss = ZIYI_COMPUTER_MAC,
            ipAddress = "172.16.18.19",
            isOnLine = false
        )
        kidsComputerStatusList.add(ziyi_from_office)

        val bitMineWindows = HomeComputerStatus(
            owner = "Peisheng",
            osType = "Windows",
            computerName = "BitMine",
            macAddrss = ZIYI_COMPUTER_MAC,
            ipAddress = "192.168.106.110",
            portNumber = 8088,
            isOnLine = false
        )
        kidsComputerStatusList.add(bitMineWindows)
    }



    private fun isKidsComputerOnLine(){
        viewModelScope.launch {


                while (true){
                    kidsComputerStatusList.forEach {

                        isPortForScreenshotOpenByHttp(it.ipAddress, it.portNumber)

                        if (isPortOpen){
                            Log.v(TAG, "isCheckingPortDone : open on ${it.ipAddress}")
                            if (it.isOnLine == false ){
                                _connectStatus.value = "${it.owner} connected"
                            }
                            it.isOnLine = true
                        }else{
                            Log.v(TAG, "isCheckingPortDone : close on ${it.ipAddress}")
                            if (it.isOnLine == true ){
                                _connectStatus.value = "${it.owner} disconnected"
                            }
                            it.isOnLine = false
                        }
                        delay(2000)
                    }
                }


        }
    }




       private suspend fun isPortForScreenshotOpenByHttp(ip: String, port: Int) {

            withContext(Dispatchers.IO){
                var deferred: Deferred<Boolean> = async {
                    var res = false
                    var codeResponse = 0
                    val mURL = URL("http://${ip}:${port}/getJson")
                    try {
                        with((mURL.openConnection() as HttpURLConnection)) {
                            // optional default is GET
                            this.connectTimeout = 2000
                            this.readTimeout = 2000
                            requestMethod = "GET"

                            println("URL : $url")
                            println("Response Code : $responseCode")
                            codeResponse = responseCode
                            BufferedReader(InputStreamReader(inputStream)).use {
                                val response = StringBuffer()

                                var inputLine = it.readLine()
                                while (inputLine != null) {
                                    response.append(inputLine)
                                    inputLine = it.readLine()
                                }
                                it.close()
                                println("Response : $response")
                                if (response.contains("Nice work!")) {
                                    res = true
                                }
                            }

                        }
                    }catch (cause: Throwable) {
                        // If anything throws an exception, inform the caller

                        Log.v(TAG, "  ${ip}:$port is not available:  " + cause)
                        res = false
                        if (codeResponse == 404 ) res = true
                    }
                    res
                }
                isPortOpen = deferred.await()
            }


        }


    fun updateScreenShot(){
        viewModelScope.launch {
            var URL_SCREENSHOT = ""
            while (isContinueScanScreenshot) {
                kidsComputerStatusList.forEach {
                    if (it.isOnLine){
                        Log.v(TAG, "${it.owner} computer is alive!")
                        URL_SCREENSHOT = "http://${it.ipAddress}:${it.portNumber}//image/"
                        _currentComputerOwner.value = it.owner
                        _currentComputerName.value = it.computerName
                        _imageUrl.value = URL_SCREENSHOT



                    }
                    delay(5000)
                }
            }
        }
    }


    /**
     * Wait one second then update the tap count.
     */
//     fun updateScreenShot_to_del() {
//
//
//
//        // launch a coroutine in viewModelScope
//        viewModelScope.launch {
//            var URL_SCREENSHOT = ""
//            val networkScope = CoroutineScope(Dispatchers.Default)
//            var resZihan = false
//            var resZiyi = false
//            _snackBar.postValue("Scan stared!")
//            while (isContinueScanScreenshot) {
//                delay(5000)
//                /*-> CAUTION, not blocked to get the result */
//                networkScope.launch {
//                    var deferred: Deferred<Boolean> = async {
//                        isPortForScreenshotOpen(ziyi_ip)
//                    }
//                    resZiyi= deferred.await()
//                }
//                /*<- CAUTION, not blocked to get the result */
//
//                if (resZiyi) {
//                    Log.v(TAG, "Ziyi computer is alive!")
//                    URL_SCREENSHOT = "http://${ziyi_ip}:8080//image/"
//                    _kidsComputerOnline.value = true
//                    _imageUrl.value = URL_SCREENSHOT
//                    _titleInfo.value = "ZIYI: ${ziyi_ip} "
//                    _connectStatus.value = "Ziyi connected"
//                    delay(5000)
//                }else{
//                    Log.v(TAG, "Ziyi computer is not reachable!")
//                    _connectStatus.value = "Ziyi disconnected"
//                }
//
//                delay(5000)
//                networkScope.launch {
//                    var deferred: Deferred<Boolean> = async {
//                        isPortForScreenshotOpen(zihan_ip)
//                    }
//                    resZihan= deferred.await()
//                }
//
//                if (resZihan) {
//                    Log.v(TAG, "Zihan computer is alive!")
//                    URL_SCREENSHOT = "http://${zihan_ip}:8080//image/"
//                    _kidsComputerOnline.value = true
//                    _imageUrl.value = URL_SCREENSHOT
//                    _titleInfo.value = "ZIHAN: ${zihan_ip}"
//                    _connectStatus.value = "Zihan connected"
//                    delay(5000)
//                }else{
//                    Log.v(TAG, "Zihan computer is not reachable!")
//                    _connectStatus.value = "Zihan disconnected"
//                }
//
//            }
//            isScanScreenshotFinished = true
//            _snackBar.postValue("Scan stopped!")
//        }
//
//
//    }



    private  suspend  fun isPortForScreenshotOpen(ip: String, port: Int) {
                withContext(Dispatchers.IO){
                    var deferred: Deferred<Boolean> = async {
                        var res = false
                        try {

                            val client = Socket()
                            val socketAddress: SocketAddress = InetSocketAddress(ip, port)
                            client.connect(socketAddress, 500)
                            val output = PrintWriter(client.getOutputStream(), true)
                            val input = BufferedReader(InputStreamReader(client.inputStream))

                            println("Client ${ip} sending [Hello]")
                            output.println("Hello")
                            println("Client ${ip} receiving [${input.readLine()}]")
                            client.close()
                            res = true
                        } catch (cause: Throwable) {
                            // If anything throws an exception, inform the caller

                            Log.v(TAG, "  ${ip}:$port is not available:  " + cause)
                            res = false
                        }
                        res
                    }
                    isPortOpen = deferred.await()
                }
    }

    private  fun getMacIptable(){
        val networkScope = CoroutineScope(Dispatchers.Default)
        networkScope.launch {
            macIPTable = ScanIpAddress.getMacIPTable()
        }
    }

    fun rescanIPMacTable(){

        getMacIptable()

    }

    fun reconnect(){
//        viewModelScope.launch {
//            isContinueScanScreenshot = false
//            while (!isScanScreenshotFinished)
//                delay(500)
//            isKidsComputerOnLine()
//        }

    }

    init {


        initKidsComputerStatus()

        isKidsComputerOnLine()
        isContinueScanScreenshot = true
        updateScreenShot()
    }

    /**
     * Called immediately after the UI shows the snackbar.
     */
    fun onSnackbarShown() {
        _snackBar.value = null
    }

    /**
     * Refresh the title, showing a loading spinner while it refreshes and errors via snackbar.
     */
    fun refreshTitle() {
        // TODO: Convert refreshTitle to use coroutines
        _spinner.value = true
        repository.refreshTitleWithCallbacks(object : TitleRefreshCallback {
            override fun onCompleted() {
                _spinner.postValue(false)
            }

            override fun onError(cause: Throwable) {
                _snackBar.postValue(cause.message)
                _spinner.postValue(false)
            }
        })
    }
}
