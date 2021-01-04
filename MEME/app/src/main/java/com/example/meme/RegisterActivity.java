package com.example.meme;

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
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class RegisterActivity extends AppCompatActivity {

	FirebaseAuth auth;

	EditText usernameEditText;
	EditText emailEditText;
	EditText passwordEditText;
	Button signUpButton;
	LinearLayout loginActivityButton;

	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		// Initialize firebase auth
		auth = FirebaseAuth.getInstance();

		// Checks if user is already logged in
		if (auth.getCurrentUser() != null) {
			// Move user to profile activity
			// Create new intent
			Intent homeIntent = new Intent(RegisterActivity.this, HomeActivity.class);
			// Start profile activity
			startActivity(homeIntent);
			// Finish the current activity (Register activity).
			// Prevent user from coming back to this activity
			finish();
		}

		// Initially hide the keyboard when the activity starts
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		// Initialize Views
		usernameEditText = findViewById(R.id.username_edit_text);
		emailEditText = findViewById(R.id.email_edit_text);
		passwordEditText = findViewById(R.id.password_edit_text);
		loginActivityButton = findViewById(R.id.login_activity_button);
		signUpButton = findViewById(R.id.sign_up_button);
		progressBar = findViewById(R.id.progress_bar);

		setupListeners();
	}

	private void setupListeners() {
		// Set on click listener for sign up
		signUpButton.setOnClickListener(v -> {
			String username = usernameEditText.getText().toString();
			String email = emailEditText.getText().toString();
			String password = passwordEditText.getText().toString();

			registerUser(username, email, password);
		});

		// Set onClick listener for loginActivityButton to switch to login activity
		loginActivityButton.setOnClickListener(v -> {
			Intent loginActivityIntent = new Intent(RegisterActivity.this, LoginActivity.class);
			startActivity(loginActivityIntent);
		});

	}

	private void registerUser(final String username, final String email, final String password) {

		try {
			// Make POST request to url
			RequestQueue requestQueue = Volley.newRequestQueue(RegisterActivity.this);
			String registerURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/register";
			JSONObject registrationInfo = new JSONObject();
			registrationInfo.put("username", username);
			registrationInfo.put("email", email);
			registrationInfo.put("password", password);

			final String requestBody = registrationInfo.toString();

			progressBar.setVisibility(View.VISIBLE);
			StringRequest stringRequest = new StringRequest(Request.Method.POST, registerURL, response -> auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
				progressBar.setVisibility(View.GONE);
				Intent detailsActivityIntent = new Intent(
						RegisterActivity.this,
						DetailsActivity.class
				);

				startActivity(detailsActivityIntent);
			}), error -> {
				if(error != null) {
					int statusCode = error.networkResponse.statusCode;
					try {
						if (statusCode == 400) {
								String jsonError = new String(error.networkResponse.data);
								JSONObject errRegistrationInformation = new JSONObject(jsonError);
								progressBar.setVisibility(View.GONE);

								// Get all the errors
								String errEmail = errRegistrationInformation.getString("errEmail");
								String errUsername = errRegistrationInformation.getString("errUsername");
								String errPassword = errRegistrationInformation.getString("errPassword");

								// if errors exists, then display those errors
								emailEditText.setError(errEmail.isEmpty() ? null : errEmail);
								usernameEditText.setError(errUsername.isEmpty() ? null : errUsername);
								passwordEditText.setError(errPassword.isEmpty() ? null : errPassword);

						}
					} catch (JSONException | NullPointerException e) {
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
