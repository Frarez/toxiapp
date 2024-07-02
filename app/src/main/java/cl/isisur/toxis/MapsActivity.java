package cl.isisur.toxis;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cl.isisur.toxis.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference mDatabase;

    private ArrayList<Marker> tmpRealTimeMarker = new ArrayList<>();
    private ArrayList<Marker> realTimeMarkers = new ArrayList<>();

    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mDatabase.child("usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (Marker marker : realTimeMarkers) {
                    marker.remove();
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Mapsp mp = snapshot.getValue(Mapsp.class);
                    if (mp != null) {
                        Double latitud = mp.getLatitud();
                        Double longitud = mp.getLongitud();
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(new LatLng(latitud, longitud));
                        tmpRealTimeMarker.add(mMap.addMarker(markerOptions));
                    }
                }

                realTimeMarkers.clear();
                realTimeMarkers.addAll(tmpRealTimeMarker);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}


