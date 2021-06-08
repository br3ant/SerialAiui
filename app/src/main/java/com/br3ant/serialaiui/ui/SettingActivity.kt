package com.br3ant.serialaiui.ui

import android.text.InputType
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.blankj.utilcode.util.AppUtils
import com.br3ant.base.BaseActivity
import com.br3ant.serialaiui.R
import com.br3ant.serialaiui.databinding.ActivitySettingBinding
import com.br3ant.serialaiui.utils.PreHolder
import com.hi.dhl.binding.viewbind

class SettingActivity : BaseActivity(R.layout.activity_setting) {
    private val binding: ActivitySettingBinding by viewbind()

    override fun initView() {
        binding.tvSerialPath.text = PreHolder.serialPath
        binding.tvSerialPort.text = PreHolder.serialPort
        binding.swOneshot.isClickable = PreHolder.oneshot


        binding.tvSerialPath.setOnClickListener {
            MaterialDialog(this).show {
                input(hint = PreHolder.serialPath) { _, text ->
                    binding.tvSerialPath.text = text
                    PreHolder.serialPath = text.toString().trim()
                }
                positiveButton(R.string.done)
            }
        }


        binding.tvSerialPort.setOnClickListener {
            MaterialDialog(this).show {
                input(hint = PreHolder.serialPort, inputType = InputType.TYPE_NUMBER_VARIATION_NORMAL) { _, text ->
                    binding.tvSerialPort.text = text
                    PreHolder.serialPort = text.toString().trim()
                }
                positiveButton(R.string.done)
            }
        }

        binding.swOneshot.setOnCheckedChangeListener { _, isChecked ->
            PreHolder.oneshot = isChecked
        }

        binding.btnInit.setOnClickListener {
            PreHolder.serialPath = "/dev/ttyS4"
            PreHolder.serialPort = "115200"
            PreHolder.oneshot = true

            binding.tvSerialPath.text = PreHolder.serialPath
            binding.tvSerialPort.text = PreHolder.serialPort
            binding.swOneshot.isClickable = PreHolder.oneshot
        }

        binding.btnRestart.setOnClickListener {
            AppUtils.relaunchApp(true)
        }
    }


}