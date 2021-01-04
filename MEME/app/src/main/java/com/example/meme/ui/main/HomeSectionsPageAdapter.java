package com.example.meme.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.meme.HotPostsFragment;
import com.example.meme.LatestPostsFragment;

import org.jetbrains.annotations.NotNull;

public class HomeSectionsPageAdapter extends FragmentPagerAdapter {
	@SuppressLint("SupportAnnotationUsage")
	@StringRes
	private final Context mContext;

	public HomeSectionsPageAdapter(Context context, FragmentManager fm) {
		super(fm);
		mContext = context;
	}

	@NotNull
	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		switch (position){
			case 0:
				return new HotPostsFragment();

			case 1:
				return new LatestPostsFragment();

			default:
				return null;
		}
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		switch (position){
			case 0:
				return "Hot";

			case 1:
				return "Latest";

			default:
				return null;
		}
	}

	@Override
	public int getCount() {
		// Show 3 total pages.
		return 2;
	}
}
