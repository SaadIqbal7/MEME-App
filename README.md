Social media application for creating and sharing memes.

The APIs folder contains the Rest APIs created using Firebase Cloud Functions.

> Before setting up Firebase Cloud Functions, if you don't have a Firebase blaze plan you can use Node.js version 8 and upload the Cloud Functions. But this will expire after March 2021 so you have to buy a blaze plan in order to upload your Cloud Functions.

Inorder to use the Rest APIs, you first need to a new Firebase project. After the project is created, initialize Firebase Firestore, Firebase Authentication, Firebase Cloud Storage. Then from the Firebase Authentication tab, create a new service_account.json file and place it in APIs folder. Your APIs folder would look like

```
APIs/
  ...
  service_account.json
  ...
```
Then in the APIs/index.js file, paste the URL of your storage bucket and your database url. 
```
admin.initializeApp({
	credential: admin.credential.cert(serviceAccount),
	databaseURL: "YOUR DATABASE URL",
	storageBucket: "YOUR STORAGE BUCKET URL"
});
```
You can get the storage bucket URL and database URL from their tabs. If you can't find the URLs please refer to Firebase's official documentation on how and where to get the URL from.

After all is setup, use the command below to upload Cloud Functions to Firebase.
```
firebase deploy --only functions
```

The MEME folder is an Android Studio project. 

First open the project in Android Studio. Then go to container/Helpers class and in the <b>apiURL</b> add your Cloud Function URL:
```
// https://xx-xx-xx-xx-xx.cloudfunctions.net
public static String apiUrl = "YOUR CLOUD FUNCTION URL";
```
Then build the project and download the APK to use the App :)



