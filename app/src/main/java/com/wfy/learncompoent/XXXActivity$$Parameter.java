package com.wfy.learncompoent;



import android.os.Parcelable;

import com.wfy.arouter_api.core.ParameterInject;

import java.util.ArrayList;

//必须和Activity同包
public class XXXActivity$$Parameter implements ParameterInject {
    @Override
    public void inject(Object target) {
        MainActivity mainActivity = (MainActivity) target;
       mainActivity.getIntent().getSerializableExtra("");
        ArrayList<Parcelable> parcelableArrayListExtra = mainActivity.getIntent().getParcelableArrayListExtra("");

        mainActivity.age = mainActivity.getIntent().getBundleExtra("").getInt("");

//        new Fragment().getArguments().getString()"", 0);
    }
}
