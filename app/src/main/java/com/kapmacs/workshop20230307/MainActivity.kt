package com.kapmacs.workshop20230307

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.kapmacs.workshop20230307.ui.theme.Workshop20230307Theme
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(
                    networkType = NetworkType.CONNECTED
                ).build()
            )
            .build()

        val downloadRequest1 = PeriodicWorkRequestBuilder<DownloadWorker>(20, TimeUnit.SECONDS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(
                    networkType = NetworkType.CONNECTED
                ).build()
            )
            .build()

        val workManager = WorkManager.getInstance(applicationContext)
        setContent {
            Workshop20230307Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background

                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val workInfos = workManager
                            .getWorkInfosForUniqueWorkLiveData("download")
                            .observeAsState()
                            .value
                        val downloadInfo = remember(key1 = workInfos) {
                            workInfos?.find { it.id == downloadRequest.id }
                        }
                        val progress by derivedStateOf {
                            downloadInfo?.progress?.getInt(WorkerKeys.PROGRESS, 0) ?: 0
                        }
                        val downloadedIMGURI by derivedStateOf {
                            downloadInfo?.outputData?.getString(WorkerKeys.IMAGE_URI)
                        }
                        val inProgress by derivedStateOf {
                            downloadInfo?.progress?.getBoolean(WorkerKeys.INPROGRESS, false)
                                ?: false
                        }

                        ProgressBarCircle(
                            modifier = Modifier
                                .size(200.dp)
                                .padding(20.dp),
                            progressState = progress,
                            sizeDp = 200.dp,
                            barThickness = 5.dp,
                            inProgress = inProgress,
                            imgURI = downloadedIMGURI
                        )
                        Spacer(modifier = Modifier.fillMaxWidth())



                        Button(onClick = {
                            val work = downloadRequest as WorkRequest
                            if (work is OneTimeWorkRequest) {
                                workManager.beginUniqueWork(
                                    "download",
                                    ExistingWorkPolicy.KEEP,
                                    work
                                ).enqueue()
                            } else if (work is PeriodicWorkRequest) {
                                workManager.enqueueUniquePeriodicWork(
                                    "download",
                                    ExistingPeriodicWorkPolicy.KEEP,
                                    work
                                )
                            }
                        }) {
                            Text(text = "Download")
                        }
                        Button(onClick = {
                            workManager.cancelAllWork()
                        }) {
                            Text(text = "Cancel Work")
                        }
                    }
                }
            }

        }

    }


    @Composable
    fun ProgressBarCircle(
        modifier: Modifier = Modifier,
        progressState: Int,
        sizeDp: Dp,
        barThickness: Dp,
        inProgress: Boolean,
        imgURI: String?,
    ) {
        var center by remember {
            mutableStateOf(Offset.Zero)
        }
        Canvas(modifier = modifier) {

            center = this.center
            drawContext.canvas.nativeCanvas.apply {

                if (imgURI != null && inProgress == false) {
                    val imgBitmap = BitmapFactory.decodeFile(imgURI).asImageBitmap()
                    val circle = Path().apply {
                        addOval(Rect(center = center, radius = sizeDp.toPx() / 2))

                    }

                    clipPath(path = circle, clipOp = ClipOp.Intersect) {
                        drawImage(
                            image = imgBitmap,
                            dstSize = IntSize(
                                sizeDp.toPx().roundToInt(),
                                sizeDp.toPx().roundToInt()
                            ),
                            dstOffset = IntOffset(
                                (center.x - (sizeDp.toPx() / 2)).roundToInt(),
                                (center.y - (sizeDp.toPx() / 2)).roundToInt()
                            ),
                        )
                    }
                }

                if (inProgress) {
                    drawText(
                        progressState.toString(),
                        center.x,
                        center.y + (sizeDp.toPx() / 4),
                        Paint().apply {
                            color = Color.BLUE
                            textSize = sizeDp.toPx()
                            textAlign = Paint.Align.CENTER
                        }
                    )

                    val circle = Path().apply {
                        addOval(Rect(center = center, radius = sizeDp.toPx()))
                    }
                    val progressPath = Path()
                    PathMeasure().apply {
                        setPath(circle, false)
                        getSegment(0f, (progressState / 100F) * length, progressPath, true)
                    }
                    drawPath(
                        path = circle,
                        color = androidx.compose.ui.graphics.Color.Red,
                        style = Stroke(width = barThickness.toPx())
                    )
                    drawPath(
                        path = progressPath,
                        color = androidx.compose.ui.graphics.Color.Green,
                        style = Stroke(width = barThickness.toPx())
                    )
                }
                val nativeCanvasWidth = this.width
                val nativeCanvasHeight = this.height
                Log.d(TAG, "NativeCanvas: $nativeCanvasWidth $nativeCanvasHeight")
            }
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            Log.d(TAG, "Canvas: $canvasWidth $canvasHeight")
        }
    }
}
