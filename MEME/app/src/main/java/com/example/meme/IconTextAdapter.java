package com.example.meme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import java.util.ArrayList;

public class IconTextAdapter extends Adapter<IconTextAdapter.IconTextViewHolder> {

	private RecycleViewerClickListener recycleViewerClickListener;
	private ArrayList<IconTextListItem> iconTextListItems;
	private Context context;

	IconTextAdapter(Context context, ArrayList<IconTextListItem> iconTextListItems, RecycleViewerClickListener recycleViewerClickListener) {
		this.context = context;
		this.iconTextListItems = iconTextListItems;
		this.recycleViewerClickListener = recycleViewerClickListener;
	}

	@NonNull
	@Override
	public IconTextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(this.context);
		View listItemView = inflater.inflate(R.layout.icon_text_list_item, parent, false);
		return new IconTextViewHolder(listItemView, this.recycleViewerClickListener);
	}

	@Override
	public void onBindViewHolder(@NonNull IconTextViewHolder holder, int position) {
		IconTextListItem iconTextListItem = iconTextListItems.get(position);
		holder.setIconTextItem(iconTextListItem);
	}

	@Override
	public int getItemCount() {
		return this.iconTextListItems.size();
	}

	class IconTextViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

		private ImageView iconImageView;
		private TextView descriptionTextView;
		private TextView detailTextView;
		private ImageView arrowRightIcon;

		private RecycleViewerClickListener recycleViewerClickListener;

		IconTextViewHolder(@NonNull View itemView, RecycleViewerClickListener recycleViewerClickListener) {
			super(itemView);
			this.iconImageView = itemView.findViewById(R.id.icon_image_view);
			this.descriptionTextView = itemView.findViewById(R.id.description_text_view);
			this.detailTextView = itemView.findViewById(R.id.detail_text_view);
			this.arrowRightIcon = itemView.findViewById(R.id.arrow_right_icon);

			itemView.setOnClickListener(this);
			this.recycleViewerClickListener = recycleViewerClickListener;
		}

		void setIconTextItem(IconTextListItem iconTextItem) {
			this.iconImageView.setImageResource(iconTextItem.getImage());
			this.descriptionTextView.setText(iconTextItem.getDescription());
			this.descriptionTextView.setTextColor(iconTextItem.getDescriptionColor());

			if(!iconTextItem.getDetail().isEmpty()) {
				detailTextView.setVisibility(View.VISIBLE);
				detailTextView.setText(iconTextItem.getDetail());
				arrowRightIcon.setVisibility(View.GONE);
			} else {
				arrowRightIcon.setVisibility(View.VISIBLE);
				detailTextView.setVisibility(View.GONE);
			}
		}

		@Override
		public void onClick(View v) {
			recycleViewerClickListener.onClick(v, getAdapterPosition());
		}
	}
}
