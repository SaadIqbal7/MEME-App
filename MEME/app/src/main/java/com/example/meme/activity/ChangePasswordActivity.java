package com.example.meme.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meme.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {

	private FirebaseAuth auth;
	private AlertDialog.Builder alertDialog;

	private EditText oldPasswordEditText;
	private EditText newPasswordEditText;
	private EditText confirmNewPasswordEditText;
	private ProgressBar progressBar;
	private Button changePasswordButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);

		initializeVariables();
		setListeners();
	}

	private void initializeVariables() {
		auth = FirebaseAuth.getInstance();
		alertDialog = new AlertDialog.Builder(ChangePasswordActivity.this);
		alertDialog.setPositiveButton("OK", null);

		oldPasswordEditText = findViewById(R.id.old_password_edit_text);
		newPasswordEditText = findViewById(R.id.new_password_edit_text);
		confirmNewPasswordEditText = findViewById(R.id.confirm_new_password_edit_text);
		progressBar = findViewById(R.id.progress_bar);
		changePasswordButton = findViewById(R.id.change_password_button);
	}

	private void setListeners() {
		changePasswordButton.setOnClickListener(v -> changePassword());
	}

	private void changePassword() {
		// Check if new password fields are empty
		if (newPasswordEditText.getText().toString().isEmpty() || confirmNewPasswordEditText.getText().toString().isEmpty()) {
			alertDialog.setTitle("Insufficient details");
			alertDialog.setMessage("Enter new password");
			alertDialog.show();
			return;
		}

		if(!newPasswordEditText.getText().toString().equals(confirmNewPasswordEditText.getText().toString())) {
			alertDialog.setTitle("Enter correct password");
			alertDialog.setMessage("Newly entered password do not match");
			alertDialog.show();
			return;
		}

		// Check length of newly entered password
		if(newPasswordEditText.getText().length() < 6 || confirmNewPasswordEditText.getText().length() < 6) {
			alertDialog.setTitle("Password too short");
			alertDialog.setMessage("Newly entered password should be 6 letters long at least");
			alertDialog.show();
			return;
		}

		FirebaseUser user = auth.getCurrentUser();
		try {
			assert user != null;
			progressBar.setVisibility(View.VISIBLE);
			AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), oldPasswordEditText.getText().toString());
			user.reauthenticate(credential).addOnSuccessListener(aVoid -> user.updatePassword(oldPasswordEditText.getText().toString()).addOnSuccessListener(aVoid1 -> {
				progressBar.setVisibility(View.GONE);
				Toast.makeText(ChangePasswordActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
			})).addOnFailureListener(e -> {
				progressBar.setVisibility(View.GONE);
				alertDialog.setMessage(e.getMessage());
				alertDialog.show();
			});
		} catch (IllegalArgumentException e) {
			progressBar.setVisibility(View.GONE);
			alertDialog.setMessage("Enter old password");
			alertDialog.show();
		}
	}
}
