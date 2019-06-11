package com.casvanluijtelaar.beatable;

import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

public class listChecker {
    public void checkForInstrument(List<String> list, View view) {
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.createLayout);
        if (list.isEmpty()) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
    }
}

