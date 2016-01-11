package be.programmeercursussen.parkinggent2;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;

import be.programmeercursussen.parkinggent2.fragment.InfoFragment;
import be.programmeercursussen.parkinggent2.fragment.RouteFragment;
import be.programmeercursussen.parkinggent2.model.Parking;


public class Info extends Activity {

    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Parking parking = getIntent().getParcelableExtra("parking");

        ActionBar ab = getActionBar();
        ab.setTitle(parking.getName());
        ab.setSubtitle("Vrij : " + parking.getStatus().getAvailableCapacity() +
                " - Bezet : " + (parking.getStatus().getTotalCapacity() - parking.getStatus().getAvailableCapacity()) + " - Totaal : " + parking.getStatus().getTotalCapacity());

        // declare radioGroup component
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);

        // Checked change Listener for radioGroup
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                FragmentManager fm;
                FragmentTransaction ft;

                switch (checkedId)
                {
                    case R.id.radioRoute:
                        RouteFragment routeFragment = new RouteFragment();

                        fm = getFragmentManager();

                        ft = fm.beginTransaction();

                        ft.replace(R.id.fragment_container, routeFragment);

                        ft.commit();
                        break;
                    case R.id.radioInfo:
                        InfoFragment infoFragment = new InfoFragment();

                        fm = getFragmentManager();

                        ft = fm.beginTransaction();

                        ft.replace(R.id.fragment_container, infoFragment);

                        ft.commit();
                        break;
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_previous_info:
                // close current activity Info
                this.finish();
                break;
            case R.id.action_quit_info:
                // put this activity to backstack activity
                moveTaskToBack(true);
                // close program
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
