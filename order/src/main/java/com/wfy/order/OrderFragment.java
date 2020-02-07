package com.wfy.order;

import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wfy.annotation.Parameter;

public class OrderFragment extends Fragment {

    @Parameter
    int orderId = 1;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getArguments().getParcelable("");
    }
}
