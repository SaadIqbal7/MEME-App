package com.example.meme;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

	// Temporarily store message ID so that, that message can be deleted
	private String tempMessageID;

	private BottomSheet bottomSheet;
	private FirebaseAuth auth;
	private FirebaseFirestore db;
	private ArrayList<ChatItem> chatList;
	private ChatMessageAdapter chatMessageAdapter;
	private String conversationID;
	private User user;

	private ImageView smallProfilePictureImageView;
	private RelativeLayout onlineStatus;
	private RecyclerView messagesRecyclerView;
	private ImageView sendButton;
	private EditText sendMessageEditText;
	private ImageView backButton;
	private TextView usernameTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		user = new User();

		Bundle dataBundle = getIntent().getExtras();
		if (dataBundle == null) {
			ChatActivity.this.finish();
			return;
		}
		// Get conversation ID and receiving user's Email address (ID) from data bundle passed by conversation activity
		conversationID = dataBundle.getString("conversationID");
		user.setEmail(dataBundle.getString("receiverEmail"));

		// Initialize firebase tools
		auth = FirebaseAuth.getInstance();
		db = FirebaseFirestore.getInstance();

		// Initialize view
		sendButton = findViewById(R.id.send_button);
		sendMessageEditText = findViewById(R.id.send_message_edit_text);
		messagesRecyclerView = findViewById(R.id.messages_recycler_view);
		backButton = findViewById(R.id.back_button);
		smallProfilePictureImageView = findViewById(R.id.small_profile_picture_image_view);
		onlineStatus = findViewById(R.id.online_status);
		usernameTextView = findViewById(R.id.username_text_view);

		// Initially hide the keyboard when the activity starts
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		setUserDetails();
		createBottomSheet();
		setMessagesRecyclerView();
		setListeners();
		setRealtimeListeners();
	}

	private void setUserDetails() {
		db.collection("users")
				.whereEqualTo("email", user.getEmail())
				.addSnapshotListener((queryDocumentSnapshots, e) -> {
					if (e != null) {
						Log.i("FirebaseFirestoreException", "" + e.getMessage());
						return;
					}

					assert queryDocumentSnapshots != null;
					for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
						// Checks if the document is initially added or is modified
						if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {

							// Get email and online status
							user.setOnlineStatus(Integer.parseInt(Objects.requireNonNull(dc.getDocument().getData().get("onlineStatus")).toString()));
							user.setUsername(Objects.requireNonNull(dc.getDocument().get("username")).toString());

							if (user.getOnlineStatus() == 0) {
								onlineStatus.setVisibility(View.GONE);
							} else {
								onlineStatus.setVisibility(View.VISIBLE);
							}


							//Set username
							usernameTextView.setText(user.getUsername());

							// Change the profile picture
							// Get image URL
							user.setImageURL(Objects.requireNonNull(dc.getDocument().getData().get("imageURL")).toString());

							try {
								// Load profile picture
								Glide.with(ChatActivity.this).load(user.getImageURL()).into(smallProfilePictureImageView);
							} catch (IllegalArgumentException err) {
								err.printStackTrace();
							}
						}
					}
				});

	}

	private void createBottomSheet() {
		ArrayList<BottomSheetListItem> messageOptions = new ArrayList<>();

		messageOptions.add(new BottomSheetListItem(R.drawable.ic_delete_white_24dp, "Delete message"));

		RecycleViewerClickListener recycleViewerClickListener = (v, position) -> {
			if(position == 0) {
				Objects.requireNonNull(
						auth.getCurrentUser()).getIdToken(true).
						addOnSuccessListener(getTokenResult -> deleteMessage(ChatActivity.this.tempMessageID, getTokenResult.getToken())
				);
			}

			bottomSheet.hideBottomSheet();
		};

		this.bottomSheet = new BottomSheet(ChatActivity.this, messageOptions, recycleViewerClickListener);
	}

	private void setMessagesRecyclerView() {
		chatList = new ArrayList<>();
		RecycleViewerLongClickListener recycleViewerLongClickListener = (view, position) -> {
			ChatActivity.this.tempMessageID = chatList.get(position).getMessage().getMessageID();
			bottomSheet.showBottomSheet();
		};

		chatMessageAdapter = new ChatMessageAdapter(ChatActivity.this, chatList, recycleViewerLongClickListener);
		messagesRecyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
		messagesRecyclerView.setAdapter(chatMessageAdapter);
	}

	private void setListeners() {
		sendButton.setOnClickListener(v -> {
			try{
				String message = sendMessageEditText.getText().toString();
				// Clear the edit text
				sendMessageEditText.setText("");
				Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> sendMessage(message, getTokenResult.getToken()));
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		});

		backButton.setOnClickListener(v -> ChatActivity.this.finish());
	}

	private void setRealtimeListeners() {
		loadMessages();
	}

	private void loadMessages() {
		// Get messages of the current conversation
		db.collection("messages")
				.whereEqualTo("conversationID", this.conversationID)
				.orderBy("timeSent")
				.addSnapshotListener((queryDocumentSnapshots, e) -> {
					if (e != null) {
						Log.i("FirebaseFirestoreException", "onEvent: " + e.getMessage());
						return;
					}

					assert queryDocumentSnapshots != null;
					for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
						// Check the new message that is added
						if (dc.getType() == DocumentChange.Type.ADDED) {
							QueryDocumentSnapshot messageDocument = dc.getDocument();
							if(!chatList.isEmpty()) {
								// Compare currently added message with the previous message
								// If previous message is sent on some other day, then add a time tag to the chat list

								// Get last item in the list
								ChatMessage lastMessage = chatList.get(chatList.size() - 1).getMessage();

								// Get day from the last message and the currently added message and compare them
								int lastMessageDate = Integer.parseInt(lastMessage.getTimeSent().split("/")[0]);
								int currentMessageDate = Integer.parseInt(Objects.requireNonNull(messageDocument.get("timeSent")).toString().split("/")[0]);

								// Check if current message is sent on some other day
								if(currentMessageDate > lastMessageDate) {
									addTimeTag(Objects.requireNonNull(messageDocument.get("timeSent")).toString());
								}
								addChatMessage(messageDocument);

							} else {
								addTimeTag(Objects.requireNonNull(messageDocument.get("timeSent")).toString());
								addChatMessage(messageDocument);
							}

							// Move to bottom of the chat
							messagesRecyclerView.scrollToPosition(chatList.size() - 1);
						} else if(dc.getType() == DocumentChange.Type.REMOVED) {
							// Delete the message from chat
							String messageID = dc.getDocument().getId();
							chatList.removeIf(chatMessageItem -> ((chatMessageItem.getMessage() != null) && chatMessageItem.getMessage().getMessageID().equals(messageID)));

							// Remove time tag item
							// Check if time tag is the last item in the list
							if(chatList.get(chatList.size() - 1).getTimeTag() != null) {
								chatList.remove(chatList.size() - 1);
							}
						}
						chatMessageAdapter.notifyDataSetChanged();
					}
				});
	}

	private void addTimeTag(String timeSent) {
		TimeTag timeTag = new TimeTag();


		timeTag.setDay(timeSent.split("/")[4]);
		timeTag.setDate(Integer.parseInt(timeSent.split("/")[0]));
		timeTag.setMonth(Integer.parseInt(timeSent.split("/")[1]));

		ChatItem timeTagItem = new ChatItem(timeTag);
		chatList.add(timeTagItem);
	}

	private void addChatMessage(QueryDocumentSnapshot messageDocument) {
		ChatItem chatMessageItem = new ChatItem();
		ChatMessage message = new ChatMessage();

		if (Objects.requireNonNull(messageDocument.get("sender")).toString().equals(Objects.requireNonNull(auth.getCurrentUser()).getEmail())) {
			chatMessageItem.setViewType(0);
		} else {
			chatMessageItem.setViewType(1);
		}

		message.setMessageID(messageDocument.getId());
		message.setMessage(Objects.requireNonNull(messageDocument.get("message")).toString());
		message.setTimeSent(Objects.requireNonNull(messageDocument.get("timeSent")).toString());
		chatMessageItem.setMessage(message);

		chatList.add(chatMessageItem);
	}

	private void sendMessage(String message, final String token) {

		RequestQueue requestQueue = Volley.newRequestQueue(ChatActivity.this);
		String messageURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/message";
		try {
			JSONObject messageInfo =  new JSONObject();
			messageInfo.put("conversationID", this.conversationID);
			messageInfo.put("message", message);

			final String requestBody = messageInfo.toString();

			StringRequest stringRequest = new StringRequest(Request.Method.POST, messageURL, response -> {
			}, error -> Toast.makeText(ChatActivity.this, "Couldn't sent message", Toast.LENGTH_SHORT).show()) {
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

			// Prevent multiple volley requests
			stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
			requestQueue.add(stringRequest);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void deleteMessage(String messageID, final String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(ChatActivity.this);
		String messageURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/message/delete";

		try {
			JSONObject messageInfo =  new JSONObject();
			messageInfo.put("messageID", messageID);

			final String requestBody = messageInfo.toString();
			StringRequest stringRequest = new StringRequest(Request.Method.POST, messageURL, response -> {
			}, error -> {
				if(error != null) {
					try {
						String jsonErrorResponse = new String(error.networkResponse.data);
						JSONObject err = new JSONObject(jsonErrorResponse);
						Log.i("Error: Send message", "onErrorResponse: " + err.getString("error"));
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

			// Prevent multiple volley requests
			stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

			requestQueue.add(stringRequest);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
