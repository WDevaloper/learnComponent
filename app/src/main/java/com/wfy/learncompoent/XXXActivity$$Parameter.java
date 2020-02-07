package com.wfy.learncompoent;



import com.wfy.arouter_api.core.ParameterInject;


//必须和Activity同包
public class XXXActivity$$Parameter implements ParameterInject {
    @Override
    public void inject(Object target) {
        MainActivity mainActivity = (MainActivity) target;
        mainActivity.user = mainActivity.getIntent().getParcelableExtra("user");
        mainActivity.age = mainActivity.getIntent().getBundleExtra("").getInt("");

//        new Fragment().getArguments().getString()"", 0);
    }
}
