package com.example.meme.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.meme.adapter.IconTextAdapter;
import com.example.meme.container.Helpers;
import com.example.meme.listitem.IconTextListItem;
import com.example.meme.R;
import com.example.meme.listener.RecycleViewerClickListener;
import com.example.meme.container.User;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserScoreAndRankActivity extends AppCompatActivity {

	private FirebaseAuth auth;
	private String email;
	private ArrayList<IconTextListItem> iconTextListItems;
	private User user;
	private IconTextAdapter iconTextAdapter;

	private TextView userScore;
	private TextView maxScore;
	private ProgressBar foregroundProgressBar;
	private RecyclerView scoreDetailsRecyclerView;
	private ImageView backButton;
	private ProgressBar progressBar;
	private ImageView refreshButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_score_and_rank);

		email = Objects.requireNonNull(getIntent().getExtras()).getString("email");

		initializeVariables();
		setListeners();
		setAdapter();

		Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getIdToken -> getInformation(getIdToken.getToken()));

	}

	private void initializeVariables() {
		this.userScore = findViewById(R.id.user_score);
		this.maxScore = findViewById(R.id.max_score);
		this.foregroundProgressBar = findViewById(R.id.foreground_progress_bar);
		this.scoreDetailsRecyclerView = findViewById(R.id.score_details_recycler_view);
		this.backButton = findViewById(R.id.back_button);
		this.progressBar = findViewById(R.id.progress_bar);
		this.refreshButton = findViewById(R.id.refresh_button);

		this.auth = FirebaseAuth.getInstance();
		this.user = new User();
	}

	private void setListeners() {
		backButton.setOnClickListener(v -> UserScoreAndRankActivity.this.finish());
		refreshButton.setOnClickListener(v -> Objects.requireNonNull(auth.getCurrentUser()).getIdToken(true).addOnSuccessListener(getTokenResult -> {
			iconTextListItems.clear();
			getInformation(getTokenResult.getToken());
		}));
	}

	private void setAdapter() {
		iconTextListItems = new ArrayList<>();
		RecycleViewerClickListener recycleViewerClickListener = (view, position) -> {};

		iconTextAdapter = new IconTextAdapter(UserScoreAndRankActivity.this, iconTextListItems, recycleViewerClickListener);
		scoreDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(UserScoreAndRankActivity.this));
		scoreDetailsRecyclerView.setAdapter(iconTextAdapter);
	}

	private void getInformation(final String token) {

		RequestQueue requestQueue = Volley.newRequestQueue(UserScoreAndRankActivity.this);
		final String userURL = Helpers.apiUrl + "/webApi/api/user/" + email;

		refreshButton.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		StringRequest stringRequest = new StringRequest(Request.Method.GET, userURL, response -> {
			try {
				refreshButton.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);

				JSONObject userInfo = new JSONObject(response);
				user.setUserScore(userInfo.getInt("userScore"));
				user.setUserRank(userInfo.getString("userRank"));
				displayScoreDetails();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}, error -> {
			refreshButton.setVisibility(View.VISIBLE);
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
			public Map<String, String> getHeaders() {
				Map<String, String> headers = new HashMap<>();
				headers.put("Authorization", "Bearer " + token);

				return headers;
			}
		};

		requestQueue.add(stringRequest);
	}

	private void displayScoreDetails() {
		float userScore = user.getUserScore();
		float maxScore = 0;

		if(userScore < 0) {
			maxScore = 0;
		} else if (userScore < 20) {
			maxScore = 20;
		} else if (userScore < 100) {
			maxScore = 100;
		} else if (userScore < 350) {
			maxScore = 350;
		} else if (userScore < 500) {
			maxScore = 500;
		} else if (userScore < 1000) {
			maxScore = 1000;
		} else if (userScore < 2000) {
			maxScore = 2000;
		} else if (userScore < 3500) {
			maxScore = 3500;
		} else if (userScore < 5000) {
			maxScore = 5000;
		} else if (userScore >= 5000) {
			maxScore = 7500;
		}

		this.userScore.setText(String.valueOf((int) userScore));
		this.maxScore.setText(String.valueOf((int) maxScore));

		this.foregroundProgressBar.setMax((int) maxScore);
		this.foregroundProgressBar.setProgress((int) userScore);

		iconTextListItems.add(new IconTextListItem(
				R.drawable.score_icon,
				"Score",
				ContextCompat.getColor(UserScoreAndRankActivity.this, R.color.primary_dark),
				String.valueOf(user.getUserScore())
		));

		iconTextListItems.add(new IconTextListItem(
				R.drawable.slow,
				"Level up score",
				ContextCompat.getColor(UserScoreAndRankActivity.this, R.color.iconLighterBlue),
				String.valueOf((int)maxScore)
		));

		if(maxScore == 0) {
			String percentage = "--";
			iconTextListItems.add(new IconTextListItem(
					R.drawable.percentage,
					"Percentage",
					ContextCompat.getColor(UserScoreAndRankActivity.this, R.color.iconLightOrange),
					percentage
			));
		} else {
			iconTextListItems.add(new IconTextListItem(
					R.drawable.percentage,
					"Percentage",
					ContextCompat.getColor(UserScoreAndRankActivity.this, R.color.iconLightOrange),
					String.valueOf(Float.valueOf((userScore / maxScore) * 100))
			));
		}
		iconTextListItems.add(new IconTextListItem(
				R.drawable.rank,
				"Rank",
				ContextCompat.getColor(UserScoreAndRankActivity.this, R.color.iconDarkPink),
				user.getUserRank()
		));
		iconTextAdapter.notifyDataSetChanged();
	}
}
