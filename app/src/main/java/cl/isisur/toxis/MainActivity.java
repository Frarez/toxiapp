package cl.isisur.toxis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference mDatabase;
    private Button btnVerDatos;
    private Button btnCalcular;
    private EditText etc1;
    private EditText etc2;
    private EditText etc3;
    private EditText etc4;
    private TextView tvresul;
    private Handler handler;
    private Runnable runnable;
    private static final int MAX_LOCATIONS = 10;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etc1 = findViewById(R.id.txt_certamen1);
        etc2 = findViewById(R.id.txt_certamen2);
        etc3 = findViewById(R.id.txt_certamen3);
        etc4 = findViewById(R.id.txt_certamen4);
        tvresul = findViewById(R.id.tv_resultado);

        btnCalcular = findViewById(R.id.btn_calcular);
        btnCalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcular(v);
            }
        });

        btnVerDatos = findViewById(R.id.btn_ver_datos);
        btnVerDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Datos.class);
                startActivity(intent);
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("usuarios");

        // Generar o cargar nombre de usuario aleatorio
        username = generateUsername();

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                obtenerUbicacion();
                handler.postDelayed(this, 60000); // Ejecutar cada 1 minuto
            }
        };

        handler.post(runnable);
    }

    private String generateUsername() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String storedUsername = prefs.getString("username", null);

        if (storedUsername == null) {
            // Generar un nuevo nombre de usuario aleatorio
            String newUsername = "User" + new Random().nextInt(100000);
            // Guardar el nombre de usuario en las preferencias compartidas
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("username", newUsername);
            editor.apply();
            return newUsername;
        } else {
            // Usar el nombre de usuario almacenado
            return storedUsername;
        }
    }

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Log.d("Latitud: ", String.valueOf(latitude));
                            Log.d("Longitud: ", String.valueOf(longitude));

                            // Guardar en Firebase y actualizar datos existentes
                            guardarUbicacionEnFirebase(latitude, longitude, username);
                        }
                    }
                });
    }

    private void guardarUbicacionEnFirebase(double latitude, double longitude, String usuario) {
        Map<String, Object> latlang = new HashMap<>();
        latlang.put("latitud", latitude);
        latlang.put("longitud", longitude);
        latlang.put("usuario", usuario);
        mDatabase.child(usuario).setValue(latlang);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacion();
            } else {
                // Permiso denegado
            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
