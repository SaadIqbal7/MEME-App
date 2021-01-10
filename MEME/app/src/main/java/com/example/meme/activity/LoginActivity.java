package com.example.meme.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.meme.R;
import com.example.meme.container.Helpers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

	FirebaseAuth auth;
	private EditText emailEditText;
	private EditText passwordEditText;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		auth = FirebaseAuth.getInstance();

		// Checks if user is already logged in
		if (auth.getCurrentUser() != null) {
			// Move user to profile activity
			// Create new intent
			Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
			// Start profile activity
			startActivity(homeIntent);
			// Finish the current activity
			// Prevent user from coming back to this activity
			finish();
		}
		// Initially hide the keyboard when the activity starts
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		// Initialize Views
		emailEditText = findViewById(R.id.email_edit_text);
		passwordEditText = findViewById(R.id.password_edit_text);
		progressBar = findViewById(R.id.progress_bar);

		LinearLayout registerActivityButton = findViewById(R.id.register_activity_button);
		Button loginButton = findViewById(R.id.login_button);
		LinearLayout forgotPasswordButton = findViewById(R.id.forgot_password_button);

		// Set onClick listener for registerActivityButton to switch to register activity
		registerActivityButton.setOnClickListener(v -> {
			Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
			startActivity(registerIntent);
		});

		loginButton.setOnClickListener(v -> {
			String email = emailEditText.getText().toString();
			String password = passwordEditText.getText().toString();
			loginUser(email, password);
		});

		forgotPasswordButton.setOnClickListener(v -> {
			Intent forgotPasswordIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
			startActivity(forgotPasswordIntent);
		});
	}

	private void loginUser(final String email, final String password) {
		try {
			// Make POST request to url
			RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
			String loginURL = Helpers.apiUrl + "/webApi/api/login";
			JSONObject registrationInfo = new JSONObject();
			registrationInfo.put("email", email);
			registrationInfo.put("password", password);

			final String requestBody = registrationInfo.toString();

			progressBar.setVisibility(View.VISIBLE);
			StringRequest stringRequest = new StringRequest(Request.Method.POST, loginURL, response -> auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
				progressBar.setVisibility(View.GONE);
				// Check if user hasn't provided details. redirect to details activity
				if (auth.getCurrentUser() != null
						&& auth.getCurrentUser().getDisplayName() == null) {
					Intent detailsActivityIntent = new Intent(LoginActivity.this, DetailsActivity.class);
					startActivity(detailsActivityIntent);
				}
				// Check if the user has provided details but hasn't verified their account
				else if (auth.getCurrentUser() != null
						&& auth.getCurrentUser().getDisplayName() != null
						&& !auth.getCurrentUser().isEmailVerified()) {
					Intent confirmationActivityIntent = new Intent(LoginActivity.this, EmailConfirmationActivity.class);
					startActivity(confirmationActivityIntent);
				}
				// Check if all details are provided and user has verified their account
				else if (auth.getCurrentUser() != null
						&& auth.getCurrentUser().getDisplayName() != null
						&& auth.getCurrentUser().isEmailVerified()) {
					Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
					startActivity(homeIntent);
					LoginActivity.this.finish();
				}
			}).addOnFailureListener(e -> {
				progressBar.setVisibility(View.GONE);
				if (e instanceof FirebaseAuthInvalidCredentialsException) {
					passwordEditText.setError(Objects.requireNonNull(e.getLocalizedMessage()).isEmpty() ? null : "Incorrect password");
				}
			}), error -> {
				progressBar.setVisibility(View.GONE);
				if(error != null) {
					try {
						int statusCode = error.networkResponse.statusCode;
						if (statusCode == 400) {
							String jsonErrorResponse = new String(error.networkResponse.data);
							JSONObject errLoginInformation = new JSONObject(jsonErrorResponse);

							// Get all the errors
							String errEmail = errLoginInformation.getString("errEmail");
							String errPassword = errLoginInformation.getString("errPassword");

							// if errors exists, then display those errors
							emailEditText.setError(errEmail.isEmpty() ? null : errEmail);
							passwordEditText.setError(errPassword.isEmpty() ? null : errPassword);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}) {
				@Override
				public String getBodyContentType() {
					return "application/json; charset=utf-8";
				}

				@Override
				public byte[] getBody() {
					return requestBody.isEmpty() ? null : requestBody.getBytes(StandardCharsets.UTF_8);
				}
			};

			// Prevent multiple volley requests
			stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

			requestQueue.add(stringRequest);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
