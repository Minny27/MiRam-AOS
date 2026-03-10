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
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmForegroundService : Service() {
    private data class ActiveAlarm(
        val alarmId: String,
        val label: String,
        val hour: Int,
        val minute: Int,
        val ringDuration: Int,
        val soundUri: String,
        val soundEnabled: Boolean,
        val vibrateEnabled: Boolean,
        val vibrationMode: String,
        val snoozeEnabled: Boolean,
        val snoozeIntervalMinutes: Int,
        val snoozeRepeatCount: Int
    )

    @Inject lateinit var scheduler: AlarmScheduler

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var vibrator: Vibrator? = null
    private var activeAlarm: ActiveAlarm? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAlarm()
            return START_NOT_STICKY
        }
        if (intent?.action == ACTION_SNOOZE) {
            intent.toActiveAlarm()?.let(::scheduleSnooze)
            stopAlarm()
            return START_NOT_STICKY
        }

        val currentAlarm = intent?.toActiveAlarm() ?: return START_NOT_STICKY
        activeAlarm = currentAlarm

        // UI에 알람 발화 상태 전달 (포그라운드·백그라운드 모두 처리)
        AlarmStateHolder.startRinging(currentAlarm.alarmId, currentAlarm.label, currentAlarm.ringDuration)

        val timeText = String.format("%02d:%02d", currentAlarm.hour, currentAlarm.minute)
        val notification = AlarmNotificationHelper.buildAlarmNotification(
            context = this,
            label = currentAlarm.label,
            alarmId = currentAlarm.alarmId,
            timeText = timeText,
            hour = currentAlarm.hour,
            minute = currentAlarm.minute,
            ringDuration = currentAlarm.ringDuration,
            soundUri = currentAlarm.soundUri,
            soundEnabled = currentAlarm.soundEnabled,
            vibrateEnabled = currentAlarm.vibrateEnabled,
            vibrationMode = currentAlarm.vibrationMode,
            snoozeEnabled = currentAlarm.snoozeEnabled,
            snoozeIntervalMinutes = currentAlarm.snoozeIntervalMinutes,
            snoozeRepeatCount = currentAlarm.snoozeRepeatCount
        )
        startForeground(AlarmNotificationHelper.NOTIFICATION_ID, notification)

        requestAudioFocus()
        if (currentAlarm.soundEnabled) startMediaPlayer(currentAlarm.soundUri)
        if (currentAlarm.vibrateEnabled) startVibration(currentAlarm.vibrationMode)

        val scheduledStop = Runnable {
            activeAlarm?.let(::scheduleSnooze)
            stopAlarm()
        }
        stopRunnable = scheduledStop
        handler.postDelayed(scheduledStop, currentAlarm.ringDuration * 1000L)

        return START_NOT_STICKY
    }

    private fun scheduleSnooze(alarm: ActiveAlarm) {
        if (!alarm.snoozeEnabled || alarm.snoozeRepeatCount <= 0) return
        scheduler.scheduleSnooze(
            alarmId = alarm.alarmId,
            label = alarm.label,
            hour = alarm.hour,
            minute = alarm.minute,
            ringDuration = alarm.ringDuration,
            soundUri = alarm.soundUri,
            soundEnabled = alarm.soundEnabled,
            vibrateEnabled = alarm.vibrateEnabled,
            vibrationMode = alarm.vibrationMode,
            snoozeEnabled = alarm.snoozeEnabled,
            snoozeIntervalMinutes = alarm.snoozeIntervalMinutes,
            snoozeRepeatCount = alarm.snoozeRepeatCount - 1
        )
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
        activeAlarm = null
        AlarmStateHolder.stopRinging()
        stopRunnable?.let { handler.removeCallbacks(it) }
        mediaPlayer?.runCatching { if (isPlaying) stop(); release() }
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
        audioFocusRequest?.let {
            (getSystemService(Context.AUDIO_SERVICE) as? AudioManager)
                ?.abandonAudioFocusRequest(it)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        activeAlarm = null
        stopRunnable?.let { handler.removeCallbacks(it) }
        mediaPlayer?.runCatching { if (isPlaying) stop(); release() }
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun startVibration(mode: String) {
        if (mode == "무음") return
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator = vib
        val pattern = when (mode) {
            "Heartbeat" -> longArrayOf(0, 120, 120, 300)
            "Short" -> longArrayOf(0, 200, 150, 200)
            else -> longArrayOf(0, 500, 500)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(pattern, 0)
        }
    }

    companion object {
        const val ACTION_STOP = "com.example.miram.ACTION_ALARM_STOP"
        const val ACTION_SNOOZE = "com.example.miram.ACTION_ALARM_SNOOZE"
    }

    private fun Intent.toActiveAlarm(): ActiveAlarm {
        return ActiveAlarm(
            alarmId = getStringExtra(AlarmReceiver.EXTRA_ALARM_ID) ?: "",
            label = getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL) ?: "",
            hour = getIntExtra(AlarmReceiver.EXTRA_ALARM_HOUR, 0),
            minute = getIntExtra(AlarmReceiver.EXTRA_ALARM_MINUTE, 0),
            ringDuration = getIntExtra(AlarmReceiver.EXTRA_RING_DURATION, 60),
            soundUri = getStringExtra(AlarmReceiver.EXTRA_SOUND_URI) ?: "",
            soundEnabled = getBooleanExtra(AlarmReceiver.EXTRA_SOUND_ENABLED, true),
            vibrateEnabled = getBooleanExtra(AlarmReceiver.EXTRA_VIBRATE_ENABLED, true),
            vibrationMode = getStringExtra(AlarmReceiver.EXTRA_VIBRATION_MODE) ?: "Basic call",
            snoozeEnabled = getBooleanExtra(AlarmReceiver.EXTRA_SNOOZE_ENABLED, true),
            snoozeIntervalMinutes = getIntExtra(AlarmReceiver.EXTRA_SNOOZE_INTERVAL_MIN, 5),
            snoozeRepeatCount = getIntExtra(AlarmReceiver.EXTRA_SNOOZE_REPEAT_COUNT, 3)
        )
    }
}
