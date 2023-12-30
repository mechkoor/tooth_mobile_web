package com.example.darssa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.darssa.adapters.StudentPWadapter;
import com.example.darssa.beans.StudentPW;
import com.example.darssa.config.APIConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class StudentPWActivity extends AppCompatActivity {

    List<StudentPW> studentpws=new ArrayList<>();
    RecyclerView recyclerView;
    StudentPWadapter studentpwadapter;
    private long studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_pwactivity);

        studentId= getIntent().getLongExtra("id", -1);

        loadTp();

        recyclerView = findViewById(R.id.recyclerView);
        studentpwadapter = new StudentPWadapter(StudentPWActivity.this,StudentPWActivity.this,studentpws);
        recyclerView.setAdapter(studentpwadapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(StudentPWActivity.this));
    }


    private void loadTp() {
        String endpoint = "/etudiant/studentpw?id=" + studentId;
        String url = APIConfig.BASE_URL + endpoint;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("JSONResponse", response.toString());

                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                JSONObject pw=jsonObject.getJSONObject("pw");
                                String title = pw.getString("title");
                                String remarque = !jsonObject.isNull("remarque") ? jsonObject.getString("remarque") : "pas de remarque pour le moment";

                                // Vérifier si la clé "note" existe
                                String validation = !(jsonObject.getString("note").equals("0")) ? jsonObject.getString("note") : "pas de note pour le moment";
                                    byte[] photoBytes = Base64.decode(jsonObject.getString("image3"), Base64.DEFAULT);
                                    // Convertir les octets de l'image en Bitmap
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);

                                    // Convertir le Bitmap en tableau d'octets
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                    byte[] bitmapByteArray = byteArrayOutputStream.toByteArray();

                                StudentPW studentpw = new StudentPW(remarque, validation, title,bitmapByteArray);
                                studentpws.add(studentpw);

                            }
                            studentpwadapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(StudentPWActivity.this, "Erreur de chargement de donnes", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }
}