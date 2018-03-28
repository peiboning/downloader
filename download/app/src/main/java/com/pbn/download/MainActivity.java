package com.pbn.download;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.download.lib.Downloader;
import com.download.model.DownloadListener;
import com.download.util.MLog;

/**
 * @author peiboning
 */
public class MainActivity extends AppCompatActivity {
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progress = (ProgressBar) findViewById(R.id.progress);
        Downloader.getInstance().setContext(getApplicationContext());
    }

    public void begin(View view){
        String url = "http:imtt.dd.qq.com/16891/46B2F98C83B94180C52F054A52691FFF.apk";

        Downloader.getInstance().addTask(url, new DownloadListener() {
            @Override
            public void onDownloadStart() {

            }

            @Override
            public void onProcess(final int percent) {
                MLog.i("DOWN_SDK", "  download progress is " +percent + "%");
                progress.post(new Runnable() {
                    @Override
                    public void run() {
                        progress.setProgress(percent);
                    }
                });
            }

            @Override
            public void onDownloadEnd(final boolean isSuccess) {
                progress.post(new Runnable() {
                    @Override
                    public void run() {
                        if(isSuccess){
                            Toast.makeText(MainActivity.this, "download success1", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "download failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        progress.postDelayed(new Runnable() {
            @Override
            public void run() {
                String url = "http:imtt.dd.qq.com/16891/C3C92F0D22203C748CCF6BCAFE489F71.apk";
                Downloader.getInstance().addTask(url, new DownloadListener() {
                    @Override
                    public void onDownloadStart() {

                    }

                    @Override
                    public void onProcess(final int percent) {
                        MLog.i("DOWN_SDK", "  download progress is " +percent + "%");

                        progress.post(new Runnable() {
                            @Override
                            public void run() {
                                progress.setProgress(percent);
                            }
                        });
                    }

                    @Override
                    public void onDownloadEnd(final boolean isSuccess) {
                        progress.post(new Runnable() {
                            @Override
                            public void run() {
                                if(isSuccess){
                                    Toast.makeText(MainActivity.this, "download success2", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(MainActivity.this, "download failed2", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
            }
        }, 2000);
    }
    public void stop(View view){
        Downloader.getInstance().stopDownload();
    }
    public void resume(View view){
        Downloader.getInstance().resumeDownload();
    }

    public void start2(View view){
        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);
    }
}
