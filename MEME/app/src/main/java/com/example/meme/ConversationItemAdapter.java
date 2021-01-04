package com.example.meme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationItemAdapter  extends RecyclerView.Adapter<ConversationItemAdapter.ConversationItemViewHolder> {

	private Context context;
	private ArrayList<ConversationItem> conversations;
	private RecycleViewerClickListener recycleViewerClickListener;
	private RecycleViewerLongClickListener recycleViewerLongClickListener;

	public ConversationItemAdapter(Context context, ArrayList<ConversationItem> conversations, RecycleViewerClickListener recycleViewerClickListener, RecycleViewerLongClickListener recycleViewerLongClickListener) {
		this.context = context;
		this.conversations = conversations;
		this.recycleViewerClickListener = recycleViewerClickListener;
		this.recycleViewerLongClickListener = recycleViewerLongClickListener;
	}

	@NonNull
	@Override
	public ConversationItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(this.context);
		View conversationItemView = inflater.inflate(R.layout.conversation_item, parent, false);
		return new ConversationItemViewHolder(this.context, conversationItemView, recycleViewerClickListener, recycleViewerLongClickListener);
	}

	@Override
	public void onBindViewHolder(@NonNull ConversationItemViewHolder holder, int position) {
		ConversationItem conversation = this.conversations.get(position);
		holder.setConversation(conversation);
	}

	@Override
	public int getItemCount() {
		return conversations.size();
	}

	class ConversationItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
		private CircleImageView profilePicture;
		private TextView nameTextView;
		private TextView lastMessageTextView;
		private TextView lastMessageTimeTextView;

		private Context context;
		private RecycleViewerClickListener recycleViewerClickListener;
		private  RecycleViewerLongClickListener recycleViewerLongClickListener;

		ConversationItemViewHolder(Context context, @NonNull View itemView, RecycleViewerClickListener recycleViewerClickListener, RecycleViewerLongClickListener recycleViewerLongClickListener) {
			super(itemView);

			this.context = context;
			this.recycleViewerClickListener = recycleViewerClickListener;
			this.recycleViewerLongClickListener = recycleViewerLongClickListener;

			profilePicture = itemView.findViewById(R.id.profile_picture);
			nameTextView = itemView.findViewById(R.id.name_text_view);
			lastMessageTextView = itemView.findViewById(R.id.last_message_text_view);
			lastMessageTimeTextView = itemView.findViewById(R.id.last_message_time_text_view);

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		void setConversation(ConversationItem conversationItem) {
			Glide.with(this.context).load(conversationItem.getImageURL()).into(profilePicture);
			nameTextView.setText(conversationItem.getName());
			lastMessageTextView.setText(conversationItem.getLastMessage());
			lastMessageTimeTextView.setText(conversationItem.getLastMessageTime());
		}

		@Override
		public void onClick(View v) {
			this.recycleViewerClickListener.onClick(v, getAdapterPosition());
		}

		@Override
		public boolean onLongClick(View v) {
			recycleViewerLongClickListener.onLongClick(v, getAdapterPosition());
			return false;
		}
	}
}
