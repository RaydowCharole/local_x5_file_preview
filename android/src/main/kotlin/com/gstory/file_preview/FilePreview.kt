package com.gstory.file_preview

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.example.flutter_pangrowth.utils.UIUtils
import com.gstory.file_preview.utils.FileUtils
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.TbsReaderView
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import java.io.File


/**
 * @Author: gstory
 * @CreateDate: 2021/12/27 10:34 上午
 * @Description: 描述
 */

internal class FilePreview(
        context: Context,
        var activity: Activity,
        messenger: BinaryMessenger,
        id: Int,
        params: Map<String?, Any?>
) :
        PlatformView, MethodChannel.MethodCallHandler {

    private val TAG = "FilePreview"

    private var mContainer: FrameLayout = FrameLayout(activity)
    private var width: Double = params["width"] as Double
    private var height: Double = params["height"] as Double
    private var path: String = params["path"] as String

    private var tbsReaderView: TbsReaderView? = null

    private var channel: MethodChannel?

    private var readerCallback = object : TbsReaderView.ReaderCallback {
        override fun onCallBackAction(p0: Int?, p1: Any?, p2: Any?) {
//            Log.e(TAG, "文件打开回调$p0 $p1 $p2")
        }
    }

    init {
        channel = MethodChannel(messenger, "com.gstory.file_preview/filePreview_$id")
        channel?.setMethodCallHandler(this)

        mContainer.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT)
        mContainer.setBackgroundColor(Color.parseColor("#FFFFFF"))

        initTbsReaderView(context, activity)

        loadFile(path)
    }

    override fun getView(): View {
        return mContainer
    }

    private fun initTbsReaderView(context: Context, activity: Activity) {
        //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
        val tbsReaderTemp = FileUtils.getTbsReaderTemp(context)
        val tbsReaderTempFile = File(tbsReaderTemp)
        if (!tbsReaderTempFile.exists()) {
            Log.e("initTbsReaderView", "准备创建TbsReaderTemp")
            val mkdir = tbsReaderTempFile.mkdir()
            if (!mkdir) {
                Log.e("initTbsReaderView", "创建TbsReaderTemp失败！！！！！")
            }
        }

        mContainer.removeAllViews()
        if (tbsReaderView != null) {
            tbsReaderView?.onStop()
            tbsReaderView = null
        }

        QbSdk.clearAllWebViewCache(context, true)
        tbsReaderView = TbsReaderView(activity) { _, _, _ -> }

        mContainer.addView(tbsReaderView)
    }

    private fun loadFile(filePath: String) {
        mContainer.removeAllViews()
        if (tbsReaderView != null) {
            tbsReaderView?.onStop()
            tbsReaderView = null
        }
        tbsReaderView = TbsReaderView(activity, readerCallback)
        tbsReaderView?.layoutParams?.width = (UIUtils.dip2px(activity, width.toFloat())).toInt()
        tbsReaderView?.layoutParams?.height = (UIUtils.dip2px(activity, height.toFloat())).toInt()
        mContainer.addView(tbsReaderView)
        if (!TbsManager.instance.isInit) {
            val map: MutableMap<String, Any?> = mutableMapOf("code" to 1004, "msg" to "TBS未初始化")
            channel?.invokeMethod("onFail", map)
            return
        }
        //tbs只能加载本地文件 如果是网络文件则先下载
        if (filePath.startsWith("http")) {
            FileUtils.downLoadFile(activity, filePath, object : FileUtils.DownloadCallback {
                override fun onProgress(progress: Int) {
//                    Log.e(TAG, "文件下载进度$progress")
                    activity.runOnUiThread {
                        val map: MutableMap<String, Any?> = mutableMapOf("progress" to progress)
                        channel?.invokeMethod("onDownload", map)
                    }
                }

                override fun onFail(msg: String) {
                    Log.e(TAG, "文件下载失败$msg")
                    activity.runOnUiThread {
                        val map: MutableMap<String, Any?> = mutableMapOf("code" to 1000, "msg" to msg)
                        channel?.invokeMethod("onFail", map)
                    }
                }

                override fun onFinish(file: File) {
                    Log.e(TAG, "文件下载完成！")
                    activity.runOnUiThread {
                        openFile(file)
                    }
                }

            })
        } else {
            openFile(File(filePath))
        }
    }

    /**
     * 打开文件
     */
    private fun openFile(file: File?) {
        if (file != null && !TextUtils.isEmpty(file.toString())) {
            //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
            val bsReaderTemp = FileUtils.getDir(activity).toString() + File.separator + "TbsReaderTemp"
            val bsReaderTempFile = File(bsReaderTemp)
            if (!bsReaderTempFile.exists()) {
                val mkdir: Boolean = bsReaderTempFile.mkdir()
                if (!mkdir) {
                    Log.e(TAG, "创建$bsReaderTemp 失败")
                    val map: MutableMap<String, Any?> = mutableMapOf("code" to 1001, "msg" to "文件下载失败")
                    channel?.invokeMethod("onFail", map)
                }
            }
            //加载文件
            val localBundle = Bundle()
            Log.d(TAG, file.toString())
            localBundle.putString("filePath", file.toString())
            localBundle.putString("tempPath", bsReaderTemp)
            val bool = tbsReaderView?.preOpen(FileUtils.getFileType(file.toString()), false)
            if (bool == true) {
                tbsReaderView?.openFile(localBundle)
                val map: MutableMap<String, Any?> = mutableMapOf()
                channel?.invokeMethod("onShow", map)
            } else {
                Log.e(TAG, "文件打开失败！")
                val map: MutableMap<String, Any?> = mutableMapOf("code" to 1002, "msg" to "文件格式不支持或者打开失败")
                channel?.invokeMethod("onFail", map)
            }
        } else {
            Log.e(TAG, "文件路径无效！")
            val map: MutableMap<String, Any?> = mutableMapOf("code" to 1003, "msg" to "本地文件路径无效")
            channel?.invokeMethod("onFail", map)
        }
    }

    override fun dispose() {
        tbsReaderView?.onStop()
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if ("showFile" == call.method) {
            val path = call.arguments as String
            loadFile(path)
        }
    }

}