package com.pbn.download;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.download.lib.Downloader;
import com.download.model.DownloadListener;

public class Main2Activity extends Activity {
    private ProgressBar progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        progress = (ProgressBar) findViewById(R.id.progress);
        String url = "http://imtt.dd.qq.com/16891/46B2F98C83B94180C52F054A52691FFF.apk";
        Downloader.getInstance().addListener(url, new DownloadListener() {
            @Override
            public void onDownloadStart() {

            }

            @Override
            public void onProcess(int percent) {
                progress.setProgress(percent);
            }

            @Override
            public void onDownloadEnd(boolean isSuccess) {

            }
        });
    }
}
