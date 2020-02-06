package com.wfy.order.debug;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.wfy.annotation.ARouter;
import com.wfy.order.R;

@ARouter(path = "/order/Order_DebugMainActivity")
public class Order_DebugMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order__debug_main);
    }
}
