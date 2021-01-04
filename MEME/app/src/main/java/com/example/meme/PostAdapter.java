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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

	private Context context;
	private ArrayList<Post> posts;
	private OnBottomReachedListener onBottomReachedListener;
	private PostRecyclerViewOnClickListeners postRecyclerViewOnClickListeners;

	PostAdapter(Context context, ArrayList<Post> posts, OnBottomReachedListener onBottomReachedListener, PostRecyclerViewOnClickListeners postRecyclerViewOnClickListeners) {
		this.context = context;
		this.posts = posts;
		this.onBottomReachedListener = onBottomReachedListener;
		this.postRecyclerViewOnClickListeners = postRecyclerViewOnClickListeners;
	}

	@NonNull
	@Override
	public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(this.context);
		View postView = inflater.inflate(R.layout.post_layout, parent, false);
		return new PostViewHolder(this.context, postView, this.postRecyclerViewOnClickListeners);
	}

	@Override
	public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
		if(position == posts.size() - 1) {
			onBottomReachedListener.onBottomReached(position);
		}
		try {
			Post post = posts.get(position);
			holder.setPost(post);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getItemCount() {
		return posts.size();
	}

	class PostViewHolder extends RecyclerView.ViewHolder{
		private Context context;
		private PostRecyclerViewOnClickListeners postRecyclerViewOnClickListeners;
		private FirebaseFirestore db;


		private CircleImageView profilePicture;
		private TextView usernameTextView;
		private TextView timeCreatedTextView;
		private ImageView postOptionsButton;
		private TextView categoryTextView;
		private TextView postDescriptionTextView;
		private ImageView postImage;
		private TextView tag1TextView;
		private TextView tag2TextView;
		private TextView tag3TextView;
		private LinearLayout upvoteButton;
		private ImageView upvoteImageView;
		private TextView upvoteCountTextView;
		private LinearLayout downvoteButton;
		private ImageView downvoteImageView;
		private TextView downvoteCountTextView;
		private LinearLayout commentButton;
		private TextView commentCountTextView;
		private LinearLayout downloadButton;

		PostViewHolder(Context context, @NonNull View itemView, PostRecyclerViewOnClickListeners postRecyclerViewOnClickListeners) {
			super(itemView);
			this.context= context;
			this.postRecyclerViewOnClickListeners = postRecyclerViewOnClickListeners;
			this.db = FirebaseFirestore.getInstance();

			// Initialize views
			profilePicture = itemView.findViewById(R.id.profile_picture);
			usernameTextView = itemView.findViewById(R.id.username_text_view);
			timeCreatedTextView = itemView.findViewById(R.id.time_created_text_view);
			postOptionsButton = itemView.findViewById(R.id.post_options);
			categoryTextView = itemView.findViewById(R.id.category_text_view);
			postDescriptionTextView = itemView.findViewById(R.id.post_description_text_view);
			postImage = itemView.findViewById(R.id.post_image);
			tag1TextView = itemView.findViewById(R.id.tag1_text_view);
			tag2TextView = itemView.findViewById(R.id.tag2_text_view);
			tag3TextView = itemView.findViewById(R.id.tag3_text_view);
			upvoteButton = itemView.findViewById(R.id.upvotes_button);
			upvoteImageView = itemView.findViewById(R.id.upvotes_image_view);
			upvoteCountTextView = itemView.findViewById(R.id.upvote_count_text_view);
			downvoteButton = itemView.findViewById(R.id.downvotes_button);
			downvoteImageView = itemView.findViewById(R.id.downvotes_image_view);
			downvoteCountTextView = itemView.findViewById(R.id.downvote_count_text_view);
			commentButton = itemView.findViewById(R.id.comment_button);
			commentCountTextView = itemView.findViewById(R.id.comment_count_text_view);
			downloadButton = itemView.findViewById(R.id.download_button);

			setListeners();
		}

		private void setListeners() {
			upvoteButton.setOnClickListener(v -> this.postRecyclerViewOnClickListeners.upvotesButtonOnClickListener(v, getAdapterPosition()));
			downvoteButton.setOnClickListener(v -> this.postRecyclerViewOnClickListeners.downvotesButtonOnClickListener(v, getAdapterPosition()));
			commentButton.setOnClickListener(v -> this.postRecyclerViewOnClickListeners.commentButtonOnClickListener(v, getAdapterPosition()));
			downloadButton.setOnClickListener(v -> this.postRecyclerViewOnClickListeners.downloadButtonOnClickListener(v, getAdapterPosition()));
			postOptionsButton.setOnClickListener(v -> this.postRecyclerViewOnClickListeners.postOptionsButtonOnClickListener(v, getAdapterPosition()));
			usernameTextView.setOnClickListener(v -> this.postRecyclerViewOnClickListeners.usernameOnClickListener(v, getAdapterPosition()));
			tag1TextView.setOnClickListener(v -> this.postRecyclerViewOnClickListeners.tag1OnClickListener(v, getAdapterPosition()));
			tag2TextView.setOnClickListener(v -> this.postRecyclerViewOnClickListeners.tag2OnClickListener(v, getAdapterPosition()));
			tag3TextView.setOnClickListener(v -> this.postRecyclerViewOnClickListeners.tag3OnClickListener(v, getAdapterPosition()));
		}

		void setPost(Post post) {

			db.collection("users").whereEqualTo("email", post.getEmail()).addSnapshotListener((queryDocumentSnapshots, e) -> {
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

			try {
				Glide.with(this.context).load(post.getImageURL()).into(this.postImage);
			} catch(IllegalArgumentException e) {
				e.printStackTrace();
			}
			usernameTextView.setText(post.getUsername());

			String[] date = post.getTimeCreated().split("/");
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

			categoryTextView.setText(post.getCategory());
			postDescriptionTextView.setText(post.getDescription());

			// Hide all tags
			tag1TextView.setVisibility(View.GONE);
			tag2TextView.setVisibility(View.GONE);
			tag3TextView.setVisibility(View.GONE);
			int postTagsSize = post.getTags().size();
			if(postTagsSize >= 1) {
				tag1TextView.setText(post.getTags().get(0));
				tag1TextView.setVisibility(View.VISIBLE);
			}
			if(postTagsSize >= 2) {
				tag2TextView.setText(post.getTags().get(1));
				tag2TextView.setVisibility(View.VISIBLE);
			}
			if(postTagsSize == 3) {
				tag3TextView.setText(post.getTags().get(2));
				tag3TextView.setVisibility(View.VISIBLE);
			}

			upvoteImageView.setImageResource(post.getUpvoteImage());
			downvoteImageView.setImageResource(post.getDownvoteImage());

			upvoteCountTextView.setText(String.valueOf(post.getNumberOfUpVotes()));
			downvoteCountTextView.setText(String.valueOf(post.getNumberOfDownVotes()));
			commentCountTextView.setText(String.valueOf(post.getNumberOfComments()));
		}
	}
}
