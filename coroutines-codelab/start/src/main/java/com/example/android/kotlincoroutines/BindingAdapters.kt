package com.example.android.kotlincoroutines

import android.util.Log
import android.widget.ImageView
import com.example.android.kotlincoroutines.R
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import androidx.core.net.toUri
import com.bumptech.glide.request.RequestOptions

/**
 * Uses the Glide library to load an image by URL into an [ImageView]
 */
@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("http").build()
        Log.v("BindingAdapter", "bindImage()>> started with Glide with URL: " + it)
        Glide.with(imgView.context)
                .load(imgUri)
                .apply(RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image))
                .into(imgView)
    }
}
