package com.loopeer.android.librarys.dragblurheaderview;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.facebook.drawee.view.SimpleDraweeView;
import com.loopeer.android.librarys.dragblurheader.DragBlurHeaderView;

public class MainActivity extends AppCompatActivity implements DragBlurHeaderView.OnPosChangeListener {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SimpleDraweeView mImage;
    private DragBlurHeaderView mDragBlurHeaderView;
    private Bitmap mLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mImage = (SimpleDraweeView) findViewById(R.id.icon);

        mTabLayout = (TabLayout) findViewById(R.id.tab_main);
        mViewPager = (ViewPager) findViewById(R.id.pager_main);
        mDragBlurHeaderView = (DragBlurHeaderView) findViewById(R.id.view_main_drager);
        initTopImage();
        setUpPager();
        mDragBlurHeaderView.setOnPosChangeListener(this);
    }

    private void initTopImage() {
        ImageUtils.displayBlurImageRes(mImage, R.drawable.img_cat, 25);
    }

    private void setUpPager() {
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return new TestFragment();
            }

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return "Tab" + position;
            }
        });
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onPosYChange(int y) {
        ImageUtils.displayBlurImageRes(mImage, R.drawable.img_cat, 80 - y < 0 ? 0 : (int) ((80.f - y) * 25.f / 80.f));
    }

}
