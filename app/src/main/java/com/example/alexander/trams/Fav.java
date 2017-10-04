package com.example.alexander.trams;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class Fav extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ArrayList<Station> listofstations;
    private Station selectedStation;
    private ArrayList<FlexboxLayout> buttons;
    private TextView tramsLabel, trolleybusLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buttons = new ArrayList<>();
        setContentView(R.layout.activity_fav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle("Избранное");
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
        navigationView.getMenu().getItem(1).setChecked(true);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearFav);

        listofstations = new ArrayList<>();
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.com_example_alexander_preference_file_key), Context.MODE_PRIVATE);
        Map<String, ?> mapOfStations = sharedPref.getAll();
        for (Map.Entry<String, ?> value : mapOfStations.entrySet()) {
            Gson gson = new Gson();
            String json = sharedPref.getString(value.getKey(), "0");
            if (!json.equals("0")) {
                Station st = gson.fromJson(json, Station.class);
                if (st.getTypeStation().equals("Tram"))
                    listofstations.add(new TramStation(st.getIdStation(), st.getNameStation()));
                else
                    listofstations.add(new TrolleybusStation(st.getIdStation(), st.getNameStation()));
            }
        }

        boolean isTram = false;
        boolean isTrolleybus = false;
        for (Station station : listofstations) {
            FlexboxLayout components = new FlexboxLayout(this);
            if (station instanceof TramStation && !isTram) {
                tramsLabel = new TextView(this);
                tramsLabel.setText("Трамваи:");
                tramsLabel.setPadding(10, 10, 10, 10);
                tramsLabel.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                tramsLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                linearLayout.addView(tramsLabel);
                isTram = true;
            }
            if (station instanceof TrolleybusStation && !isTrolleybus) {
                trolleybusLabel = new TextView(this);
                trolleybusLabel.setText("Троллейбусы:");
                trolleybusLabel.setPadding(10, 10, 10, 10);
                trolleybusLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                trolleybusLabel.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                linearLayout.addView(trolleybusLabel);
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
            components.setId(station.getIdStation());
            buttons.add(components);
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            btn.setWidth((width / 10) * 8);
            CheckBox checkbox = new CheckBox(this);
            checkbox.setId(station.getIdStation());
            checkbox.setButtonDrawable(R.drawable.checkbox_selector);

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
                } else {
                    sharedPref.edit().remove(station.getIdStation().toString()).apply();
                    linearLayout.removeView(linearLayout.findViewById(station.getIdStation()));
                    listofstations.remove(station);
                    int countOfTrams = 0;
                    int countOfTrolleybus = 0;
                    for (Station st : listofstations) {
                        if (st instanceof TramStation)
                            countOfTrams++;
                        else
                            countOfTrolleybus++;
                    }
                    if (countOfTrams == 0)
                        linearLayout.removeView(this.tramsLabel);
                    if (countOfTrolleybus == 0)
                        linearLayout.removeView(this.trolleybusLabel);
                }
            });
            checkbox.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            components.addView(btn);
            components.addView(checkbox);
            linearLayout.addView(components);
        }
    }

    private View.OnClickListener Listener() {
        View.OnClickListener listener = (v) -> {
            int selectedStationId = v.getId();
            String selectedStationName = ((TextView) v).getText().toString();
            Collections.sort(listofstations, (o1, o2) -> o1.getIdStation().compareTo(o2.getIdStation()));
            int index = Collections.binarySearch(listofstations, new Station(selectedStationId, selectedStationName), (o1, o2) -> o1.getIdStation().compareTo(o2.getIdStation()));
            selectedStation = listofstations.get(index);
            SelectStation.setSelectedStation(selectedStation);
            Intent myIntent = new Intent(Fav.this, Trams.class);
            startActivity(myIntent);
        };
        return listener;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return true;
    }
}
