package vn.cser21.incoming

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import java.util.concurrent.CopyOnWriteArrayList

class Foreground : Application.ActivityLifecycleCallbacks {

    private var foreground = false
    private var paused = false
    private val listeners = CopyOnWriteArrayList<Listener>()
    private val handler = Handler()
    private var check: Runnable? = null

    fun get(): Foreground {
        return instance
    }

    operator fun get(application: Application): Foreground {
        return instance
    }

    operator fun get(ctx: Context): Foreground {
        return instance
    }

    fun isForeground(): Boolean {
        return foreground
    }

    fun isBackground(): Boolean {
        return !foreground
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    override fun onActivityPaused(activity: Activity) {
        paused = true

        check?.run { handler.removeCallbacks(this) }

        handler.postDelayed(Runnable {
            if (foreground && paused) {
                foreground = false
                for (listener in listeners) {
                    try {
                        listener.onBecameBackground()
                    } catch (exc: Exception) {
                    }
                }
                return@Runnable
            }
        }, CHECK_DELAY)
    }

    override fun onActivityResumed(activity: Activity) {
        paused = false

        val wasBackground = !foreground

        foreground = true

        check?.run { handler.removeCallbacks(this) }

        if (wasBackground) {
            for (listener in listeners) {
                try {
                    listener.onBecameForeground()
                } catch (exc: Exception) {
                }
            }
            return
        }
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    interface Listener {
        fun onBecameForeground()
        fun onBecameBackground()
    }

    companion object {
        lateinit var instance: Foreground
        private const val CHECK_DELAY: Long = 500

        fun inject(application: Application): Foreground {
            instance = Foreground()
            application.registerActivityLifecycleCallbacks(instance)
            return instance
        }
    }
}
