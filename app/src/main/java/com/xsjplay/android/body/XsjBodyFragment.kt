package com.xsjplay.android.body

import android.os.Bundle
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
import org.autojs.autojs6.R
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
}