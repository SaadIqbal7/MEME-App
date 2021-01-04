const express = require('express');
const admin = require('firebase-admin');
const validator = require('validator');

const router = express.Router();
const db = admin.firestore();

// Validate registration information (username, email and password)
router.post('/', async (req, res) => {
	// Get user's registration info (username, email, password)
	// registrationInfo = {
	// 	username: '',
	// 	email: '',
	// 	password: ''
	// }
	let registrationInfo = req.body;

	// Initialize an object for errors
	// errRegistrationInfo = {
	// 	errUsername: '',
	// 	errEmail: '',
	// 	errPassword: ''
	// }

	let errRegistrationInfo = {
		errUsername: '',
		errEmail: '',
		errPassword: ''
	}
	
	// Snapshot of user table
	let userSnapshot = null;

	// Get user's information
	try {
		// Remove white spaces from email
		registrationInfo.email = registrationInfo.email.trim();

		// Lowercase the email address
		registrationInfo.email = registrationInfo.email.toLowerCase();

		// Remove white spaces from username
		registrationInfo.username = registrationInfo.username.trim();

		// Lowercase the username
		registrationInfo.username = registrationInfo.username.toLowerCase();

		// Check is email is empty
		if (validator.isEmpty(registrationInfo.email)) {
			errRegistrationInfo.errEmail = 'Please provide an E-mail address';
		} else {
			// Check if email is valid
			let isValidEmail = false;
			isValidEmail = validator.isEmail(registrationInfo.email);

			if (!isValidEmail) {
				errRegistrationInfo.errEmail = 'Invalid E-mail address';
			} else {
				// Get the usersSnapshot and return the a specific document of a specific id (registrationInfo.email)
				userSnapshot = await db.collection('users').doc(registrationInfo.email).get();

				if (userSnapshot.exists) {
					errRegistrationInfo.errEmail = 'Account already exists for this E-mail address';
				}
			}
		}

		// Checks if the username is empty
		if (validator.isEmpty(registrationInfo.username)) {
			errRegistrationInfo.errUsername = 'Please provide a username';
		} else {
			// Check if username is valid
			if (registrationInfo.username.includes(' ')) {
				errRegistrationInfo.errUsername = 'Username should not contain any spaces';
			} else {
				try {
					// Get users with username matching the provided username
					userSnapshot = await db
						.collection('users')
						.where('username', '==', registrationInfo.username)
						.get();

					// Check if the username already exists
					if (userSnapshot.docs.length === 1) {
						errRegistrationInfo.errUsername = 'Username is already taken';
					}
				} catch (e) {
					console.log(e);
				}
			}
		}

		// Checks if the password is less than 5 characters
		if (registrationInfo.password.length < 5) {
			errRegistrationInfo.errPassword = 'Password must be greater than 5 characters';
		}

		// Checks if an error exists
		if (!validator.isEmpty(errRegistrationInfo.errEmail) ||
			!validator.isEmpty(errRegistrationInfo.errUsername) ||
			!validator.isEmpty(errRegistrationInfo.errPassword)) {

			// Send 400 (Bad request) along with the errors
			return res.status(400).json(errRegistrationInfo);
		} else {
			// Add user's registration information to firestore
			await db.collection('users').doc(registrationInfo.email)
				.set({
					username: registrationInfo.username
				});

			// Send 200 (OK) along with success message
			return res.status(200).json({
				success: "User registered"
			});
		}
	} catch (e) {
		console.log(e);
	}
});

module.exports = router;
