package com.example.meme.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

	private User user;
	private FirebaseAuth auth;
	private AlertDialog.Builder alertDialog;

	private EditText fullNameEditText;
	private EditText profileDescriptionEditText;
	private EditText locationEditText;
	private Spinner genderSpinner;
	private EditText dateOfBirthEditText;
	private TextView onlineStatusTextView;
	private ProgressBar progressBar;
	private Button saveButton;
	private ImageView renewButton;
	private ImageView backButton;
	private RelativeLayout onlineStatusItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);

		// Set up the toolbar
		Toolbar toolbar = findViewById(R.id.toolbar);
		setActionBar(toolbar);

		// Initialize new User
		user = new User();
		alertDialog = new AlertDialog.Builder(EditProfileActivity.this);

		// Initialize views and variables
		fullNameEditText = findViewById(R.id.full_name_edit_text);
		profileDescriptionEditText = findViewById(R.id.profile_description_edit_text);
		locationEditText = findViewById(R.id.location_edit_text);
		genderSpinner = findViewById(R.id.genders_spinner);
		dateOfBirthEditText = findViewById(R.id.date_of_birth_edit_text);
		onlineStatusTextView = findViewById(R.id.online_status);
		progressBar = findViewById(R.id.progress_bar);
		saveButton = findViewById(R.id.save_button);
		renewButton = findViewById(R.id.renew_button);
		backButton = findViewById(R.id.back_button);
		onlineStatusItem = findViewById(R.id.online_status_item);

		// Initialize firebase
		auth = FirebaseAuth.getInstance();

		// Initially hide the keyboard when the activity starts
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		// Get the JWT for currently signed in user
		Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
			String token = getTokenResult.getToken();
			getUserInformation(token);
		});

		setupListeners();

		// Populate spinner for genders
		ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this, R.array.genders, R.layout.spinner_item_layout);
		genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_layout);
		genderSpinner.setAdapter(genderAdapter);
	}

	@SuppressLint("SetTextI18n")
	private void setupListeners() {
		// Set up onClick Listener
		// Set Refresh capability
		renewButton.setOnClickListener(v -> {
			// Get the JWT for currently signed in user
			Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
				String token = getTokenResult.getToken();
				getUserInformation(token);
			});
		});

		dateOfBirthEditText.setOnClickListener(v -> {
			// Create the date picker fragment
			DialogFragment datePicker = new DatePickerFragment();

			// Show the date picker fragment
			datePicker.show(getSupportFragmentManager(), "datePicker");
		});

		onlineStatusItem.setOnClickListener(v -> {
			if(onlineStatusTextView.getText().toString().equals("Online")) {
				onlineStatusTextView.setText("Offline");
			} else if(onlineStatusTextView.getText().toString().equals("Offline")) {
				onlineStatusTextView.setText("Online");
			}
		});

		saveButton.setOnClickListener(v -> Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
			user.setFullName(fullNameEditText.getText().toString());
			user.setDescription(profileDescriptionEditText.getText().toString());
			user.setLocation(locationEditText.getText().toString());
			user.setGender(genderSpinner.getSelectedItem().toString());
			user.setDateOfBirth(dateOfBirthEditText.getText().toString());
			String token = getTokenResult.getToken();

			if (onlineStatusTextView.getText().toString().equals("Online")){
				user.setOnlineStatus(1);
			} else if(onlineStatusTextView.getText().toString().equals("Offline")){
				user.setOnlineStatus(0);
			}

			updateProfileInformation(user, token);
		}));

		backButton.setOnClickListener(v -> EditProfileActivity.this.finish());
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
		// Set the text of edit text with the selected date
		dateOfBirthEditText.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
	}

	private void getUserInformation(final String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(EditProfileActivity.this);
		// Get user's email
		final String email = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
		final String userURL = Helpers.apiUrl + "/webApi/api/user/" + email;

		renewButton.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		StringRequest stringRequest = new StringRequest(Request.Method.GET, userURL, response -> {
			try {
				renewButton.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);

				JSONObject userInfo = new JSONObject(response);

				user.setEmail(email);
				user.setUsername(userInfo.getString("username"));
				user.setFullName(userInfo.getString("fullName"));
				user.setDescription(userInfo.getString("description"));
				user.setLocation(userInfo.getString("location"));
				user.setGender(userInfo.getString("gender"));
				user.setDateOfBirth(userInfo.getString("dob"));
				user.setOnlineStatus(userInfo.getInt("onlineStatus"));

				displayUserInformation(user);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}, error -> {
			renewButton.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			if(error != null) {
				try {
					int statusCode = error.networkResponse.statusCode;
					String jsonErrorResponse = new String(error.networkResponse.data);
					JSONObject err = new JSONObject(jsonErrorResponse);
					if(statusCode == 400) {
						Log.i("errEmail", "onErrorResponse: " + err.getString("errEmail"));
					} else if (statusCode == 403) {
						JSONObject authorizationError = new JSONObject(jsonErrorResponse);
						Log.i("Unauthorized", "onErrorResponse: " + authorizationError.getString("unauthorized"));
					}
				} catch (JSONException | NullPointerException e) {
					e.printStackTrace();
				}
			}
		}){
			@Override
			public Map<String, String> getHeaders() {
				Map<String, String> headers = new HashMap<>();
				headers.put("Authorization", "Bearer " + token);

				return headers;
			}
		};

		requestQueue.add(stringRequest);
	}

	@SuppressLint("SetTextI18n")
	private void displayUserInformation(User user) {
		fullNameEditText.setText(user.getFullName());
		profileDescriptionEditText.setText(user.getDescription());
		locationEditText.setText(user.getLocation());

		switch (user.getGender()) {
			case "Male":
				genderSpinner.setSelection(1);
				break;
			case "Female":
				genderSpinner.setSelection(2);
				break;
			case "Other":
				genderSpinner.setSelection(3);
				break;
		}

		dateOfBirthEditText.setText(user.getDateOfBirth());

		if(user.getOnlineStatus() == 1) {
			onlineStatusTextView.setText("Online");
		} else if(user.getOnlineStatus() == 0){
			onlineStatusTextView.setText("Offline");
		}

		saveButton.setEnabled(true);
	}

	private void updateProfileInformation(User user, final String token) {
		try {
			RequestQueue requestQueue = Volley.newRequestQueue(EditProfileActivity.this);
			String userURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/user/";

			JSONObject userUpdatedInfo = new JSONObject();
			userUpdatedInfo.put("email", user.getEmail());
			userUpdatedInfo.put("fullName", user.getFullName());
			userUpdatedInfo.put("location", user.getLocation());
			userUpdatedInfo.put("gender", user.getGender());
			userUpdatedInfo.put("dob", user.getDateOfBirth());
			userUpdatedInfo.put("description", user.getDescription());
			userUpdatedInfo.put("onlineStatus", user.getOnlineStatus());

			final String requestBody = userUpdatedInfo.toString();

			renewButton.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
			StringRequest stringRequest = new StringRequest(Request.Method.PUT, userURL, response -> {
				renewButton.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
			}, error -> {
				renewButton.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				if(error != null) {
					try {
						int statusCode = error.networkResponse.statusCode;
						String jsonErrorResponse = new String(error.networkResponse.data);
						if(statusCode == 400) {
							JSONObject errUserInfo = new JSONObject(jsonErrorResponse);
							String errFullName = errUserInfo.getString("errFullName");
							String errLocation = errUserInfo.getString("errLocation");
							String errGender = errUserInfo.getString("errGender");
							String errDOB = errUserInfo.getString("errDOB");
							alertDialog.setTitle("Insufficient details");
							alertDialog.setPositiveButton("Ok", null);
							if(!errFullName.isEmpty()) {
								alertDialog.setMessage(errFullName);
							}
							else if (!errLocation.isEmpty()) {
								alertDialog.setMessage(errLocation);
							}
							else if (!errGender.isEmpty()) {
								alertDialog.setMessage(errGender);
							} else if (!errDOB.isEmpty()) {
								alertDialog.setMessage(errDOB);
							}
							alertDialog.show();

						} else if (statusCode == 403) {
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
					return requestBody.getBytes(StandardCharsets.UTF_8);
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
