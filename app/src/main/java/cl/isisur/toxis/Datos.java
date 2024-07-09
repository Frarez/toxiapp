package cl.isisur.toxis;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Datos extends AppCompatActivity {
    private TextView tvLatitud;
    private TextView tvLongitud;
    private TextView tvUser;
    private EditText etSearchUser;
    private Button btnSearchUser;
    private Button btnIrMaps;
    private ListView listUsers;
    private DatabaseReference mDatabase;
    private ArrayList<String> userList;
    private ArrayAdapter<String> adapter;
    private String selectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos);

        tvLatitud = findViewById(R.id.tv_latitud);
        tvLongitud = findViewById(R.id.tv_longitud);
        tvUser = findViewById(R.id.tv_user);
        etSearchUser = findViewById(R.id.et_search_user);
        btnSearchUser = findViewById(R.id.btn_search_user);
        btnIrMaps = findViewById(R.id.btn_ir_maps);
        listUsers = findViewById(R.id.list_users);

        userList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        listUsers.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("usuarios");

        btnSearchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etSearchUser.getText().toString().trim();
                if (!username.isEmpty()) {
                    searchUser(username);
                } else {
                    Toast.makeText(Datos.this, "Ingrese un nombre de usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnIrMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUser != null) {
                    Intent intent = new Intent(Datos.this, MapsActivity.class);
                    intent.putExtra("username", selectedUser);
                    startActivity(intent);
                } else {
                    Toast.makeText(Datos.this, "Seleccione un usuario primero", Toast.LENGTH_SHORT).show();
                }
            }
        });

        listUsers.setOnItemClickListener((parent, view, position, id) -> {
            selectedUser = userList.get(position);
            etSearchUser.setText(selectedUser);
            searchUser(selectedUser);
        });

        fetchUsers();
    }

    private void fetchUsers() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Mapsp datos = snapshot.getValue(Mapsp.class);
                    if (datos != null && datos.getUsuario() != null && !userList.contains(datos.getUsuario())) {
                        userList.add(datos.getUsuario());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Datos.this, "Error al cargar la lista de usuarios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchUser(final String username) {
        mDatabase.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Mapsp datos = dataSnapshot.getValue(Mapsp.class);
                    if (datos != null) {
                        tvLatitud.setText("Latitud: " + datos.getLatitud());
                        tvLongitud.setText("Longitud: " + datos.getLongitud());
                        tvUser.setText("Usuario: " + datos.getUsuario());
                        selectedUser = datos.getUsuario();
                    }
                } else {
                    Toast.makeText(Datos.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    tvLatitud.setText("Latitud: ");
                    tvLongitud.setText("Longitud: ");
                    tvUser.setText("Usuario: ");
                    selectedUser = null;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Datos.this, "Error en la búsqueda", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
