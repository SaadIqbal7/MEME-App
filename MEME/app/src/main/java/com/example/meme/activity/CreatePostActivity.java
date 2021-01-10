package com.example.meme.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.meme.container.BottomSheet;
import com.example.meme.listitem.BottomSheetListItem;
import com.example.meme.container.Helpers;
import com.example.meme.container.Post;
import com.example.meme.R;
import com.example.meme.listener.RecycleViewerClickListener;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
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

public class CreatePostActivity extends AppCompatActivity {

	static final int REQUEST_LOAD_IMG = 1;

	private FirebaseAuth auth;
	private BottomSheet bottomSheet;
	private Post post;
	private AlertDialog.Builder alertDialog;

	private ProgressBar progressBar;
	private ImageView backButton;
	private EditText postDescriptionEditText;
	private EditText tag1EditText;
	private EditText tag2EditText;
	private EditText tag3EditText;
	private ImageView postImage;
	private ImageView choosePictureButton;
	private Button postButton;
	private Spinner categoriesSpinner;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_post);

		// Initially hide the keyboard when the activity starts
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		// Initialize all the global views and variables
		initializeVariables();
		setBottomSheet();
		setCategoriesSpinner();
		setListeners();
	}

	private void initializeVariables() {
		// Initialize views
		progressBar = findViewById(R.id.progress_bar);
		backButton = findViewById(R.id.back_button);
		postDescriptionEditText = findViewById(R.id.post_description_edit_text);
		tag1EditText = findViewById(R.id.tag_1_edit_text);
		tag2EditText = findViewById(R.id.tag_2_edit_text);
		tag3EditText = findViewById(R.id.tag_3_edit_text);
		postImage = findViewById(R.id.post_image);
		choosePictureButton = findViewById(R.id.choose_picture_button);
		postButton = findViewById(R.id.post_button);
		categoriesSpinner = findViewById(R.id.categories_spinner);

		alertDialog = new AlertDialog.Builder(CreatePostActivity.this);
		auth = FirebaseAuth.getInstance();
		post = new Post();
	}

	private void setListeners() {
		backButton.setOnClickListener(v -> CreatePostActivity.this.finish());

		choosePictureButton.setOnClickListener(v -> bottomSheet.showBottomSheet());

		postButton.setOnClickListener(v -> {
			try {
				Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
					post.setEmail(auth.getCurrentUser().getEmail());
					post.setDescription(postDescriptionEditText.getText().toString());
					post.setCategory(categoriesSpinner.getSelectedItem().toString());

					ArrayList<String> tags = new ArrayList<>();
					if(!tag1EditText.getText().toString().isEmpty()){
						tags.add(tag1EditText.getText().toString());
					}
					if(!tag2EditText.getText().toString().isEmpty()){
						tags.add(tag2EditText.getText().toString());
					}
					if(!tag3EditText.getText().toString().isEmpty()){
						tags.add(tag3EditText.getText().toString());
					}
					post.setTags(tags);
					createPost(getTokenResult.getToken());
				});
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		});
	}

	private void setBottomSheet() {
		// Create new Array list for bottom sheet items
		ArrayList<BottomSheetListItem> bottomSheetListItemArrayList = new ArrayList<>();

		// Initialize items in bottom sheet
		bottomSheetListItemArrayList.add(
				new BottomSheetListItem(R.drawable.ic_image_white_24dp, "Choose from gallery")
		);

		// Set up listener for Bottom sheet list items
		RecycleViewerClickListener recycleViewerClickListener = (v, position) -> {
			if (position == 0) {
				chooseFromGallery();
			}
			// Close bottom sheet when an item is pressed
			this.bottomSheet.hideBottomSheet();
		};

		this.bottomSheet = new BottomSheet(CreatePostActivity.this, bottomSheetListItemArrayList, recycleViewerClickListener);
	}

	private void setCategoriesSpinner() {
		// Populate spinner for genders
		ArrayAdapter<CharSequence> categoriesAdapter = ArrayAdapter.createFromResource(this, R.array.categories, R.layout.spinner_item_layout);
		categoriesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_layout);
		categoriesSpinner.setAdapter(categoriesAdapter);
	}

	private void chooseFromGallery() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, REQUEST_LOAD_IMG);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_LOAD_IMG && resultCode == RESULT_OK) {
			if(data != null) {
				try {
					Uri imageUri = data.getData();
					assert imageUri != null;
					// Convert uri image to bitmap
					InputStream imageStream = getContentResolver().openInputStream(imageUri);
					Bitmap bitmapImage = BitmapFactory.decodeStream(imageStream);

					// Display the image in the image view
					postImage.setImageBitmap(bitmapImage);

					// Convert image into base64 image
					post.setBase64Image(convertToBase64(imageUri));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String convertToBase64(Uri imageUri) {
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
			String extension = Helpers.getExtension(CreatePostActivity.this, imageUri);

			return "data:image/" + extension + ";base64," + encoded;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void createPost(final String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(CreatePostActivity.this);
		String postURL = Helpers.apiUrl + "/webApi/api/post";

		try{
			JSONObject postInfo = new JSONObject();
			postInfo.put("email", post.getEmail());
			postInfo.put("description", post.getDescription());
			postInfo.put("tags", new JSONArray(post.getTags()));
			postInfo.put("image", post.getBase64Image());
			postInfo.put("category", post.getCategory());

			String requestBody = postInfo.toString();
			progressBar.setVisibility(View.VISIBLE);
			StringRequest stringRequest = new StringRequest(Request.Method.POST, postURL, response -> {
				// Close this activity when the post is created
				progressBar.setVisibility(View.GONE);
				CreatePostActivity.this.finish();
			}, error -> {
				progressBar.setVisibility(View.GONE);
				if(error != null) {
					try{
						int statusCode = error.networkResponse.statusCode;
						JSONObject errPostInfo = new JSONObject(new String(error.networkResponse.data));

						if(statusCode == 400) {
							alertDialog.setTitle("Insufficient details");
							alertDialog.setPositiveButton("Ok", null);
							if(!errPostInfo.getString("errDescription").isEmpty()) {
								alertDialog.setMessage(errPostInfo.getString("errDescription"));
							}
							else if (!errPostInfo.getString("errTags").isEmpty()) {
								alertDialog.setMessage(errPostInfo.getString("errTags"));
							}
							else if (!errPostInfo.getString("errImage").isEmpty()) {
								alertDialog.setMessage(errPostInfo.getString("errImage"));
							} else if (!errPostInfo.getString("errCategory").isEmpty()) {
								alertDialog.setMessage(errPostInfo.getString("errCategory"));
							}
							alertDialog.show();
						}
					} catch(JSONException | NullPointerException e){
						e.printStackTrace();
					}
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

			// Prevent multiple volley requests
			stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
			requestQueue.add(stringRequest);
		} catch (JSONException | NullPointerException e) {
			e.printStackTrace();
		}
	}
}
