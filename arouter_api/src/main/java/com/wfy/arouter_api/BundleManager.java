package com.wfy.arouter_api;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BundleManager {
    private Bundle bundle = new Bundle();

    // @NonNull不允许传null，@Nullable可以传null
    public BundleManager withString(@NonNull String key, @Nullable String value) {
        bundle.putString(key, value);
        return this;
    }

    // 示例代码，需要拓展
    public BundleManager withResultString(@NonNull String key, @Nullable String value) {
        bundle.putString(key, value);
        return this;
    }

    public BundleManager withBoolean(@NonNull String key, boolean value) {
        bundle.putBoolean(key, value);
        return this;
    }

    public BundleManager withInt(@NonNull String key, int value) {
        bundle.putInt(key, value);
        return this;
    }

    public BundleManager withBundle(@NonNull Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

      public Object navigation(Context context) {
        return RouterManager.getInstance().navigation(context, -1);
    }

    public Object navigation(Context context, int code) {
        return RouterManager.getInstance().navigation(context, code);
    }
}
