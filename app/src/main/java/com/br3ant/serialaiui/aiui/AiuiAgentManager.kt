package com.br3ant.serialaiui.aiui

import android.content.Context
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.LogUtils
import com.br3ant.utils.GsonUtil
import com.iflytek.aiui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * 文档：https://doc.iflyos.cn/aiui/sdk/mobile_doc/#sdk%E4%BB%8B%E7%BB%8D
 * AIUI 提供了流式tts方案，无时间戳数据。可采用流式方式ASR、Align方案，此处采用流式ASR方式
 *
 * @author Richie on 2019.05.17
 */
class AiuiAgentManager(context: Context) : AIUIListener {
    private lateinit var mAIUIAgent: AIUIAgent
    private val mContext: Context = context.applicationContext

    private var mPendingWork: Runnable? = null
    private var mAiuiAgentCallback: AiuiAgentCallback? = null
    private val mExecutor: Executor = Executors.newSingleThreadExecutor()
    private val mStartTimeNano: Long = 0
    private val scope = MainScope() + Dispatchers.IO

    fun setAiuiAgentCallback(aiuiAgentCallback: AiuiAgentCallback?) {
        mAiuiAgentCallback = aiuiAgentCallback
    }

    fun createAgent() {
        AIUIAgent.setSystemInfo(AIUIConstant.KEY_SERIAL_NUM, CLIENT_DEVICE_ID)
        // when createAgent called, agent is in ready state
        mAIUIAgent = AIUIAgent.createAgent(mContext, getAIUIParams(), this)
        getStatus()
        LogUtils.iTag(TAG, "android =$CLIENT_DEVICE_ID")
    }

    /**
     * 语音对话
     *
     * @param audioData
     */
    fun writeAudio(audioData: ByteArray?) {
        mPendingWork = Runnable {
            val writeAudio = AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, AUDIO_PARAMS, audioData)
            mAIUIAgent.sendMessage(writeAudio)
            val stopWrite = AIUIMessage(AIUIConstant.CMD_STOP_WRITE, 0, 0, AUDIO_PARAMS, null)
            mAIUIAgent.sendMessage(stopWrite)
        }
        getStatus()
    }

    /**
     * 文本对话
     *
     * @param text
     */
    fun writeText(text: String) {
        mPendingWork = Runnable {
            val writeText =
                AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, TEXT_PARAMS, text.toByteArray())
            mAIUIAgent.sendMessage(writeText)
        }
        getStatus()
    }

    /**
     * 合成语音，生成 PCM 格式
     *
     * @param text
     */
    fun startTts(text: String) {
        mPendingWork = Runnable {
            val params = "vcn=xiaoyan" +  //合成发音人
                    ",speed=50" +  //合成速度
                    ",pitch=50" +  //合成音调
                    ",volume=50" //合成音量
            val startTts = AIUIMessage(
                AIUIConstant.CMD_TTS, AIUIConstant.START, 0,
                params, text.toByteArray()
            )
            mAIUIAgent.sendMessage(startTts)
        }
        getStatus()
    }

    fun sendMessage(aiuiMessage: AIUIMessage?) {
        mAIUIAgent.sendMessage(aiuiMessage)
    }

    fun settingAgent() {}
    fun destroy() {
        mAIUIAgent.destroy()
    }

    override fun onEvent(aiuiEvent: AIUIEvent) {
        LogUtils.vTag(
            TAG,
            "onEvent. type: = ${aiuiEvent.eventType}, args1 = ${aiuiEvent.arg1}, args2 = ${aiuiEvent.arg2}, info:=${aiuiEvent.info}"
        )
        when (aiuiEvent.eventType) {
            AIUIConstant.EVENT_STATE -> {
                // 1:idle, 2:ready, 3:working
                LogUtils.i("state: = ${aiuiEvent.arg1}")
                when (aiuiEvent.arg1) {
                    AIUIConstant.STATE_WORKING -> {
                        if (mPendingWork != null) {
                            LogUtils.i("working {}", mPendingWork)
                            mPendingWork!!.run()
                            mPendingWork = null
                        }
                    }
                    AIUIConstant.STATE_IDLE -> {
                        LogUtils.i("now idle, send start")
                        mAIUIAgent.sendMessage(createMessageByType(AIUIConstant.CMD_START))
                    }
                    AIUIConstant.STATE_READY -> {
                        LogUtils.i("now ready, send wakeup")
                        mAIUIAgent.sendMessage(createMessageByType(AIUIConstant.CMD_WAKEUP))
                    }
                    else -> {
                    }
                }
            }
            AIUIConstant.EVENT_RESULT -> {
                mExecutor.execute {
                    try {
                        processResult(aiuiEvent)
                    } catch (e: Exception) {
                        LogUtils.e(e)
                        mAiuiAgentCallback?.onFailure(e.message)
                    }
                }
            }
            else -> {
            }
        }
    }

    @Throws(Exception::class)
    private fun processResult(event: AIUIEvent) {
        val data = JSONObject(event.info).optJSONArray("data").optJSONObject(0)
        val sub = data.optJSONObject("params").optString("sub")
        // iat 听写结果，nlp 语义结果，tts 云端TTS结果
//        LogUtils.debug("sub:{}", sub);
        val content = data.optJSONArray("content").optJSONObject(0)
        if (content.has("cnt_id")) {
            val cntId = content.optString("cnt_id")
            if ("tts" == sub) {

            } else if ("tpp" == sub) {
                val sid = event.data.getString("sid")
                mAiuiAgentCallback?.onAudioSuccess(JSONArray(), "", sid)

            } else {
                val result = JSONObject(String(event.data.getByteArray(cntId)!!))
                LogUtils.iTag(TAG, "sub = $sub", "result = $result")
                if (result.has("intent")) {
                    val intent = result.optJSONObject("intent")
                    if (intent.has("answer")) {
                        val answer = intent.optJSONObject("answer")
                        if (answer.has("text")) {
                            val text = answer.optString("text")
                            LogUtils.i("text:{}", text)
                            startTts(text)
                            mAiuiAgentCallback?.onNlpSucceed(text)
                        }
                    }
                    val service = intent.optString("service")
                    if (service == "musicX") {
                        if (intent.has("data")) {
                            val data = intent.optJSONObject("data")
                            if (data.has("result")) {
                                val result = data.optJSONArray("result")
                                if (result.length() > 0) {
                                    val sid = intent.optString("sid")
                                    mAiuiAgentCallback?.onAudioSuccess(result, service, sid)
                                }
                            }
                        }
                    }
                } else if (result.has("text")) {
                    val text = result.optJSONObject("text")
                    val iatResult = GsonUtil.json2Bean(text.toString(), IatResult::class.java)
                    val iatContent = iatResult.content
                    LogUtils.i("iatContent: = $iatContent")
                    mAiuiAgentCallback?.onAsrSucceed(iatContent)
                }
            }
        }
    }

//    private fun queryExpressions(pcmBytes: ByteArray?, audioProgressType: FUAudioProgressType) {
//        // audio/L16;rate=16000. 单声道 16位精度 16k采样率
//
////        scope.launch {
////            if (audioProgressType == FUAudioProgressType.AUDIO_SINGLE) {
//////                PcmToWav.makePcmStreamToWavStream(pcmBytes, true)
//////                val wav = PcmToWav.convertPcmToWav()
//////                LogUtils.iTag("保存wav = $wav")
////            } else if (audioProgressType == FUAudioProgressType.AUDIO_START) {
////                LogUtils.iTag("保存wav", "start")
////                PcmToWav.makePcmStreamToWavStream(pcmBytes, true)
////            } else if (audioProgressType == FUAudioProgressType.AUDIO_END) {
////                PcmToWav.makePcmStreamToWavStream(pcmBytes, false)
////                val wav = PcmToWav.convertPcmToWav()
////                LogUtils.iTag("保存wav", wav)
////            } else {
////                PcmToWav.makePcmStreamToWavStream(pcmBytes, false)
////            }
////        }
//
//
//        val startTime = System.nanoTime()
//        val params = FUParams()
//            .setStreamMode(1)
//            .setAudioProgressType(audioProgressType)
//            .setAudioData(pcmBytes)
//            .setAudioType(FUAudioType.PCM)
//            .setTimestampType(FUTimestampType.PHONE)
//        StaUnityUtils.getInstance().fuStaEngine.startStaDrivingProcess(params)
//        val duration = (System.nanoTime() - startTime) / 1000000
//        LogUtils.iTag(TAG, "tts duration: ${duration}ms")
//    }

    fun getStatus() {
        mAIUIAgent.sendMessage(createMessageByType(AIUIConstant.CMD_GET_STATE))
    }

    private fun createMessageByType(type: Int): AIUIMessage {
        return AIUIMessage(type, 0, 0, null, null)
    }

    // ignored
    private fun getAIUIParams(): String {
        var params = ""
        val assetManager = mContext.assets
        var ins: InputStream? = null
        try {
            ins = assetManager.open("cfg/aiui_phone.cfg")
            val buffer = ByteArray(ins.available())
            ins.read(buffer)
            ins.close()
            params = String(buffer)
            val paramsJson = JSONObject(params)
            params = paramsJson.toString()
        } catch (e: IOException) {
            LogUtils.e(e)
        } catch (e: JSONException) {
            LogUtils.e(e)
        } finally {
            if (ins != null) {
                try {
                    ins.close()
                } catch (e: IOException) {
                    // ignored
                }
            }
        }
        return params
    }

    fun startRecord() {
        //开始录音
        val msg = AIUIMessage(
            AIUIConstant.CMD_START_RECORD,
            0,
            0,
            "data_type=audio,sample_rate=16000",
            null
        )
        mAIUIAgent.sendMessage(msg)
    }

    fun stopRecord() {
        //停止录音
        val msg = AIUIMessage(
            AIUIConstant.CMD_STOP_RECORD,
            0,
            0,
            "data_type=audio,sample_rate=16000",
            null
        )
        mAIUIAgent.sendMessage(msg)
    }

    fun sendWeakUp() {
        mAIUIAgent.sendMessage(createMessageByType(AIUIConstant.CMD_WAKEUP))
    }

    interface AiuiAgentCallback {
        /**
         * 语音识别结果
         *
         * @param content
         */
        fun onAsrSucceed(content: String?)

        /**
         * 语义处理结果
         *
         * @param answer
         */
        fun onNlpSucceed(answer: String?)

        /**
         * 语音合成结果
         *
         * @param pcmBytes
         * @param expressionList
         */
        fun onTtsSucceed(pcmBytes: ByteArray?, expressionList: List<FloatArray?>?)

        fun onAudioSuccess(data: JSONArray, service: String, sid: String? = "")

        /**
         * 发生错误
         *
         * @param errorMessage
         */
        fun onFailure(errorMessage: String?)
    }

    companion object {
        private const val AUDIO_PARAMS = "data_type=audio,sample_rate=16000"
        private const val TEXT_PARAMS = "data_type=text"
        const val APP_ID = "5abcfb32"
        const val APP_KEY = "f3bd8ae5824ca84a7733886e0e223457"
        val CLIENT_DEVICE_ID: String by lazy {
            DeviceUtils.getUniqueDeviceId()
        }
        private const val TAG = "AiuiAgentManager"
    }

}