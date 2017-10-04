package com.example.alexander.trams;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Trams extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private char letter;
    private ArrayList<Transport> listoftransport;
    private SwipeRefreshLayout swipeLayout;
    private TableLayout table;
    private ScrollView scrollView;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            this.overridePendingTransition(R.anim.trans_right_in,
                    R.anim.trans_right_out);
            /*Intent myIntent = new Intent(Trams.this, SelectStation.class);
            startActivity(myIntent);
            return;*/
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        scrollView.removeAllViews();
        setTrams();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trams);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarTrams);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle(SelectStation.getSelectedStation().getNameStation());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toggle.setToolbarNavigationClickListener(v -> {
            onBackPressed();
        });
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setTrams();
    }

    void setTrams() {

        table = new TableLayout(this);
        table.setStretchAllColumns(true);
        table.setShrinkAllColumns(true);
        listoftransport = new ArrayList<>();

        TableRow rowTitle = new TableRow(this);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        rowTitle.setLayoutParams(new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        FlexboxLayout flexboxLayout = new FlexboxLayout(this);
        flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
        flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_NOWRAP);
        flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND);
        flexboxLayout.setPadding(10, 20, 10, 20);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width / 8, width / 8);

        ImageView transportIcon = new ImageView(this);
        if (SelectStation.getSelectedStation() instanceof TramStation) {
            transportIcon.setImageResource(R.drawable.ic_tram);
        } else transportIcon.setImageResource(R.drawable.ic_trolleybus);
        transportIcon.setLayoutParams(layoutParams);

        ImageView timeIcon = new ImageView(this);
        timeIcon.setLayoutParams(layoutParams);
        timeIcon.setImageResource(R.drawable.ic_time);

        ImageView distanceIcon = new ImageView(this);
        distanceIcon.setImageResource(R.drawable.ic_distance);
        distanceIcon.setLayoutParams(layoutParams);

        flexboxLayout.addView(transportIcon);
        flexboxLayout.addView(timeIcon);
        flexboxLayout.addView(distanceIcon);

        rowTitle.addView(flexboxLayout);
        table.addView(rowTitle);

        final int selectedStationId = SelectStation.getSelectedStation().getIdStation();

        Thread downloadThread = new Thread(() -> {
            try {
                Document doc = Jsoup.connect("http://m.ettu.ru/station/" + selectedStationId).get();
                Elements elements = doc.select("div div div");
                for (int i = 0; i < elements.size(); i += 3) {
                    Integer idTram, timeTram, distanceTram;
                    idTram = Integer.parseInt(elements.get(i).text());
                    Element e2 = elements.get(i + 1);
                    timeTram = IntegerParser.getIntFromString(e2, e2.text());
                    Element e3 = elements.get(i + 2);
                    distanceTram = IntegerParser.getIntFromString(e3, e3.text());
                    listoftransport.add(new Tram(idTram, timeTram, distanceTram));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        downloadThread.start();
        try {
            downloadThread.join();
            Collections.sort(listoftransport, (o1, o2) -> o1.getTimeTransport().compareTo(o2.getTimeTransport()));
            boolean even = true;
            for (Transport transport : listoftransport) {
                TableRow tramRow = new TableRow(this);
                rowTitle.setLayoutParams(new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));
                FlexboxLayout flex = new FlexboxLayout(this);
                flex.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
                flex.setFlexWrap(FlexboxLayout.FLEX_WRAP_NOWRAP);
                flex.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND);
                flex.setAlignItems(FlexboxLayout.ALIGN_ITEMS_CENTER);
                flex.setAlignContent(FlexboxLayout.ALIGN_CONTENT_CENTER);
                flex.setPadding(10, 20, 10, 20);

                if (even) {
                    flex.setBackgroundColor(Color.parseColor("#E1F2F1"));
                }
                even = !even;

                TextView idTram = new TextView(this);
                idTram.setWidth(width / 3);
                idTram.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                idTram.setGravity(Gravity.CENTER);
                idTram.setText("№ " + transport.getIdTransport().toString());

                TextView timeTram = new TextView(this);
                timeTram.setWidth(width / 3);
                timeTram.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                timeTram.setGravity(Gravity.CENTER);
                timeTram.setText(transport.getTimeTransport().toString() + " мин");

                TextView distanceTram = new TextView(this);
                distanceTram.setWidth(width / 3);
                distanceTram.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                distanceTram.setGravity(Gravity.CENTER);
                distanceTram.setText(transport.getDistanceTransport().toString() + " м");

                flex.addView(idTram);
                flex.addView(timeTram);
                flex.addView(distanceTram);
                tramRow.addView(flex);
                table.addView(tramRow);
            }
            swipeLayout = (SwipeRefreshLayout) findViewById(R.id.SwipeLayout);
            swipeLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.colorPrimaryDark));
            swipeLayout.setOnRefreshListener(() -> {
                swipeLayout.setRefreshing(true);
                swipeLayout.postDelayed(() -> {
                    scrollView.removeAllViews();
                    swipeLayout.setRefreshing(false);
                    setTrams();
                }, 1000);
            });
            scrollView = (ScrollView) findViewById(R.id.scrollViewTrams);
            scrollView.addView(table);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info: {
                startActivity(new Intent(Trams.this, Info.class));
            }
            break;
            case R.id.fav: {
                startActivity(new Intent(Trams.this, Fav.class));
            }
            break;
            default:
                return false;
        }
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return true;
    }
}
