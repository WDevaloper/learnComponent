package com.wfy.learncompoent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.wfy.annotation.ARouter;
import com.wfy.annotation.model.RouterBean;
import com.wfy.arouter_api.RouterManager;
import com.wfy.arouter_api.core.ARouterLoadGroup;
import com.wfy.arouter_api.core.ARouterLoadPath;
import com.wfy.common.Order;
import com.wfy.common.User;
import com.wfy.learncompoent.test.ARouter$$Group$$order;
import com.wfy.order.Order_MainActivity;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@ARouter(path = "/app2/MainActivity", group = "app")
public class MainActivity extends AppCompatActivity {

    int age = 1;
    User user;
    ArrayList<User> users = new ArrayList<>();
    ArrayList<Order> orders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, Order_MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("age", 88);
        bundle.putParcelable("user", new User());
        users.add(new User());
        bundle.putParcelableArrayList("users", users);

        bundle.putSerializable("order", new Order());
        bundle.putSerializable("orders",orders);


        intent.putExtras(bundle);

        intent.putExtra("bundle",bundle);

        startActivity(intent);
    }


    public void jumpOrder(View view) {
        RouterManager
                .getInstance()
                .build("/order/Order_MainActivity")
                .navigation(this, 10);
    }

    private void code() {
        ARouter$$Group$$order aRouter$$Group$$order = new ARouter$$Group$$order();
        Map<String, Class<? extends ARouterLoadPath>> group = aRouter$$Group$$order.loadGroup();
        // 通过组名获取ARouterLoadPath,一般我们要跳转到指定的Activity，需要提供一个路径，如：/app/MainActivity
        //那么我们可以截取到组名，即；可以拿到组名和path
        Class<? extends ARouterLoadPath> order = group.get("order");
        try {
            if (order != null) {
                ARouterLoadPath routerLoadPath = order.newInstance();
                Map<String, RouterBean> routerBeanMap = routerLoadPath.loadPath();
                // 通过ARouterLoadPath获取指定的RouterBean
                if (routerBeanMap != null) {
                    //通过path获取RouterBean并跳转
                    RouterBean routerBean = routerBeanMap.get("/order/Order_MainActivity");
                    Intent intent = new Intent(this, routerBean.getClzz());
                    intent.putExtra("name", "wfy");
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}