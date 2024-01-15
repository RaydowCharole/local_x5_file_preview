package com.gstory.file_preview.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * @Author: gstory
 * @CreateDate: 2021/12/27 11:02 上午
 * @Description: 描述
 */

object FileUtils {

    /**
     * 获取缓存目录
     * /data/data/<package_name>/files/file_preview
     */
    fun getDir(context: Context): File {
        val dir = File(context.filesDir, "file_preview")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getTbsCoreDest(fileName: String, context: Context): String {
        return getDir(context).path + File.separator + fileName
    }

    fun getTbsReaderTemp(context: Context): String {
        return getDir(context).path + File.separator + "TbsReaderTemp"
    }

    fun copyTbsCoreFromAssets(fileName: String, context: Context): Boolean {
        val toPath = getTbsCoreDest(fileName, context)
        var file: File? = null
        try {
            // 目录存在，则将apk中raw中的需要的文档复制到该目录下
            file = File(toPath)
            if (!file!!.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            if (!file.exists()) { // 文件不存在
                println("要打开的文件不存在")
                val ins = context.resources.assets.open(fileName)
                println("开始读入")
                val fos = FileOutputStream(file)
                println("开始写出")
                val buffer = ByteArray(8192)
                var count = 0 // 循环写出
                while (ins.read(buffer).also { count = it } > 0) {
                    fos.write(buffer, 0, count)
                }
                println("已经创建该文件")
                fos.close() // 关闭流
                ins.close()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return file != null && file.exists()
    }

    /**
     * 删除缓存文件夹
     */
    fun deleteCache(context: Context, dir: File) {
        if (!dir.exists() || !dir.isDirectory) {
            return
        }
        for (file in dir.listFiles()!!) {
            if (file.isFile) file.delete()
            else if (file.isDirectory) deleteCache(context, file)
        }
        dir.delete()
    }


    /**
     * 获取文件格式
     */
    fun getFileType(paramString: String): String {
        var str = ""
        if (TextUtils.isEmpty(paramString)) {
            return str
        }
        val i = paramString.lastIndexOf('.')
        if (i <= -1) {
            return str
        }
        str = paramString.substring(i + 1)
        Log.d("FileUtils", "当前文件格式$str")
        return str
    }

    /**
     * 下载文件
     */
    fun downLoadFile(context: Context, downloadUrl: String, callback: DownloadCallback) {
        var saveFile: File? = null
        Log.e("saveFile===+>", "$downloadUrl")
        Thread {
            // 流和链接
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null
            var connection: HttpURLConnection? = null
            // 下载准备
            var downloadedSize = 0 // 已经下载的文件大小
            var fileTotalSize = 0 // 文件总大小

            // 开始链接
            try {
                val url = URL(downloadUrl)
                connection = url.openConnection() as HttpURLConnection?
                connection?.connectTimeout = 10 * 1000;
                connection?.readTimeout = 10 * 1000;
                connection?.connect();
                //储存文件
                saveFile =
                        File("${getDir(context)}${File.separator}${downloadUrl.hashCode()}${connection?.fileExt()}")
                Log.e("saveFile===+>", "$saveFile")
                //如果文件已存在 不再下载 直接读取展示
                if (saveFile!!.exists()) {
                    callback.onFinish(saveFile!!)
                } else {
                    // 获取要下载的文件信息
                    fileTotalSize = connection?.contentLength!!       // 文件总大小
                    inputStream = connection.inputStream
                    outputStream = FileOutputStream(saveFile)
                    val buffer = ByteArray(1024 * 4)
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } > 0) {
                        outputStream.write(buffer, 0, len);
                        downloadedSize += len;
                        // 计算文件下载进度
                        val progress: Int = (downloadedSize * 1.0f / fileTotalSize * 100).toInt()
                        callback.onProgress(progress)
                    }
                    // 下载成功
                    callback.onFinish(saveFile!!)
                }
            } catch (e: Exception) {
                Log.e("download error", "$e")
                if (saveFile?.exists() == true) {
                    if (saveFile!!.delete()) {
                        callback.onFail("下载失败$e")
                    } else {
                        callback.onFail("下载失败$e")
                    }
                } else {
                    callback.onFail("下载失败$e")
                }
            } finally {
                try {
                    inputStream?.close();
                    outputStream?.close();
                    connection?.disconnect();
                } catch (e: Exception) {
                    callback.onFail("IO流关闭失败$e")
                }
            }
        }.start()
    }

    interface DownloadCallback {
        fun onProgress(progress: Int)
        fun onFail(msg: String)
        fun onFinish(file: File)
    }
}