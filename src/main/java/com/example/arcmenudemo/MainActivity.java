package com.example.arcmenudemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
    private ArcMenu menu;
    private int[] resids = new int[]{R.drawable.ic_1, R.drawable.ic_2,
            R.drawable.ic_3, R.drawable.ic_4,
            R.drawable.ic_5, R.drawable.ic_6};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        menu = (ArcMenu) findViewById(R.id.am_view);
        menu.addViewAndListener(resids, new ArcMenu.ItemSelectListener() {
            @Override
            public void onItemSelect(int resID) {
                Toast.makeText(MainActivity.this, "resid = " + resID, Toast.LENGTH_LONG).show();
            }
        });
    }
}
