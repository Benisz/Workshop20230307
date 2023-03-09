/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.kapmacs.workshopwatch.presentation

import WorkshopViewState
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import com.kapmacs.workshopwatch.presentation.theme.Workshop20230307Theme

class MainActivity : ComponentActivity(), SensorEventListener {
    lateinit var sensorManager: SensorManager
    lateinit var heartRateSensor: Sensor
    var viewState by mutableStateOf(WorkshopViewState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
        setContent {
            WearApp(viewState)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_HEART_RATE -> {
            //   val heartRate = if (event.values[0].toInt() > 20) event.values[0].toInt() else 60
            //    viewState = viewState.copy(heartRate = heartRate)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}

@Composable
fun WearApp(viewState: WorkshopViewState) {
    Workshop20230307Theme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .scrollable(rememberScrollState(), orientation = Orientation.Vertical),
            verticalArrangement = Arrangement.Center,
        ) {
            heartShape(bpm = viewState.heartRate)
        }
    }
}

@Composable
fun heartShape(modifier: Modifier = Modifier, bpm: Int = 60) {
    var center by remember {
        mutableStateOf(Offset.Zero)
    }
    var leftSide by remember {
        mutableStateOf(Offset.Zero)
    }
    var rightSide by remember {
        mutableStateOf(Offset.Zero)
    }
    var bottomSide by remember {
        mutableStateOf(Offset.Zero)
    }
    val puslsationAnimationStateShape = remember { Animatable(0.2F) }
    val puslsationAnimationStateText = remember { Animatable(0.6F) }
    Canvas(modifier = modifier.fillMaxSize()) {
        center = Offset(this.center.x, this.center.y)
        leftSide = Offset(center.x - 20.dp.toPx(), center.y - 20.dp.toPx())
        rightSide = Offset(center.x + 20.dp.toPx(), center.y - 20.dp.toPx())
        bottomSide = Offset(center.x, center.y + 70.dp.toPx())

        val circles = Path().apply {
            addOval(Rect(center = leftSide, radius = 20.dp.toPx()))
            addOval(Rect(center = rightSide, radius = 20.dp.toPx()))
        }
        val vpath = Path().apply {
            moveTo(leftSide.x - 20.dp.toPx(), leftSide.y)
            lineTo(bottomSide.x, bottomSide.y)
            lineTo(rightSide.x + 20.dp.toPx(), rightSide.y)
        }

        val heartPath = Path().apply {
            op(
                vpath,
                circles,
                PathOperation.Union
            )
        }
        scale(puslsationAnimationStateShape.value, pivot = center) {
            drawPath(
                path = heartPath, color = Color.Red, style =
                Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    miter = 30F
                )
            )
        }
        drawContext.canvas.nativeCanvas.apply {
            scale(puslsationAnimationStateText.value, pivot = center) {
                drawText(
                    bpm.toString(),
                    center.x,
                    center.y,
                    Paint().apply {
                        color = android.graphics.Color.BLUE
                        textSize = 20.dp.toPx()
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }
    LaunchedEffect(bpm) {
        puslsationAnimationStateShape.animateTo(
            targetValue = 2f, animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Restart
            )
        )

    }
    LaunchedEffect(bpm) {
        puslsationAnimationStateText.animateTo(
            targetValue = 1f, animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

}