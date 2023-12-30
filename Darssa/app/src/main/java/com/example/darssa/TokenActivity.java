package com.example.darssa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TokenActivity extends AppCompatActivity implements View.OnClickListener {

    private String token,email;
    private String check;

    private Button verifier;

    private EditText token_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);


        token_txt=findViewById(R.id.token);
        verifier=findViewById(R.id.verifier);



        verifier.setOnClickListener(this);



        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        email=intent.getStringExtra("email");
    }

    @Override
    public void onClick(View v) {
        check=token_txt.getText().toString();
        if(check.equals(token)){
            Intent intent = new Intent(TokenActivity.this, ResetPassword.class);
            intent.putExtra("email", email);
            startActivity(intent);
        }else{
            Toast.makeText(TokenActivity.this, "Le token n'est pas valide", Toast.LENGTH_SHORT).show();
        }
    }
}