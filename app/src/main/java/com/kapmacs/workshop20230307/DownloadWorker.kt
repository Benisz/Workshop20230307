package com.kapmacs.workshop20230307

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okio.use
import java.io.File
import java.io.IOException

class DownloadWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val response = ImgApi.instance.downloadImg()
        response.body()?.let { body ->
            return withContext(Dispatchers.IO) {
                try {
                    val file = File(context.cacheDir, "image.jpg")
                    body.byteStream().use { inputStream ->
                        file.outputStream().use { outputStream ->
                            val totalBytes = body.contentLength()
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var progressBytes = 0L
                            var bytes = inputStream.read(buffer)
                            while (bytes >= 0) {
                                outputStream.write(buffer, 0, bytes)
                                progressBytes += bytes
                                bytes = inputStream.read(buffer)
                                val progress = ((progressBytes * 100) / totalBytes).toInt()
                                setProgress(
                                    workDataOf(
                                        WorkerKeys.PROGRESS to progress,
                                        WorkerKeys.INPROGRESS to true
                                    )
                                )
                            }
                        }
                    }
                    Result.success(
                        workDataOf(
                            WorkerKeys.IMAGE_URI to file.absolutePath
                        )
                    )

                } catch (e: IOException) {
                    return@withContext Result.failure(
                        workDataOf(
                            WorkerKeys.ERROR_MSG to e.localizedMessage,
                        )
                    )
                }
            }

        }
        if (!response.isSuccessful) {
            if (response.code().toString().startsWith("5")) {
                return Result.retry()
            }
            return Result.failure(
                workDataOf(
                    WorkerKeys.ERROR_MSG to "Network error"
                )
            )
        }
        return Result.failure(
            workDataOf(WorkerKeys.ERROR_MSG to "Unknown error")
        )
    }

}
