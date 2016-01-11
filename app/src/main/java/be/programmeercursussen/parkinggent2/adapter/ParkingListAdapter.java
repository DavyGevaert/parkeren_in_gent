package be.programmeercursussen.parkinggent2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import be.programmeercursussen.parkinggent2.R;
import be.programmeercursussen.parkinggent2.model.Parking;

/**
 * Created by Davy on 20/12/2015.
 */
public class ParkingListAdapter extends ArrayAdapter<Parking> {

    // View lookup cache
    private static class ViewHolder {
        TextView parking_name;
        TextView free_capacity;
    }

    public ParkingListAdapter(Context context, ArrayList<Parking> parkings) {
        super(context, 0, parkings);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Parking parking = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_parking, parent, false);
            viewHolder.parking_name = (TextView) convertView.findViewById(R.id.tv_parking_name);
            viewHolder.free_capacity = (TextView) convertView.findViewById(R.id.tv_free_capacity);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        viewHolder.parking_name.setText(parking.getName());
        viewHolder.free_capacity.setText("Vrij : " + parking.getStatus().getAvailableCapacity() +
                " - Bezet : " + (parking.getStatus().getTotalCapacity() - parking.getStatus().getAvailableCapacity()) + " - Totaal : " + parking.getStatus().getTotalCapacity());

        return convertView;
    }
}
