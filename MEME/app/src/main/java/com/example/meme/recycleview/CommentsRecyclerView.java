package com.example.meme.recycleview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.meme.listener.CommentRecyclerViewClickListeners;
import com.example.meme.activity.ProfileActivity;
import com.example.meme.R;
import com.example.meme.listener.RecycleViewerClickListener;
import com.example.meme.activity.RepliesActivity;
import com.example.meme.adapter.CommentAdapter;
import com.example.meme.container.BottomSheet;
import com.example.meme.container.Comment;
import com.example.meme.listitem.BottomSheetListItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CommentsRecyclerView {
	private Context context;
	private ArrayList<Comment> comments;
	private CommentAdapter commentAdapter;
	private RecyclerView commentsRecyclerView;
	private String postId;
	private String parentId;
	private BottomSheet bottomSheet;
	private SwipeRefreshLayout swipeRefreshLayout;

	private FirebaseFirestore db;
	private FirebaseAuth auth;
	// Temporary comment for deleting a comment
	private Comment tempComment;

	public CommentsRecyclerView(Context context, RecyclerView commentsRecyclerView, String postId, String parentId) {
		this.context = context;
		this.commentsRecyclerView = commentsRecyclerView;
		this.postId = postId;
		this.parentId = parentId;

		this.auth = FirebaseAuth.getInstance();
		this.db = FirebaseFirestore.getInstance();
		this.comments = new ArrayList<>();

		setAdapter();
		getComments();
		commentsRecyclerView.scrollToPosition(comments.size() - 1);
		createCommentBottomSheet();
	}

	public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
		this.swipeRefreshLayout = swipeRefreshLayout;
	}

	public void refreshPostsRecyclerView() {
		comments.clear();
		getComments();
	}

	private void setAdapter() {
		CommentRecyclerViewClickListeners commentRecyclerViewClickListeners = new CommentRecyclerViewClickListeners() {
			@Override
			public void upvotesButtonOnClickListener(View v, int position) {
				try {
					Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> upvoteComment(comments.get(position).getCommentId(), getTokenResult.getToken()));
				} catch (NullPointerException e) {
					e.printStackTrace();
				}

				if(comments.get(position).getUpvoteImage() == R.drawable.empty_heart) {
					comments.get(position).setUpvoteImage(R.drawable.color_heart);
					comments.get(position).setNumberOfUpvotes(comments.get(position).getNumberOfUpvotes() + 1);

					if(comments.get(position).getDownvoteImage() == R.drawable.color_dislike) {
						comments.get(position).setDownvoteImage(R.drawable.empty_dislike);
						comments.get(position).setNumberOfDownvotes(comments.get(position).getNumberOfDownvotes() - 1);
					}
				} else {
					comments.get(position).setUpvoteImage(R.drawable.empty_heart);
					comments.get(position).setNumberOfUpvotes(comments.get(position).getNumberOfUpvotes() - 1);
				}
				commentAdapter.notifyDataSetChanged();

			}

			@Override
			public void downvotesButtonOnClickListener(View v, int position) {
				try {
					Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> downvoteComment(comments.get(position).getCommentId(), getTokenResult.getToken()));
				} catch (NullPointerException e) {
					e.printStackTrace();
				}

				if(comments.get(position).getDownvoteImage() == R.drawable.empty_dislike) {
					comments.get(position).setDownvoteImage(R.drawable.color_dislike);
					comments.get(position).setNumberOfDownvotes(comments.get(position).getNumberOfDownvotes() + 1);
					if(comments.get(position).getUpvoteImage() == R.drawable.color_heart) {
						comments.get(position).setUpvoteImage(R.drawable.empty_heart);
						comments.get(position).setNumberOfUpvotes(comments.get(position).getNumberOfUpvotes() - 1);
					}
				} else {
					comments.get(position).setDownvoteImage(R.drawable.empty_dislike);
					comments.get(position).setNumberOfDownvotes(comments.get(position).getNumberOfDownvotes() - 1);
				}
				commentAdapter.notifyDataSetChanged();
			}

			@Override
			public void replyButtonOnClickListener(View v, int position) {
				Intent repliesIntent = new Intent(CommentsRecyclerView.this.context, RepliesActivity.class);
				Bundle dataBundle = new Bundle();
				dataBundle.putString("postId", comments.get(position).getPostId());
				dataBundle.putString("parentId", comments.get(position).getCommentId());
				repliesIntent.putExtras(dataBundle);
				context.startActivity(repliesIntent);
			}

			@Override
			public void commentOptionsButtonOnClickListener(View v, int position) {
				tempComment = comments.get(position);
				if(tempComment.getEmail().equals(Objects.requireNonNull(auth.getCurrentUser()).getEmail())) {
					ArrayList<BottomSheetListItem> bottomSheetListItemArrayList = new ArrayList<>();
					bottomSheetListItemArrayList.add(new BottomSheetListItem(
							R.drawable.ic_delete_white_24dp,
							"Delete"
					));
					bottomSheet.setBottomSheetListItemArrayList(bottomSheetListItemArrayList);
					bottomSheet.showBottomSheet();
				}
			}

			@Override
			public void usernameOnClickListener(View v, int position) {
				toProfileActivity(position);
			}

			@Override
			public void profilePictureOnClickListener(View n, int position) {
				toProfileActivity(position);
			}
		};

		this.commentAdapter = new CommentAdapter(this.context, this.comments, commentRecyclerViewClickListeners);
		commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this.context));
		commentsRecyclerView.setAdapter(commentAdapter);
	}

	private void toProfileActivity(int position) {
		Comment comment = comments.get(position);
		Intent profileIntent = new Intent(CommentsRecyclerView.this.context, ProfileActivity.class);
		Bundle dataBundle = new Bundle();
		dataBundle.putString("email", comment.getEmail());
		profileIntent.putExtras(dataBundle);
		context.startActivity(profileIntent);
	}

	private void getComments() {
		// Get comments in real time

		// Check if comments are required
		if(!postId.isEmpty() && parentId.isEmpty()) {
			db.collection("comments").whereEqualTo("postId", postId).whereEqualTo("parent", "").orderBy("timeCreated", Query.Direction.ASCENDING).addSnapshotListener((queryDocumentSnapshots, e) -> {
				if(e != null) {
					Log.i("FirebaseFirestoreException", "getInformation: " + e.getMessage());
					return;
				}

				try {
					assert queryDocumentSnapshots != null;
					for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
						if(dc.getType() == DocumentChange.Type.ADDED) {
							displayComment(dc.getDocument());
						} else if(dc.getType() == DocumentChange.Type.REMOVED) {
							removeComment(dc.getDocument());
						}
					}
				} catch (NullPointerException err) {
					err.printStackTrace();
				}
			});
			// Check if replies are required
		} else if(!postId.isEmpty() && !parentId.isEmpty()) {
			db.collection("comments").whereEqualTo("parent", parentId).orderBy("timeCreated", Query.Direction.ASCENDING).addSnapshotListener((queryDocumentSnapshots, e) -> {
				if(e != null) {
					Log.i("FirebaseFirestoreException", "getInformation: " + e.getMessage());
					return;
				}

				try {
					assert queryDocumentSnapshots != null;
					for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
						if(dc.getType() == DocumentChange.Type.ADDED) {
							displayComment(dc.getDocument());
						}
					}
				} catch (NullPointerException err) {
					err.printStackTrace();
				}
			});
		}
	}

	private void displayComment(QueryDocumentSnapshot document) {
		Comment comment = new Comment();
		comment.setCommentId(document.getId());
		comment.setParent(document.getString("parent"));
		comment.setPostId(document.getString("postId"));
		comment.setEmail(document.getString("email"));
		comment.setDescription(document.getString("comment"));
		comment.setNumberOfUpvotes(Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(document.get("numberOfUpvotes")).toString())));
		comment.setNumberOfDownvotes(Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(document.get("numberOfDownvotes")).toString())));
		comment.setNumberOfReplies(Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(document.get("numberOfReplies")).toString())));
		comment.setTimeCreated(document.getString("timeCreated"));
		comment.setUsername(document.getString("username"));
		voteUpdateRealtimeListener(comment);

		commentAdapter.notifyDataSetChanged();
		comments.add(comment);
		swipeRefreshLayout.setRefreshing(false);
	}

	private void voteUpdateRealtimeListener(Comment comment) {
		db.collection("commentUpvotes")
				.whereEqualTo("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail())
				.whereEqualTo("commentId", comment.getCommentId())
				.addSnapshotListener((queryDocumentSnapshots, e) -> {
					if(e != null) {
						Log.i("FirebaseFirestoreException", "onEvent: " + e.getMessage());
					}

					try {
						if(queryDocumentSnapshots != null) {
							for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
								if(dc.getType() == DocumentChange.Type.ADDED) {
									comment.setUpvoteImage(R.drawable.color_heart);
									comment.setDownvoteImage(R.drawable.empty_dislike);
								} else if(dc.getType() == DocumentChange.Type.REMOVED) {
									comment.setUpvoteImage(R.drawable.empty_heart);
								}
							}
							commentAdapter.notifyDataSetChanged();
						}

					} catch (NullPointerException err) {
						err.printStackTrace();
					}
				});

		db.collection("commentDownvotes")
				.whereEqualTo("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail())
				.whereEqualTo("commentId", comment.getCommentId())
				.addSnapshotListener((queryDocumentSnapshots, e) -> {
					if(e != null) {
						Log.i("FirebaseFirestoreException", "onEvent: " + e.getMessage());
					}

					try {
						if(queryDocumentSnapshots != null) {
							for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
								if(dc.getType() == DocumentChange.Type.ADDED) {
									comment.setDownvoteImage(R.drawable.color_dislike);
									comment.setUpvoteImage(R.drawable.empty_heart);
								} else if(dc.getType() == DocumentChange.Type.REMOVED) {
									comment.setDownvoteImage(R.drawable.empty_dislike);
								}
							}
							commentAdapter.notifyDataSetChanged();
						}
					} catch (NullPointerException err) {
						err.printStackTrace();
					}
				});
	}

	private void downvoteComment(String commentId, String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this.context));
		String commentURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/comment/downvote";
		try {
			JSONObject upvoteInfo = new JSONObject();
			upvoteInfo.put("commentId", commentId);
			upvoteInfo.put("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail());

			String requestBody = upvoteInfo.toString();

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

	private void upvoteComment(String commentId, String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this.context));
		String commentURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/comment/upvote";
		try {
			JSONObject upvoteInfo = new JSONObject();
			upvoteInfo.put("commentId", commentId);
			upvoteInfo.put("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail());

			String requestBody = upvoteInfo.toString();

			StringRequest stringRequest = new StringRequest(Request.Method.POST, commentURL, response -> {
			}, error -> {
				if(error != null) {
					Log.i("UpvoteCommentError", "upvoteComment: " + error.getMessage());
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

	private void createCommentBottomSheet() {
		RecycleViewerClickListener recycleViewerClickListener = (v, position) -> {
			if(position == 0) {
				// Delete comment
				Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> deleteComment(getTokenResult.getToken()));
			}
			this.bottomSheet.hideBottomSheet();
		};
		this.bottomSheet = new BottomSheet(this.context, new ArrayList<>(), recycleViewerClickListener);
	}

	private void deleteComment(final String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(this.context);
		String commentURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/comment/delete";
		try {
			JSONObject postInfo = new JSONObject();
			postInfo.put("commentId", tempComment.getCommentId());
			postInfo.put("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail());

			String requestBody = postInfo.toString();

			StringRequest stringRequest = new StringRequest(Request.Method.POST, commentURL, response -> {
				try{
					JSONObject successResponse = new JSONObject(response);
					Toast.makeText(this.context, successResponse.getString("success"), Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			},error -> {
				try {
					int statusCode = error.networkResponse.statusCode;
					if(statusCode == 400) {
						JSONObject err = new JSONObject(new String(error.networkResponse.data));
						Toast.makeText(this.context, err.getString("error"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException | NullPointerException e) {
					e.printStackTrace();
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

	private void removeComment(QueryDocumentSnapshot document) {
		comments.removeIf(comment -> comment.getCommentId().equals(document.getId()));
		commentAdapter.notifyDataSetChanged();
	}
}
