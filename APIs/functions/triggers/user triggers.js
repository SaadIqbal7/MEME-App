const functions = require('firebase-functions');
const admin = require('firebase-admin');

const db = admin.firestore();

const updateNumberOfPosts = functions.firestore.document('posts/{postId}').onWrite(async (change, context) => {
	// return if the post is only updated
	if(change.before.exists && change.after.exists) {
		return;
	}

	// Get the post
	const post = change.after.data() || change.before.data();
	// Get the user's email from the post
	const email = post.email;
	// Get the user
	const userSnapshot = await db.collection('users').doc(email).get();
	// Get the number of posts user have
	let numberOfPosts = userSnapshot.data().numberOfPosts;
	
	// Check if new post is added
	if(!change.before.exists) {
		numberOfPosts = numberOfPosts + 1;
	} // Check if a post is deleted
	else if(!change.after.exists) {
		numberOfPosts = numberOfPosts - 1;
	}

	// Update the number of posts of the user
	await db.collection('users').doc(email).update({
		numberOfPosts: numberOfPosts
	});	
});

const updateNumberOfSavedPosts = functions.firestore.document('savedPosts/{savedPostId}').onWrite(async (change, context) => {
	// return if the post is only updated
	if(change.before.exists && change.after.exists) {
		return;
	}

	// Get the post
	const post = change.after.data() || change.before.data();
	// Get the user's email from the post
	const email = post.email;
	// Get the user
	const userSnapshot = await db.collection('users').doc(email).get();
	// Get the number of posts user have
	let numberOfSavedPosts = userSnapshot.data().numberOfSavedPosts;
	
	// Check if new post is added
	if(!change.before.exists) {
		numberOfSavedPosts = numberOfSavedPosts + 1;
	} // Check if a post is deleted
	else if(!change.after.exists) {
		numberOfSavedPosts = numberOfSavedPosts - 1;
	}

	// Update the number of posts of the user
	await db.collection('users').doc(email).update({
		numberOfSavedPosts: numberOfSavedPosts
	});	
});

const updateNumberOfUpvotedPosts = functions.firestore.document('postUpvotes/{postUpvoteId}').onWrite(async (change, context) => {
	// return if the post is only updated
	if(change.before.exists && change.after.exists) {
		return;
	}

	// Get the post
	const post = change.after.data() || change.before.data();
	// Get the user's email from the post
	const email = post.email;
	// Get the user
	const userSnapshot = await db.collection('users').doc(email).get();
	// Get the number of posts user have
	let numberOfUpvotedPosts = userSnapshot.data().numberOfUpvotedPosts;
	
	// Check if new post is added
	if(!change.before.exists) {
		numberOfUpvotedPosts = numberOfUpvotedPosts + 1;
	} // Check if a post is deleted
	else if(!change.after.exists) {
		numberOfUpvotedPosts = numberOfUpvotedPosts - 1;
	}

	// Update the number of posts of the user
	await db.collection('users').doc(email).update({
		numberOfUpvotedPosts: numberOfUpvotedPosts
	});	
});

module.exports = {
	updateNumberOfPosts,
	updateNumberOfSavedPosts,
	updateNumberOfUpvotedPosts
}
