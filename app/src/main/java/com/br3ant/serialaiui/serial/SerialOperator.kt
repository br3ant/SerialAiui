package com.br3ant.serialaiui.serial

import com.blankj.utilcode.util.LogUtils
import com.br3ant.serialaiui.utils.BytesArrayUtils
import com.firefly.api.serialport.SerialPort
import java.io.File

class SerialOperator {
    private var mSerialPort: SerialPort? = null
    private var data: ByteArray = byteArrayOf()

    fun openSerialPort(path: String, baudrate: Int, callback: (String) -> Unit): Boolean {
        closeSerialPort()
        try {
            mSerialPort = SerialPort(File(path), baudrate, 0)
            mSerialPort!!.setCallback { bytes: ByteArray, size: Int ->
                val readBytes = bytes.copyOf(size)
                val hexString = BytesArrayUtils.bytesToHexString(readBytes)
                if (hexString.startsWith("fe")) {
                    data = byteArrayOf(*readBytes)
                } else {
                    data += readBytes
                }
                if (hexString.endsWith("fe00")) {
                    callback.invoke(String(data.copyOfRange(4, data.size - 2)))
                }
                LogUtils.iTag("SerialOperator", hexString)
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