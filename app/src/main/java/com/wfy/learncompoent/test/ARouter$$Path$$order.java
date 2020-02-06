package com.wfy.learncompoent.test;

import com.wfy.annotation.model.RouterBean;
import com.wfy.arouter_api.core.ARouterLoadPath;
import com.wfy.order.Order_MainActivity;

import java.util.HashMap;
import java.util.Map;

public class ARouter$$Path$$order implements ARouterLoadPath {
    @Override
    public Map<String, RouterBean> loadPath() {
        Map<String, RouterBean> map = new HashMap<>();

        map.put("/order/Order_MainActivity",
                RouterBean.create(RouterBean.Type.ACTIVITY, Order_MainActivity.class,
                        "/order/Order_MainActivity", "order"));

        map.put("/order/Order_ListMainActivity",
                RouterBean.create(RouterBean.Type.ACTIVITY, Order_MainActivity.class,
                        "/order/Order_ListMainActivity", "order"));
        return map;
    }
}
