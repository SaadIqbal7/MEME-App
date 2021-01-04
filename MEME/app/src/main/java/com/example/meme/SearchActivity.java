package com.example.meme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {

	private FirebaseAuth auth;
	private ArrayList<SearchItem> filteredSearchItems;
	private SearchItemAdapter searchItemAdapter;

	private RecyclerView searchItemRecycleViewer;
	private EditText searchBar;
	private ProgressBar progressBar;
	private ImageView backButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		auth = FirebaseAuth.getInstance();
		filteredSearchItems = new ArrayList<>();

		// Initialize views
		searchBar = findViewById(R.id.search_bar);
		searchItemRecycleViewer = findViewById(R.id.search_items_recycle_viewer);
		progressBar = findViewById(R.id.progress_bar);
		backButton = findViewById(R.id.back_button);

		setSearchItemRecycleViewer();
		setListeners();
	}

	private void setListeners() {
		searchBar.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(final CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(final Editable s) {
				final RequestQueue requestQueue = Volley.newRequestQueue(SearchActivity.this);
				Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
					@Override
					public void onSuccess(GetTokenResult getTokenResult) {
						searchUser(requestQueue, s, getTokenResult.getToken());
					}
				});
			}
		});

		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchActivity.this.finish();
			}
		});
	}

	private void setSearchItemRecycleViewer() {
		// Initialize onItemClick listener for recycleViewer
		RecycleViewerClickListener recycleViewerClickListener = new RecycleViewerClickListener() {
			@Override
			public void onClick(View view, int position) {
				// Get user from the search list
				SearchItem user = filteredSearchItems.get(position);

				// Go to user profile
				Bundle dataBundle = new Bundle();
				dataBundle.putString("email", user.getEmail());
				Intent profileIntent = new Intent(SearchActivity.this, ProfileActivity.class);
				profileIntent.putExtras(dataBundle);
				startActivity(profileIntent);
			}
		};

		searchItemAdapter = new SearchItemAdapter(SearchActivity.this, filteredSearchItems, recycleViewerClickListener);
		searchItemRecycleViewer.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
		searchItemRecycleViewer.setAdapter(searchItemAdapter);
	}

	private void searchUser(RequestQueue requestQueue, Editable s, final String token) {
		String filterPattern = s.toString();
		String searchUsernameURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/user/username/" + filterPattern;

		progressBar.setVisibility(View.VISIBLE);
		StringRequest stringRequest = new StringRequest(Request.Method.GET, searchUsernameURL, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				// Clear the search results and then fetch new results
				filteredSearchItems.clear();
				searchItemAdapter.notifyDataSetChanged();

				progressBar.setVisibility(View.INVISIBLE);
				try {
					JSONArray users = new JSONArray(response);

					for (int i = 0; i < users.length(); i++) {
						JSONObject usersJSONObject = users.getJSONObject(i);
						SearchItem searchItem = new SearchItem();
						searchItem.setUsername(usersJSONObject.getString("username"));
						searchItem.setName(usersJSONObject.getString("fullName"));
						searchItem.setImageURL(usersJSONObject.getString("imageURL"));
						searchItem.setEmail(usersJSONObject.getString("email"));

						// Filters out current user from the search results
						if(!searchItem.getEmail().equals(Objects.requireNonNull(auth.getCurrentUser()).getEmail())) {
							filteredSearchItems.add(searchItem);
						}
					}
					searchItemAdapter.notifyDataSetChanged();

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				// Clear the search results and then fetch new results
				filteredSearchItems.clear();
				searchItemAdapter.notifyDataSetChanged();

				progressBar.setVisibility(View.INVISIBLE);
				if (error != null) {
					try {
						int statusCode = error.networkResponse.statusCode;
						String jsonErrorResponse = new String(error.networkResponse.data);
						if (statusCode == 400) {
								JSONObject errorResponse = new JSONObject(jsonErrorResponse);
								Toast.makeText(SearchActivity.this, errorResponse.getString("error"), Toast.LENGTH_SHORT).show();
						} else {
							Log.i("Error", "onErrorResponse: " + error.getMessage());
						}
					} catch (JSONException | NullPointerException e) {
						e.printStackTrace();
					}
				}
			}
		}) {
			public String getBodyContentType() {
				return "application/json; charset=utf-8";
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
}
