package com.example.checkroute;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private BottomNavigationView bottomNavigationView;
    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bNavigation);

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiverStation, new IntentFilter("find_station"));

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiverRoute, new IntentFilter("find_route"));

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiverStraightRoute,
                new IntentFilter("straight_route"));

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiverStraightRoute,
                        new IntentFilter("straight_route"));

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.home:
                                viewPager.setCurrentItem(0);
                                break;
                            case R.id.station:
                                viewPager.setCurrentItem(1);
                                break;
                            case R.id.route:
                                viewPager.setCurrentItem(2);
                                break;
                            case R.id.prioritize:
                                viewPager.setCurrentItem(3);
                                break;
                        }
                        return false;
                    }
                }
        );

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (menuItem != null){
                    menuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                Log.d("page", "onPageSelected: " + i);
                bottomNavigationView.getMenu().getItem(i).setChecked(true);
                menuItem = bottomNavigationView.getMenu().getItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        setupViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new MapFragment());
        viewPagerAdapter.addFragment(new StationFragment());
        viewPagerAdapter.addFragment(new RouteFragment());
        viewPagerAdapter.addFragment(new PrioritizeFragment());
        viewPager.setAdapter(viewPagerAdapter);
    }

    BroadcastReceiver broadcastReceiverStation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            viewPager.setCurrentItem(0);
        }
    };

    BroadcastReceiver broadcastReceiverRoute = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            viewPager.setCurrentItem(0);
        }
    };

    BroadcastReceiver broadcastReceiverStraightRoute = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            viewPager.setCurrentItem(0);
        }
    };

    BroadcastReceiver broadcastReceiverMiddleRoute = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            viewPager.setCurrentItem(0);
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiverStation, new IntentFilter("find_station"));
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiverRoute, new IntentFilter("find_route"));
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiverStraightRoute,
                new IntentFilter("straight_route"));
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiverMiddleRoute,
                        new IntentFilter("multiple_route"));
    }

}
