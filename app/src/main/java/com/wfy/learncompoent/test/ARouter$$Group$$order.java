package com.wfy.learncompoent.test;

import com.wfy.arouter_api.core.ARouterLoadGroup;
import com.wfy.arouter_api.core.ARouterLoadPath;

import java.util.HashMap;
import java.util.Map;

public class ARouter$$Group$$order implements ARouterLoadGroup {
    @Override
    public Map<String, Class<? extends ARouterLoadPath>> loadGroup() {
        Map<String, Class<? extends ARouterLoadPath>> map = new HashMap<>();
        map.put("order",ARouter$$Path$$order.class);
        return map;
    }
}
