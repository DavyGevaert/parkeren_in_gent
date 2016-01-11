package be.programmeercursussen.parkinggent2.receiver;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Davy on 6/12/2015.
 */
public class DownloadResultReceiver extends ResultReceiver {
    /* variabele met als datatype de interface Receiver */
    private Receiver mReceiver;

    /* constructor */
    public DownloadResultReceiver(Handler handler) {
        super(handler);
    }

    /* setter */
    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    /* interface */
    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
