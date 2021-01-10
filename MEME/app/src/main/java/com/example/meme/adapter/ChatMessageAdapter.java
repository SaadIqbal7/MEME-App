package com.example.meme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meme.container.ChatMessage;
import com.example.meme.container.Helpers;
import com.example.meme.R;
import com.example.meme.listener.RecycleViewerLongClickListener;
import com.example.meme.container.TimeTag;
import com.example.meme.listitem.ChatItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private Context context;
	private ArrayList<ChatItem> chatItems;
	private RecycleViewerLongClickListener recycleViewerLongClickListener;

	public ChatMessageAdapter(Context context, ArrayList<ChatItem> chatItems, RecycleViewerLongClickListener recycleViewerLongClickListener) {
		this.context = context;
		this.chatItems = chatItems;
		this.recycleViewerLongClickListener = recycleViewerLongClickListener;
	}

	@NotNull
	@Override
	public RecyclerView.ViewHolder  onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(this.context);
		View chatItemView;
		switch (viewType) {
			case 0:
				chatItemView = inflater.inflate(R.layout.sender_message_item, parent, false);
				return new ChatMessageViewHolder(chatItemView, this.recycleViewerLongClickListener);
			case 1:
				chatItemView = inflater.inflate(R.layout.receiver_message_item, parent, false);
				return new ChatMessageViewHolder(chatItemView, this.recycleViewerLongClickListener);
			case 2:
				chatItemView = inflater.inflate(R.layout.chat_time_tag, parent, false);
				return new TimeTagViewHolder(chatItemView);
			default:
				return null;
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if(holder.getItemViewType() == 0 || holder.getItemViewType() == 1) {
			((ChatMessageViewHolder) holder).setChatMessage(chatItems.get(position).getMessage());
		} else {
			((TimeTagViewHolder) holder).setTimeTag(chatItems.get(position).getTimeTag());
		}
	}

	@Override
	public int getItemViewType(int position) {
		return chatItems.get(position).getViewType();
	}

	@Override
	public int getItemCount() {
		return chatItems.size();
	}

	class TimeTagViewHolder extends RecyclerView.ViewHolder {
		private TextView dayTextView;
		private TextView dateTextView;
		private TextView monthTextView;

		TimeTagViewHolder(@NonNull View itemView) {
			super(itemView);
			this.dayTextView = itemView.findViewById(R.id.day_text_view);
			this.dateTextView = itemView.findViewById(R.id.date_text_view);
			this.monthTextView = itemView.findViewById(R.id.month_text_view);
		}

		private void setTimeTag(TimeTag timeTag) {
			this.dayTextView.setText(timeTag.getDay());
			this.dateTextView.setText(String.valueOf(timeTag.getDate()));
			this.monthTextView.setText(String.valueOf(timeTag.getMonth()));
		}
	}

	class ChatMessageViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
		private TextView messageTextView;
		private TextView timeSentTextView;
		private RecycleViewerLongClickListener recycleViewerLongClickListener;

		ChatMessageViewHolder(@NonNull View itemView, RecycleViewerLongClickListener recycleViewerLongClickListener) {
			super(itemView);

			this.messageTextView = itemView.findViewById(R.id.message_text_view);
			this.timeSentTextView = itemView.findViewById(R.id.time_sent_text_view);

			itemView.setOnLongClickListener(this);
			this.recycleViewerLongClickListener = recycleViewerLongClickListener;
		}

		void setChatMessage(ChatMessage chatMessage) {
			this.messageTextView.setText(chatMessage.getMessage());

			// Take hours and minutes from timeSent
			String[] hoursAndMinutes = chatMessage.getTimeSent().split("/")[3].split(":");
			int hours = Integer.parseInt(hoursAndMinutes[0]);
			int minutes = Integer.parseInt(hoursAndMinutes[1]);
			String period = "";
			// Convert time from 24-hour clock to 12-hour clock
			if (hours == 0) {
				hours = 12;
				period = "am";
			} else if (hours < 12) {
				period = "am";
			} else if (hours == 12) {
				period = "pm";
			} else if (hours > 12) {
				// Convert time in 12 hour clock
				hours = hours % 12;
				period = "pm";
			}

			String time = Helpers.addZero(hours) + ":" + Helpers.addZero(minutes) + " " + period;
			this.timeSentTextView.setText(time);
		}

		@Override
		public boolean onLongClick(View v) {
			this.recycleViewerLongClickListener.onLongClick(v, getAdapterPosition());
			return false;
		}
	}
}
