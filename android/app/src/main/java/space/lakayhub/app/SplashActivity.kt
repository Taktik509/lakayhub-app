package space.lakayhub.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

/**
 * Splash natif ak animasyon logo LakayHub
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.splash_logo)

        // Animasyon: logo antre ak rebondisman
        val scaleX = ScaleAnimation(0.3f, 1f, 0.3f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleX.duration = 800
        scaleX.interpolator = OvershootInterpolator(1.5f)

        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 600

        val set = AnimationSet(true)
        set.addAnimation(scaleX)
        set.addAnimation(fadeIn)
        logo.startAnimation(set)

        // Ale nan MainActivity apre 2 segonn
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
}
