package com.wfy.order;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.wfy.annotation.ARouter;
import com.wfy.annotation.Parameter;
import com.wfy.arouter_api.ParameterManager;
import com.wfy.arouter_api.RouterManager;
import com.wfy.common.Order;
import com.wfy.common.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ARouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Parameter
    int age = 1;

    @Parameter
    String name = "";

    @Parameter
    User user;

    @Parameter(name = "users")
    ArrayList<User> users;

    @Parameter
    Order order;

    @Parameter
    ArrayList<Order> orders;

    @Parameter
    Bundle bundle;

    @Parameter
    String[] args;


    @Parameter
    int[] ints;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order__main);
        ParameterManager.getInstance().inject(this);
        RouterManager.inject(this);
        Log.e("tag", "age >>> " + age + " users>>>" +
                users + " >>> " + user + " >>> " + order + "  >>>  " + orders + " >>> " + bundle.getInt("age"));
    }
}
