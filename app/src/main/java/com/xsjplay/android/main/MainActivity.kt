package com.xsjplay.android.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.xsjplay.android.body.XsjBodyFragment
import org.autojs.autojs.app.FragmentPagerAdapterBuilder.StoredFragmentPagerAdapter
import org.autojs.autojs.app.OnActivityResultDelegate
import org.autojs.autojs.app.OnActivityResultDelegate.DelegateHost
import org.autojs.autojs.core.permission.RequestPermissionCallbacks
import org.autojs.autojs.core.pref.Pref
import org.autojs.autojs.event.BackPressedHandler
import org.autojs.autojs.event.BackPressedHandler.HostActivity
import org.autojs.autojs.model.explorer.Explorers
import org.autojs.autojs.permission.DisplayOverOtherAppsPermission
import org.autojs.autojs.permission.ManageAllFilesPermission
import org.autojs.autojs.permission.PostNotificationsPermission
import org.autojs.autojs.runtime.api.WrappedShizuku
import org.autojs.autojs.theme.ThemeColorManager.addViewBackground
import org.autojs.autojs.theme.widget.ThemeColorToolbar
import org.autojs.autojs.ui.BaseActivity
import org.autojs.autojs.ui.explorer.ExplorerView
import org.autojs.autojs.ui.floating.FloatyWindowManger
import org.autojs.autojs.ui.log.LogActivity
import org.autojs.autojs.util.StringUtils.key
import org.autojs.autojs.util.UpdateUtils
import org.autojs.autojs.util.ViewUtils
import org.autojs.autojs.util.ViewUtils.setMenuIconsColorByThemeColorLuminance
import org.autojs.autojs.util.ViewUtils.setNavigationIconColorByThemeColorLuminance
import org.autojs.autojs.util.WorkingDirectoryUtils
import com.xsjplay.android.R
import com.xsjplay.android.databinding.ActivityMainXsjBinding

/**
 * Modified by SuperMonster003 as of Dec 1, 2021.
 * Transformed by SuperMonster003 on May 11, 2023.
 */
class MainActivity : BaseActivity(), DelegateHost, HostActivity {

    override val handleStatusBarThemeColorAutomatically = false

    // private lateinit var mViewPager: ViewPager
    // private lateinit var mFab: ThemeColorFloatingActionButton
    // private lateinit var mTab: TabLayout
    private lateinit var mTextLabel: TextView
    private lateinit var mToolbar: ThemeColorToolbar
    private lateinit var mPagerAdapter: StoredFragmentPagerAdapter
    private lateinit var mLogMenuItem: MenuItem

    private val mActivityResultMediator = OnActivityResultDelegate.Mediator()
    private val mRequestPermissionCallbacks = RequestPermissionCallbacks()
    private val mBackPressObserver = BackPressedHandler.Observer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityMainXsjBinding.inflate(layoutInflater).also {
            val constraintLayout = it.constraintLayout
            setContentView(it.root)
            // mViewPager = it.viewpager
            // mFab = it.fab.apply { ViewUtils.excludeFloatingActionButtonFromNavigationBar(this) }
            // mTab = it.tab
            mTextLabel = it.titleLabel
            mToolbar = it.toolbar
            addViewBackground(it.appBar)
            setUpToolbar()
            // setUpTabViewPager(it)
            // registerBackPressHandlers(drawerLayout)

            // supportFragmentManager.commit {
            //     setReorderingAllowed(true)
            //     add(mainJs.id, XsjBodyFragment())
            // }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_main_js, XsjBodyFragment())
                .commit()

        }

        Pref.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == key(R.string.key_keep_screen_on_when_in_foreground)) {
                ViewUtils.configKeepScreenOnWhenInForeground(this)
            }
        }

        WorkingDirectoryUtils.determineIfNeeded()
        ExplorerView.clearViewStates()

        FloatyWindowManger.refreshCircularMenuIfNeeded(this)

        PostNotificationsPermission(this).urgeIfNeeded()
        ManageAllFilesPermission(this).urgeIfNeeded()
        DisplayOverOtherAppsPermission(this).urgeIfNeeded()
    }

    override fun onPostResume() {
        recreateIfNeeded()
        UpdateUtils.autoCheckForUpdatesIfNeededWithSnackbar(this)
        super.onPostResume()
    }

    override fun onStart() {
        super.onStart()
        WrappedShizuku.bindUserServiceIfNeeded()
    }

    private fun recreateIfNeeded() {
        if (shouldRecreateMainActivity) {
            shouldRecreateMainActivity = false
            recreate()
            Explorers.workspace().refreshAll()
        }
    }

    private fun setUpToolbar() {
        mToolbar.also {
            setSupportActionBar(it)
            it.setTitle(R.string.app_name)
        }
    }

    override fun initThemeColors() {
        super.initThemeColors()
        setUpToolbarColors()
        // setUpTabLayoutColors()
        setUpStatusBarAppearanceLight()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setUpStatusBarAppearanceLight()
        }
    }

    private fun setUpToolbarColors() {
        mToolbar.setMenuIconsColorByThemeColorLuminance(this)
        mToolbar.setNavigationIconColorByThemeColorLuminance(this)
    }

    private fun setUpStatusBarAppearanceLight() {
        Handler(Looper.getMainLooper()).post {
            if (sIsActionBarDrawerOpened) {
                setUpStatusBarAppearanceLightByNightMode()
            } else {
                setUpStatusBarAppearanceLightByThemeColor()
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mActivityResultMediator.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (mRequestPermissionCallbacks.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            return
        }
        if (getReadExternalStoragePermissionResult(permissions, grantResults) == PackageManager.PERMISSION_GRANTED) {
            Explorers.workspace().refreshAll()
        }
    }

    private fun getReadExternalStoragePermissionResult(permissions: Array<String>, grantResults: IntArray): Int {
        val i = permissions.indexOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        return if (i > -1) grantResults[i] else PackageManager.PERMISSION_DENIED
    }

    override fun getOnActivityResultDelegateMediator() = mActivityResultMediator


    override fun getBackPressedObserver() = mBackPressObserver

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_xsj, menu)
        mLogMenuItem = menu.findItem(R.id.action_log)
        setUpToolbarColors()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_log) {
            LogActivity.launch(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onResume() {
        super.onResume()
        ViewUtils.configKeepScreenOnWhenInForeground(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {

        private var sIsActionBarDrawerOpened = false

        var shouldRecreateMainActivity = false

        @JvmStatic
        fun launch(context: Context) = context.startActivity(getIntent(context).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

        @JvmStatic
        fun getIntent(context: Context?) = Intent(context, MainActivity::class.java)

    }

}
