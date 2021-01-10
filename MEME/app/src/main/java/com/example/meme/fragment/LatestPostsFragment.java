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

import com.example.meme.recycleview.PostsRecyclerView;
import com.example.meme.R;
import com.example.meme.activity.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class LatestPostsFragment extends Fragment {

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_latest_posts, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		RecyclerView latestPostRecyclerView = view.findViewById(R.id.latest_posts_recycler_view);
		SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
		String postURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/post/latest";

		try {
			HomeActivity homeActivity = (HomeActivity) getActivity();
			assert homeActivity != null;
			Bundle dataBundle = homeActivity.getDataBundle();

			JSONObject jsonObject = new JSONObject();
			if(dataBundle == null) {
				jsonObject = new JSONObject();
			} else {
				// option 1 is for category, 2 is for tags
				int option = dataBundle.getInt("option");
				if(option == 1) {
					String category = dataBundle.getString("category");
					jsonObject.put("category", category);
					postURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/post/latest/category";
				} else if(option == 2) {
					String tag = dataBundle.getString("tag");
					jsonObject.put("tag", tag);
					postURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/post/latest/tag";
				}
			}

			PostsRecyclerView postsRecyclerView = new PostsRecyclerView(
					getActivity(),
					latestPostRecyclerView,
					postURL,
					jsonObject
			);

			postsRecyclerView.setSwipeRefreshLayout(swipeRefreshLayout);
			swipeRefreshLayout.setOnRefreshListener(postsRecyclerView::refreshPostsRecyclerView);
		} catch (JSONException | NullPointerException e) {
			e.printStackTrace();
		}
	}
}
