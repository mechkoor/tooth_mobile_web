package com.example.darssa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.darssa.adapters.PwAdapter;
import com.example.darssa.beans.PW;
import com.example.darssa.config.APIConfig;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PwActivity extends AppCompatActivity implements View.OnClickListener{

        private FloatingActionButton profil,soummis;
        byte[] bitmapByteArray;
        String email, username, nom, prenom, code;
        long studentId;
        List<PW> pws=new ArrayList<>();
        RecyclerView recyclerView;
        PwAdapter studentadapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_pw);


            Intent intent = getIntent();
            bitmapByteArray = intent.getByteArrayExtra("bitmap");
            email = intent.getStringExtra("email");
            username = intent.getStringExtra("username");
            nom = intent.getStringExtra("nom");
            prenom = intent.getStringExtra("prenom");
            code = intent.getStringExtra("code");
            studentId= getIntent().getLongExtra("studentid", -1);


            loadProfs();

            recyclerView = findViewById(R.id.recyclerView);
            studentadapter = new PwAdapter(PwActivity.this,PwActivity.this,pws);
            recyclerView.setAdapter(studentadapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(PwActivity.this));

            profil=findViewById(R.id.profil);
            profil.setOnClickListener(this);

            soummis=findViewById(R.id.soummis);
            soummis.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PwActivity.this, StudentPWActivity.class);
                    intent.putExtra("id", studentId);
                    startActivity(intent);
                }
            });






        }



    private void loadProfs() {
        String endpoint = "/etudiant/all?code=" + code;
        String url = APIConfig.BASE_URL + endpoint;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("JSONResponse", response.toString());

                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                int id = jsonObject.getInt("id");
                                String title = jsonObject.getString("title");
                                String docs = jsonObject.getString("docs");
                                String objectif = jsonObject.getString("objectif");
                                JSONObject tooth =jsonObject.getJSONObject("tooth");
                                String nom=tooth.getString("name");


                                PW pw = new  PW(id,studentId,title,objectif,docs,nom);
                                pws.add(pw);

                            }
                            studentadapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PwActivity.this, "Erreur de chargement de donnes", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(PwActivity.this, ProfilActivity.class);
        intent.putExtra("code", code);
        if (bitmapByteArray != null) {
            intent.putExtra("bitmap", bitmapByteArray);
        }
        intent.putExtra("email", email);
        intent.putExtra("nom", nom);
        intent.putExtra("prenom", prenom);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}