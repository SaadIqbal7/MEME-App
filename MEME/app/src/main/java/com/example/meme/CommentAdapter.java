package com.example.meme;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

	private Context context;
	private ArrayList<Comment> comments;
	private CommentRecyclerViewClickListeners commentRecyclerViewClickListeners;

	CommentAdapter(Context context, ArrayList<Comment> comments, CommentRecyclerViewClickListeners commentRecyclerViewClickListeners) {
		this.context = context;
		this.comments = comments;
		this.commentRecyclerViewClickListeners = commentRecyclerViewClickListeners;
	}

	@NonNull
	@Override
	public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(this.context);
		View commentView = inflater.inflate(R.layout.comment_layout, parent, false);
		return new CommentViewHolder(this.context, commentView, this.commentRecyclerViewClickListeners);
	}

	@Override
	public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
		Comment comment = comments.get(position);
		holder.setComment(comment);
	}

	@Override
	public int getItemCount() {
		return comments.size();
	}

	class CommentViewHolder extends RecyclerView.ViewHolder {
		private Context context;
		private CommentRecyclerViewClickListeners commentRecyclerViewClickListeners;
		private FirebaseFirestore db;

		private CircleImageView profilePicture;
		private TextView usernameTextView;
		private TextView timeCreatedTextView;
		private ImageView commentOptionsButton;
		private TextView commentTextView;
		private LinearLayout upvoteButton;
		private ImageView upvoteImageView;
		private TextView upvoteCountTextView;
		private LinearLayout downvoteButton;
		private ImageView downvoteImageView;
		private TextView downvoteCountTextView;
		private LinearLayout replyButton;
		private TextView replyCountTextView;

		CommentViewHolder(Context context, @NonNull View itemView, CommentRecyclerViewClickListeners commentRecyclerViewClickListeners) {
			super(itemView);
			this.context= context;
			this.commentRecyclerViewClickListeners = commentRecyclerViewClickListeners;
			this.db = FirebaseFirestore.getInstance();

			// Initialize views
			profilePicture = itemView.findViewById(R.id.profile_picture);
			usernameTextView = itemView.findViewById(R.id.username_text_view);
			timeCreatedTextView = itemView.findViewById(R.id.time_created_text_view);
			commentOptionsButton = itemView.findViewById(R.id.comment_options);
			commentTextView = itemView.findViewById(R.id.comment_text_view);
			upvoteButton = itemView.findViewById(R.id.upvotes_button);
			upvoteImageView = itemView.findViewById(R.id.upvotes_image_view);
			upvoteCountTextView = itemView.findViewById(R.id.upvote_count_text_view);
			downvoteButton = itemView.findViewById(R.id.downvotes_button);
			downvoteImageView = itemView.findViewById(R.id.downvotes_image_view);
			downvoteCountTextView = itemView.findViewById(R.id.downvote_count_text_view);
			replyButton = itemView.findViewById(R.id.replies_button);
			replyCountTextView = itemView.findViewById(R.id.reply_count_text_view);

			setListeners();
		}

		private void setListeners() {
			upvoteButton.setOnClickListener(v -> this.commentRecyclerViewClickListeners.upvotesButtonOnClickListener(v, getAdapterPosition()));
			downvoteButton.setOnClickListener(v -> this.commentRecyclerViewClickListeners.downvotesButtonOnClickListener(v, getAdapterPosition()));
			replyButton.setOnClickListener(v -> this.commentRecyclerViewClickListeners.replyButtonOnClickListener(v, getAdapterPosition()));
			commentOptionsButton.setOnClickListener(v -> this.commentRecyclerViewClickListeners.commentOptionsButtonOnClickListener(v, getAdapterPosition()));
			usernameTextView.setOnClickListener(v -> this.commentRecyclerViewClickListeners.usernameOnClickListener(v, getAdapterPosition()));
			profilePicture.setOnClickListener(v -> this.commentRecyclerViewClickListeners.profilePictureOnClickListener(v, getAdapterPosition()));
		}

		private void setComment(Comment comment) {
			db.collection("users").whereEqualTo("email", comment.getEmail()).addSnapshotListener((queryDocumentSnapshots, e) -> {
				if(e != null) {
					Log.i("FirebaseFirestoreException", "setPost: " + e.getMessage());
					return;
				}

				assert queryDocumentSnapshots != null;
				for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
					if(dc.getType() == DocumentChange.Type.MODIFIED || dc.getType() == DocumentChange.Type.ADDED) {
						try {
							Glide.with(this.context).load(dc.getDocument().getString("imageURL")).into(this.profilePicture);
						} catch (IllegalArgumentException err) {
							err.printStackTrace();
						}
					}
				}
			});

			usernameTextView.setText(comment.getUsername());

			String[] date = comment.getTimeCreated().split("/");
			// Take hours and minutes from timeSent
			String[] hoursAndMinutes = date[3].split(":");
			int hours = Integer.parseInt(hoursAndMinutes[0]);
			int minutes = Integer.parseInt(hoursAndMinutes[1]);
			String period;
			// Convert time from 24-hour clock to 12-hour clock
			if (hours == 0) {
				hours = 12;
				period = "am";
			} else if (hours < 12) {
				period = "am";
			} else if (hours == 12) {
				period = "pm";
			} else {
				// Convert time in 12 hour clock
				hours = hours % 12;
				period = "pm";
			}

			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("Asia/Karachi"));
			int currentDate = calendar.get(Calendar.DATE);
			int currentMonth = calendar.get(Calendar.MONTH) + 1;
			int currentYear = calendar.get(Calendar.YEAR);

			String time;
			if(currentDate > Integer.parseInt(date[0]) && currentMonth >= Integer.parseInt(date[1]) && currentYear > Integer.parseInt(date[2])) {
				// 1:02 pm mon 2/12/19
				time = Helpers.addZero(hours) + ":" + Helpers.addZero(minutes) + " " + period + " " + date[4] + " " + date[0] + "/" + date[1] + "/" + date[2];
			} else if(currentDate > Integer.parseInt(date[0]) && currentMonth >= Integer.parseInt(date[1])) {
				// 1:02 pm mon 2/12
				time = Helpers.addZero(hours) + ":" + Helpers.addZero(minutes) + " " + period + " " + date[4] + " " + date[0] + "/" + date[1];
			} else {
				// 1:02 pm
				time = Helpers.addZero(hours) + ":" + Helpers.addZero(minutes) + " " + period;
			}
			timeCreatedTextView.setText(time);
			commentTextView.setText(comment.getDescription());
			upvoteImageView.setImageResource(comment.getUpvoteImage());
			downvoteImageView.setImageResource(comment.getDownvoteImage());

			upvoteCountTextView.setText(String.valueOf(comment.getNumberOfUpvotes()));
			downvoteCountTextView.setText(String.valueOf(comment.getNumberOfDownvotes()));
			replyCountTextView.setText(String.valueOf(comment.getNumberOfReplies()));

			// Check if the comment is a reply to some other comment
			if(!comment.getParent().isEmpty()) {
				replyButton.setVisibility(View.GONE);
			}
		}
	}
}
