const express = require('express');
const admin = require('firebase-admin');
const validator = require('validator');
const { FireSQL } = require('firesql');
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

// Send a new message
router.post('/', async (req, res) => {
	// Initialize messageInfo
	// const messageInfo = {
	// 	conversationID,
	// 	message
	// }

	let messageInfo = req.body;

	// Check if message is empty
	if (validator.isEmpty(messageInfo.message)) {
		return res.status(400).json({
			error: "Enter a message"
		});
	}

	// Remove any white spaces from the message
	messageInfo.message = messageInfo.message.trim();

	// Initialize fireSQL
	const fireSQL = new FireSQL(db, { includeId: true });

	// Get sender and receiver information from the conversationID
	const user1Conversation = await fireSQL.query(
		`SELECT * FROM conversations WHERE __name__ = '${messageInfo.conversationID}'`
	);

	// Get other conversation of same two users
	const user2Conversation = await fireSQL.query(
		`SELECT * FROM conversations WHERE sender = '${user1Conversation[0].receiver}' AND receiver = '${user1Conversation[0].sender}'`
	);

	// Initialize document reference for new conversation that can be created variable
	let newConversationDocumentRef = null;

	// If receiving user has deleted his chat or hasn't created one
	// then make a new conversation for the receiving user
	if(user2Conversation.length === 0) {
		newConversationDocumentRef = await db.collection('conversations').add({
			sender: user1Conversation[0].receiver,
			receiver: user1Conversation[0].sender
		});
	}

	// Get time for message when it is sent
	const currentDateAndTime = helpers.getCurrentDateAndTime();

	// Store two instances of message
	await db.collection('messages').add({
		timeSent: currentDateAndTime,
		message: messageInfo.message,
		conversationID: messageInfo.conversationID,
		sender: user1Conversation[0].sender,
		receiver: user1Conversation[0].receiver
	});

	await db.collection('messages').add({
		timeSent: currentDateAndTime,
		message: messageInfo.message,
		conversationID: newConversationDocumentRef === null ? user2Conversation[0].__name__ : newConversationDocumentRef.id,
		sender: user1Conversation[0].sender,
		receiver: user1Conversation[0].receiver
	});

	return res.status(200).json({
		success: "Message sent"
	});
});

router.post('/delete', async (req, res) => {
	// Get messageID
	// const messageInfo = {
	// 	messageID: ''
	// }
	const messageID = req.body.messageID;

	if (validator.isEmpty(messageID)) {
		return res.status(400).json({
			error: "No message seleted to delete"
		});
	}

	try{
		await db.collection('messages').doc(messageID).delete();

		return res.status(200).json({
			success: "Message deleted"
		});
	} catch(e) {
		console.log(e);
	}
});

module.exports = router;
