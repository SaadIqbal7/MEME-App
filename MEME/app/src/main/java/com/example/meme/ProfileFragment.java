package com.example.meme;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileFragment extends Fragment {

	private FirebaseAuth auth;
	private ArrayList<IconTextListItem> iconTextListItems;
	private Bundle dataBundle;
	private RecycleViewerClickListener listener;
	private User user;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Get profileActivity
		ProfileActivity profileActivity = (ProfileActivity) getActivity();

		// Set the dataBundle to be used in this fragment
		assert profileActivity != null;
		dataBundle = profileActivity.getDataBundle();

		try {
			JSONObject userInfo = new JSONObject(Objects.requireNonNull(dataBundle.getString("userInfo")));
			// Create new user
			user = new User();
			user.setEmail(userInfo.getString("email"));
			user.setNumberOfPosts(userInfo.getInt("numberOfPosts"));
			user.setNumberOfSavedPosts(userInfo.getInt("numberOfSavedPosts"));
			user.setNumberOfUpvotedPosts(userInfo.getInt("numberOfUpvotedPosts"));

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return inflater.inflate(R.layout.fragment_profile, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		// Initialize views and variables
		RecyclerView profileItemRecyclerView = view.findViewById(R.id.profile_items_recycler_view);
		iconTextListItems = new ArrayList<>();
		auth = FirebaseAuth.getInstance();

		// Sets up the profile according to the user visiting the profile
		// (The logged in user might be visiting their own profile or some other user's)
		displayProfileInformation();

		// Instantiate the custom adapter
		IconTextAdapter iconTextAdapter = new IconTextAdapter(getContext(), iconTextListItems, listener);

		// Set layout manager for recyclerView
		profileItemRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		// Set the custom adapter for the recyclerView
		profileItemRecyclerView.setAdapter(iconTextAdapter);
	}

	private void displayProfileInformation() {
		iconTextListItems.add(
				new IconTextListItem(
						R.drawable.invoice,
						"Posts (" + user.getNumberOfPosts() + ")",
						ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorPrimaryDark),
						""));
		iconTextListItems.add(
				new IconTextListItem(
						R.drawable.talk,
						"Upvoted Posts (" + user.getNumberOfUpvotedPosts() + ")",
						ContextCompat.getColor(getContext(), R.color.iconColorPink),
						""));

		// Checks if the profile is of the logged in user's or not
		if (Objects.equals(dataBundle.getString("email"), Objects.requireNonNull(auth.getCurrentUser()).getEmail())) {
			// Add list of options
			iconTextListItems.add(
					new IconTextListItem(
							R.drawable.bookmark,
							"Saved Posts (" + user.getNumberOfSavedPosts() + ")",
							ContextCompat.getColor(getContext(), R.color.iconColorOrange),
							""));
			iconTextListItems.add(
					new IconTextListItem(
							R.drawable.one,
							"Memer rank and score",
							ContextCompat.getColor(getContext(), R.color.iconColorLightBlue),
							""));
			iconTextListItems.add(
					new IconTextListItem(
							R.drawable.resume,
							"Edit profile",
							ContextCompat.getColor(getContext(), R.color.iconColorLightOrange),
							""));
			iconTextListItems.add(
					new IconTextListItem(
							R.drawable.shield,
							"Change password",
							ContextCompat.getColor(getContext(), R.color.iconColorLightGreen),
							""));

			listener = (v, position) -> {

				if(position == 2){
					Intent savedPostsIntent = new Intent(getActivity(), UserSavedPostsActivity.class);
					startActivity(savedPostsIntent);
				} else if(position == 3){
					Intent userScoreAndActivityIntent = new Intent(getActivity(), UserScoreAndRankActivity.class);
					Bundle dataBundle = new Bundle();
					dataBundle.putString("email", auth.getCurrentUser().getEmail());
					userScoreAndActivityIntent.putExtras(dataBundle);
					startActivity(userScoreAndActivityIntent);
				} else if(position == 4) {
					Intent editProfileIntent = new Intent(getContext(), EditProfileActivity.class);
					startActivity(editProfileIntent);
				} else if(position == 5) {
					Intent changePasswordIntent = new Intent(getActivity(), ChangePasswordActivity.class);
					startActivity(changePasswordIntent);
				}
			};
		} else {
			iconTextListItems.add(
					new IconTextListItem(
							R.drawable.one,
							"Memer rank and score",
							ContextCompat.getColor(getContext(), R.color.iconColorLightBlue),
							""));

			listener = (v, position) -> {
				if(position == 2) {
					Intent userScoreAndActivityIntent = new Intent(getActivity(), UserScoreAndRankActivity.class);
					Bundle dataBundle = new Bundle();
					dataBundle.putString("email", user.getEmail());
					userScoreAndActivityIntent.putExtras(dataBundle);
					startActivity(userScoreAndActivityIntent);
				}
			};
		}
	}
}
