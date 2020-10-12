package com.sty.ne.modularapt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.sty.ne.annotation.ARouter;

@ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jumpToOrder(View view) {
        Class<?> targetClass = OrderActivity$$ARouter.findTargetClass("/app/OrderActivity");
        startActivity(new Intent(this, targetClass));
    }
}