package com.example.wanghui.mweibo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import ru.truba.touchgallery.GalleryWidget.GalleryViewPager;
import ru.truba.touchgallery.GalleryWidget.UrlPagerAdapter;

public class TouchGallery extends AppCompatActivity {

    private GalleryViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.gallery);
        ArrayList<String> urls = getIntent().getStringArrayListExtra("lUrls");
        int x = getIntent().getIntExtra("pos", 0);
        UrlPagerAdapter pagerAdapter = new UrlPagerAdapter(this, urls);
        mViewPager = (GalleryViewPager)findViewById(R.id.viewer);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(x);
    }
}
