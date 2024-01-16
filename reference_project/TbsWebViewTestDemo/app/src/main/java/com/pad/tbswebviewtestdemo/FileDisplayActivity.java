package com.pad.tbswebviewtestdemo;

import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class FileDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_display);

        QbSdk.clearAllWebViewCache(this,true);

        TbsReaderView mTbsReaderView = new TbsReaderView(this, new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {
            }
        });
        RelativeLayout rlRoot = findViewById(R.id.rl_root);
        rlRoot.addView(mTbsReaderView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
        String bsReaderTemp = FileUtils.getTBSFileDir(FileDisplayActivity.this) + "/TbsReaderTemp";
        File bsReaderTempFile = new File(bsReaderTemp);
        if (!bsReaderTempFile.exists()) {
            Log.e("111111", "准备创建/storage/emulated/0/TbsReaderTemp！！");
            boolean mkdir = bsReaderTempFile.mkdir();
            if (!mkdir) {
                Log.e("111111", "创建/storage/emulated/0/TbsReaderTemp失败！！！！！");
            }
        }

        String path = FileUtils.getTBSFileDir(getApplicationContext()).getPath() +"/"+ getIntent().getExtras().getString("fileName");
        Log.e("11111","path: "+path);
        boolean bool = mTbsReaderView.preOpen(FileUtils.getSuffix(path), false);
        Log.e("11111","bool:"+bool);
        if (bool) {
            // try {
            //     TimeUnit.SECONDS.sleep(1);
            // } catch (InterruptedException e) {
            //     throw new RuntimeException(e);
            // }

            Bundle bundle = new Bundle();
            bundle.putString("filePath", path);
            bundle.putString("tempPath", FileUtils.getTBSFileDir(FileDisplayActivity.this) + "/" + "TbsReaderTemp");
            mTbsReaderView.openFile(bundle);
        }
    }
}
