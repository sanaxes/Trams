package com.example.alexander.trams;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.*;

import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

public class SelectStation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static char letter;
    private static ArrayList<Station> listofstations;
    private static Station selectedStation;

    public static Station getSelectedStation() {
        return selectedStation;
    }

    public static void setSelectedStation(Station station) {
        selectedStation = station;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_station);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        letter = MainActivity.getLetter();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearStation);
        Thread thread = new Thread(() -> {
            try {
                listofstations = new ArrayList<>();
                Document doc = Jsoup.connect("http://m.ettu.ru/stations/" + Character.toUpperCase(letter)).get();
                Elements elements = doc.getAllElements();
                int isEnd = 0;
                for (Element e : elements) {
                    if (e.is("h3"))
                        isEnd++;
                    if (e.hasAttr("href") && e.attr("href").startsWith("/station")) {
                        Integer idStation = IntegerParser.getIntFromString(e, e.attr("href"));
                        String nameStation = e.text();
                        if (isEnd == 2) {
                            listofstations.add(new TrolleybusStation(idStation, nameStation));
                        } else
                            listofstations.add(new TramStation(idStation, nameStation));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            thread.join();
            boolean isTram = false;
            boolean isTrolleybus = false;
            for (Station station : listofstations) {
                FlexboxLayout components = new FlexboxLayout(this);
                if (station instanceof TramStation && !isTram) {
                    TextView t = new TextView(this);
                    t.setText("Трамваи: ");
                    t.setPadding(10, 10, 10, 10);
                    t.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                    linearLayout.addView(t);
                    isTram = true;
                }
                if (station instanceof TrolleybusStation && !isTrolleybus) {
                    TextView k = new TextView(this);
                    k.setText("Троллейбусы: ");
                    k.setPadding(10, 10, 10, 10);
                    k.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                    k.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    linearLayout.addView(k);
                    isTrolleybus = true;
                }
                components.setFlexWrap(FlexboxLayout.FLEX_WRAP_NOWRAP);
                components.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
                components.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND);
                Button btn = new Button(this);
                btn.setId(station.getIdStation());
                btn.setText(station.getNameStation());
                btn.setOnClickListener(Listener());
                btn.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                btn.setOnClickListener(Listener());
                btn.setBackgroundColor(Color.TRANSPARENT);
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                components.setBackgroundColor(ContextCompat.getColor(this, R.color.buttons_light));
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                btn.setWidth((width / 10) * 8);
                CheckBox checkbox = new CheckBox(this);
                checkbox.setId(station.getIdStation());
                checkbox.setButtonDrawable(R.drawable.checkbox_selector);
                SharedPreferences sharedPref = this.getSharedPreferences(
                        getString(R.string.com_example_alexander_preference_file_key), Context.MODE_PRIVATE);

                Gson gson = new Gson();
                String json = sharedPref.getString(station.getIdStation().toString(), "0");
                int currentStation = -1;
                if (!json.equals("0")) {
                    Station st = gson.fromJson(json, Station.class);
                    currentStation = st.getIdStation();
                }
                if (currentStation == station.getIdStation())
                    checkbox.setChecked(true);
                checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        String JSON = gson.toJson(station);
                        sharedPref.edit().putString(station.getIdStation().toString(), JSON).apply();
                        Toast.makeText(this, "Добавлено в избранное!", Toast.LENGTH_SHORT).show();
                    } else
                        sharedPref.edit().remove(station.getIdStation().toString()).apply();
                });
                checkbox.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                components.addView(btn);
                components.addView(checkbox);
                linearLayout.addView(components);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener Listener() {
        View.OnClickListener listener = (v) -> {
            int selectedStationId = v.getId();
            String selectedStationName = ((TextView) v).getText().toString();
            Collections.sort(listofstations, (o1, o2) -> o1.getIdStation().compareTo(o2.getIdStation()));
            int index = Collections.binarySearch(listofstations, new Station(selectedStationId, selectedStationName), (o1, o2) -> o1.getIdStation().compareTo(o2.getIdStation()));
            selectedStation = listofstations.get(index);
            Intent myIntent = new Intent(SelectStation.this, Trams.class);
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
            this.overridePendingTransition(R.anim.trans_right_in,
                    R.anim.trans_right_out);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info: {
                startActivity(new Intent(SelectStation.this, Info.class));
            }
            break;
            case R.id.fav: {
                startActivity(new Intent(SelectStation.this, Fav.class));
            }
            break;
            default:
                return false;
        }
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return true;
    }
}
