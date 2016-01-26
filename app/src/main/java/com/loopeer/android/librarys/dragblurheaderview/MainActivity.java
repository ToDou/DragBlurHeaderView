package com.loopeer.android.librarys.dragblurheaderview;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

public class MainActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SimpleDraweeView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);

        setContentView(R.layout.activity_main);
        mImage = (SimpleDraweeView) findViewById(R.id.icon);

        mTabLayout = (TabLayout) findViewById(R.id.tab_main);
        mViewPager = (ViewPager) findViewById(R.id.pager_main);
        mImage.setImageURI(Uri.parse("http://img.taopic.com/uploads/allimg/130501/240451-13050106450911.jpg"));

        setUpPager();
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
}
