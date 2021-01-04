const functions = require('firebase-functions');
const admin = require('firebase-admin');
const validator = require('validator');

const db = admin.firestore();

const incrementNumberOfReplies = functions.firestore.document('comments/{commentId}').onCreate(async (snap, context) => {
	// Get the added or removed reply
	const reply = snap.data();
	// Get parent from the reply
	const parent = reply.parent;

	// Check if its not a reply
	if(validator.default.isEmpty(parent)){
		return;
	}

	// Get the comment from parentId
	const comment = await db.collection('comments').doc(parent).get();
	// Get number of replies from the comment
	let numberOfReplies = comment.data().numberOfReplies;

	// Increment number of replies
	numberOfReplies = numberOfReplies + 1;

	// Update number of replies
	await db.collection('comments').doc(parent).update({
		numberOfReplies : numberOfReplies
	});
});

const updateCommentProfilePictureURL = functions.firestore.document('users/{userId}').onUpdate(async (change, context) => {
	// Check if profile picture is not changed
	if (change.before.data().imageURL === change.after.data().imageURL) {
		return;
	}

	// Get user's email
	const email = change.after.data().email;
	// Get changed profile picture URL
	const profilePictureURL = change.after.data().imageURL;
	// Get all posts
	const commentsSnapshot = await db.collection('comments').where('email', '==', email).get();

	// Initialize batch
	const batch = db.batch();
	commentsSnapshot.forEach(post => {
		// Get each documents reference
		const documentRef = post.ref;
		// Update a document
		batch.update(documentRef, { 'userProfilePictureURL': profilePictureURL });
	});

	// Commit the batch
	await batch.commit();
});

const updateNumberOfUpvotesOfComment = functions.firestore.document('commentUpvotes/{commentUpvotesId}').onWrite(async (snap, context) => {
	// Get the upvote document
	const upvoteDocument = snap.before.data() || snap.after.data();
	// Get the comment Id
	const commentId = upvoteDocument.commentId;

	const comment = await db.collection('comments').doc(commentId).get();

	if (!comment.exists) {
		return;
	}

	let numberOfUpvotes = comment.data().numberOfUpvotes;

	// Check if upvote is added
	if (!snap.before.exists) {
		numberOfUpvotes = numberOfUpvotes + 1;
	}
	// Check if upvote is removed
	else if (!snap.after.exists) {
		numberOfUpvotes = numberOfUpvotes - 1;
	}

	await db.collection('comments').doc(commentId).update({
		numberOfUpvotes: numberOfUpvotes
	});
});

const updateNumberOfDownvotesOfComment = functions.firestore.document('commentDownvotes/{commentDownvotesId}').onWrite(async (snap, context) => {
	// Get the downvote document
	const downvoteDocument = snap.before.data() || snap.after.data();
	// Get the comment Id
	const commentId = downvoteDocument.commentId;

	const comment = await db.collection('comments').doc(commentId).get();

	if (!comment.exists) {
		return;
	}

	let numberOfDownvotes = comment.data().numberOfDownvotes;

	// Check if downvote is added
	if (!snap.before.exists) {
		numberOfDownvotes = numberOfDownvotes + 1;
	}
	// Check if downvote is removed
	else if (!snap.after.exists) {
		numberOfDownvotes = numberOfDownvotes - 1;
	}

	await db.collection('comments').doc(commentId).update({
		numberOfDownvotes: numberOfDownvotes
	});

});

// Trigger fires when a comment is deleted
const deleteComment = functions.firestore.document('comments/{commentId}').onDelete(async (snap, context) => {

	// Initialize batch
	const batch = db.batch();

	// Get commentId
	const commentId = snap.id;
	const parent = snap.data().parent;

	//It means this comment is a reply
	if (!validator.isEmpty(parent)) {
		// Check if the parent is already deleted
		// Like if the comment is deleted than its replies will automatically delete
		// So in this case we need check if the parent is deleted than
		// No need to decrement its replies
		const parentComment = await db.collection('comments').doc(parent).get();
		if(parentComment.exists) {
			let numberOfReplies = parentComment.data().numberOfReplies;
			numberOfReplies = numberOfReplies - 1;
	
			db.collection('comments').doc(parent).update({
				numberOfReplies: numberOfReplies
			});	
		}
	} else {
		// Get all replies of this comment
		const replies = await db.collection('comments').where('parent', '==', commentId).get();
		// Delete  replies
		replies.forEach(reply => {
			// Get documents snapshot
			const replyDocRef = reply.ref;
			// Delete each document
			batch.delete(replyDocRef);
		});
	}

	// Get all the upvotes of this comment
	const upvotesSnapshot = await db.collection('commentUpvotes').where('commentId', '==', commentId).get();
	// Get all downvotes of this comment
	const downvotesSnapshot = await db.collection('commentDownvotes').where('commentId', '==', commentId).get();

	// Delete upvotes
	upvotesSnapshot.forEach(upvote => {
		// Get documents snapshot
		const upvoteDocRef = upvote.ref;
		// Delete each document
		batch.delete(upvoteDocRef);
	});

	// Delete downvotes
	downvotesSnapshot.forEach(downvote => {
		// Get documents snapshot
		const downvoteDocRef = downvote.ref;
		// Delete each document
		batch.delete(downvoteDocRef);
	});

	// Commit batch
	await batch.commit();
});

module.exports = {
	incrementNumberOfReplies,
	updateNumberOfUpvotesOfComment,
	updateNumberOfDownvotesOfComment,
	deleteComment,
	updateCommentProfilePictureURL
}
