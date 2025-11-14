package com.mihab.tictactoe.utils

import android.content.Context
import android.media.MediaPlayer
import com.mihab.tictactoe.R

object SoundManager {
    private var movePlayer: MediaPlayer? = null
    private var winPlayer: MediaPlayer? = null

    var isSoundOn = true   // 🔊 toggle flag

    fun playMove(context: Context) {
        if (!isSoundOn) return
        release(movePlayer)
        movePlayer = MediaPlayer.create(context, R.raw.click)
        movePlayer?.start()
    }

    fun playWin(context: Context) {
        if (!isSoundOn) return
        release(winPlayer)
        winPlayer = MediaPlayer.create(context, R.raw.success)
        winPlayer?.start()
    }

    private fun release(player: MediaPlayer?) {
        player?.release()
    }
}