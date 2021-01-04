package com.example.meme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class HotPostsFragment extends Fragment {

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_hot_posts, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		RecyclerView hotPostsRecyclerView = view.findViewById(R.id.hot_posts_recycler_view);
		SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

		String postURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/post/hot";

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
					postURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/post/hot/category";
				} else if(option == 2) {
					String tag = dataBundle.getString("tag");
					jsonObject.put("tag", tag);
					postURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/post/hot/tag";
				}
			}

			PostsRecyclerView postsRecyclerView = new PostsRecyclerView(
					getActivity(),
					hotPostsRecyclerView,
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
