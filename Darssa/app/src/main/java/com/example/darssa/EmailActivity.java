package com.example.darssa;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.darssa.config.APIConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailActivity extends AppCompatActivity {

    private static final String EMAIL = "mohjamoutawadii1@gmail.com";
    private static final String PASSWORD = "ffik puzw oeiy yoxa";

    private EditText email;



    String receiver;

    String randomToken;

    private static final int TOKEN_LENGTH = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        Button sendEmailButton = findViewById(R.id.sendEmailButton);
        email=findViewById(R.id.email);

        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiver = email.getText().toString();
                randomToken = generateRandomToken();
                check();

            }
        });


    }



    public static String generateRandomToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        StringBuilder tokenBuilder = new StringBuilder();
        for (byte b : randomBytes) {
            tokenBuilder.append(String.format("%02x", b));
        }

        return tokenBuilder.toString();
    }

    private void check() {
        String endpoint = "/etudiant/check?email=" + receiver;
        String url = APIConfig.BASE_URL + endpoint;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if ("Email trouvé dans la base de données".equals(response)) {
                            sendEmail(randomToken);
                        } else {
                            Toast.makeText(EmailActivity.this, "Email n'existe pas", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EmailActivity.this, "Erreur de communication avec le serveur", Toast.LENGTH_SHORT).show();
                    }
                }

        );

        Volley.newRequestQueue(this).add(stringRequest);
    }



    private void sendEmail(final String token) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                                return new javax.mail.PasswordAuthentication(EMAIL, PASSWORD);
                            }
                        });

                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(EMAIL));
                    message.setRecipients(Message.RecipientType.TO,
                            InternetAddress.parse(receiver));
                    message.setSubject("Votre token de vérification");
                    message.setText("Votre token est : " + token);

                    Transport.send(message);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EmailActivity.this, "E-mail envoyé avec succès", Toast.LENGTH_SHORT).show();
                            Intent tokenIntent = new Intent(EmailActivity.this, TokenActivity.class);
                            tokenIntent.putExtra("email", receiver);
                            tokenIntent.putExtra("token", token);
                            startActivity(tokenIntent);
                        }
                    });
                    Intent intent = new Intent(EmailActivity.this, TokenActivity.class);
                    intent.putExtra("token", token);
                    startActivity(intent);

                } catch (MessagingException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EmailActivity.this, "Erreur lors de l'envoi de l'e-mail", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return null;
            }
        }.execute();
    }
}
