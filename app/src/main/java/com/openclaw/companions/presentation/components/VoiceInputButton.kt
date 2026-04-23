package com.openclaw.companions.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

@Composable
fun VoiceInputButton(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: (ByteArray) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isLocalRecording by remember { mutableStateOf(false) }
    var audioData by remember { mutableStateOf(ByteArray(0)) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    val scale by animateFloatAsState(
        targetValue = if (isRecording || isLocalRecording) 1.2f else 1f,
        label = "mic_scale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isRecording || isLocalRecording) {
            // Recording pulse effect
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        CircleShape
                    )
            )
        }

        FilledIconButton(
            onClick = {
                if (!hasPermission) {
                    launcher.launch(Manifest.permission.RECORD_AUDIO)
                    return@FilledIconButton
                }

                if (isLocalRecording) {
                    isLocalRecording = false
                    // Stop recording and send data
                    // TODO: Stop AudioRecord and process data
                } else {
                    isLocalRecording = true
                    onStartRecording()
                    // TODO: Start AudioRecord
                }
            },
            modifier = Modifier
                .scale(scale)
                .size(48.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isRecording || isLocalRecording)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = if (isLocalRecording) "Stop recording" else "Start recording",
                tint = if (isRecording || isLocalRecording)
                    Color.White
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

class AudioRecorder {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startRecording(
        sampleRate: Int = 16000,
        onAudioData: (ByteArray) -> Unit
    ) {
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()

        recordingJob = scope.launch {
            val buffer = ByteArray(bufferSize)
            while (isActive) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val data = buffer.copyOf(read)
                    onAudioData(data)
                }
            }
        }
    }

    fun stopRecording(): ByteArray {
        recordingJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        return ByteArray(0) // TODO: Accumulate and return all audio data
    }
}
