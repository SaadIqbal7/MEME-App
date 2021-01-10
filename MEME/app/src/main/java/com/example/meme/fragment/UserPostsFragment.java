package com.example.meme.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.meme.container.Helpers;
import com.example.meme.recycleview.PostsRecyclerView;
import com.example.meme.activity.ProfileActivity;
import com.example.meme.R;

import org.json.JSONException;
import org.json.JSONObject;

public class UserPostsFragment extends Fragment {

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_user_posts, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		// Get profileActivity
		ProfileActivity profileActivity = (ProfileActivity) getActivity();

		try {
			// Set the dataBundle to be used in this fragment
			assert profileActivity != null;
			Bundle dataBundle = profileActivity.getDataBundle();

			RecyclerView hotPostsRecyclerView = view.findViewById(R.id.user_posts_recycler_view);
			SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("email", dataBundle.getString("email"));

			PostsRecyclerView postsRecyclerView = new PostsRecyclerView(
					getActivity(),
					hotPostsRecyclerView,
					Helpers.apiUrl + "/webApi/api/post/email",
					jsonObject
			);

			postsRecyclerView.setSwipeRefreshLayout(swipeRefreshLayout);
			swipeRefreshLayout.setOnRefreshListener(postsRecyclerView::refreshPostsRecyclerView);

		} catch (NullPointerException | JSONException e) {
			e.printStackTrace();
		}

	}
}
