package com.example.meme.container;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meme.R;
import com.example.meme.listener.RecycleViewerClickListener;
import com.example.meme.adapter.BottomSheetRecyclerViewAdapter;
import com.example.meme.listitem.BottomSheetListItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class BottomSheet {
	private Context context;
	private BottomSheetDialog bottomSheetDialog;
	private ArrayList<BottomSheetListItem> bottomSheetListItemArrayList;
	private RecycleViewerClickListener recycleViewerClickListener;
	private BottomSheetRecyclerViewAdapter bottomSheetRecyclerViewAdapter;
	public BottomSheet(Context context, ArrayList<BottomSheetListItem> bottomSheetListItems, RecycleViewerClickListener recycleViewerClickListener) {
		this.context = context;
		this.bottomSheetListItemArrayList = bottomSheetListItems;
		this.recycleViewerClickListener = recycleViewerClickListener;
		this.bottomSheetDialog = null;
	}

	@SuppressLint("InflateParams")
	private void createBottomSheetDialog() {
		// Checks if bottom sheet dialog exists
		if (bottomSheetDialog == null) {
			// Inflate bottom sheet layout
			LayoutInflater inflater = LayoutInflater.from(this.context);
			View bottomSheetView = inflater.inflate(R.layout.primary_bottom_sheet, null, false);

			// Get recycler view from bottom sheet
			RecyclerView bottomSheetRecyclerView = bottomSheetView.findViewById(R.id.bottom_sheet_recycler_view);

			// Initialize adapter for bottom sheet list items
			bottomSheetRecyclerViewAdapter = new BottomSheetRecyclerViewAdapter(
					this.context,
					this.bottomSheetListItemArrayList,
					this.recycleViewerClickListener
			);

			// Set layout manager for recycler view
			bottomSheetRecyclerView.setLayoutManager(new LinearLayoutManager(this.context));

			// Set adapter for bottom sheet recycler viewer
			bottomSheetRecyclerView.setAdapter(bottomSheetRecyclerViewAdapter);

			bottomSheetDialog = new BottomSheetDialog(this.context, R.style.bottomSheetDialogStyle);
			bottomSheetDialog.setContentView(bottomSheetView);

			bottomSheetDialog.setOnDismissListener(dialog -> {
				bottomSheetDialog = null;
				bottomSheetRecyclerViewAdapter = null;
			});
		}
	}

	public void showBottomSheet() {
		createBottomSheetDialog();
		this.bottomSheetDialog.show();
	}

	public void hideBottomSheet() {
		this.bottomSheetDialog.cancel();
	}

	public void setBottomSheetListItemArrayList(ArrayList<BottomSheetListItem> bottomSheetListItemArrayList) {
		this.bottomSheetListItemArrayList = bottomSheetListItemArrayList;
	}
}
