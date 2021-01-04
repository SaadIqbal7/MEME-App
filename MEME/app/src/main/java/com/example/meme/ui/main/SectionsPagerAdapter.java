package com.example.meme.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.meme.ProfileFragment;
import com.example.meme.UserPostsFragment;
import com.example.meme.UserUpvotedPostsFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

	@SuppressLint("SupportAnnotationUsage")
	@StringRes
	private final Context mContext;

	public SectionsPagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		mContext = context;
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		switch (position){
			case 0:
				return new ProfileFragment();

			case 1:
				return new UserPostsFragment();

			case 2:
				return new UserUpvotedPostsFragment();

			default:
				return null;
		}
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		switch (position){
			case 0:
				return "Profile";

			case 1:
				return "Posts";

			case 2:
				return "Upvoted";

			default:
				return null;
		}
	}

	@Override
	public int getCount() {
		// Show 3 total pages.
		return 3;
	}
}