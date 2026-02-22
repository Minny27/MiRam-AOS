package com.example.miram.shared.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class AlarmForegroundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAlarm()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getStringExtra(AlarmReceiver.EXTRA_ALARM_ID) ?: ""
        val label = intent?.getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL) ?: ""
        val ringDuration = intent?.getIntExtra(AlarmReceiver.EXTRA_RING_DURATION, 60) ?: 60
        val soundUri = intent?.getStringExtra(AlarmReceiver.EXTRA_SOUND_URI) ?: ""

        // UI에 알람 발화 상태 전달 (포그라운드·백그라운드 모두 처리)
        AlarmStateHolder.startRinging(alarmId, label, ringDuration)

        val notification = AlarmNotificationHelper.buildAlarmNotification(this, label, alarmId)
        startForeground(AlarmNotificationHelper.NOTIFICATION_ID, notification)

        requestAudioFocus()
        startMediaPlayer(soundUri)

        stopRunnable = Runnable { stopAlarm() }
        handler.postDelayed(stopRunnable!!, ringDuration * 1000L)

        return START_NOT_STICKY
    }

    private fun requestAudioFocus() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .build()
                .also { audioManager.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN)
        }
    }

    private fun startMediaPlayer(soundUri: String) {
        val uri: Uri = soundUri.takeIf { it.isNotBlank() }
            ?.runCatching { Uri.parse(this) }?.getOrNull()
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: return

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmForegroundService, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            // soundUri 파싱 실패 시 시스템 기본 알람으로 재시도
            val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: return
            runCatching {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(this@AlarmForegroundService, fallbackUri)
                    isLooping = true
                    prepare()
                    start()
                }
            }
        }
    }

    private fun stopAlarm() {
        AlarmStateHolder.stopRinging()
        stopRunnable?.let { handler.removeCallbacks(it) }
        mediaPlayer?.runCatching { if (isPlaying) stop(); release() }
        mediaPlayer = null
        audioFocusRequest?.let {
            (getSystemService(Context.AUDIO_SERVICE) as? AudioManager)
                ?.abandonAudioFocusRequest(it)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRunnable?.let { handler.removeCallbacks(it) }
        mediaPlayer?.runCatching { if (isPlaying) stop(); release() }
        mediaPlayer = null
    }

    companion object {
        const val ACTION_STOP = "com.example.miram.ACTION_ALARM_STOP"
    }
}
