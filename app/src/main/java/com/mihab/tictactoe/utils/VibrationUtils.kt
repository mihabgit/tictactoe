package com.mihab.tictactoe

import android.Manifest
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission

object VibrationUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate(context: Context, duration: Long = 40L) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val effect =
            VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)

        if (effect != null) vibrator.vibrate(effect)
        else @Suppress("DEPRECATION") vibrator.vibrate(duration)
    }
}
