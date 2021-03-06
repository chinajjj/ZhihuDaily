package com.yao.zhihudaily.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.material.navigation.NavigationView;
import com.yao.zhihudaily.R;
import com.yao.zhihudaily.ui.daily.DailyMainFragment;
import com.yao.zhihudaily.ui.hot.HotMainFragment;
import com.yao.zhihudaily.ui.section.SectionMainFragment;
import com.yao.zhihudaily.util.ResUtil;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Yao
 * @date 2016/7/21
 */
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int REQUEST_PERMISSION_STORAGE = 200;

    private static final String TAG = "MainActivity";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;//适配BottomNavigation的ViewPager
    @BindView(R.id.bottom_navigation)
    AHBottomNavigation mBottomNavigation;//底部的BottomNavigation
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.drawerLayout)
    DrawerLayout mDrawerLayout;

    private BaseFragment currentFragment;
    private MainViewPagerAdapter adapter;
    private ArrayList<AHBottomNavigationItem> bottomNavigationItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);//隐藏左上角的DrawerLayout图标
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);//暂时关闭侧边栏,因为没有什么业务好写
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);


        mToolbar.inflateMenu(R.menu.main);//设置右上角的填充菜单
        mToolbar.setOnMenuItemClickListener(item -> {
            int menuItemId = item.getItemId();
            if (menuItemId == R.id.action_settings) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            } else if (menuItemId == R.id.action_introduce) {
                startActivity(new Intent(MainActivity.this, SoftwareIntroductionActivity.class));
            }
            return true;
        });

        //是否用Menu资源去完成,menu资源即对应的menu布局文件. 否则就是用代码new出来并且添加上去的AHBottomNavigationItem
        boolean useMenuResource = true;
        if (useMenuResource) {//方式一:通过menu菜单去完成
            int[] tabColors = getApplicationContext().getResources().getIntArray(R.array.tab_colors);
            AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_menu_3);
            navigationAdapter.setupWithBottomNavigation(mBottomNavigation, tabColors);
        } else {//方式二:通过代码new出去并且添加上去完成
            AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.daily, R.mipmap.ic_bottom_navigation_daily, R.color.color_tab_1);
            AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.theme, R.mipmap.ic_bottom_navigation_theme, R.color.color_tab_2);
            AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.hot, R.mipmap.ic_bottom_navigation_hot, R.color.color_tab_3);
            AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.more, R.mipmap.ic_bottom_navigation_more, R.color.color_tab_4);

            bottomNavigationItems.add(item1);
            bottomNavigationItems.add(item2);
            bottomNavigationItems.add(item3);
            bottomNavigationItems.add(item4);

            mBottomNavigation.addItems(bottomNavigationItems);
        }

        mBottomNavigation.setBehaviorTranslationEnabled(true);//重要属性 设置向上滑动时是否隐藏底部栏
        mBottomNavigation.setAccentColor(ResUtil.getColor(R.color.zhihu_blue)); //设置选中的颜色
        mBottomNavigation.setInactiveColor(ResUtil.getColor(R.color.bottom_navigation_inactive));//设置闲置的颜色
        mBottomNavigation.setDefaultBackgroundColor(ResUtil.getColor(R.color.bottom_navigation_bg));//设置背景颜色

        //mBottomNavigation.setNotification("", position);//给Item设置通知图标
        mViewPager.setOffscreenPageLimit(3);

        DailyMainFragment feedMainFragment = new DailyMainFragment();
        //ThemeMainFragment themeMainFragment = new ThemeMainFragment();
        HotMainFragment hotMainFragment = new HotMainFragment();
        SectionMainFragment sectionMainFragment = new SectionMainFragment();
        ArrayList<BaseFragment> baseFragments = new ArrayList<>();
        baseFragments.add(feedMainFragment);
        //baseFragments.add(themeMainFragment);
        baseFragments.add(hotMainFragment);
        baseFragments.add(sectionMainFragment);
        adapter = new MainViewPagerAdapter(getSupportFragmentManager(), baseFragments);
        mViewPager.setAdapter(adapter);
        currentFragment = adapter.getCurrentFragment();

        mBottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {//wasSelected为真时,表示当前显示与当前点击的是同一个Item


            if (currentFragment == null) {
                currentFragment = adapter.getCurrentFragment();
            }

            if (wasSelected) {//为真时,刷新一个当前item就行
                currentFragment.refresh();
                return true;
            }

            if (currentFragment != null) {
                currentFragment.willBeHidden();
            }

            mViewPager.setCurrentItem(position, false);
            currentFragment = adapter.getCurrentFragment();
            currentFragment.willBeDisplayed();
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}
