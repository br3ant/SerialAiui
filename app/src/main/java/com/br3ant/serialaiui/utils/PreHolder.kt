package com.br3ant.serialaiui.utils

import com.br3ant.utils.PreferenceUtil

/**
 * <pre>
 *     copyright: mukun
 *     @author : br3ant
 *     e-mail : xxx@xx
 *     time   : 2021 06 2021/6/8
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object PreHolder {
    var serialPath by PreferenceUtil("serialPath", "/dev/ttyS4")
    var serialPort by PreferenceUtil("serialPort", "115200")
    var oneshot by PreferenceUtil("oneshot", true)
}