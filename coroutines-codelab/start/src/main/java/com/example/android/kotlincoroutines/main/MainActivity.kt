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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.example.android.kotlincoroutines.R
import com.example.android.kotlincoroutines.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar


/**
 * Show layout.activity_main and setup data binding.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Inflate layout.activity_main and setup data binding.
     */
    private lateinit var  viewModelLocal: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val binding =
                DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)
        binding.lifecycleOwner = this
        val rootLayout: ConstraintLayout = binding.rootLayout



        // Get MainViewModel by passing a database to the factory
        val database = getDatabase(this)
        val repository = TitleRepository(getNetworkService(), database.titleDao)
        val viewModel = ViewModelProviders
                .of(this, MainViewModel.FACTORY(repository))
                .get(MainViewModel::class.java)
        viewModelLocal = viewModel
        binding.viewModel = viewModel
        // When rootLayout is clicked call onMainViewClicked in ViewModel
//        rootLayout.setOnClickListener {
//            viewModel.onMainViewClicked()
//        }
        binding.reconnect.setEnabled(false)
        binding.reconnect.setOnClickListener{
            viewModel.reconnect()
        }
//        // update the title when the [MainViewModel.title] changes
//        viewModel.titleInfo.observe(this) { value ->
//            value?.let {
//                title.text = it
//            }
//        }


        viewModel.scanningProgress.observe(this) { value ->
//            Log.v("MainActivity", "with value : ${value}")
            val progress = value/255.0*100
            binding.scanningProgress.setProgress(progress.toInt())
//            Log.v("MainActivity", "with progress : ${progress}")
            if (progress.toInt() > 98){
                binding.scanningProgress.visibility = View.GONE
                binding.reconnect.setEnabled(true)
            }

        }

        viewModel.connectStatus.observe(this) { value ->
            value?.let {
                if (it.contains("Ziyi")){
                    if (it.contains("disconnected")){
                        binding.kid1Status.setImageResource(R.drawable.ic_connection_error)

                    }else{
                        binding.kid1Status.setImageResource(R.drawable.round_cast_connected_20)
                    }
                }
                if (it.contains("Zihan")){
                    if (it.contains("disconnected")){

                        binding.kid2Status.setImageResource(R.drawable.ic_connection_error)
                    }else{
                        binding.kid2Status.setImageResource(R.drawable.round_cast_connected_20)
                    }
                }
            }
        }


//        viewModel.taps.observe(this) { value ->
//            taps.text = value
//        }

        // show the spinner when [MainViewModel.spinner] is true
//        viewModel.spinner.observe(this) { value ->
//            value.let { show ->
//                spinner.visibility = if (show) View.VISIBLE else View.GONE
//            }
//        }

        // Show a snackbar whenever the [ViewModel.snackbar] is updated a non-null value
        viewModel.snackbar.observe(this) { text ->
            text?.let {
                Snackbar.make(rootLayout, text, Snackbar.LENGTH_SHORT).show()
                viewModel.onSnackbarShown()
            }
        }


        viewModel.waitingStatus.observe(this) { text ->
            text?.let {
                if (text.length > 3){
                    binding.waitingStatus.setText(text)
                }
            }
        }

        viewModel.kidsComputerOnline.observe(this) { isOnLine ->
            isOnLine?.let {
                if (isOnLine) {
                    binding.scanning.visibility = View.GONE
                    binding.waitingStatus.visibility = View.GONE
//                    Snackbar.make(rootLayout, "finish scanning the local network", Snackbar.LENGTH_SHORT).show()
//                    viewModel.updateScreenShot()
                }
            }
        }
//        setHasOptionsMenu(true)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() === R.id.menu_rescan) {
//            showStylesDialog()
            viewModelLocal.rescanIPMacTable()
        }
        return true
    }


}
