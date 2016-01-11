package be.programmeercursussen.parkinggent2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import be.programmeercursussen.parkinggent2.model.Parking;
import be.programmeercursussen.parkinggent2.receiver.DownloadResultReceiver;
import be.programmeercursussen.parkinggent2.service.DownloadService;


public class SplashScreen extends Activity implements DownloadResultReceiver.Receiver {

    private DownloadResultReceiver mReceiver;
    private final String url = "http://datatank.stad.gent/4/mobiliteit/bezettingparkingsrealtime.json";
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Allow activity to show indeterminate progressbar */
        // not in use, since AndroidManifest.xml states that there is no titlebar for this activity
        // requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_splash_screen);

        /* Starting Download Service */
        mReceiver = new DownloadResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DownloadService.class);

        /* Send optional extras to Download IntentService */
        intent.putExtra("url", url);
        intent.putExtra("receiver", mReceiver);

        startService(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.splash_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case DownloadService.STATUS_RUNNING:
                setProgressBarIndeterminateVisibility(true);
                break;
            case DownloadService.STATUS_FINISHED:                   // indien service volledig en succesvol is gestopt, dan wordt dit gestart
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);

                /* Get results from DownloadService */
                final ArrayList<Parking> results = resultData.getParcelableArrayList("result");

                /* Create runnable thread to see SplashScreen for a couple of seconds */
                handler = new Handler();

                final Intent i = new Intent(SplashScreen.this, MainActivity.class);

                final Runnable r = new Runnable() {
                    public void run() {

                        /* Create an intent to pass through the results which are Parcelable from Activity A to Activity B */

                        Bundle mBundle = new Bundle();
                        mBundle.putParcelableArrayList("parkings", results);
                        i.putExtras(mBundle);

                        startActivity(i);

                        // close this activity
                        finish();
                    }
                };


                handler.postDelayed(r, 1000);


                break;
            case DownloadService.STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
