package be.programmeercursussen.parkinggent2.fragment;



import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import be.programmeercursussen.parkinggent2.R;
import be.programmeercursussen.parkinggent2.model.Parking;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class InfoFragment extends Fragment {

    private Parking parking;
    private TextView adress;
    private TextView contactInfo;
    private TextView openingTimesInfo;


    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        // assign variables to their id's in layout fragment_info
        adress = (TextView) view.findViewById(R.id.tv_info_adress);
        contactInfo = (TextView) view.findViewById(R.id.tv_info_contact);
        openingTimesInfo = (TextView) view.findViewById(R.id.tv_info_openingsTimesInfo);

        // get our object send by the Intent from the SplashScreen which had a Service running (network operation)
        parking = getActivity().getIntent().getExtras().getParcelable("parking");

        // assign data to the variables
        adress.setText(parking.getAddress());
        contactInfo.setText(parking.getContactInfo());
        openingTimesInfo.setText(parking.getOpeningTimesInfo().getText());

        return view;
    }

    private void returnImage(String parkingName) {
        // datatype Image?
        // switch op basis van parkingnaam en laad afbeelding van in drawable
        // return
    }
}
