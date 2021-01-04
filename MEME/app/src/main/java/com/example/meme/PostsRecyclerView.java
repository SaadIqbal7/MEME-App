package com.example.meme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PostsRecyclerView {
	private Context context;
	private ArrayList<Post> posts;
	private PostAdapter postAdapter;
	private RecyclerView postsRecyclerView;
	private String postURL;
	private BottomSheet bottomSheet;
	private boolean bottomReached;
	private JSONObject postInfo;
	private SwipeRefreshLayout swipeRefreshLayout;

	private FirebaseFirestore db;
	private FirebaseAuth auth;
	// Temporary post for deleting and saving the post
	private Post tempPost;

	public PostsRecyclerView(Context context, RecyclerView postsRecyclerView, String postURL, JSONObject postInfo) {
		this.context = context;
		this.postsRecyclerView = postsRecyclerView;
		this.postURL = postURL;
		this.postInfo = postInfo;

		this.auth = FirebaseAuth.getInstance();
		this.db = FirebaseFirestore.getInstance();
		this.posts = new ArrayList<>();
		this.bottomReached = false;

		setAdapter();
		getInformation();
		createPostBottomSheet();
	}

	public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
		this.swipeRefreshLayout = swipeRefreshLayout;
	}

	public void refreshPostsRecyclerView() {
		posts.clear();
		postAdapter.notifyDataSetChanged();
		getInformation();
	}

	private void setAdapter() {
		OnBottomReachedListener onBottomReachedListener = (position) -> {
			if(bottomReached) {
				getInformation();
				bottomReached = false;
			}
		};

		PostRecyclerViewOnClickListeners postRecyclerViewOnClickListeners = new PostRecyclerViewOnClickListeners() {
			@Override
			public void upvotesButtonOnClickListener(View v, int position) {
				try {
					Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> upvotePost(posts.get(position).getPostID(), getTokenResult.getToken()));
				} catch (NullPointerException e) {
					e.printStackTrace();
				}

				if(posts.get(position).getUpvoteImage() == R.drawable.empty_heart) {
					posts.get(position).setUpvoteImage(R.drawable.color_heart);
					posts.get(position).setNumberOfUpVotes(posts.get(position).getNumberOfUpVotes() + 1);

					if(posts.get(position).getDownvoteImage() == R.drawable.color_dislike) {
						posts.get(position).setDownvoteImage(R.drawable.empty_dislike);
						posts.get(position).setNumberOfDownVotes(posts.get(position).getNumberOfDownVotes() - 1);
					}
				} else {
					posts.get(position).setUpvoteImage(R.drawable.empty_heart);
					posts.get(position).setNumberOfUpVotes(posts.get(position).getNumberOfUpVotes() - 1);
				}
				postAdapter.notifyDataSetChanged();
			}

			@Override
			public void downvotesButtonOnClickListener(View v, int position) {
				try {
					Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> downvotePost(posts.get(position).getPostID(), getTokenResult.getToken()));
				} catch (NullPointerException e) {
					e.printStackTrace();
				}

				if(posts.get(position).getDownvoteImage() == R.drawable.empty_dislike) {
					posts.get(position).setDownvoteImage(R.drawable.color_dislike);
					posts.get(position).setNumberOfDownVotes(posts.get(position).getNumberOfDownVotes() + 1);
					if(posts.get(position).getUpvoteImage() == R.drawable.color_heart) {
						posts.get(position).setUpvoteImage(R.drawable.empty_heart);
						posts.get(position).setNumberOfUpVotes(posts.get(position).getNumberOfUpVotes() - 1);
					}
				} else {
					posts.get(position).setDownvoteImage(R.drawable.empty_dislike);
					posts.get(position).setNumberOfDownVotes(posts.get(position).getNumberOfDownVotes() - 1);
				}
				postAdapter.notifyDataSetChanged();
			}

			@Override
			public void commentButtonOnClickListener(View v, int position) {
				// Move to comment activity
				Intent commentActivity = new Intent(context, CommentActivity.class);
				Bundle dataBundle = new Bundle();
				dataBundle.putString("postId", posts.get(position).getPostID());
				commentActivity.putExtras(dataBundle);
				context.startActivity(commentActivity);
			}

			@Override
			public void downloadButtonOnClickListener(View v, int position) {
				// Download image
				downloadImage(posts.get(position).getImageURL());
			}

			@Override
			public void postOptionsButtonOnClickListener(View v, int position) {
				tempPost = posts.get(position);
				ArrayList<BottomSheetListItem> bottomSheetListItemArrayList = new ArrayList<>();

				bottomSheetListItemArrayList.add(new BottomSheetListItem(
						R.drawable.ic_bookmark_border_white_24dp,
						"Save"
				));
				if(tempPost.getEmail().equals(Objects.requireNonNull(auth.getCurrentUser()).getEmail())) {
					bottomSheetListItemArrayList.add(new BottomSheetListItem(
							R.drawable.ic_delete_white_24dp,
							"Delete"
					));
				}

				bottomSheet.setBottomSheetListItemArrayList(bottomSheetListItemArrayList);
				bottomSheet.showBottomSheet();
			}

			@Override
			public void usernameOnClickListener(View v, int position) {
				Post post = posts.get(position);
				Intent profileIntent = new Intent(PostsRecyclerView.this.context, ProfileActivity.class);
				Bundle dataBundle = new Bundle();
				dataBundle.putString("email", post.getEmail());
				profileIntent.putExtras(dataBundle);
				context.startActivity(profileIntent);
			}

			@Override
			public void tag1OnClickListener(View v, int position) {
				Intent homeIntent = new Intent(PostsRecyclerView.this.context, HomeActivity.class);
				Bundle dataBundle = new Bundle();
				dataBundle.putInt("option", 2);
				dataBundle.putString("tag", posts.get(position).getTags().get(0));
				homeIntent.putExtras(dataBundle);
				context.startActivity(homeIntent);
			}

			@Override
			public void tag2OnClickListener(View v, int position) {
				Intent homeIntent = new Intent(PostsRecyclerView.this.context, HomeActivity.class);
				Bundle dataBundle = new Bundle();
				dataBundle.putInt("option", 2);
				dataBundle.putString("tag", posts.get(position).getTags().get(1));
				homeIntent.putExtras(dataBundle);
				context.startActivity(homeIntent);
			}

			@Override
			public void tag3OnClickListener(View v, int position) {
				Intent homeIntent = new Intent(PostsRecyclerView.this.context, HomeActivity.class);
				Bundle dataBundle = new Bundle();
				dataBundle.putInt("option", 2);
				dataBundle.putString("tag", posts.get(position).getTags().get(2));
				homeIntent.putExtras(dataBundle);
				context.startActivity(homeIntent);
			}
		};

		try {
			this.postAdapter = new PostAdapter(this.context, posts, onBottomReachedListener, postRecyclerViewOnClickListeners);
			postsRecyclerView.setLayoutManager(new LinearLayoutManager(this.context));
			postsRecyclerView.setAdapter(postAdapter);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

	private void getInformation() {
		Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
			RequestQueue requestQueue = Volley.newRequestQueue(Objects.requireNonNull(PostsRecyclerView.this.context));
			String postURL = PostsRecyclerView.this.postURL;
			try {
				if (posts.size() == 0) {
					postInfo.put("postId", "");
				} else {
					postInfo.put("postId", posts.get(posts.size() - 1).getPostID());
				}
				String requestBody = postInfo.toString();

				swipeRefreshLayout.setRefreshing(true);
				StringRequest stringRequest = new StringRequest(Request.Method.POST, postURL, response -> {
					try {
						JSONArray posts = new JSONArray(response);
						displayPosts(posts);
				} catch (JSONException e) {
						e.printStackTrace();
					}
				}, error -> {
					swipeRefreshLayout.setRefreshing(false);
					if(error != null) {
						Log.i("Get post information", "upvotePost: " + error.getMessage());
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
						headers.put("Authorization", "Bearer " 	+ getTokenResult.getToken());

						return headers;
					}
				};
				requestQueue.add(stringRequest);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		});
	}

	private void displayPosts(JSONArray postsInfo) {
		try {
			for (int i = 0; i < postsInfo.length(); i++) {
				JSONObject postInfo = postsInfo.getJSONObject(i);
				JSONObject postData = new JSONObject(postInfo.getString("postData"));
				String postId = postInfo.getString("postId");

				Post post = new Post();
				post.setPostID(postId);
				post.setEmail(postData.getString("email"));
				post.setCategory(postData.getString("category"));
				post.setDescription(postData.getString("description"));
				post.setImageURL(postData.getString("imageURL"));
				post.setNumberOfUpVotes(Integer.parseInt(postData.getString("numberOfUpvotes")));
				post.setNumberOfDownVotes(Integer.parseInt(postData.getString("numberOfDownvotes")));
				post.setNumberOfComments(Integer.parseInt(postData.getString("numberOfComments")));
				post.setTimeCreated(postData.getString("timeCreated"));
				post.setUsername(postData.getString("username"));

				ArrayList<String> tags = new ArrayList<>();
				JSONArray JSONArrayTags = postData.getJSONArray("tags");
				for (int j = 0; j < JSONArrayTags.length(); j++) {
					tags.add(JSONArrayTags.getString(j));
				}

				post.setTags(tags);

				try{
					voteUpdateRealtimeListener(post);
					posts.add(post);
				} catch (IndexOutOfBoundsException | NullPointerException e) {
					e.printStackTrace();
				}
			}
			try {
				postAdapter.notifyDataSetChanged();
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			bottomReached = true;
			swipeRefreshLayout.setRefreshing(false);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void upvotePost(String postId, final String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this.context));
		String postURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/post/upvote";
		try {
			JSONObject upvoteInfo = new JSONObject();
			upvoteInfo.put("postId", postId);
			upvoteInfo.put("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail());

			String requestBody = upvoteInfo.toString();

			StringRequest stringRequest = new StringRequest(Request.Method.POST, postURL, response -> {
			}, error -> {
				if(error != null) {
					Log.i("Upvote Post error", "upvotePost: " + error.getMessage());
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

	private void downvotePost(String postId, final String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(Objects.requireNonNull(this.context));
		String postURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/post/downvote";
		try {
			JSONObject downvoteInfo = new JSONObject();
			downvoteInfo.put("postId", postId);
			downvoteInfo.put("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail());

			String requestBody = downvoteInfo.toString();

			StringRequest stringRequest = new StringRequest(Request.Method.POST, postURL, response -> {
			}, error -> {
				if(error != null) {
					Log.i("Downvote Post error", "downvotePost: " + error.getMessage());
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

	private void voteUpdateRealtimeListener(Post post) {

		db.collection("postUpvotes")
				.whereEqualTo("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail())
				.whereEqualTo("postId", post.getPostID())
				.addSnapshotListener((queryDocumentSnapshots, e) -> {
					if(e != null) {
						Log.i("FirebaseFirestoreException", "onEvent: " + e.getMessage());
					}

					try {
						if(queryDocumentSnapshots != null) {
							for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
								if(dc.getType() == DocumentChange.Type.ADDED) {
									post.setUpvoteImage(R.drawable.color_heart);
									post.setDownvoteImage(R.drawable.empty_dislike);
								} else if(dc.getType() == DocumentChange.Type.REMOVED) {
									post.setUpvoteImage(R.drawable.empty_heart);
								}
							}
							postAdapter.notifyDataSetChanged();
						}

					} catch (NullPointerException err) {
						err.printStackTrace();
					}
				});

		db.collection("postDownvotes")
				.whereEqualTo("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail())
				.whereEqualTo("postId", post.getPostID())
				.addSnapshotListener((queryDocumentSnapshots, e) -> {
					if(e != null) {
						Log.i("FirebaseFirestoreException", "onEvent: " + e.getMessage());
					}

					try {
						if(queryDocumentSnapshots != null) {
							for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
								if(dc.getType() == DocumentChange.Type.ADDED) {
									post.setDownvoteImage(R.drawable.color_dislike);
									post.setUpvoteImage(R.drawable.empty_heart);
								} else if(dc.getType() == DocumentChange.Type.REMOVED) {
									post.setDownvoteImage(R.drawable.empty_dislike);
								}
							}
							postAdapter.notifyDataSetChanged();
						}
					} catch (NullPointerException err) {
						err.printStackTrace();
					}
				});
	}

	private void createPostBottomSheet() {
		RecycleViewerClickListener recycleViewerClickListener = (v, position) -> {
			if(position == 0) {
				// Save post
				Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> savePost(getTokenResult.getToken()));
			} else if(position == 1){
				// Delete post
				Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> deletePost(getTokenResult.getToken()));
			}
			this.bottomSheet.hideBottomSheet();
		};

		this.bottomSheet = new BottomSheet(this.context, new ArrayList<>(), recycleViewerClickListener);
	}

	private void savePost(final String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(this.context);
		String postURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/post/save";
		try {
			JSONObject postInfo = new JSONObject();
			postInfo.put("postId", tempPost.getPostID());
			postInfo.put("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail());

			String requestBody = postInfo.toString();

			StringRequest stringRequest = new StringRequest(Request.Method.POST, postURL, response -> {
				try{
					JSONObject successResponse = new JSONObject(response);
					Toast.makeText(this.context, successResponse.getString("success"), Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			},error -> Log.i("savePostError", "savePost: " + error.getMessage())) {
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

	private void deletePost(final String token) {
		RequestQueue requestQueue = Volley.newRequestQueue(this.context);
		String postURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/post/delete";
		try {
			JSONObject postInfo = new JSONObject();
			postInfo.put("postId", tempPost.getPostID());
			postInfo.put("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail());

			String requestBody = postInfo.toString();

			StringRequest stringRequest = new StringRequest(Request.Method.POST, postURL, response -> {
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

	private void downloadImage(String imageURL) {
		// Get image name
		// Image name will be the unique token of the image
		String imageName = imageURL.split("token=")[1];
		// Get extension
		String extension = imageURL.split("_post")[1].split("\\?")[0].replace(imageName, "");
		Thread thread = new Thread(() -> {
			// Convert image url to bitmap
			try {
				URL url = new URL(imageURL);
				Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/MEME";
				File dir = new File(filePath);
				if(!dir.exists())
					dir.mkdirs();

				File file = new File(dir, imageName + extension);
				FileOutputStream fOut = new FileOutputStream(file);
				image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
				fOut.flush();
				fOut.close();
				isFileAvailable(file);
				// Will display a toast message
				ableToSave();
			} catch(IOException e) {
				e.printStackTrace();
			}
		});
		thread.start();
	}

	private void ableToSave() {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(() -> Toast.makeText(PostsRecyclerView.this.context, "Picture saved", Toast.LENGTH_SHORT).show());
	}

	private void isFileAvailable(File file) {
		MediaScannerConnection.scanFile(this.context, new String[]{file.toString()}, null, (path, uri) -> Log.i("OnScanCompleted", "onScanCompleted: " + path));
	}
}
