package com.example.mis_final_project_touchpivot;

import android.content.Context;

public class SCpair {
    String string_;
    Context context;

    public SCpair(String string_, Context context) {
        this.string_ = string_;
        this.context = context;
    }

    public String getString_() {
        return string_;
    }

    public Context getContext() {
        return context;
    }
}
