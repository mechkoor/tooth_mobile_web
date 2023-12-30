package com.example.darssa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class ResetPassword extends AppCompatActivity  implements View.OnClickListener{

    String updateStudent="http://192.168.1.112:5050/etudiant/changePassword";

    EditText pswd,cpswd;
    Button add;

    String password,cpassword,email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        pswd=findViewById(R.id.pswd);
        cpswd=findViewById(R.id.cpswd);
        add=findViewById(R.id.add);

        Intent intent = getIntent();
        email=intent.getStringExtra("email");
        add.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        password = pswd.getText().toString();
        cpassword = cpswd.getText().toString();
        if (password.equals(cpassword)) {
            System.out.println(email);
            System.out.println(password);
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);
                jsonBody.put("password", password);
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        updateStudent,
                        jsonBody,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                    Toast.makeText(ResetPassword.this, "Mot De Passe change avec succes", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ResetPassword.this, LoginActivity.class);
                                    startActivity(intent);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(ResetPassword.this, "Erreur de connexion avec le serveur", Toast.LENGTH_SHORT).show();
                            }
                        }
                );


                Volley.newRequestQueue(this).add(request);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Les deux mot de passes sont differents", Toast.LENGTH_SHORT).show();
        }
    }

}