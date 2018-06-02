package com.zillion.dostrider.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.zillion.dostrider.R;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {

    View myView;

    public CustomInfoWindow(Context context){
        myView = LayoutInflater.from(context)
                .inflate(R.layout.custom_rider_info_window, null);

    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView textPickupTitle = (TextView) myView.findViewById(R.id.textPickupInfo);
        textPickupTitle.setText(marker.getTitle());

        TextView textPickupSnippet = (TextView) myView.findViewById(R.id.textPickupSnippet);
        textPickupSnippet.setText(marker.getSnippet());

        return myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
