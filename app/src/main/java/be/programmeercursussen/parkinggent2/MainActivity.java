package be.programmeercursussen.parkinggent2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import be.programmeercursussen.parkinggent2.adapter.ParkingListAdapter;
import be.programmeercursussen.parkinggent2.asynctask.DownloadTask;
import be.programmeercursussen.parkinggent2.callback.AsyncResponse;
import be.programmeercursussen.parkinggent2.model.Parking;

public class MainActivity extends Activity  {

    private ListView listView;
    private ParkingListAdapter parkingListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Context context;

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        // Construct the data source through the intent received
        ArrayList<Parking> parkingArrayList = getIntent().getParcelableArrayListExtra("parkings");

        // for logging purposes
        for (int i = 0; i < parkingArrayList.size(); i++) {
            Log.i(TAG, "naam parking : " + parkingArrayList.get(i).getName() +
                    ", adres: " + parkingArrayList.get(i).getAddress() + " " + parkingArrayList.get(i).getCity().getName());
        }

        // Create the adapter to convert the array to views
        parkingListAdapter = new ParkingListAdapter(this, parkingArrayList);

        // attach the adapter to a ListView;
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(parkingListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //get selected Parking item
                Parking parking = (Parking) adapterView.getItemAtPosition(position);

                Intent i = new Intent(MainActivity.this, Info.class);
                i.putExtra("parking", parking);
                startActivity(i);
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        // sets the colors used in the refresh animation
        mSwipeRefreshLayout.setColorSchemeResources(
                                                    android.R.color.holo_red_light,
                                                    android.R.color.holo_orange_light,
                                                    android.R.color.holo_green_light);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // run asynctask DownloadTask and get callback through interface AsyncResponse
                        // so we can do something with our ArrayList<Parking> parkingArrayList generated inside the asynctask
                        new DownloadTask(new AsyncResponse() {
                                                @Override
                                                public void processFinish(ArrayList<Parking> parkingArrayList) {

                                                    parkingListAdapter = new ParkingListAdapter(context, parkingArrayList);
                                                    listView.setAdapter(parkingListAdapter);
                                                    parkingListAdapter.notifyDataSetChanged();
                                                }
                        }).execute("http://datatank.stad.gent/4/mobiliteit/bezettingparkingsrealtime.json");
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_quit_main) {
            // put this activity to backstack activity
            moveTaskToBack(true);
            // close program
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return super.onOptionsItemSelected(item);
    }
}
