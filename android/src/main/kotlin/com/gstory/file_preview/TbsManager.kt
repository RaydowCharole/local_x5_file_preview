package com.gstory.file_preview

import android.content.Context
import android.util.Log
import com.gstory.file_preview.utils.FileUtils
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.TbsListener
import kotlin.collections.HashMap

class TbsManager private constructor() {

    var isInit: Boolean = false


    companion object {
        val instance: TbsManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TbsManager()
        }
    }

    private fun initQbsdk(context: Context): Boolean {
        var canLoad = QbSdk.canLoadX5(context)
        println("canLoadX5: " + canLoad + " | TbsVersion:" + QbSdk.getTbsVersion(context))
        if (canLoad) {
            return true
        }

        val tbsCoreFileName = "046007_x5.tbs.apk"
        val tbsCoreVersion = 46007
        if (!canLoad || QbSdk.getTbsVersion(context) < tbsCoreVersion) {
            FileUtils.copyTbsCoreFromAssets(tbsCoreFileName, context)
        }
        //BS内核首次使用和加载时，ART虚拟机会将Dex文件转为Oat，该过程由系统底层触发且耗时较长，很容易引起anr问题，解决方法是使用TBS的 ”dex2oat优化方案“。
        // 在调用TBS初始化、创建WebView之前进行如下配置
        val map = HashMap<String, Any>(2)
        map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
        QbSdk.initTbsSettings(map)
        QbSdk.setTbsListener(object : TbsListener {
            override fun onDownloadFinish(i: Int) {
                //tbs内核下载完成回调
                Log.e("TBS内核", "下载结束 状态码$i")
            }

            override fun onInstallFinish(i: Int) {
                //内核安装完成回调，
                Log.e("TBS内核", "安装完成")
            }

            override fun onDownloadProgress(i: Int) {
                //下载进度监听
                Log.e("TBS内核", "下载进度 $i")
            }
        })
        QbSdk.reset(context)
        QbSdk.installLocalTbsCore(context, tbsCoreVersion, FileUtils.getTbsCoreDest(tbsCoreFileName, context))
        canLoad = QbSdk.canLoadX5(context)
        println("canLoadX5: " + canLoad + " | TbsVersion:" + QbSdk.getTbsVersion(context))

        return canLoad
    }

    fun initTBS(context: Context, callBack: InitCallBack?) {
        if (isInit) {
            callBack?.initFinish(true)
            return
        }
        isInit = initQbsdk(context)
        callBack?.initFinish(isInit)
        return
    }

}

interface InitCallBack {
    fun initFinish(b: Boolean)
}