package com.example.meme.listener;

import android.view.View;

public interface PostRecyclerViewOnClickListeners {
	void upvotesButtonOnClickListener(View v, int position);
	void downvotesButtonOnClickListener(View v, int position);
	void commentButtonOnClickListener(View v, int position);
	void downloadButtonOnClickListener(View v, int position);
	void postOptionsButtonOnClickListener(View v, int position);
	void usernameOnClickListener(View v, int position);
	void tag1OnClickListener(View v, int position);
	void tag2OnClickListener(View v, int position);
	void tag3OnClickListener(View v, int position);
}
