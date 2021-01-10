package com.example.meme.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meme.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

	private FirebaseAuth auth;
	private AlertDialog.Builder alertDialog;

	private EditText emailEditText;
	private Button sendEmailButton;
	private ProgressBar progressBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);

		initializeVariables();
		setListeners();
	}

	private void initializeVariables() {
		auth = FirebaseAuth.getInstance();
		emailEditText = findViewById(R.id.email_edit_text);
		sendEmailButton = findViewById(R.id.send_password_reset_email);
		progressBar = findViewById(R.id.progress_bar);
		alertDialog = new AlertDialog.Builder(ForgotPasswordActivity.this);
		alertDialog.setPositiveButton("OK", null);
	}

	private void setListeners() {
		sendEmailButton.setOnClickListener(v -> sendPasswordResetEmail());
	}

	private void sendPasswordResetEmail() {
		if(emailEditText.getText().toString().isEmpty()) {
			alertDialog.setTitle("Insufficient details");
			alertDialog.setMessage("Provide an E-mail address");
			alertDialog.show();
			return;
		}

		progressBar.setVisibility(View.VISIBLE);
		auth.sendPasswordResetEmail(emailEditText.getText().toString()).addOnSuccessListener(aVoid -> {
			progressBar.setVisibility(View.GONE);
			Toast.makeText(ForgotPasswordActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
		}).addOnFailureListener(e -> {
			alertDialog.setTitle("Wrong details");
			progressBar.setVisibility(View.GONE);
			alertDialog.setMessage(e.getMessage());
			alertDialog.show();
		});
	}
}
