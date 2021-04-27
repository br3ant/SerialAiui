package com.br3ant.serialaiui.serial

import com.firefly.api.serialport.SerialPort
import java.io.File
import java.io.IOException

/**
 * <pre>
 *     copyright: mukun
 *     @author : br3ant
 *     e-mail : xxx@xx
 *     time   : 2021 04 4/23/21
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class SerialPortIml(device: File, baudrate: Int, flags: Int, private val callback: (String) -> Unit) : SerialPort(device, baudrate, flags) {
    private val readThread: Thread = ReadThread()

    init {
        readThread.start()
    }


    override fun closeSerialPort() {
        super.closeSerialPort()
        readThread.interrupt()
    }

    private inner class ReadThread : Thread() {
        override fun run() {
            super.run()
            while (!this.isInterrupted) {
                try {
                    val buffer = ByteArray(1024)
                    if (inputStream == null) {
                        return
                    }
                    val size: Int = inputStream.read(buffer)
                    if (size > 0) {
                        callback.invoke(String(buffer))
                    }
                } catch (var3: IOException) {
                    var3.printStackTrace()
                    return
                }
            }
        }
    }
}