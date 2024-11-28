package com.example.bustrack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nafis.bottomnavigation.NafisBottomNavigation;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {

    NafisBottomNavigation bottomNavigation;

    private FloatingActionButton addbusbtn;

    private final static int map = 1;
    private final static int garage = 2;
    private final static int profile = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         bottomNavigation = findViewById(R.id.bottomNavigation);


        bottomNavigation.add(new NafisBottomNavigation.Model(map, R.drawable.baseline_map_24));
        bottomNavigation.add(new NafisBottomNavigation.Model(garage, R.drawable.baseline_garage_24));
        bottomNavigation.add(new NafisBottomNavigation.Model(profile, R.drawable.baseline_person_pin_24));

        bottomNavigation.show(garage,true);

        bottomNavigation.setOnReselectListener(new Function1<NafisBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(NafisBottomNavigation.Model model) {
                Fragment fragment = null;
                if (model.getId() == 1)
                {

                } else if(model.getId() == 2) {
                    fragment = new GarageFragment();
                }
                else
                {
                    fragment = new ProfileFragement();
                }

                LoadAndReplaceFragment(fragment);
                return null;
            }
        });

        addbusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment addBusFragment = new AddbusFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, addBusFragment, "ADD_BUS_FRAGMENT")
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

    private void LoadAndReplaceFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container,fragment,null)
                .commit();
    }
}