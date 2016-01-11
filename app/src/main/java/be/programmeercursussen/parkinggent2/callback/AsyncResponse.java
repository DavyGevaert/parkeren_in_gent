package be.programmeercursussen.parkinggent2.callback;

import java.util.ArrayList;

import be.programmeercursussen.parkinggent2.model.Parking;

/**
 * Created by Davy on 28/12/2015.
 */
public interface AsyncResponse {
    void processFinish(ArrayList<Parking> parkingArrayList);
}
