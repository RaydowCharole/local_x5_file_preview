@file:Suppress("INACCESSIBLE_TYPE")

package com.cjx.x5_webview

//import android.content.ContextWrapper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.*
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.io.File
//import java.util.*
//import androidx.core.content.ContextCompat
//import androidx.core.*

class X5WebViewPlugin(var context: Context, var activity: Activity) : MethodCallHandler {

    private var readerView: TbsReaderView? = null

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "com.cjx/x5Video")
            channel.setMethodCallHandler(X5WebViewPlugin(registrar.context(), registrar.activity()))
            setCallBack(channel)

            registrar.platformViewRegistry().registerViewFactory("com.cjx/x5WebView", X5WebViewFactory(registrar.messenger(), registrar.activeContext()))

        }

        private fun setCallBack(channel: MethodChannel) {

        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "init" -> {
                val canLoad = QbSdk.canLoadX5(context.applicationContext)
                if (!canLoad || QbSdk.getTbsVersion(context.applicationContext) < 46007) {
                    FileUtils.copyAssets(context.applicationContext, "046007_x5.tbs.apk", FileUtils.getTBSFileDir(context.applicationContext).path + "/046007_x5.tbs.apk")
                }
                val map = HashMap<String, Any>(2)
                map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
                map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
                QbSdk.initTbsSettings(map)
                println("canLoadX5: " + canLoad + " | TbsVersion:" + QbSdk.getTbsVersion(context.applicationContext))
                if (canLoad) {
                    result.success(true)
                    return
                }
                QbSdk.reset(context.applicationContext)
                QbSdk.setTbsListener(object : TbsListener {
                    override fun onDownloadFinish(i: Int) {}
                    override fun onInstallFinish(i: Int) {
                        println("onInstallFinish: $i")
//                        Log.e(MainActivity.TAG, "onInstallFinish: $i")
                        val tbsVersion = QbSdk.getTbsVersion(context.applicationContext)
                        println("tbsVersion: $tbsVersion")
//                        Log.e(MainActivity.TAG, "tbsVersion: $tbsVersion")

                    }

                    override fun onDownloadProgress(i: Int) {}
                })
                QbSdk.reset(context.applicationContext)
                QbSdk.installLocalTbsCore(context.applicationContext, 46007, FileUtils.getTBSFileDir(context.applicationContext).path + "/046007_x5.tbs.apk")

                //        boolean needDownload = TbsDownloader.needDownload(this,TbsDownloader.DOWNLOAD_OVERSEA_TBS);
//        Log.e(TAG,"onCreate: "+needDownload);
//        if(needDownload){
//            TbsDownloader.startDownload(this);
//        }
                //动态下载内核安装
//        QbSdk.initX5Environment(getApplicationContext(), new QbSdk.PreInitCallback() {
//            @Override
//            public void onCoreInitFinished() {
//                Toast.makeText(MainActivity.this, "加载完成", Toast.LENGTH_SHORT);
//            }
//
//            @Override
//            public void onViewInitFinished(boolean b) {
//
//            }
//        });
//                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                //验证是否许可权限
//                for (str in permissions) {
//                    if (checkSelfPermission(str) !== PackageManager.PERMISSION_GRANTED) {
//                        //申请权限
//                        requestPermissions(permissions, this.REQUEST_CODE_CONTACT)
//                        return
//                    }
//                }

                // TODO:
//                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                for (str in permissions) {
//                    if (ContextCompat.checkSelfPermission(this,str )) {
//                        //申请权限
////                        requestPermissions(permissions, this.REQUEST_CODE_CONTACT)
//                        return
//                    }
//                }

                result.success(true)

//                QbSdk.initX5Environment(context.applicationContext, object : QbSdk.PreInitCallback {
//                    override fun onCoreInitFinished() {
//
//                    }
//
//                    override fun onViewInitFinished(p0: Boolean) {
//                        //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
//                        result.success(p0)
//                    }
//
//                })
            }
            "canUseTbsPlayer" -> {
                //返回是否可以使用tbsPlayer
                result.success(TbsVideo.canUseTbsPlayer(context))
            }
            "openVideo" -> {
                val url = call.argument<String>("url")
                val screenMode = call.argument<Int>("screenMode") ?: 103
                val bundle = Bundle()
                bundle.putInt("screenMode", screenMode)
                TbsVideo.openVideo(context, url, bundle)
                result.success(null)
            }
            "openFile" -> {
                var filePath = call.argument<String>("filePath")
                val params = hashMapOf<String, String>()
                params["local"] = call.argument<String>("local") ?: "false"
                params["style"] = call.argument<String>("style") ?: "0"
                params["topBarBgColor"] = call.argument<String>("topBarBgColor") ?: "#2CFC47"
                var menuData = call.argument<String>("menuData")
                if (menuData != null) {
                    params["menuData"] = menuData
                }
                if (!File(filePath).exists()) {
                    Toast.makeText(context, "文件不存在,请确认$filePath 是否正确", Toast.LENGTH_LONG).show()
                    result.success("文件不存在,请确认$filePath 是否正确")
                    return
                }
                QbSdk.canOpenFile(activity, filePath) { canOpenFile ->
                    val intent = Intent(activity, FileActivity::class.java)
//                    filePath=FileUtils.getTBSFileDir(context.applicationContext).getPath() +"/"+filePath
                    intent.putExtra("filepath", filePath);
                    activity.startActivity(intent)
                }

            }

            "openWebActivity" -> {
                val url = call.argument<String>("url")
                val title = call.argument<String>("title")
                val intent = Intent(activity, X5WebViewActivity::class.java)
                intent.putExtra("url", url)
                intent.putExtra("title", title)
                activity.startActivity(intent)
                result.success(null)
            }
            "getCarshInfo" -> {
                val info = WebView.getCrashExtraMessage(context)
                result.success(info)
            }
            "setDownloadWithoutWifi" -> {
                val isWithoutWifi = call.argument<Boolean>("isWithoutWifi")
                QbSdk.setDownloadWithoutWifi(isWithoutWifi ?: false)
                result.success(null)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

}