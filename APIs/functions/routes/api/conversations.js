const express = require('express');
const admin = require('firebase-admin');
const {FireSQL} = require('firesql');
const validator = require('validator');

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
		const sender = req.body.sender || req.params.sender;

		// Checks user uid only if email is provided
		// Else, bipasses this if statement 
		if (sender) {

			try {
				// Get user using provided email address
				const user = await admin.auth().getUserByEmail(sender);

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

router.use(authenticateUser);

// Create a new conversation
router.post('/', async (req, res) => {
	// initialize conversationInfo
	// sender and receiver are email addresses of the message sender and message
	// receiver users
	// const conversationInfo = {
	// 	sender: '',
	// 	receiver: ''
	// }
	const conversationInfo = req.body;

	// Check if sender and receiver is specified
	if(!conversationInfo.sender || !conversationInfo.receiver) {
		// Empty response, won't display the error to users
		return res.status(400).json({});
	}

	// Initialize fireSQL
	const fireSQL = new FireSQL(admin.firestore(), {includeId: true});

	// Check if current users exists in the database or not
	const users = await fireSQL.query(`SELECT email FROM users WHERE email IN ("${conversationInfo.sender}", "${conversationInfo.receiver}")`);

	// Check if two users are returned
	if(users.length !== 2) {
		// Empty response, won't display the error to users
		return res.status(400).json({});
	}

	// Check if the senders conversation already exists
	const conversation = await fireSQL.query(`SELECT __name__ FROM conversations WHERE sender = '${conversationInfo.sender}' AND receiver = '${conversationInfo.receiver}'`);

	if(conversation.length === 1) {
		return res.status(200).json({
			success: "Conversation already exists"
		});
	} else {
		// Create a new conversation
		await db.collection('conversations').add({
			sender: conversationInfo.sender,
			receiver: conversationInfo.receiver
		});
	}

	return res.status(200).json({
		success: "Conversation added"
	});
});

router.post('/delete', async (req, res) => {
	// Get conversationID
	// const conversationInfo = {
	// 	conversationID: ''
	// }

	const conversationID = req.body.conversationID;

	// Check if conversationID is provided
	if(validator.isEmpty(conversationID)) {
		return res.status(400).json({
			error: "No conversation selected to delete"
		});
	}

	// Get all the messages associated with the conversationID
	const messagesSnapshot = await db.collection('messages').where('conversationID', '==', conversationID).get()

	var batch = db.batch();
	// Delete all the messages associated with the conversationID
	messagesSnapshot.docs.forEach(message => {
		batch.delete(message.ref);
	});
	await batch.commit();

	// Delete the conversation
	await db.collection('conversations').doc(conversationID).delete();

	return res.status(200).json({
		success: "Conversation deleted"
	});
});

module.exports = router;
