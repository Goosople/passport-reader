/*
 * Copyright 2016 - 2022 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tananaev.passportreader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.gemalto.jp2.JP2Decoder
import org.jnbis.WsqDecoder
import java.io.InputStream
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtil {

/**
 * 将 Bitmap 保存为 JPG 到应用内部存储
 * @param context 上下文对象
 * @param bitmap 要保存的 Bitmap
 * @param filename 文件名（不包含路径）
 * @param quality 图片质量 (0-100)
 * @return 保存成功返回文件路径，失败返回 null
 */
fun saveBitmapToInternalStorage(
    context: Context,
    bitmap: Bitmap,
    filename: String,
    quality: Int = 100
): String? {
    return try {
        // 使用应用内部存储目录
        val directory = context.filesDir
        val file = File(directory, filename)
        
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
            fos.flush()
        }
        
        file.absolutePath
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

// 使用示例
// val filePath = saveBitmapToInternalStorage(context, myBitmap, "my_image.jpg")
// if (filePath != null) {
//     // 保存成功
// } else {
//     // 保存失败
// }


    fun decodeImage(context: Context?, mimeType: String, inputStream: InputStream?): Bitmap {
        return if (mimeType.equals("image/jp2", ignoreCase = true) || mimeType.equals(
                "image/jpeg2000",
                ignoreCase = true
            )
        ) {
            JP2Decoder(inputStream).decode()
        } else if (mimeType.equals("image/x-wsq", ignoreCase = true)) {
            val wsqDecoder = WsqDecoder()
            val bitmap = wsqDecoder.decode(inputStream)
            val byteData = bitmap.pixels
            val intData = IntArray(byteData.size)
            for (j in byteData.indices) {
                intData[j] = 0xFF000000.toInt() or
                        (byteData[j].toInt() and 0xFF shl 16) or
                        (byteData[j].toInt() and 0xFF shl 8) or
                        (byteData[j].toInt() and 0xFF)
            }
            Bitmap.createBitmap(
                intData,
                0,
                bitmap.width,
                bitmap.width,
                bitmap.height,
                Bitmap.Config.ARGB_8888
            )
        } else {
            BitmapFactory.decodeStream(inputStream)
        }
    }
}
