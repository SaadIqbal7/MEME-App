const express = require('express');
const admin = require('firebase-admin');
const validator = require('validator');

const router = express.Router();
const db = admin.firestore();

router.post('/', async (req, res) => {
	// Get user's login info (email, password)
	// loginInfo = {
	// 	email: '',
	// 	password: ''
	// }
	let loginInfo = req.body;

	// Initialize an object for errors
	// errLoginInfo = {
	// 	errEmail: '',
	// 	errPassword: ''
	// }
	let errLoginInfo = {
		errEmail: '',
		errPassword: ''
	};

	// Check if email is empty
	if (validator.isEmpty(loginInfo.email)) {
		errLoginInfo.errEmail = 'Please provide an E-mail address';
	} else {
		// remove white space
		loginInfo.email = loginInfo.email.trim();

		// Lower case email address
		loginInfo.email = loginInfo.email.toLowerCase();

		// Check if email is valid
		let isValidEmail = false;
		isValidEmail = validator.isEmail(loginInfo.email);

		if (!isValidEmail) {
			errLoginInfo.errEmail = 'Invalid E-mail address';
		} else {
			try {
				// Get the usersSnapshot and return the a specific document of a specific id (loginInfo.email)
				const userSnapshot = await db.collection('users')
					.doc(loginInfo.email).get();

				if (!userSnapshot.exists) {
					errLoginInfo.errEmail = 'No user exists for the provided E-mail address';
				}
			} catch (e) {
				console.log(e);
			}
		}
	}

	if (loginInfo.password.length < 5) {
		errLoginInfo.errPassword = 'Password must be greater than 5 characters';
	}

	if (!validator.isEmpty(errLoginInfo.errEmail) ||
		!validator.isEmpty(errLoginInfo.errPassword)) {
		// Send 400 (Bad request) along with the errors
		return res.status(400).json(errLoginInfo);
	} else {
		// Send 200 (OK) along with success message
		return res.status(200).json({
			success: "User logged in"
		});
	}
});

module.exports = router;
