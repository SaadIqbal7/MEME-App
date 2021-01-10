package com.example.meme.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.widget.ImageView;

import com.example.meme.container.Helpers;
import com.example.meme.recycleview.PostsRecyclerView;
import com.example.meme.R;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class UserSavedPostsActivity extends AppCompatActivity {

	private FirebaseAuth auth;

	private SwipeRefreshLayout swipeRefreshLayout;
	private RecyclerView savedPostsRecyclerView;
	private ImageView backButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_saved_posts);

		initializeVariables();
		setListeners();
		setPostsRecyclerView();
	}

	private void initializeVariables() {
		swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
		savedPostsRecyclerView = findViewById(R.id.saved_posts_recycler_view);
		backButton = findViewById(R.id.back_button);

		auth = FirebaseAuth.getInstance();
	}

	private void setListeners() {
		backButton.setOnClickListener(v -> UserSavedPostsActivity.this.finish());
	}

	private void setPostsRecyclerView() {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail());

			PostsRecyclerView postsRecyclerView = new PostsRecyclerView(
					UserSavedPostsActivity.this,
					savedPostsRecyclerView,
					Helpers.apiUrl + "/webApi/api/post/saved",
					jsonObject
			);

			postsRecyclerView.setSwipeRefreshLayout(swipeRefreshLayout);
			swipeRefreshLayout.setOnRefreshListener(postsRecyclerView::refreshPostsRecyclerView);

		} catch (NullPointerException | JSONException e) {
			e.printStackTrace();
		}
	}
}
