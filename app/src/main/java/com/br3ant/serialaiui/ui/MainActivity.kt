package com.br3ant.serialaiui.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Environment
import androidx.lifecycle.rxLifeScope
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.br3ant.base.BaseActivity
import com.br3ant.serialaiui.R
import com.br3ant.serialaiui.aiui.AiuiAgentManager
import com.br3ant.serialaiui.databinding.ActivityMainBinding
import com.br3ant.serialaiui.serial.SerialData
import com.br3ant.serialaiui.serial.SerialManager
import com.br3ant.serialaiui.utils.PreHolder
import com.br3ant.utils.toBean
import com.drake.brv.utils.addModels
import com.drake.brv.utils.setup
import com.hi.dhl.binding.viewbind
import org.json.JSONArray

class MainActivity : BaseActivity(R.layout.activity_main) {
    private val binding: ActivityMainBinding by viewbind()

    private lateinit var mAiuiAgentManager: AiuiAgentManager


    override fun initView() {
        binding.rvInfo.setup {
            addType<String>(R.layout.item_aiui_info)
        }.models = emptyList()

        PermissionUtils.permission(PermissionConstants.STORAGE, PermissionConstants.MICROPHONE)
            .callback { isAllGranted, _, _, _ ->
                if (isAllGranted) {
                    initLog()
                    initSerialOperator()
                    initAiuiManager()
                } else {
                    finish()
                }
            }.request()
        binding.btnAction.setOnClickListener {
            writeText()
        }
        binding.btnSetting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
    }

    private fun addInfo(info: String?) {
        rxLifeScope.launch {
            binding.rvInfo.addModels(listOf(info))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mAiuiAgentManager.destroy()
    }

    private fun initLog() {
        LogUtils.getConfig().isLog2FileSwitch = true
        LogUtils.getConfig().dir = Environment.getExternalStorageDirectory().absolutePath + "/br3ant"
        FileUtils.deleteAllInDir(LogUtils.getConfig().dir)
        LogUtils.iTag("hqq", LogUtils.getConfig().dir)
    }

    private fun initAiuiManager() {
        mAiuiAgentManager = AiuiAgentManager(this)
        mAiuiAgentManager.createAgent()
        mAiuiAgentManager.setAiuiAgentCallback(aiuiCallback)

    }

//    override fun onResume() {
//        super.onResume()
//        mAiuiAgentManager.startRecord()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        mAiuiAgentManager.stopRecord()
//    }

    private fun initSerialOperator() {

        val openResult = SerialManager.operator.openSerialPort(PreHolder.serialPath, PreHolder.serialPort.toIntOrNull() ?: 115200) {
            if (it.toBean<SerialData>()?.param1?.keyword == "xiao3 fei1 xiao3 fei1") {
                val message = "监测到串口唤醒指令"
                ToastUtils.showShort(message)
//                mAiuiAgentManager.stopRecord()
                mAiuiAgentManager.resetWakeup()
                startText("我在")
                mAiuiAgentManager.startRecord()

            } else {
                ToastUtils.showShort(it)
                LogUtils.iTag("hqq", "onDataReceived = $it")
                addInfo(it)
            }
        }
        val message = "串口path = ${PreHolder.serialPath};; port = ${PreHolder.serialPort} ;; 打开 ${if (openResult) "成功" else "失败"}"
        ToastUtils.showShort(message)
        addInfo(message)
    }

    private fun startText(text: String = "今天天气怎么样") {
        mAiuiAgentManager.startTts(text)

    }

    private fun writeText(text: String = "今天天气怎么样") {
        mAiuiAgentManager.writeText(text)
    }

    private fun getStatus() = mAiuiAgentManager.getStatus()

    @SuppressLint("SetTextI18n")
    private val aiuiCallback: AiuiAgentManager.AiuiAgentCallback =
        object : AiuiAgentManager.AiuiAgentCallback {

            override fun onAsrSucceed(content: String?) {
//                ToastUtils.showShort(content)
                LogUtils.iTag("hqq", "onAsrSucceed = $content")
                addInfo(content)

            }

            override fun onNlpSucceed(answer: String?) {
//            ToastUtils.showShort(answer)
                LogUtils.iTag("hqq", "onNlpSucceed = $answer")
                addInfo(answer)
            }

            override fun onTtsSucceed(pcmBytes: ByteArray?, expressionList: List<FloatArray?>?) {
                LogUtils.d(
                    "onTtsSucceed. pcmBytes:{}, expressionList size:{}",
                    pcmBytes?.size ?: 0,
                    expressionList?.size ?: 0
                )
            }

            override fun onAudioSuccess(data: JSONArray, service: String, sid: String?) {
                LogUtils.iTag("hqq", "onAudioSuccess = $data")
            }


            override fun onFailure(errorMessage: String?) {
                LogUtils.iTag("hqq", "onFailure = $errorMessage")
                ToastUtils.showShort(errorMessage)
            }
        }
}