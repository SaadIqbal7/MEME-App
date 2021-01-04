package com.example.meme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RepliesActivity extends AppCompatActivity {


	private FirebaseAuth auth;
	private String parentId;
	private String postId;

	private RecyclerView replies;
	private SwipeRefreshLayout swipeRefreshLayout;
	private EditText commentEditText;
	private ImageView sendButton;
	private ImageView backButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_replies);

		initializeVariables();
		setListeners();
		setCommentRecyclerView();
	}

	private void initializeVariables() {
		Bundle dataBundle = getIntent().getExtras();
		assert dataBundle != null;
		parentId = dataBundle.getString("parentId");
		postId = dataBundle.getString("postId");
		auth = FirebaseAuth.getInstance();

		replies = findViewById(R.id.replies_recycler_view);
		swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
		commentEditText = findViewById(R.id.comment_edit_text);
		sendButton = findViewById(R.id.send_button);
		backButton = findViewById(R.id.back_button);
	}

	private void setListeners() {
		try {
			sendButton.setOnClickListener(v -> Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
				String comment = commentEditText.getText().toString();
				commentEditText.setText("");
				writeComment(comment, getTokenResult.getToken());
			}));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		backButton.setOnClickListener(v -> RepliesActivity.this.finish());
	}

	private void writeComment(String comment, final String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(Objects.requireNonNull(RepliesActivity.this));
		String commentURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/comment/";
		try {
			JSONObject commentInfo = new JSONObject();
			commentInfo.put("comment", comment);
			commentInfo.put("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail());
			commentInfo.put("parent", parentId);
			commentInfo.put("postId", postId);

			String requestBody = commentInfo.toString();

			StringRequest stringRequest = new StringRequest(Request.Method.POST, commentURL, response -> {
			}, error -> {
				if(error != null) {
					Log.i("downvoteCommentError", "downvoteComment: " + error.getMessage());
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

	private void setCommentRecyclerView() {
		try {
			CommentsRecyclerView commentsRecyclerView = new CommentsRecyclerView(RepliesActivity.this, replies, postId, parentId);
			commentsRecyclerView.setSwipeRefreshLayout(swipeRefreshLayout);
			swipeRefreshLayout.setOnRefreshListener(commentsRecyclerView::refreshPostsRecyclerView);

		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

}
