package com.br3ant.serialaiui.ui

import android.annotation.SuppressLint
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.br3ant.base.BaseActivity
import com.br3ant.serialaiui.R
import com.br3ant.serialaiui.aiui.AiuiAgentManager
import com.br3ant.serialaiui.serial.SerialData
import com.br3ant.serialaiui.serial.SerialManager
import com.br3ant.utils.toBean
import org.json.JSONArray

class MainActivity : BaseActivity(R.layout.activity_main) {
//    private val binding by binding()

    private lateinit var mAiuiAgentManager: AiuiAgentManager
    private lateinit var tvInfo: TextView

    override fun initView() {
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
        findViewById<Button>(R.id.btn_action).setOnClickListener {
            writeText()
        }
        tvInfo = findViewById(R.id.tv_info)
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

        val openResult = SerialManager.operator.openSerialPort("/dev/ttyS4", 115200) {
            if (it.toBean<SerialData>()?.param1?.keyword == "xiao3 fei1 xiao3 fei1") {
                val message = "监测到串口唤醒指令"
                ToastUtils.showShort(message)
                getStatus()
                mAiuiAgentManager.startRecord()
            } else {
                ToastUtils.showShort(it)
                LogUtils.iTag("hqq", "onDataReceived = $it")
                tvInfo.text = it
            }
        }
        ToastUtils.showShort("串口打开 ${if (openResult) "成功" else "失败"}")
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
                tvInfo.text = tvInfo.text.toString() + content + "\n"
            }

            override fun onNlpSucceed(answer: String?) {
//            ToastUtils.showShort(answer)
                LogUtils.iTag("hqq", "onNlpSucceed = $answer")
                tvInfo.text = tvInfo.text.toString() + answer + "\n"
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