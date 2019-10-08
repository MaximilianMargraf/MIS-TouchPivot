package com.example.mis_final_project_touchpivot;

import android.content.Context;
import android.widget.TableLayout;

public class SCpair {
    public SCpair(String string_, Context context) {
        this.string_ = string_;
        this.context_ = context;
    }

    public String getString_() {
        return string_;
    }

    public Context getContext() {
        return context_;
    }

    String string_;
    Context context_;
}
