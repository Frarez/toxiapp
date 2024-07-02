package cl.isisur.toxis;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FusedLocationProviderClient fusedLocationClient;
    DatabaseReference mDatabase;
    private Button mBtnMaps;
    private EditText etc1;
    private EditText etc2;
    private EditText etc3;
    private EditText etc4;
    private TextView tvresul;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        etc1= (EditText)findViewById(R.id.txt_certamen1);
        etc2= (EditText)findViewById(R.id.txt_certamen2);
        etc3= (EditText)findViewById(R.id.txt_certamen3);
        etc4= (EditText)findViewById(R.id.txt_certamen4);
        tvresul= (TextView)findViewById(R.id.tv_resultado);

        // Adjust the insets for the view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mDatabase = FirebaseDatabase.getInstance() .getReference();
        mBtnMaps = findViewById(R.id.btnMaps);
        mBtnMaps.setOnClickListener(this);

        // Get the last known location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.e("Latitud:  ", +location.getLatitude()+"Longitud:  "+ location.getLongitude() );
                            Map<String, Object> latlang;
                            latlang = new HashMap<>();
                            latlang.put("latitud", location.getLatitude());
                            latlang.put("longitud", location.getLongitude());
                            mDatabase.child("usuarios").push().setValue(latlang);
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnMaps) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        }
    }

    public void calcular(View view) {
        try {
            String certamen1_String = etc1.getText().toString();
            String certamen2_String = etc2.getText().toString();
            String certamen3_String = etc3.getText().toString();
            String certamen4_String = etc4.getText().toString();

            // Verificar que los campos no estén vacíos
            if (certamen1_String.isEmpty() || certamen2_String.isEmpty() || certamen3_String.isEmpty() || certamen4_String.isEmpty()) {
                tvresul.setText("Por favor, ingresa todas las notas.");
                return;
            }

            int certamen1_int = Integer.parseInt(certamen1_String);
            int certamen2_int = Integer.parseInt(certamen2_String);
            int certamen3_int = Integer.parseInt(certamen3_String);
            int certamen4_int = Integer.parseInt(certamen4_String);

            int promedio = (certamen1_int + certamen2_int + certamen3_int + certamen4_int) / 4;

            if (promedio >= 51) {
                tvresul.setText("Aprobado con " + promedio);
            } else {
                tvresul.setText("Reprobado con " + promedio);
            }
        } catch (NumberFormatException e) {
            tvresul.setText("Por favor, ingresa solo números válidos.");
        }
    }


}
