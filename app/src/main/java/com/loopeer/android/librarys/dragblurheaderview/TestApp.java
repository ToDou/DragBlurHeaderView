package com.loopeer.android.librarys.dragblurheaderview;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.loopeer.android.librarys.dragblurheaderview.configs.ImagePipelineConfigFactory;

public class TestApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this,
                ImagePipelineConfigFactory.getOkHttpImagePipelineConfig(getApplicationContext()));
    }
}
