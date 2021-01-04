package com.example.meme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MemeTemplateAdapter extends RecyclerView.Adapter<MemeTemplateAdapter.MemeTemplateViewHolder> {
	private Context context;
	private ArrayList<MemeTemplate> templates;
	private RecycleViewerClickListener recycleViewerClickListener;

	MemeTemplateAdapter(Context context, ArrayList<MemeTemplate> templates, RecycleViewerClickListener recycleViewerClickListener) {
		this.context = context;
		this.templates = templates;
		this.recycleViewerClickListener = recycleViewerClickListener;
	}

	@NonNull
	@Override
	public MemeTemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(this.context);
		View templateView = inflater.inflate(R.layout.meme_template_layout, parent, false);
		return new MemeTemplateViewHolder(this.context, templateView, this.recycleViewerClickListener);
	}

	@Override
	public void onBindViewHolder(@NonNull MemeTemplateViewHolder holder, int position) {
		MemeTemplate memeTemplate = templates.get(position);
		holder.setTemplate(memeTemplate);
	}

	@Override
	public int getItemCount() {
		return templates.size();
	}

	class MemeTemplateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private RecycleViewerClickListener recycleViewerClickListener;
		private Context context;

		private TextView templateNameTextView;
		private ImageView templateImage;

		public MemeTemplateViewHolder(Context context, @NonNull View itemView, RecycleViewerClickListener recycleViewerClickListener) {
			super(itemView);
			this.templateNameTextView = itemView.findViewById(R.id.template_name_text_view);
			this.templateImage = itemView.findViewById(R.id.template_image);

			this.context = context;
			this.recycleViewerClickListener = recycleViewerClickListener;
			itemView.setOnClickListener(this);
		}

		private void setTemplate(MemeTemplate template) {
			try {
				Glide.with(this.context).load(template.getImageURL()).into(templateImage);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}

			this.templateNameTextView.setText(template.getName());
		}

		@Override
		public void onClick(View v) {
			recycleViewerClickListener.onClick(v, getAdapterPosition());
		}
	}
}
