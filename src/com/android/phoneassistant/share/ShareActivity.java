package com.android.phoneassistant.share;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.android.phoneassistant.R;
import com.chukong.sdkmini.ShareDialog;

public class ShareActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_layout);
    }

    public void onClick(View view) {
        ShareDialog dialog = new ShareDialog(this);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_callassistant);
        dialog.setLogoBmp(bmp);
        dialog.show();
    }
}
