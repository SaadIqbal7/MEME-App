package com.example.meme.container;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.Objects;

public class Helpers {

	public static  String apiUrl = "https://us-central1-meme-project-0.cloudfunctions.net";

	// Convert day number into name of the day
	public static String dayOfWeek(int number) {
		if(number == 1) return "mon";
		else if (number == 2) return "tue";
		else if (number == 3) return "wed";
		else if (number == 4) return "thu";
		else if (number == 5) return "fri";
		else if (number == 6) return "sat";
		else if (number == 7) return "sun";
		else return "";
	}

	public static String getExtension(Context context, Uri imageUri) {
		String extension;

		//Check uri format to avoid null
		if (Objects.equals(imageUri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
			//If scheme is a content
			final MimeTypeMap mime = MimeTypeMap.getSingleton();
			extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(imageUri));
		} else {
			//If scheme is a File
			//This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
			extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(Objects.requireNonNull(imageUri.getPath()))).toString());
		}
		return extension;
	}

	public static String addZero(int num) {
		// If the number is less than 10, preceed the number with a zero
		String number = "" + num;
		if (num < 10) {
			number = "0" + num;
		}

		return number;
	}
}
