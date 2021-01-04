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

public class SearchItemAdapter extends RecyclerView.Adapter<SearchItemAdapter.SearchItemViewHolder> {

	private Context context;
	private ArrayList<SearchItem> searchItems;
	private RecycleViewerClickListener recycleViewerClickListener;

	public SearchItemAdapter(Context context, ArrayList<SearchItem> searchItems, RecycleViewerClickListener recycleViewerClickListener) {
		this.context = context;
		this.searchItems = searchItems;
		this.recycleViewerClickListener = recycleViewerClickListener;
	}

	@NonNull
	@Override
	public SearchItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(this.context);
		View searchItemView = inflater.inflate(R.layout.search_item_layout, parent, false);
		return new SearchItemViewHolder(this.context, searchItemView, this.recycleViewerClickListener);
	}

	@Override
	public void onBindViewHolder(@NonNull SearchItemViewHolder holder, int position) {
		SearchItem searchItem = searchItems.get(position);
		holder.setSearchItem(searchItem);
	}

	@Override
	public int getItemCount() {
		return searchItems.size();
	}

	class SearchItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		RecycleViewerClickListener recycleViewerClickListener;
		private Context context;
		private CircleImageView profilePicture;
		private TextView usernameTextView;
		private TextView nameTextView;

		public SearchItemViewHolder(Context context, @NonNull View itemView, RecycleViewerClickListener recycleViewerClickListener) {
			super(itemView);

			this.context = context;
			profilePicture = itemView.findViewById(R.id.profile_picture);
			usernameTextView = itemView.findViewById(R.id.username_text_view);
			nameTextView = itemView.findViewById(R.id.name_text_view);

			itemView.setOnClickListener(this);
			this.recycleViewerClickListener = recycleViewerClickListener;
		}

		void setSearchItem(SearchItem searchItem) {
			Glide.with(this.context).load(searchItem.getImageURL()).into(this.profilePicture);
			usernameTextView.setText(searchItem.getUsername());
			nameTextView.setText(searchItem.getName());
		}

		@Override
		public void onClick(View v) {
			recycleViewerClickListener.onClick(v, getAdapterPosition());
		}

	}
}
