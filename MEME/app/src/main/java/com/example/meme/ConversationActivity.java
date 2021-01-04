package com.example.meme;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {

	private String tempConversationID;
	private BottomSheet bottomSheet;
	private FirebaseAuth auth;
	private FirebaseFirestore db;
	private ConversationItemAdapter conversationItemAdapter;
	private User user;

	private ArrayList<ConversationItem> conversations;
	private ArrayList<ConversationItem> tempConversationsList;

	private RecyclerView conversationsListRecycleViewer;
	private ProgressBar progressBar;
	private CircleImageView profilePicture;
	private EditText searchBar;
	private ImageView backButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation);

		auth = FirebaseAuth.getInstance();
		db = FirebaseFirestore.getInstance();
		user = new User();

		conversationsListRecycleViewer = findViewById(R.id.conversations_recycle_viewer);
		// Temporary item list for searching the conversations
		tempConversationsList = new ArrayList<>();

		progressBar = findViewById(R.id.progress_bar);
		profilePicture = findViewById(R.id.profile_picture);
		searchBar = findViewById(R.id.search_bar);
		backButton = findViewById(R.id.back_button);

		// Initially hide the keyboard when the activity starts
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		createBottomSheet();
		setConversationAdapter();
		setRealtimeListeners();
		setListeners();
	}

	private void setListeners() {
		// On type listener for searching conversation
		searchBar.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String filterPattern = s.toString().toLowerCase().trim();
				searchConversation(filterPattern);
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		backButton.setOnClickListener(v -> {
			// Finish the current activity
			ConversationActivity.this.finish();
		});
	}

	private void setRealtimeListeners() {
		getConversations();
		setProfilePicture();
	}

	private void createBottomSheet() {
		ArrayList<BottomSheetListItem> bottomSheetListItems = new ArrayList<>();
		bottomSheetListItems.add(new BottomSheetListItem(R.drawable.ic_delete_white_24dp, "Delete conversation"));

		RecycleViewerClickListener recycleViewerClickListener = (view, position) -> {
			if (position == 0) {
				Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> deleteConversation(tempConversationID, getTokenResult.getToken()));
			}
			bottomSheet.hideBottomSheet();
		};

		this.bottomSheet = new BottomSheet(ConversationActivity.this, bottomSheetListItems, recycleViewerClickListener);
	}

	private void setConversationAdapter() {
		conversations = new ArrayList<>();

		RecycleViewerClickListener recycleViewerClickListener = (view, position) -> {
			// Data bundle contains conversation ID and the receiving users Email address (ID)
			Bundle dataBundle = new Bundle();
			dataBundle.putString("conversationID", conversations.get(position).getConversationID());
			dataBundle.putString("receiverEmail", conversations.get(position).getEmail());
			Intent chatIntent = new Intent(ConversationActivity.this, ChatActivity.class);
			chatIntent.putExtras(dataBundle);
			startActivity(chatIntent);
		};

		RecycleViewerLongClickListener recycleViewerLongClickListener = (view, position) -> {
			tempConversationID = conversations.get(position).getConversationID();
			bottomSheet.showBottomSheet();
		};

		conversationItemAdapter = new ConversationItemAdapter(ConversationActivity.this, conversations, recycleViewerClickListener, recycleViewerLongClickListener);
		conversationsListRecycleViewer.setLayoutManager(new LinearLayoutManager(ConversationActivity.this));
		conversationsListRecycleViewer.setAdapter(conversationItemAdapter);
	}

	private void getConversations() {
		// Set up realtime listener for conversations
		db.collection("conversations")
				.whereEqualTo("sender", Objects.requireNonNull(auth.getCurrentUser()).getEmail())
				.addSnapshotListener((queryDocumentSnapshots, e) -> {
					if (e != null) {
						Log.i("FirebaseFirestoreException", "" + e.getMessage());
						return;
					}

					// Get user information related to fetched document
					auth.getCurrentUser().getIdToken(true).addOnSuccessListener(getTokenResult -> {
						assert queryDocumentSnapshots != null;
						for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
							// If a new conversation id added than list that conversation
							if (dc.getType() == DocumentChange.Type.ADDED) {
								displayConversation(
										dc.getDocument().getId(),
										Objects.requireNonNull(dc.getDocument().getData().get("receiver")).toString(),
										getTokenResult.getToken()
								);
							} else if (dc.getType() == DocumentChange.Type.REMOVED) {
								String conversationID = dc.getDocument().getId();
								conversations.removeIf(convo -> convo.getConversationID().equals(conversationID));

								// Remove conversations to this temporary list for searching the conversations
								tempConversationsList.removeIf(convo -> convo.getConversationID().equals(conversationID));
							}
						}
						conversationItemAdapter.notifyDataSetChanged();
					});
				});
	}

	private void displayConversation(final String conversationID, final String receiverEmail, final String token) {
		final RequestQueue requestQueue = Volley.newRequestQueue(ConversationActivity.this);
		String usersURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/user/" + receiverEmail;

		progressBar.setVisibility(View.VISIBLE);
		final StringRequest stringRequest = new StringRequest(Request.Method.GET, usersURL, response -> {
			progressBar.setVisibility(View.GONE);
			try {
				JSONObject userInfo = new JSONObject(response);
				ConversationItem conversation = new ConversationItem();

				conversation.setConversationID(conversationID);
				conversation.setImageURL(userInfo.getString("imageURL"));
				conversation.setEmail(userInfo.getString("email"));
				conversation.setName(userInfo.getString("fullName"));

				conversations.add(conversation);
				// Add conversations to this temporary list for searching the conversations
				tempConversationsList.add(conversation);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}, error -> {
			progressBar.setVisibility(View.GONE);
			if (error != null) {
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

	private void deleteConversation(String conversationID, final String token) {
		final RequestQueue requestQueue = Volley.newRequestQueue(ConversationActivity.this);
		String conversationURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/conversations/delete";

		try {
			JSONObject conversationInfo = new JSONObject();
			conversationInfo.put("conversationID", conversationID);

			final String requestBody = conversationInfo.toString();
			progressBar.setVisibility(View.VISIBLE);
			final StringRequest stringRequest = new StringRequest(Request.Method.POST, conversationURL, response -> progressBar.setVisibility(View.GONE), error -> {
				progressBar.setVisibility(View.GONE);
				if (error != null) {
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

	private void setProfilePicture() {
		db.collection("users")
				.whereEqualTo("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail())
				.addSnapshotListener((queryDocumentSnapshots, e) -> {
					if (e != null) {
						Log.i("FirebaseFirestoreException", "" + e.getMessage());
						return;
					}

					assert queryDocumentSnapshots != null;
					for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
						// Checks if the document is initially added or is modified
						if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {

							// Change the profile picture
							// Get image URL
							user.setImageURL(Objects.requireNonNull(dc.getDocument().getData().get("imageURL")).toString());

							try {
								// Load profile picture
								Glide.with(ConversationActivity.this).load(user.getImageURL()).into(profilePicture);
							} catch (IllegalArgumentException err) {
								err.printStackTrace();
							}
						}
					}
				});
	}

	private void searchConversation(String filterPattern) {
		conversations.clear();
		if (filterPattern.isEmpty()) {
			conversations.addAll(tempConversationsList);
		} else {
			for (ConversationItem conversationItem : tempConversationsList) {
				String name = conversationItem.getName().toLowerCase().trim();
				if (name.startsWith(filterPattern)) {
					conversations.add(conversationItem);
				}
			}
		}
		conversationItemAdapter.notifyDataSetChanged();
	}
}
