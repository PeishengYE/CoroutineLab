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
import com.example.android.kotlincoroutines.util.singleArgViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

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

const val ZIHAN_COMPUTER = "192.168.106.131"
const val ZIYI_COMPUTER = "192.168.106.221"
//const val URL_SCREENSHOT = "http://${ZIYI_COMPUTER}:8080//image/"


class MainViewModel(private val repository: TitleRepository) : ViewModel() {

    companion object {
        /**
         * Factory for creating [MainViewModel]
         *
         * @param arg the repository to pass to [MainViewModel]
         */
        val FACTORY = singleArgViewModelFactory(::MainViewModel)
    }

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
    private val _taps = MutableLiveData<String>("$tapCount taps")

    /**
     * Public view of tap live data.
     */
    val taps: LiveData<String>
        get() = _taps



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
     * Respond to onClick events by refreshing the title.
     *
     * The loading spinner will display until a result is returned, and errors will trigger
     * a snackbar.
     */
    fun onMainViewClicked() {
//        refreshTitle()
        updateScreenShot()
    }

    /**
     * Wait one second then update the tap count.
     */
    private fun updateScreenShot() {



        // launch a coroutine in viewModelScope
        viewModelScope.launch {
            var URL_SCREENSHOT = ""
            while (true) {

                if (isComputerAlive(ZIYI_COMPUTER)) {
                    URL_SCREENSHOT = "http://${ZIYI_COMPUTER}:8080//image/"
                    _imageUrl.value = URL_SCREENSHOT
                    delay(5000)
                }
                if (isComputerAlive(ZIHAN_COMPUTER)) {
                    URL_SCREENSHOT = "http://${ZIHAN_COMPUTER}:8080//image/"
                    _imageUrl.value = URL_SCREENSHOT
                    delay(5000)
                }


            }
        }


    }

    fun isComputerAlive(ip: String):Boolean {
        var res = false
       try {
           val client = Socket(ip, 8080)
           val output = PrintWriter(client.getOutputStream(), true)
           val input = BufferedReader(InputStreamReader(client.inputStream))

           println("Client sending [Hello]")
           output.println("Hello")
           println("Client receiving [${input.readLine()}]")
           client.close()
           res = true
       }catch(cause: Throwable) {
           // If anything throws an exception, inform the caller

           Log.v(TAG, " ${ip}:8080 is not available:  " + cause)
           res = false
       }
        return res
    }

    init {
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
