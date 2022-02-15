package com.ocics.covidtoday

import android.animation.Animator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val splashTextView = findViewById<TextView>(R.id.splash_text)

        val splashAnimationView = findViewById<LottieAnimationView>(R.id.animation_view)
        splashAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {
                splashTextView.apply {
                    alpha = 0f
                    visibility = View.VISIBLE
                    animate()
                        .setStartDelay(1000)
                        .alpha(1f)
                        .setDuration(1000)
                        .setListener(null)
                }
            }
            override fun onAnimationEnd(animation: Animator?) {
                launch()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    fun launch() {
        val intent = Intent()
        intent.setClass(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
