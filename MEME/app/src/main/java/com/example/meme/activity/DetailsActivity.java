package com.example.meme.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.meme.container.Helpers;
import com.example.meme.fragment.DatePickerFragment;
import com.example.meme.R;
import com.example.meme.container.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DetailsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

	private EditText fullNameEditText;
	private EditText countryEditText ;
	private EditText dateOfBirthEditText;
	private EditText profileDescriptionEditText;
	private Spinner gendersSpinner;
	private ProgressBar progressBar;

	FirebaseAuth auth;

	User user;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		user = new User();

		// Initialize views
		fullNameEditText = findViewById(R.id.full_name_edit_text);
		countryEditText = findViewById(R.id.country_edit_text);
		dateOfBirthEditText = findViewById(R.id.date_of_birth_edit_text);
		profileDescriptionEditText = findViewById(R.id.profile_description_edit_text);
		gendersSpinner = findViewById(R.id.genders_spinner);
		progressBar = findViewById(R.id.progress_bar);

		Button addDetailsButton = findViewById(R.id.add_details_button);

		auth = FirebaseAuth.getInstance();

		// Populate spinner for genders
		ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this, R.array.genders, R.layout.spinner_item_layout);
		genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_layout);
		gendersSpinner.setAdapter(genderAdapter);

		// Set on click listener on edit text
		dateOfBirthEditText.setOnClickListener(v -> {
			// Create the date picker fragment
			DialogFragment datePicker = new DatePickerFragment();

			// Show the date picker fragment
			datePicker.show(getSupportFragmentManager(), "datePicker");
		});

		// Set on click listener for add_details_button
		addDetailsButton.setOnClickListener(v -> Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
			String token = getTokenResult.getToken();
			user.setFullName(fullNameEditText.getText().toString());
			user.setLocation(countryEditText.getText().toString());
			user.setGender(gendersSpinner.getSelectedItem().toString());
			user.setDateOfBirth(dateOfBirthEditText.getText().toString());
			user.setDescription(profileDescriptionEditText.getText().toString());
			addDetails(user, token);
		}));
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
		// Set the text of edit text with the selected date
		dateOfBirthEditText.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
	}

	private void addDetails(final User user, final String token) {
		try{
			RequestQueue requestQueue = Volley.newRequestQueue(DetailsActivity.this);
			String userURL = Helpers.apiUrl + "/webApi/api/user";

			JSONObject userInfo = new JSONObject();
			userInfo.put("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail());
			userInfo.put("fullName", user.getFullName());
			userInfo.put("location", user.getLocation());
			userInfo.put("gender", user.getGender());
			userInfo.put("dob", user.getDateOfBirth());
			userInfo.put("description", user.getDescription());

			final String requestBody = userInfo.toString();

			progressBar.setVisibility(View.VISIBLE);
			StringRequest stringRequest = new StringRequest(Request.Method.POST, userURL, response -> {
				final UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
						.setDisplayName(user.getFullName())
						.build();
				auth.getCurrentUser().updateProfile(profileUpdates)
				.addOnSuccessListener(aVoid -> auth.getCurrentUser()
						.sendEmailVerification()
						.addOnSuccessListener(aVoid1 -> {
							progressBar.setVisibility(View.GONE);

							Intent confirmationActivityIntent = new Intent(DetailsActivity.this, EmailConfirmationActivity.class);
							startActivity(confirmationActivityIntent);
						}));
			}, error -> {
				progressBar.setVisibility(View.GONE);
				if(error != null) {
					try{
						int statusCode = error.networkResponse.statusCode;
						String jsonErrorResponse = new String(error.networkResponse.data);
						if(statusCode == 400) {
							JSONObject errUserInfo = new JSONObject(jsonErrorResponse);

							String errFullName = errUserInfo.getString("errFullName");
							String errGender = errUserInfo.getString("errGender");
							String errLocation = errUserInfo.getString("errLocation");
							String errDOB = errUserInfo.getString("errDOB");

							fullNameEditText.setError(errFullName.isEmpty() ? null : errFullName);
							countryEditText.setError(errLocation.isEmpty() ? null : errLocation);
							dateOfBirthEditText.setError(errDOB.isEmpty() ? null : errDOB);
						} else if(statusCode == 403) {
							JSONObject authorizationError = new JSONObject(jsonErrorResponse);
							Log.i("Unauthorized", "onErrorResponse: " + authorizationError.getString("unauthorized"));
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

				@Override
				public Map<String, String> getHeaders() {
					Map<String, String> headers = new HashMap<>();
					headers.put("Authorization", "Bearer " + token);

					return headers;
				}
			};

			requestQueue.add(stringRequest);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
