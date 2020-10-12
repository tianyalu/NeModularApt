package com.sty.ne.modularapt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sty.ne.annotation.ARouter;

@ARouter(path = "/app/PersonalActivity")
public class PersonalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        Log.e("sty", "--> PersonalActivity");
    }

    //personal -> Home
    public void jumpToHome(View view) {
        Class<?> targetClass = MainActivity$$ARouter.findTargetClass("/app/MainActivity");
        startActivity(new Intent(this, targetClass));
    }
}