package com.example.meme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class EmailConfirmationActivity extends AppCompatActivity {

	ProgressBar progressBar;

	FirebaseAuth auth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_email_confirmation);

		// Initialize views
		progressBar = findViewById(R.id.progress_bar);

		auth = FirebaseAuth.getInstance();

		// Initialize views
		Button emailVerificationButton = findViewById(R.id.email_confirmation_button);
		Button sendEmailButton = findViewById(R.id.send_email_button);

		// Set on click listener for emailConfirmation
		emailVerificationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				verifyAccount();
			}
		});

		sendEmailButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendVerificationEmail();
			}
		});
	}

	private void verifyAccount() {
		progressBar.setVisibility(View.VISIBLE);
		auth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				if(auth.getCurrentUser().isEmailVerified()) {
					progressBar.setVisibility(View.GONE);
					// Go to home activity
					Intent homeIntent = new Intent(EmailConfirmationActivity.this, HomeActivity.class);
					startActivity(homeIntent);
				} else {
					Toast.makeText(EmailConfirmationActivity.this, "Please verify account first", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void sendVerificationEmail() {
		Objects.requireNonNull(auth.getCurrentUser()).sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Toast.makeText(EmailConfirmationActivity.this, "Verification E-mail sent", Toast.LENGTH_SHORT).show();
			}
		});
	}
}
