package com.example.darssa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.darssa.config.APIConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ProfilActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView profil;

    String Username,Email,lastName,FirstName;

    EditText email, username, nom, prenom;

    TextView groupe;
    String email_txt, username_txt, nom_txt, prenom_txt, code;

    private Button selectImageButton,envoyer,changer;

    Bitmap selectedImage;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        selectImageButton = findViewById(R.id.pickImageButton);

        profil = findViewById(R.id.profil);
        email = findViewById(R.id.email2);
        username = findViewById(R.id.username2);
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        groupe = findViewById(R.id.groupe);
        envoyer=findViewById(R.id.changeProfil);
        changer=findViewById(R.id.pass);

        Intent intent = getIntent();
        byte[] bitmapByteArray = intent.getByteArrayExtra("bitmap");
        if (bitmapByteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
            profil.setImageBitmap(bitmap);
        }


        email_txt = intent.getStringExtra("email");
        username_txt = intent.getStringExtra("username");
        nom_txt = intent.getStringExtra("nom");
        prenom_txt = intent.getStringExtra("prenom");
        code = intent.getStringExtra("code");



        email.setText(email_txt);
        username.setText(username_txt);
        nom.setText(nom_txt);
        prenom.setText(prenom_txt);
        groupe.setText(code);

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
        envoyer.setOnClickListener(this);
        changer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfilActivity.this,EmailActivity.class);
                startActivity(intent);
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // L'utilisateur a choisi une image
            Uri imageUri = data.getData();

            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);


                // Mettre à jour l'image dans l'ImageView
                profil.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }



    public void onClick(View v) {
        Username = username.getText().toString();
        Email = email.getText().toString();
        lastName = nom.getText().toString();
        FirstName = prenom.getText().toString();
        String endpoint = "/etudiant/changeProfil?email=" + email_txt;
        String url = APIConfig.BASE_URL + endpoint;


        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("userName", Username);
            jsonBody.put("email", Email);
            jsonBody.put("firstName", FirstName);
            jsonBody.put("lastName", lastName);

            if (selectedImage != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                jsonBody.put("photo", Base64.encodeToString(imageBytes, Base64.DEFAULT));
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(ProfilActivity.this, "Profil changé avec succès", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(ProfilActivity.this, "Erreur de communication avec le serveur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Volley Error", "Erreur de communication avec le serveur", error);
                        }
                    }
            );

            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
