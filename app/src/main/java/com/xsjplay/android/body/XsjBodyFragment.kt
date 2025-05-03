package com.xsjplay.android.body

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.autojs.autojs.AutoJs.Companion.instance
import org.autojs.autojs.model.explorer.ExplorerDirPage
import org.autojs.autojs.model.explorer.ExplorerFileItem
import org.autojs.autojs.model.explorer.ExplorerItem
import org.autojs.autojs.model.script.ScriptFile
import org.autojs.autojs.model.script.Scripts
import org.autojs.autojs.pio.PFile
import org.autojs.autojs.theme.ThemeColorManagerCompat
import org.autojs.autojs.ui.explorer.ExplorerViewHelper
import org.autojs.autojs.ui.widget.FirstCharView
import org.autojs.autojs.util.ColorUtils
import org.autojs.autojs.util.FileUtils
import org.autojs.autojs.util.ViewUtils.showToast
import com.xsjplay.android.R
import java.io.File

class XsjBodyFragment : Fragment() {

    private lateinit var itemView: View
    private lateinit var mName: TextView
    private lateinit var mFileDate: TextView
    private lateinit var mFileSize: TextView
    private lateinit var mOptions: View
    private lateinit var mInstall: View
    private lateinit var mRun: View
    private lateinit var mStop: View
    private lateinit var mEdit: View
    private lateinit var mInfo: View
    private lateinit var mFirstChar: FirstCharView

    private lateinit var mExplorerItem: ExplorerItem




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.explorer_file_xsj, container, false).also {
            itemView = it.findViewById(R.id.item)
            mName = it.findViewById(R.id.name)
            mFileDate = it.findViewById(R.id.script_file_date)
            mFileSize = it.findViewById(R.id.script_file_size)
            mRun = it.findViewById(R.id.run)
            mRun.setOnClickListener { run() }

            mStop = it.findViewById(R.id.stop)
            mStop.setOnClickListener { stop() }
            mName.text = "Main"

            mExplorerItem = ExplorerFileItem(
                PFile(File(requireContext().filesDir, "sample/Main/main.js").absolutePath),
                ExplorerDirPage(File(requireContext().filesDir, "sample/Main"), null)
            )
            // Log.i("ExplorerFileItem11", mExplorerItem.path + " " + mExplorerItem.name + " " + mExplorerItem.type )
            // val firstCharIconBinding = ExplorerFirstCharIconBinding.bind(itemView)
            mFirstChar = itemView.findViewById(R.id.first_char)
            setFirstChar()

            updateVisibility(mRun, true)
            updateVisibility(mStop, true)
            writeMainJs()
        }
    }

    private fun setFirstChar() {
        mFirstChar.setIcon(ExplorerViewHelper.getIcon(mExplorerItem))
        when (mExplorerItem.type) {
            FileUtils.TYPE.JAVASCRIPT, FileUtils.TYPE.AUTO -> {
                val themeColorForContrast = ColorUtils.adjustColorForContrast(requireContext().getColor(R.color.item_background_dark), ThemeColorManagerCompat.getColorPrimary(), 1.15)
                mFirstChar
                    .setIconTextColorByThemeColorLuminance()
                    .setStrokeColor(themeColorForContrast)
                    .setFillColor(themeColorForContrast)
            }
            else -> {
                mFirstChar
                    .setIconTextColorDayNight()
                    .setStrokeColorDayNight()
                    .setFillTransparent()
            }
        }
    }

    private fun run() {
        Scripts.run(requireContext(), ScriptFile(
            File(requireContext().filesDir, "sample/Main/main.js")
        ))
        // notifyItemOperated()
    }

    private fun stop() {
        if (instance.scriptEngineService.stopAllAndToast() <= 0) {
            showToast(requireContext(), requireContext().getString(R.string.text_no_scripts_to_stop_running))
        }
    }

    private fun updateVisibility(view: View, visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        if (view.visibility != visibility) {
            view.visibility = visibility
        }
    }

    private fun writeMainJs() {
        val dir = File(requireContext().filesDir, "sample/Main")
        if (!dir.exists()) {
            dir.mkdirs() // 创建多级目录
        }

        val newFile = File(dir, "main.js")
        newFile.writeText("const CMD_FILE = \"command.json\";\n" +
                "const CMD_DIR = \"/sdcard/yunGateway/\";\n" +
                "\n" +
                "\n" +
                "// 显示控制台（防止被回收）\n" +
                "console.show();\n" +
                "console.log(\"文件监听服务已启动命令文件: \" + CMD_FILE);\n" +
                "\n" +
                "//设置主main引擎\n" +
                "const mainEngine = engines.myEngine();\n" +
                "const mainEngineId = mainEngine.id;\n" +
                "\n" +
                "let cmdId = 0\n" +
                "let cmdTime = 0\n" +
                "\n" +
                "function initializeMainEngine(){\n" +
                "    engines.all().forEach(engine => {\n" +
                "        let scriptPath = engine.getSource() + ''; // 转为字符串路径\n" +
                "        // 如果不是main.js且不是自己，则停止\n" +
                "        if (engine.id !== mainEngineId) {\n" +
                "            console.log(\"开始停止脚本:\", scriptPath);\n" +
                "            engine.forceStop(); // 强制停止\n" +
                "        }});\n" +
                "}\n" +
                "\n" +
                "// 主监听循环\n" +
                "setInterval(() => {\n" +
                "    let cmdFile = CMD_DIR + CMD_FILE\n" +
                "    if (!files.exists(cmdFile)) return;\n" +
                "    let rawData = files.read(cmdFile);\n" +
                "    if (!rawData) return;\n" +
                "    let cmd = JSON.parse(rawData.trim());\n" +
                "    cmdId = cmd.id\n" +
                "    let nextCmdTime = cmd.cmdTime\n" +
                "    let action = cmd.action\n" +
                "    let script = cmd.script\n" +
                "    if (nextCmdTime === cmdTime) return;\n" +
                "\n" +
                "    if (action === \"run\"){\n" +
                "        let cmdScript = CMD_DIR + script\n" +
                "        let func = cmd.function;\n" +
                "        let deviceId = cmd.deviceId;\n" +
                "        let config = cmd.config\n" +
                "        cmdTime = nextCmdTime\n" +
                "        initializeMainEngine()\n" +
                "        sleep(3000)\n" +
                "        let runningEngine = engines.execScriptFile(cmdScript)\n" +
                "        sleep(2000)\n" +
                "        console.log(\"开始启动指令:\"+ func);\n" +
                "        let params = cmdId+\":\" + deviceId + \":\" + func+\":\" + config\n" +
                "        runningEngine.getEngine().emit(\"subTasks\", params);\n" +
                "        //清空命令文件，等待下次接受\n" +
                "        files.write(cmdFile, \"\");\n" +
                "    }else if (action === \"stopAction\"){\n" +
                "        initializeMainEngine()\n" +
                "    }\n" +
                "\n" +
                "}, 10000); // 每10秒检查一次\n" +
                "\n" +
                "// 保持脚本运行的保活机制\n" +
                "setInterval(() => {}, 1000);")
    }

}