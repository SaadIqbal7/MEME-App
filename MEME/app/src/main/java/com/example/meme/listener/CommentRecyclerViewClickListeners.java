package com.example.meme.listener;

import android.view.View;

public interface CommentRecyclerViewClickListeners {
	void upvotesButtonOnClickListener(View v, int position);
	void downvotesButtonOnClickListener(View v, int position);
	void replyButtonOnClickListener(View v, int position);
	void commentOptionsButtonOnClickListener(View v, int position);
	void usernameOnClickListener(View v, int position);
	void profilePictureOnClickListener(View n, int position);
}
