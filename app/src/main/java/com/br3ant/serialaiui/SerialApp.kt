package com.br3ant.serialaiui

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.utils.BRV
import com.tencent.bugly.Bugly

/**
 * @author LiuQiang on 2018.10.24
 */
class SerialApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Utils.init(this)

        Bugly.init(applicationContext, "c58510f4de", false)
//        BRV.modelId = BR.m
    }
}
