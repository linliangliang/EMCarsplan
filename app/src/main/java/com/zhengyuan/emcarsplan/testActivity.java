package com.zhengyuan.emcarsplan;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;

/**
 * Created by zy on 2018/8/9.
 */

public class testActivity extends Activity {
    //获取操作者的工号
    String sname = EMProApplicationDelegate.userInfo.getUserId();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        TextView textView=(TextView)findViewById(R.id.title_tv);
        textView.setText("EMTest02_"+sname);

       /* UserInfoObtainer.INSTANCE.getAllUser(new NetworkCallbacks.SimpleDataCallback() {
            @Override
            public void onFinish(boolean b, String s, Object o) {

            }
        });*/
    }


}
