package com.example.meme.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.meme.R;
import com.example.meme.listener.RecycleViewerClickListener;
import com.example.meme.container.BottomSheet;
import com.example.meme.container.Helpers;
import com.example.meme.container.User;
import com.example.meme.listitem.BottomSheetListItem;
import com.example.meme.ui.main.SectionsPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int REQUEST_LOAD_IMG = 2;

	FirebaseAuth auth;
	private User user;
	private BottomSheet bottomSheet;
	private Bundle dataBundle;

	private TextView usernameTextView;
	private TextView nameTextView;
	private TextView descriptionTextView;
	private TextView emailTextView;
	private TextView locationTextView;
	private RelativeLayout onlineStatus;
	private ProgressBar progressBar;
	private ImageView emailIconImageView;
	private ImageView locationIconImageView;
	private RelativeLayout selectImageButton;
	private LinearLayout locationField;
	private LinearLayout emailField;
	private CircleImageView profilePictureImageView;
	private CircleImageView smallProfilePictureImageView;
	private ImageView refreshButton;
	private RelativeLayout profilePictureRelativeLayout;
	private RelativeLayout smallProfilePictureRelativeLayout;
	private ImageView backButton;
	private FloatingActionButton createMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		// Set up the toolbar
		Toolbar toolbar = findViewById(R.id.toolbar);
		setActionBar(toolbar);

		user = new User();

		// Initialize views;
		usernameTextView = findViewById(R.id.username_text_view);
		nameTextView = findViewById(R.id.name_text_view);
		descriptionTextView = findViewById(R.id.description_text_view);
		emailTextView = findViewById(R.id.email_text_view);
		locationTextView = findViewById(R.id.location_text_view);
		onlineStatus = findViewById(R.id.online_status);
		progressBar = findViewById(R.id.progress_bar);
		locationIconImageView = findViewById(R.id.location_icon_image_view);
		emailIconImageView = findViewById(R.id.email_icon_image_view);
		selectImageButton = findViewById(R.id.select_image_button);
		emailField = findViewById(R.id.email_field);
		locationField = findViewById(R.id.location_field);
		profilePictureImageView = findViewById(R.id.profile_picture_image_view);
		smallProfilePictureImageView = findViewById(R.id.small_profile_picture_image_view);
		refreshButton = findViewById(R.id.refresh_button);
		profilePictureRelativeLayout = findViewById(R.id.profile_picture_relative_layout);
		smallProfilePictureRelativeLayout = findViewById(R.id.small_profile_picture_relative_layout);
		backButton = findViewById(R.id.back_button);
		createMessage = findViewById(R.id.create_message);

		auth = FirebaseAuth.getInstance();

		// The data passed by the intents is stored in the data bundle and passed
		// to fragments of linked with this activity (Tabs)
		dataBundle = getIntent().getExtras();
		// Get the JWT for currently signed in user
		Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
			String token = getTokenResult.getToken();
			getProfileInformation(token);
		});

		// Create bottom sheet
		createBottomSheet();

		setupListeners();
	}

	private void setupListeners() {

		// Set onClickListener for selectImageButton
		selectImageButton.setOnClickListener(v -> bottomSheet.showBottomSheet());

		// Set Refresh capability
		refreshButton.setOnClickListener(v -> Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
			String token = getTokenResult.getToken();
			getProfileInformation(token);
		}));

		backButton.setOnClickListener(v -> finish());

		createMessage.setOnClickListener(v -> Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
			String token = getTokenResult.getToken();
			// Create conversation
			createConversation(token);
		}));
	}

	private void createBottomSheet() {
		// Create new Array list for bottom sheet items
		ArrayList<BottomSheetListItem> bottomSheetListItemArrayList = new ArrayList<>();

		// Initialize items in bottom sheet
		bottomSheetListItemArrayList.add(
				new BottomSheetListItem(R.drawable.ic_image_white_24dp, "Choose from gallery")
		);
		bottomSheetListItemArrayList.add(
				new BottomSheetListItem(R.drawable.ic_camera_alt_white_24dp, "Take a photo")
		);
		bottomSheetListItemArrayList.add(
				new BottomSheetListItem(R.drawable.ic_card_giftcard_black_24dp, "Surprise me")
		);
		bottomSheetListItemArrayList.add(
				new BottomSheetListItem(R.drawable.ic_delete_white_24dp, "Remove picture")
		);

		// Set up listener for Bottom sheet list items
		RecycleViewerClickListener recycleViewerClickListener = (v, position) -> {
			if (position == 0) {
				chooseFromGallery();
			} else if (position == 1) {
				takePhoto();
			} else if (position == 2) {
				Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
					String token = getTokenResult.getToken();
					surpriseMe(token);
				});
			} else if (position == 3) {
				Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
					String token = getTokenResult.getToken();
					removePicture(token);
				});
			}
			// Close bottom sheet when an item is pressed
			this.bottomSheet.hideBottomSheet();
		};

		this.bottomSheet = new BottomSheet(ProfileActivity.this, bottomSheetListItemArrayList, recycleViewerClickListener);
	}

	private void removePicture(String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(ProfileActivity.this);
		String removePictureURL = Helpers.apiUrl + "/webApi/api/user/removePicture";
		final JSONObject userInformation = new JSONObject();

		try {
			userInformation.put("email", user.getEmail());
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}

		final String requestBody = userInformation.toString();

		progressBar.setVisibility(View.VISIBLE);
		refreshButton.setVisibility(View.GONE);
		StringRequest stringRequest = new StringRequest(Request.Method.POST, removePictureURL, response -> {
			progressBar.setVisibility(View.GONE);
			refreshButton.setVisibility(View.VISIBLE);
			try {
				JSONObject imageResponse = new JSONObject(response);
				String imageURL = imageResponse.getString("imageURL");
				user.setImageURL(imageURL);

				Glide.with(ProfileActivity.this).load(user.getImageURL()).fitCenter().into(profilePictureImageView);
				Glide.with(ProfileActivity.this).load(user.getImageURL()).fitCenter().into(smallProfilePictureImageView);
			} catch (JSONException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}, error -> {
			progressBar.setVisibility(View.GONE);
			refreshButton.setVisibility(View.VISIBLE);
			if(error != null) {
				try {
					int statusCode = error.networkResponse.statusCode;
					String jsonErrorResponse = new String(error.networkResponse.data);
					if (statusCode == 400) {
						JSONObject uploadError = new JSONObject(jsonErrorResponse);
						Toast.makeText(ProfileActivity.this, uploadError.getString("error"), Toast.LENGTH_SHORT).show();
					} else if (statusCode == 403) {
						JSONObject authorizationError = new JSONObject(jsonErrorResponse);
						Log.i("Unauthorized", "onErrorResponse: " + authorizationError.getString("unauthorized"));
					}
				} catch (JSONException | NullPointerException e) {
					e.printStackTrace();
				}
			}
		}) {
			@NotNull
			@Contract(pure = true)
			@Override
			public String getBodyContentType() {
				return "application/json; charset=utf-8";
			}

			@org.jetbrains.annotations.Nullable
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

		// Prevent multiple volley requests
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		requestQueue.add(stringRequest);
	}

	// Tabs of tabs layout get their data from this functions
	public Bundle getDataBundle() {
		return dataBundle;
	}

	// Displays the information of the user
	private void getProfileInformation(final String token) {
		final String email = dataBundle.getString("email");

		RequestQueue requestQueue = Volley.newRequestQueue(ProfileActivity.this);
		// Get user's email
		final String userURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/user/" + email;

		refreshButton.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		nameTextView.setVisibility(View.GONE);
		descriptionTextView.setVisibility(View.GONE);
		emailField.setVisibility(View.GONE);
		locationField.setVisibility(View.GONE);
		profilePictureRelativeLayout.setVisibility(View.GONE);
		smallProfilePictureRelativeLayout.setVisibility(View.GONE);
		usernameTextView.setVisibility(View.GONE);
		StringRequest stringRequest = new StringRequest(Request.Method.GET, userURL, response -> {
			try {
				refreshButton.setVisibility(View.VISIBLE);
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
				user.setImageURL(userInfo.getString("imageURL"));

				// Display the information on the profile
				displayProfileInformation(user);

				// Add user's data in bundle and send it to the profile fragment
				dataBundle.putString("userInfo", response);

				// Display tabs after fetching data from server
				displayTabs();

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}, error -> {
			progressBar.setVisibility(View.GONE);
			refreshButton.setVisibility(View.VISIBLE);
			if(error != null) {
				try {
					int statusCode = error.networkResponse.statusCode;
					String jsonErrorResponse = new String(error.networkResponse.data);
					if (statusCode == 400) {
						JSONObject err = new JSONObject(jsonErrorResponse);
						Log.i("errEmail", "onErrorResponse: " + err.getString("errEmail"));
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
			public Map<String, String> getHeaders() {
				Map<String, String> headers = new HashMap<>();
				headers.put("Authorization", "Bearer " + token);

				return headers;
			}
		};

		requestQueue.add(stringRequest);
	}

	@SuppressLint("RestrictedApi")
	private void displayProfileInformation(User user) {
		nameTextView.setVisibility(View.VISIBLE);
		emailField.setVisibility(View.VISIBLE);
		locationField.setVisibility(View.VISIBLE);
		profilePictureRelativeLayout.setVisibility(View.VISIBLE);
		smallProfilePictureRelativeLayout.setVisibility(View.VISIBLE);
		usernameTextView.setVisibility(View.VISIBLE);

		// Check if profile being visited by current user or some other user
		if(!user.getEmail().equals(Objects.requireNonNull(auth.getCurrentUser()).getEmail())) {
			createMessage.setVisibility(View.VISIBLE);
		}

		if(!user.getDescription().isEmpty()) {
			descriptionTextView.setVisibility(View.VISIBLE);
			descriptionTextView.setText(user.getDescription());
		} else {
			descriptionTextView.setVisibility(View.GONE);
		}

		// Set views
		usernameTextView.setText(user.getUsername());
		nameTextView.setText(user.getFullName());
		emailTextView.setText(user.getEmail());
		locationTextView.setText(user.getLocation());

		try{
			Glide.with(ProfileActivity.this).load(user.getImageURL()).into(profilePictureImageView);
			Glide.with(ProfileActivity.this).load(user.getImageURL()).into(smallProfilePictureImageView);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		if (user.getOnlineStatus() == 1) {
			onlineStatus.setVisibility(View.VISIBLE);
		} else if (user.getOnlineStatus() == 0) {
			onlineStatus.setVisibility(View.GONE);
		}

		// Check if profile is of the current user or not
		if (!Objects.equals(Objects.requireNonNull(auth.getCurrentUser()).getEmail(), user.getEmail())) {
			selectImageButton.setVisibility(View.GONE);
		} else {
			selectImageButton.setVisibility(View.VISIBLE);
		}

		// Make icons visible
		emailIconImageView.setVisibility(View.VISIBLE);
		locationIconImageView.setVisibility(View.VISIBLE);
	}

	private void displayTabs() {
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
		ViewPager viewPager = findViewById(R.id.view_pager);
		viewPager.setAdapter(sectionsPagerAdapter);
		TabLayout tabs = findViewById(R.id.tabs);
		tabs.setupWithViewPager(viewPager);
	}

	private void chooseFromGallery() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, REQUEST_LOAD_IMG);
	}

	private void takePhoto() {
		// Initialize a new intent to capture image
		Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// Check if a component or application exist to capture the image
		if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
		}
	}

	// Capture image can be handled here
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			assert data != null;
			Bundle extras = data.getExtras();
			assert extras != null;
			Bitmap image = (Bitmap) extras.get("data");
			profilePictureImageView.setImageBitmap(image);
			smallProfilePictureImageView.setImageBitmap(image);
		} else if (requestCode == REQUEST_LOAD_IMG && resultCode == RESULT_OK) {
			if (data != null) {
				final Uri imageUri = data.getData();

				convertToBase64(imageUri);
			} else {
				Toast.makeText(ProfileActivity.this, "Select an image", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void convertToBase64(Uri imageUri) {
		try {
			final InputStream imageStream = getContentResolver().openInputStream(imageUri);
			final Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageStream);

			ByteArrayOutputStream boas = new ByteArrayOutputStream();

			// Compress image
			selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, boas);

			// Convert image to bytes
			byte[] image = boas.toByteArray();

			// Convert bytes of image to base 64 image
			String encoded = Base64.encodeToString(image, Base64.DEFAULT);

			// Get the image extension
			String extension = Helpers.getExtension(ProfileActivity.this, imageUri);

			final String base64Image = "data:image/" + extension + ";base64," + encoded;

			Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
				String token = getTokenResult.getToken();
				uploadToStorage(base64Image, token);
			});

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
		}
	}

	private void uploadToStorage(String base64Image, final String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(ProfileActivity.this);
		String uploadImageURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/user/uploadImage";
		final JSONObject imageInformation = new JSONObject();

		try {
			imageInformation.put("email", user.getEmail());
			imageInformation.put("image", base64Image);

		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}

		final String requestBody = imageInformation.toString();

		progressBar.setVisibility(View.VISIBLE);
		refreshButton.setVisibility(View.GONE);
		StringRequest stringRequest = new StringRequest(Request.Method.POST, uploadImageURL, response -> {
			progressBar.setVisibility(View.GONE);
			refreshButton.setVisibility(View.VISIBLE);
			try {
				JSONObject imageResponse = new JSONObject(response);
				String imageURL = imageResponse.getString("imageURL");
				user.setImageURL(imageURL);

				Glide.with(ProfileActivity.this).load(user.getImageURL()).fitCenter().into(profilePictureImageView);
				Glide.with(ProfileActivity.this).load(user.getImageURL()).fitCenter().into(smallProfilePictureImageView);
			} catch (JSONException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}, error -> {
			progressBar.setVisibility(View.GONE);
			refreshButton.setVisibility(View.VISIBLE);
			if(error != null) {
				try {
					int statusCode = error.networkResponse.statusCode;
					String jsonErrorResponse = new String(error.networkResponse.data);
					if (statusCode == 400) {
						JSONObject uploadError = new JSONObject(jsonErrorResponse);
						Toast.makeText(ProfileActivity.this, uploadError.getString("error"), Toast.LENGTH_SHORT).show();
					} else if (statusCode == 403) {
						JSONObject authorizationError = new JSONObject(jsonErrorResponse);
						Log.i("Unauthorized", "onErrorResponse: " + authorizationError.getString("unauthorized"));
					}
				} catch (JSONException | NullPointerException e) {
					e.printStackTrace();
				}
			}
		}) {
			@NotNull
			@Contract(pure = true)
			@Override
			public String getBodyContentType() {
				return "application/json; charset=utf-8";
			}

			@org.jetbrains.annotations.Nullable
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
	}

	public void surpriseMe(final String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(ProfileActivity.this);
		String selectDefaultPicture = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/user/selectRandomPicture";
		final JSONObject userInformation = new JSONObject();

		try {
			userInformation.put("email", user.getEmail());
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}

		final String requestBody = userInformation.toString();

		progressBar.setVisibility(View.VISIBLE);
		refreshButton.setVisibility(View.GONE);
		StringRequest stringRequest = new StringRequest(Request.Method.POST, selectDefaultPicture, response -> {
			progressBar.setVisibility(View.GONE);
			refreshButton.setVisibility(View.VISIBLE);
			try {
				JSONObject imageResponse = new JSONObject(response);
				String imageURL = imageResponse.getString("imageURL");
				user.setImageURL(imageURL);

				Glide.with(ProfileActivity.this).load(user.getImageURL()).fitCenter().into(profilePictureImageView);
				Glide.with(ProfileActivity.this).load(user.getImageURL()).fitCenter().into(smallProfilePictureImageView);
			} catch (JSONException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}, error -> {
			progressBar.setVisibility(View.GONE);
			refreshButton.setVisibility(View.VISIBLE);
			if(error != null) {
				try {
					int statusCode = error.networkResponse.statusCode;
					String jsonErrorResponse = new String(error.networkResponse.data);
					if (statusCode == 400) {
						JSONObject uploadError = new JSONObject(jsonErrorResponse);
						Toast.makeText(ProfileActivity.this, uploadError.getString("error"), Toast.LENGTH_SHORT).show();
					} else if (statusCode == 403) {
						JSONObject authorizationError = new JSONObject(jsonErrorResponse);
						Log.i("Unauthorized", "onErrorResponse: " + authorizationError.getString("unauthorized"));
					}
				} catch (JSONException | NullPointerException e) {
					e.printStackTrace();
				}
			}
		}) {
			@NotNull
			@Contract(pure = true)
			@Override
			public String getBodyContentType() {
				return "application/json; charset=utf-8";
			}

			@org.jetbrains.annotations.Nullable
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

		// Prevent multiple volley requests
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		requestQueue.add(stringRequest);
	}

	private void createConversation(final String token) {
		// Check if the user is sending the message to some other user
		if(!Objects.equals(Objects.requireNonNull(auth.getCurrentUser()).getEmail(), user.getEmail())) {
			RequestQueue requestQueue = Volley.newRequestQueue(ProfileActivity.this);
			String conversationURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/conversations";
			try {
				JSONObject conversationInfo = new JSONObject();
				conversationInfo.put("sender", auth.getCurrentUser().getEmail());
				conversationInfo.put("receiver", user.getEmail());

				progressBar.setVisibility(View.VISIBLE);
				refreshButton.setVisibility(View.GONE);
				final String requestBody = conversationInfo.toString();
				StringRequest stringRequest = new StringRequest(Request.Method.POST, conversationURL, response -> {
					progressBar.setVisibility(View.GONE);
					refreshButton.setVisibility(View.VISIBLE);

					// If conversation is created successfully or already exists, move to conversations activity
					Intent conversationsIntent = new Intent(ProfileActivity.this, ConversationActivity.class);
					startActivity(conversationsIntent);
				}, error -> {
					progressBar.setVisibility(View.GONE);
					refreshButton.setVisibility(View.VISIBLE);
					if(error != null) {
						Log.i("Error", "onErrorResponse: " + error.getMessage());
					}
				}){
					@Override
					public String getBodyContentType() {
						return "application/json; charset=utf-8";
					}

					@org.jetbrains.annotations.Nullable
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
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
	}
}