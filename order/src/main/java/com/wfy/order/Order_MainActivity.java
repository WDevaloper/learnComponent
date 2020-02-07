package com.wfy.order;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.wfy.annotation.ARouter;
import com.wfy.annotation.Parameter;
import com.wfy.arouter_api.ParameterManager;
import com.wfy.common.User;

import java.util.List;

@ARouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Parameter
    int age = 1;

    @Parameter
    String name = "";

    @Parameter
    User user;


    @Parameter
    List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order__main);
        ParameterManager.getInstance().inject(this);
        Log.e("tag", "age >>> " + age);
    }
}
