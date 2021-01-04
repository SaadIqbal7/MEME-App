package com.example.meme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ly.img.android.pesdk.assets.filter.basic.FilterPackBasic;
import ly.img.android.pesdk.assets.font.basic.FontPackBasic;
import ly.img.android.pesdk.assets.frame.basic.FramePackBasic;
import ly.img.android.pesdk.assets.overlay.basic.OverlayPackBasic;
import ly.img.android.pesdk.assets.sticker.emoticons.StickerPackEmoticons;
import ly.img.android.pesdk.assets.sticker.shapes.StickerPackShapes;
import ly.img.android.pesdk.backend.model.constant.Directory;
import ly.img.android.pesdk.backend.model.state.CameraSettings;
import ly.img.android.pesdk.backend.model.state.EditorLoadSettings;
import ly.img.android.pesdk.backend.model.state.EditorSaveSettings;
import ly.img.android.pesdk.backend.model.state.manager.SettingsList;
import ly.img.android.pesdk.ui.activity.CameraPreviewBuilder;
import ly.img.android.pesdk.ui.activity.ImgLyIntent;
import ly.img.android.pesdk.ui.activity.PhotoEditorBuilder;
import ly.img.android.pesdk.ui.model.state.UiConfigFilter;
import ly.img.android.pesdk.ui.model.state.UiConfigFrame;
import ly.img.android.pesdk.ui.model.state.UiConfigOverlay;
import ly.img.android.pesdk.ui.model.state.UiConfigSticker;
import ly.img.android.pesdk.ui.model.state.UiConfigText;

public class MemeGeneratorActivity extends AppCompatActivity {

	final int EDITOR_RESULT = 1;
	final int GALLERY_RESULT = 2;

	private ArrayList<MemeTemplate> templates;
	private ArrayList<MemeTemplate> tempTemplateList;
	private MemeTemplateAdapter memeTemplateAdapter;

	private RecyclerView memeTemplatesRecyclerView;
	private EditText searchBar;
	private ImageView backButton;
	private ProgressBar progressBar;
	private FloatingActionButton pictureOptionsButton;
	private BottomSheet bottomSheet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meme_generator);

		// Initially hide the keyboard when the activity starts
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		initializeVariables();
		setListeners();
		setBottomSheet();
		setAdapter();
		getTemplates();
	}

	private void initializeVariables() {
		memeTemplatesRecyclerView = findViewById(R.id.meme_templates_recycle_viewer);
		searchBar = findViewById(R.id.search_bar);
		backButton = findViewById(R.id.back_button);
		progressBar = findViewById(R.id.progress_bar);
		pictureOptionsButton = findViewById(R.id.picture_options_button);

		templates = new ArrayList<>();
		tempTemplateList = new ArrayList<>();
	}

	private void setListeners() {
		pictureOptionsButton.setOnClickListener(v -> bottomSheet.showBottomSheet());
		backButton.setOnClickListener(v -> MemeGeneratorActivity.this.finish());

		// On type listener for searching conversation
		searchBar.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String filterPattern = s.toString().toLowerCase().trim();
				searchTemplate(filterPattern);
			}
		});
	}

	private void setBottomSheet() {
		// Create new Array list for bottom sheet items
		ArrayList<BottomSheetListItem> bottomSheetListItemArrayList = new ArrayList<>();

		// Initialize items in bottom sheet
		bottomSheetListItemArrayList.add(
				new BottomSheetListItem(R.drawable.ic_image_white_24dp, "Open gallery")
		);
		bottomSheetListItemArrayList.add(
				new BottomSheetListItem(R.drawable.ic_camera_alt_white_24dp, "Open camera")
		);

		// Set up listener for Bottom sheet list items
		RecycleViewerClickListener recycleViewerClickListener = (v, position) -> {
			if (position == 0) {
				openGallery();
			} else if (position == 1) {
				openCamera();
			}
			// Close bottom sheet when an item is pressed
			this.bottomSheet.hideBottomSheet();
		};

		this.bottomSheet = new BottomSheet(MemeGeneratorActivity.this, bottomSheetListItemArrayList, recycleViewerClickListener);
	}

	private void setAdapter() {
		RecycleViewerClickListener recycleViewerClickListener = (view, position) -> editImage(templates.get(position).getImageURL());

		memeTemplateAdapter = new MemeTemplateAdapter(MemeGeneratorActivity.this, templates, recycleViewerClickListener);
		memeTemplatesRecyclerView.setLayoutManager(new LinearLayoutManager(MemeGeneratorActivity.this));
		memeTemplatesRecyclerView.setAdapter(memeTemplateAdapter);
	}

	private void searchTemplate(String filterPattern) {
		templates.clear();
		if (filterPattern.isEmpty()) {
			templates.addAll(tempTemplateList);
		} else {
			for (MemeTemplate template : tempTemplateList) {
				String name = template.getName().toLowerCase().trim();
				if (name.startsWith(filterPattern)) {
					templates.add(template);
				}
			}
		}
		memeTemplateAdapter.notifyDataSetChanged();
	}

	private void getTemplates() {
		RequestQueue requestQueue = Volley.newRequestQueue(MemeGeneratorActivity.this);
		String templatesURL = "https://us-central1-meme-project-0.cloudfunctions.net/webApi/api/templates";
		progressBar.setVisibility(View.VISIBLE);
		StringRequest stringRequest = new StringRequest(Request.Method.GET, templatesURL, response -> {
			progressBar.setVisibility(View.GONE);
			try {
				JSONArray templates = new JSONArray(response);
				displayTemplates(templates);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}, error -> {
			progressBar.setVisibility(View.GONE);
			Log.i("GetTemplatesError", "getTemplates: " + error.getMessage());
		});
		requestQueue.add(stringRequest);
	}

	private void displayTemplates(JSONArray templatesInfo) {
		try {
			for(int i = 0; i < templatesInfo.length(); i++) {
				JSONObject  templateInfo = templatesInfo.getJSONObject(i);
				MemeTemplate template  =  new MemeTemplate();
				template.setImageURL(templateInfo.getString("imageURL"));
				template.setName(templateInfo.getString("name"));

				templates.add(template);
				tempTemplateList.add(template);
			}

			memeTemplateAdapter.notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void editImage(String imageURL) {
		Uri uri = Uri.parse(imageURL);
		openEditor(uri);
	}

	private void openGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(intent, GALLERY_RESULT);
		} else {
			Toast.makeText(
					this,
					"No Gallery APP installed",
					Toast.LENGTH_LONG
			).show();
		}
	}

	private void openCamera() {
		SettingsList settingsList = createSettingsList();

		new CameraPreviewBuilder(this)
				.setSettingsList(settingsList)
				.startActivityForResult(this, EDITOR_RESULT);
	}

	private void openEditor(Uri inputImage) {
		SettingsList settingsList = createSettingsList();

		// Set input image
		settingsList.getSettingsModel(EditorLoadSettings.class)
				.setImageSource(inputImage);

		new PhotoEditorBuilder(this)
				.setSettingsList(settingsList)
				.startActivityForResult(this, EDITOR_RESULT);
	}

	private SettingsList createSettingsList() {
		SettingsList settingsList = new SettingsList();

		settingsList.getSettingsModel(UiConfigFilter.class).setFilterList(
				FilterPackBasic.getFilterPack()
		);

		settingsList.getSettingsModel(UiConfigText.class).setFontList(
				FontPackBasic.getFontPack()
		);

		settingsList.getSettingsModel(UiConfigFrame.class).setFrameList(
				FramePackBasic.getFramePack()
		);

		settingsList.getSettingsModel(UiConfigOverlay.class).setOverlayList(
				OverlayPackBasic.getOverlayPack()
		);

		settingsList.getSettingsModel(UiConfigSticker.class).setStickerLists(
				StickerPackEmoticons.getStickerCategory(),
				StickerPackShapes.getStickerCategory()
		);

		// Set custom camera image export settings
		settingsList.getSettingsModel(CameraSettings.class)
				.setExportDir(Directory.DCIM, "MEME")
				.setExportPrefix("camera_");

		// Set custom editor image export settings
		settingsList.getSettingsModel(EditorSaveSettings.class)
				.setExportDir(Directory.DCIM, "MEME")
				.setExportPrefix("result_")
				.setSavePolicy(EditorSaveSettings.SavePolicy.RETURN_ALWAYS_ONLY_OUTPUT);

		return settingsList;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && requestCode == EDITOR_RESULT) {
			// Editor has saved an Image.
			assert data != null;
			Uri resultURI = data.getParcelableExtra(ImgLyIntent.RESULT_IMAGE_URI);
			Uri sourceURI = data.getParcelableExtra(ImgLyIntent.SOURCE_IMAGE_URI);

			if (resultURI != null) {
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(resultURI));
			}

			if (sourceURI != null) {
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(sourceURI));
			}
		} else if (resultCode == RESULT_OK && requestCode == GALLERY_RESULT) {
			assert data != null;
			Uri selectedImage = data.getData();
			openEditor(selectedImage);
		}
	}
}
