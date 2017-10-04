package com.example.alexander.trams;

import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.Display;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    public static FlexboxLayout flex;
    private static char letter;
    private SwipeRefreshLayout swipeLayout;
    private ScrollView scrollView;

    public static char getLetter() {
        return letter;
    }

    ArrayList<Button> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setWords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Menu menu = ((NavigationView) findViewById(R.id.nav_view)).getMenu();
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setChecked(false);
        }
        scrollView.removeAllViews();
        setWords();
    }

    void setWords() {
        flex = new FlexboxLayout(this);
        flex.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
        flex.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);
        flex.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_FLEX_START);
        flex.setAlignItems(FlexboxLayout.ALIGN_ITEMS_FLEX_START);
        flex.setAlignContent(FlexboxLayout.ALIGN_CONTENT_FLEX_START);
        buttons = new ArrayList<>();
        Thread downloadThread = new Thread(() -> {
            try {
                Document doc = Jsoup.connect("https://m.ettu.ru").get();
                Elements elems = doc.getElementsByClass("letter-link");
                for (Element elem : elems) {
                    Button button = new Button(this);
                    button.setText(elem.text());
                    button.setOnClickListener(Listener());
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int width = size.x;
                    button.setLayoutParams(new FlexboxLayout.LayoutParams(width / 6, width / 6));
                    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.buttons_light));
                    buttons.add(button);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        downloadThread.start();
        try {
            downloadThread.join();
            for (Button btn : buttons)
                flex.addView(btn);
            swipeLayout = (SwipeRefreshLayout) findViewById(R.id.SwipeMain);
            swipeLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.colorPrimaryDark));
            swipeLayout.setOnRefreshListener(() -> {
                swipeLayout.setRefreshing(true);
                swipeLayout.postDelayed(() -> {
                    scrollView.removeAllViews();
                    swipeLayout.setRefreshing(false);
                    setWords();
                }, 1000);
            });
            scrollView = (ScrollView) findViewById(R.id.scrollViewMain);
            scrollView.addView(flex);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private View.OnClickListener Listener() {
        final View.OnClickListener listener = (v) -> {
            letter = ((TextView) v).getText().charAt(0);
            Intent myIntent = new Intent(MainActivity.this, SelectStation.class);
            startActivity(myIntent);
        };
        return listener;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info: {
                startActivity(new Intent(MainActivity.this, Info.class));
            }
            break;
            case R.id.fav: {
                startActivity(new Intent(MainActivity.this, Fav.class));
            }
            break;
            default:
                return false;
        }
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return true;
    }

}
