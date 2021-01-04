package com.example.meme;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.meme.ui.main.HomeSectionsPageAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import ly.img.android.pesdk.ui.utils.PermissionRequest;

public class HomeActivity extends AppCompatActivity {

	private BottomSheet bottomSheet;

	private FirebaseAuth auth;
	private FirebaseFirestore db;
	private User user;
	private Bundle dataBundle = null;

	private RelativeLayout smallProfilePictureRelativeLayout;
	private Toolbar toolbar;
	private RelativeLayout onlineStatus;
	private CircleImageView smallProfilePictureImageView;
	private FloatingActionButton addPostButton;
	private ImageView refreshButton;
	private ImageView searchButton;

	@Override
	public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NonNull int[] grantResults) {
		PermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		ActivityCompat.requestPermissions(HomeActivity.this,
				new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
				1);
		ActivityCompat.requestPermissions(HomeActivity.this,
				new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
				2);
		ActivityCompat.requestPermissions(HomeActivity.this,
				new String[]{Manifest.permission.CAMERA},
				3);

		loadPostsUsingTag();

		user = new User();

		// Initialize firebase
		auth = FirebaseAuth.getInstance();
		db = FirebaseFirestore.getInstance();

		// Initialize views
		toolbar = findViewById(R.id.toolbar);
		smallProfilePictureRelativeLayout = findViewById(R.id.small_profile_picture_relative_layout);
		onlineStatus = findViewById(R.id.online_status);
		smallProfilePictureImageView = findViewById(R.id.small_profile_picture_image_view);
		addPostButton = findViewById(R.id.add_post_button);
		refreshButton = findViewById(R.id.refresh_button);
		searchButton = findViewById(R.id.search_button);

		// Set up toolbar
		setSupportActionBar(toolbar);
		// Remove the title from the toolbar
		Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

		// Set up tabs
		HomeSectionsPageAdapter homeSectionsPageAdapter = new HomeSectionsPageAdapter(this, getSupportFragmentManager());
		ViewPager viewPager = findViewById(R.id.view_pager);
		viewPager.setAdapter(homeSectionsPageAdapter);
		TabLayout tabs = findViewById(R.id.tabs);
		tabs.setupWithViewPager(viewPager);

		// Create bottom sheet
		setBottomSheet();

		// Setup listeners
		setupListeners();

		// Create navigation drawer
		createNavigationDrawer();

		// Setup realtime listener
		setupRealTimeListeners();
	}

	private void loadPostsUsingTag() {
		if(getIntent().getExtras() != null) {
			dataBundle = getIntent().getExtras();
			setTabs();
		}
	}

	private void setTabs() {
		// Set up tabs
		HomeSectionsPageAdapter homeSectionsPageAdapter = new HomeSectionsPageAdapter(this, getSupportFragmentManager());
		ViewPager viewPager = findViewById(R.id.view_pager);
		viewPager.setAdapter(homeSectionsPageAdapter);
		TabLayout tabs = findViewById(R.id.tabs);
		tabs.setupWithViewPager(viewPager);
	}

	private void setupRealTimeListeners() {
		setProfilePicture();
	}

	private void setProfilePicture() {
		db.collection("users")
				.whereEqualTo("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail())
				.addSnapshotListener((queryDocumentSnapshots, e) -> {
					if (e != null) {
						Log.i("FirebaseFirestoreException", "" + e.getMessage());
						return;
					}
					assert queryDocumentSnapshots != null;
					for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
						// Checks if the document is initially added or is modified
						if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {

							// Get email and online status
							user.setEmail(dc.getDocument().getId());
							user.setOnlineStatus(Integer.parseInt(Objects.requireNonNull(dc.getDocument().getData().get("onlineStatus")).toString()));

							if (user.getOnlineStatus() == 0) {
								onlineStatus.setVisibility(View.GONE);
							} else {
								onlineStatus.setVisibility(View.VISIBLE);
							}

							// Change the profile picture
							// Get image URL
							user.setImageURL(Objects.requireNonNull(dc.getDocument().getData().get("imageURL")).toString());

							try{
								// Load profile picture
								Glide.with(HomeActivity.this).load(user.getImageURL()).into(smallProfilePictureImageView);
							} catch (IllegalArgumentException err) {
								err.printStackTrace();
							}
						}
					}
				});
	}

	private void setupListeners() {
		smallProfilePictureRelativeLayout.setOnClickListener(v -> {
			// Open bottom sheet
			this.bottomSheet.showBottomSheet();
		});

		searchButton.setOnClickListener(v -> {
			// Move to search activity
			Intent searchIntent = new Intent(HomeActivity.this, SearchActivity.class);
			startActivity(searchIntent);
		});

		addPostButton.setOnClickListener(v -> {
			// Move to create post activity
			Intent createPostIntent = new Intent(HomeActivity.this, CreatePostActivity.class);
			startActivity(createPostIntent);
		});

		refreshButton.setOnClickListener(v -> setTabs());
	}

	private void setBottomSheet() {
		// Create new Array list for bottom sheet items
		ArrayList<BottomSheetListItem> bottomSheetListItemArrayList = new ArrayList<>();

		// Initialize items in bottom sheet
		bottomSheetListItemArrayList.add(
				new BottomSheetListItem(R.drawable.ic_description_white_24dp, "Profile")
		);
		bottomSheetListItemArrayList.add(
				new BottomSheetListItem(R.drawable.ic_speaker_notes_black_24dp, "Messages")
		);
		bottomSheetListItemArrayList.add(
				new BottomSheetListItem(R.drawable.ic_tag_faces_white_24dp, "Meme generator")
		);
		bottomSheetListItemArrayList.add(
				new BottomSheetListItem(R.drawable.ic_power_settings_new_black_24dp, "Logout")
		);

		// Set up listener for Bottom sheet list items
		RecycleViewerClickListener recycleViewerClickListener = (v, position) -> {
			if (position == 0) {
				// Send current user's email to load profile information of the current user
				Bundle dataBundle = new Bundle();
				dataBundle.putString("email", Objects.requireNonNull(auth.getCurrentUser()).getEmail());
				Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
				profileIntent.putExtras(dataBundle);
				startActivity(profileIntent);
			} else if (position == 1) {
				// Move to conversation activity
				Intent conversationIntent = new Intent(HomeActivity.this, ConversationActivity.class);
				startActivity(conversationIntent);
			}
			else if (position == 2) {
				Intent memeGeneratorIntent = new Intent(HomeActivity.this, MemeGeneratorActivity.class);
				startActivity(memeGeneratorIntent);
			} else if (position == 3) {
				// Logout
				auth.signOut();
				// Move to login activity
				Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
				startActivity(loginIntent);
			}
			// Close bottom sheet when an item is pressed
			this.bottomSheet.hideBottomSheet();
		};

		this.bottomSheet = new BottomSheet(HomeActivity.this, bottomSheetListItemArrayList, recycleViewerClickListener);
	}

	private void createNavigationDrawer() {
		PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Home");
		SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName("Animals");
		SecondaryDrawerItem item4 = new SecondaryDrawerItem().withIdentifier(3).withName("Anime & Manga");
		SecondaryDrawerItem item5 = new SecondaryDrawerItem().withIdentifier(4).withName("Apex Legend");
		SecondaryDrawerItem item6 = new SecondaryDrawerItem().withIdentifier(5).withName("Awesome");
		SecondaryDrawerItem item7 = new SecondaryDrawerItem().withIdentifier(6).withName("Car");
		SecondaryDrawerItem item8 = new SecondaryDrawerItem().withIdentifier(7).withName("Comic");
		SecondaryDrawerItem item9 = new SecondaryDrawerItem().withIdentifier(8).withName("Cosplay");
		SecondaryDrawerItem item10 = new SecondaryDrawerItem().withIdentifier(9).withName("Crappy Design");
		SecondaryDrawerItem item11 = new SecondaryDrawerItem().withIdentifier(10).withName("Dark humour");
		SecondaryDrawerItem item12 = new SecondaryDrawerItem().withIdentifier(11).withName("Drawing, DIY & Crafts");
		SecondaryDrawerItem item13 = new SecondaryDrawerItem().withIdentifier(12).withName("Food & Drinks");
		SecondaryDrawerItem item14 = new SecondaryDrawerItem().withIdentifier(13).withName("Football");
		SecondaryDrawerItem item15 = new SecondaryDrawerItem().withIdentifier(14).withName("Fortnite");
		SecondaryDrawerItem item16 = new SecondaryDrawerItem().withIdentifier(15).withName("Funny");
		SecondaryDrawerItem item17 = new SecondaryDrawerItem().withIdentifier(16).withName("Game of Thrones");
		SecondaryDrawerItem item18 = new SecondaryDrawerItem().withIdentifier(17).withName("Gaming");
		SecondaryDrawerItem item20 = new SecondaryDrawerItem().withIdentifier(19).withName("Girl");
		SecondaryDrawerItem item21 = new SecondaryDrawerItem().withIdentifier(20).withName("Girl Celebrity");
		SecondaryDrawerItem item22 = new SecondaryDrawerItem().withIdentifier(21).withName("Guy");
		SecondaryDrawerItem item23 = new SecondaryDrawerItem().withIdentifier(22).withName("History");
		SecondaryDrawerItem item24 = new SecondaryDrawerItem().withIdentifier(23).withName("Horror");
		SecondaryDrawerItem item25 = new SecondaryDrawerItem().withIdentifier(24).withName("K-pop");
		SecondaryDrawerItem item26 = new SecondaryDrawerItem().withIdentifier(25).withName("Latest News");
		SecondaryDrawerItem item27 = new SecondaryDrawerItem().withIdentifier(26).withName("League of Legends");
		SecondaryDrawerItem item28 = new SecondaryDrawerItem().withIdentifier(27).withName("LEGO");
		SecondaryDrawerItem item29 = new SecondaryDrawerItem().withIdentifier(28).withName("Marvel & DC");
		SecondaryDrawerItem item30 = new SecondaryDrawerItem().withIdentifier(29).withName("Meme");
		SecondaryDrawerItem item31 = new SecondaryDrawerItem().withIdentifier(30).withName("Music");
		SecondaryDrawerItem item32 = new SecondaryDrawerItem().withIdentifier(31).withName("NBA");
		SecondaryDrawerItem item33 = new SecondaryDrawerItem().withIdentifier(32).withName("NSFW");
		SecondaryDrawerItem item34 = new SecondaryDrawerItem().withIdentifier(33).withName("Overwatch");
		SecondaryDrawerItem item35 = new SecondaryDrawerItem().withIdentifier(34).withName("Pokemon");
		SecondaryDrawerItem item36 = new SecondaryDrawerItem().withIdentifier(35).withName("PUBG");
		SecondaryDrawerItem item37 = new SecondaryDrawerItem().withIdentifier(36).withName("Relationship");
		SecondaryDrawerItem item38 = new SecondaryDrawerItem().withIdentifier(37).withName("Savage");
		SecondaryDrawerItem item39 = new SecondaryDrawerItem().withIdentifier(38).withName("Satisfying");
		SecondaryDrawerItem item40 = new SecondaryDrawerItem().withIdentifier(39).withName("Science & Tech");
		SecondaryDrawerItem item41 = new SecondaryDrawerItem().withIdentifier(40).withName("Sport");
		SecondaryDrawerItem item42 = new SecondaryDrawerItem().withIdentifier(41).withName("Star Wars");
		SecondaryDrawerItem item43 = new SecondaryDrawerItem().withIdentifier(42).withName("Teens Can Relate");
		SecondaryDrawerItem item44 = new SecondaryDrawerItem().withIdentifier(43).withName("Today I Wore");
		SecondaryDrawerItem item45 = new SecondaryDrawerItem().withIdentifier(44).withName("Travel & Photography");
		SecondaryDrawerItem item46 = new SecondaryDrawerItem().withIdentifier(45).withName("Wallpaper");
		SecondaryDrawerItem item47 = new SecondaryDrawerItem().withIdentifier(46).withName("Warhammer");
		SecondaryDrawerItem item48 = new SecondaryDrawerItem().withIdentifier(47).withName("Wholesome");
		SecondaryDrawerItem item49 = new SecondaryDrawerItem().withIdentifier(48).withName("WTF");

		Drawer result = new DrawerBuilder()
				.withActivity(this)
				.withToolbar(toolbar)
				.addDrawerItems(
						item1, item2, item4, item5, item6, item7, item8, item9, item10, item11, item12, item13, item14, item15, item16, item17, item18,
						item20, item21, item22, item23, item24, item25, item26, item27, item28, item29, item30, item31, item32, item33, item34, item35, item36,
						item37, item38, item39, item40, item41, item42, item43, item44, item45, item46, item47, item48, item49
				).withOnDrawerItemClickListener((view, position, drawerItem) -> {
					dataBundle  = new Bundle();
					dataBundle.putInt("option", 1);
					switch (position){
						case 0:
							dataBundle = null;
							break;
						case 1:
							dataBundle.putString("category", "Animals");
							break;
						case 2:
							dataBundle.putString("category", "Anime & Manga");
							break;
						case 3:
							dataBundle.putString("category", "Apex Legend");
							break;
						case 4:
							dataBundle.putString("category", "Awesome");
							break;
						case 5:
							dataBundle.putString("category", "Car");
							break;
						case 6:
							dataBundle.putString("category", "Comic");
							break;
						case 7:
							dataBundle.putString("category", "Cosplay");
							break;
						case 8:
							dataBundle.putString("category", "Crappy Design");
							break;
						case 9:
							dataBundle.putString("category", "Dark humour");
							break;
						case 10:
							dataBundle.putString("category", "Drawing, DIY & Crafts");
							break;
						case 11:
							dataBundle.putString("category", "Food & Drinks");
							break;
						case 12:
							dataBundle.putString("category", "Football");
							break;
						case 13:
							dataBundle.putString("category", "Fortnite");
							break;
						case 14:
							dataBundle.putString("category", "Funny");
							break;
						case 15:
							dataBundle.putString("category", "Game of Thrones");
							break;
						case 16:
							dataBundle.putString("category", "Gaming");
							break;
						case 17:
							dataBundle.putString("category", "Girl");
							break;
						case 18:
							dataBundle.putString("category", "Girl Celebrity");
							break;
						case 19:
							dataBundle.putString("category", "Guy");
							break;
						case 20:
							dataBundle.putString("category", "History");
							break;
						case 21:
							dataBundle.putString("category", "Horror");
							break;
						case 22:
							dataBundle.putString("category", "K-pop");
							break;
						case 23:
							dataBundle.putString("category", "Latest News");
							break;
						case 24:
							dataBundle.putString("category", "League of Legends");
							break;
						case 25:
							dataBundle.putString("category", "LEGO");
							break;
						case 26:
							dataBundle.putString("category", "Marvel & DC");
							break;
						case 27:
							dataBundle.putString("category", "Meme");
							break;
						case 28:
							dataBundle.putString("category", "Music");
							break;
						case 29:
							dataBundle.putString("category", "NBA");
							break;
						case 30:
							dataBundle.putString("category", "NSFW");
							break;
						case 31:
							dataBundle.putString("category", "Overwatch");
							break;
						case 32:
							dataBundle.putString("category", "Pokemon");
							break;
						case 33:
							dataBundle.putString("category", "PUBG");
							break;
						case 34:
							dataBundle.putString("category", "Relationship");
							break;
						case 35:
							dataBundle.putString("category", "Savage");
							break;
						case 36:
							dataBundle.putString("category", "Satisfying");
							break;
						case 37:
							dataBundle.putString("category", "Science & Tech");
							break;
						case 38:
							dataBundle.putString("category", "Sport");
							break;
						case 39:
							dataBundle.putString("category", "Star Wars");
							break;
						case 40:
							dataBundle.putString("category", "Teens Can Relate");
							break;
						case 41:
							dataBundle.putString("category", "Today I Wore");
							break;
						case 42:
							dataBundle.putString("category", "Travel & Photography");
							break;
						case 43:
							dataBundle.putString("category", "Wallpaper");
							break;
						case 44:
							dataBundle.putString("category", "Warhammer");
							break;
						case 45:
							dataBundle.putString("category", "Wholesome");
							break;
						case 46:
							dataBundle.putString("category", "WTF");
							break;
					}
					// Refresh tabs
					setTabs();
					return false;
				}).build();

		// Hide the hamburger icon
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
		result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
	}

	public Bundle getDataBundle() {
		return dataBundle;
	}
}
