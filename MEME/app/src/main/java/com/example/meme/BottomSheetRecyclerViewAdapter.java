package com.example.meme;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BottomSheetRecyclerViewAdapter extends RecyclerView.Adapter<BottomSheetRecyclerViewAdapter.BottomSheetRecyclerViewHolder> {

	private RecycleViewerClickListener recycleViewerClickListener;
	private ArrayList<BottomSheetListItem> bottomSheetListItems;
	private Context context;

	public BottomSheetRecyclerViewAdapter(Context context, ArrayList<BottomSheetListItem> bottomSheetListItems, RecycleViewerClickListener recycleViewerClickListener) {
		this.context = context;
		this.bottomSheetListItems = bottomSheetListItems;
		this.recycleViewerClickListener = recycleViewerClickListener;
	}

	@NonNull
	@Override
	public BottomSheetRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(this.context);
		View listItemView = inflater.inflate(R.layout.list_item_bottom_sheet, parent, false);
		return new BottomSheetRecyclerViewHolder(listItemView, this.recycleViewerClickListener);
	}

	@Override
	public void onBindViewHolder(@NonNull BottomSheetRecyclerViewHolder holder, int position) {
		BottomSheetListItem bottomSheetListItem = this.bottomSheetListItems.get(position);
		holder.setBottomSheetListItem(bottomSheetListItem);
	}

	@Override
	public int getItemCount() {
		return bottomSheetListItems.size();
	}

	class BottomSheetRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		ImageView iconImageView;
		TextView descriptionTextView;
		RecycleViewerClickListener recycleViewerClickListener;

		public BottomSheetRecyclerViewHolder(@NonNull View itemView, RecycleViewerClickListener recycleViewerClickListener) {
			super(itemView);
			this.iconImageView = itemView.findViewById(R.id.icon_image_view);
			this.descriptionTextView = itemView.findViewById(R.id.description_text_view);

			itemView.setOnClickListener(this);
			this.recycleViewerClickListener = recycleViewerClickListener;
		}

		void setBottomSheetListItem(BottomSheetListItem bottomSheetListItem) {
			iconImageView.setImageResource(bottomSheetListItem.getIcon());
			descriptionTextView.setText(bottomSheetListItem.getDescription());
		}

		@Override
		public void onClick(View v) {
			this.recycleViewerClickListener.onClick(v, getAdapterPosition());
		}
	}
}
