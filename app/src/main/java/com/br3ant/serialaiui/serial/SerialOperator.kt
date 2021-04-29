package com.br3ant.serialaiui.serial

import com.blankj.utilcode.util.LogUtils
import com.br3ant.serialaiui.utils.BytesArrayUtils
import com.firefly.api.serialport.SerialPort
import java.io.File

class SerialOperator {
    private var mSerialPort: SerialPort? = null
    private var data: String = ""

    fun openSerialPort(path: String, baudrate: Int, callback: (String) -> Unit): Boolean {
        closeSerialPort()
        try {
            mSerialPort = SerialPort(File(path), baudrate, 0)
            mSerialPort!!.setCallback { bytes: ByteArray, size: Int ->
                val readBytes = bytes.copyOf(size)
                val hexString = BytesArrayUtils.bytesToHexString(readBytes)
                if (hexString.startsWith("fe")) {
                    data = hexString
                } else {
                    data += hexString
                }
                if (data.endsWith("fe00")) {
                    val dataBytes = BytesArrayUtils.hexStringToBytes(data)
                    if (dataBytes.size > 6) {
                        callback.invoke(String(dataBytes, 4, data.substring(2, 6).toInt(16)))
                    }
                }
                LogUtils.iTag("SerialOperator", data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun closeSerialPort() {
        mSerialPort?.closeSerialPort()
        mSerialPort = null
    }
}