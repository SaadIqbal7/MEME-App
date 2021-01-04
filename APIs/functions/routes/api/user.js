const express = require('express');
const admin = require('firebase-admin');
const validator = require('validator');
const {FireSQL} = require('firesql');
const path = require('path');
const fs = require('fs');
const os = require('os');
const UUID = require('uuid-v4');
const helpers = require('../../helpers/helpers');

const router = express.Router();
const db = admin.firestore();

// Middleware to authenticate the user
const authenticateUser = async (req, res, next) => {

	if (!req.headers.authorization || !req.headers.authorization.startsWith('Bearer ')) {
		return res.status(403).json({
			unauthorized: "User unauthorized"
		});
	}

	// Split the header into two parts
	// Take the id token from the header passed by the authenticated user
	const idToken = req.headers.authorization.split('Bearer ')[1];
	try {
		// Verify that the token is valid
		const decodedIdToken = await admin.auth().verifyIdToken(idToken);

		// when decoded successfully, the ID Token content will be added as `req.user`.
		req.user = decodedIdToken;

		// Checks the provided email againts the email taken from the bearer token
		// if email matches, user is authenticated
		// Get email
		const email = req.body.email || req.params.email;

		// Checks user uid only if email is provided
		// Else, bipasses this if statement 
		if (email) {

			try {
				// Get user using provided email address
				const user = await admin.auth().getUserByEmail(email);

				// Compare uids of the email address
				if (user.uid !== decodedIdToken.uid) {
					// Send error response if uids do not match
					return res.status(403).json({
						unauthorized: "User unauthorized"
					});
				}

			} catch (e) {
				return res.status(403).json({
					unauthorized: "User unauthorized"
				});
			}
		}

		next();
		return;
	} catch (e) {
		return res.status(403).json({
			unauthorized: "User unauthorized"
		});
	}
}

// Use the middleware
router.use(authenticateUser);

// Make a record of user in the database
router.post('/', async (req, res) => {
	// Initialize the user's information
	let userInfo = req.body;
	// Get user's information
	// userInfo = {
	//	email: '',
	// 	fullName: '',
	// 	location: '',
	//	description,
	// 	gender: '',
	//	dob: '',
	//	numberOfPosts: 0,
	//	numberOfUpvotedPosts: 0,
	//	numberOfSavedPosts,
	//	userScore: 0,
	//	userRank: 'beginner',
	//	onlineStatus: 1
	// }

	let errUserInfo = {
		errFullName: '',
		errLocation: '',
		errGender: '',
		errDOB: ''
	}

	// Check if valid information is provided

	//Check if full name is empty
	if (validator.isEmpty(userInfo.fullName)) {
		errUserInfo.errFullName = 'Please provide a name';
	}

	//Check if location is empty
	if (validator.isEmpty(userInfo.location)) {
		errUserInfo.errLocation = 'Please provide a location';
	}

	//Check if gender is empty
	if (validator.isEmpty(userInfo.gender)) {
		errUserInfo.errGender = 'Please specify a gender';
	} else {
		
		// Check if correct gender is provided
		if (!validator.equals(userInfo.gender, 'Male') &&
			!validator.equals(userInfo.gender, 'Female') &&
			!validator.equals(userInfo.gender, 'Other')) {
			errUserInfo.errGender = 'Please select a gender';
		}
	}

	//Check if dob is empty
	if (validator.isEmpty(userInfo.dob)) {
		errUserInfo.errDOB = 'Please specify your date of birth';
	} else {
		// Set an age limit (13 years)
		// Get the age of user
		const age = helpers.calculateAge(userInfo.dob);

		if (age < 13) {
			errUserInfo.errDOB = "You must be at least 13 years old to access your account";
		}
	}

	// Checks if description is not empty
	if (!validator.isEmpty(userInfo.description)) {
		// Trim the description provided
		userInfo.description = userInfo.description.trim();
	}

	if (!validator.isEmpty(errUserInfo.errFullName) ||
		!validator.isEmpty(errUserInfo.errGender) ||
		!validator.isEmpty(errUserInfo.errLocation) ||
		!validator.isEmpty(errUserInfo.errDOB)) {

		// Send 400 (Bad request) along with the errors
		return res.status(400).json(errUserInfo);
	} else {
		// Add user's information to firestore
		// Add all the additional information not passed by the user
		userInfo.numberOfPosts = 0;
		userInfo.numberOfUpvotedPosts = 0;
		userInfo.numberOfSavedPosts = 0;
		userInfo.userScore = 0;
		userInfo.userRank = 'beginner';
		userInfo.onlineStatus = 1;
		//	imageURL: URL of some random image (will be stored in the cloud storage)
		userInfo.imageURL = 'https://firebasestorage.googleapis.com/v0/b/meme-project-0.appspot.com/o/Profile%20picures%2Fdefault%20pictures%2Fpikachu.png?alt=media&token=dcfa4122-1ed7-4f9e-a9f5-ea85ef92fa69';

		try {
			// Add the user's information
			await db.collection('users').doc(userInfo.email).set(userInfo, {
				merge: true
			});

			return res.status(200).json({
				success: "User information added"
			});
		} catch (e) {
			console.log(e);
		}
	}
});

// Get specific user's data
router.get('/:email', async (req, res) => {
	// Checks if email is provided or not
	if(!req.params.email) {
		return res.status(400).json({
			error: "Provide an E-mail address"
		});
	}

	// Get email
	const email = req.params.email;

	// Check if email is valid
	if (!validator.isEmail(email)) {
		return res.status(400).json({
			errEmail: 'Provide a valid E-mail address'
		});
	} else {
		try {
			// Get the user information from firestore
			const userSnapshot = await db.collection('users')
				.doc(email)
				.get();

			if (!userSnapshot.exists) {
				return res.status(400).json({
					errEmail: 'No user exists for the provided E-mail address'
				});
			}

			// Store the user information
			const userInfo = userSnapshot.data();

			// Send the user information back
			return res.status(200).json(userInfo);
		} catch (e) {
			console.log(e);
		}
	}
});

router.get('/username/:username', async (req, res) => {
	// Initialize fireSQL
	const fireSQL = new FireSQL(db);

	// Checks if username is provided or not
	if(!req.params.username) {
		// Return error indicating, username is empty
		return res.status(400).json({
			error: "Please provide a username"
		});
	}

	// Get username
	let username = req.params.username;

	// Convert the username to lowercase and remove any white spaces from start and end
	username = username.toLowerCase().trim();

	// Queries the users collection to find any username starting with the provided username
	let users = await fireSQL.query(`SELECT * FROM users WHERE username LIKE '${username}%'`);

	// Checks if users exists for the provided username
	if(users.length > 0) {
		// Return users
		return res.status(200).json(users);
	}

	// Return error indicating, no users found
	return res.status(400).json({
		error: "No user found"
	});
});

router.put("/", async (req, res) => {
	// Get user's information
	// userInfo = {
	//	email: '',
	// 	fullName: '',
	// 	location: '',
	//	description,
	// 	gender: '',
	//	dob: '',
	//	allowMessages: false,
	//	onlineStatus: 1,
	// }

	const userInfo = req.body;

	let errUserInfo = {
		errFullName: '',
		errLocation: '',
		errGender: '',
		errDOB: ''
	}

	//Check if full name is empty
	if (validator.isEmpty(userInfo.fullName)) {
		errUserInfo.errFullName = 'Please provide a name';
	}

	//Check if location is empty
	if (validator.isEmpty(userInfo.location)) {
		errUserInfo.errLocation = 'Please provide a location';
	}

	//Check if gender is empty
	if (validator.isEmpty(userInfo.gender)) {
		errUserInfo.errGender = 'Please specify a gender';
	} else {
		// Check if correct gender is provided
		if (!validator.equals(userInfo.gender, 'Male') &&
			!validator.equals(userInfo.gender, 'Female') &&
			!validator.equals(userInfo.gender, 'Other')) {
			errUserInfo.errGender = 'Please select a gender';
		}
	}

	//Check if dob is empty
	if (validator.isEmpty(userInfo.dob)) {
		errUserInfo.errDOB = 'Please specify your date of birth';
	} else {
		// Set an age limit (13 years)
		// Get the age of user
		const age = helpers.calculateAge(userInfo.dob);

		if (age < 13) {
			errUserInfo.errDOB = "You must be at least 13 years old to access your account";
		}
	}

	// Checks if description is not empty
	if (!validator.isEmpty(userInfo.description)) {
		// Trim the description provided
		userInfo.description = userInfo.description.trim();
	}

	if (!validator.isEmpty(errUserInfo.errFullName) ||
		!validator.isEmpty(errUserInfo.errGender) ||
		!validator.isEmpty(errUserInfo.errLocation) ||
		!validator.isEmpty(errUserInfo.errDOB)) {

		return res.status(400).json(errUserInfo);
	} else {
		try {
			await db.collection('users')
				.doc(userInfo.email)
				.update(userInfo);

			return res.status(200).json({
				success: "User infomation updated"
			});
		} catch (e) {
			console.log(e);
		}
	}
});

router.post('/uploadImage', async (req, res) => {

	// const imageInformation = {
	// 	email: '',
	// 	image: '' // Base64 image
	// }
	// Get image information
	const imageInformation = req.body;

	// Generate image name
	const userSnapshot = await db.collection('users').doc(imageInformation.email).get();
	const imageName = "profilepic_" + userSnapshot.data().username;

	// Split the base64 Image 2 parts
	const base64String = imageInformation.image.split(',');

	// Get the image part from base64 String
	const base64Image = base64String[1];

	// Get the information part from base64 String
	const base64ImageInformation = base64String[0];

	// Get the extension from base64 image information
	const extension = base64ImageInformation.split('/')[1].replace(';base64', '');

	// Get temp directory on the server and temporarily store image on that directory
	const tempFilePath = path.join(os.tmpdir(), imageName + "." + extension);
	fs.writeFileSync(tempFilePath, base64Image, 'base64', err => {
		if (err) {
			console.log(e);
		}
	});

	// Initialize cloud storage bucket
	const bucket = admin.storage().bucket();

	// Generate a random ID for image
	const uuid = UUID();

	try {
		// Upload image in "Profile pictures" folder on cloud storage
		const data = await bucket.upload(tempFilePath, {
			destination: "Profile picures/" + path.basename(tempFilePath),
			uploadType: 'media',
			metadata: {
				contentType: 'image/' + extension,
				metadata: {
					firebaseStorageDownloadTokens: uuid
				}
			}
		});

		// Get the uploaded file
		const file = data[0];

		try{
			// Store the image in user's collection
			await admin.firestore().collection('users').doc(imageInformation.email).update({
				imageURL: "https://firebasestorage.googleapis.com/v0/b/" + bucket.name + "/o/" + encodeURIComponent(file.name) + "?alt=media&token=" + uuid
			});

			// Return the download url of image
			return res.status(200).json({
				imageURL: "https://firebasestorage.googleapis.com/v0/b/" + bucket.name + "/o/" + encodeURIComponent(file.name) + "?alt=media&token=" + uuid
			});

		} catch(e){
			return res.status(400).json({
				error: "Could not upload image"
			})
		}
	} catch (e) {
		return res.status(400).json({
			error: "Could not upload image"
		});
	}
});

// Choose a random profile image for user
router.post('/selectRandomPicture', async (req, res) => {
	// const userInformation = {
	// 	email: ''
	// }

	const userInformation = req.body;

	// Get all photos from default profile picture collection
	const picturesSnapshot = await db.collection('defaultProfilePictures').get();

	// Select a random number image from the picturesSnapshot array
	const randomNumber = Math.round(Math.random() * 100) % picturesSnapshot.docs.length;
	const imageURL = picturesSnapshot.docs[randomNumber].data().imageURL

	try{
		// Store the image in user's collection
		await admin.firestore().collection('users').doc(userInformation.email).update({
			imageURL: imageURL
		});

		// Return the download url of image
		return res.status(200).json({
			imageURL: imageURL
		});

	} catch(e){
		return res.status(400).json({
			error: "Could not upload image"
		});
	}
});

router.post('/removePicture', async (req, res) => {
	// Get user's email
	const email = req.body.email;

	// Save the default profile picture
	await db.collection('users').doc(email).update({
		imageURL: 'https://firebasestorage.googleapis.com/v0/b/meme-project-0.appspot.com/o/Profile%20picures%2Fdefault%20pictures%2Fpikachu.png?alt=media&token=dcfa4122-1ed7-4f9e-a9f5-ea85ef92fa69'
	});

	return res.status(200).json({
		imageURL: 'https://firebasestorage.googleapis.com/v0/b/meme-project-0.appspot.com/o/Profile%20picures%2Fdefault%20pictures%2Fpikachu.png?alt=media&token=dcfa4122-1ed7-4f9e-a9f5-ea85ef92fa69'
	});
});

module.exports = router;

