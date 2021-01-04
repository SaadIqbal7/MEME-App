const functions = require('firebase-functions');
const admin = require('firebase-admin');

const db = admin.firestore();

const updateNumberOfUpvotesOfPost = functions.firestore.document('postUpvotes/{upvotesId}').onWrite(async (change, context) => {
	// Get the upvote document
	const upvote = change.before.data() || change.after.data();
	// Get the post Id
	const postId = upvote.postId;
	// Get the post from the postId
	const post = await db.collection('posts').doc(postId).get();
	// Check if post exists
	if(!post.exists) {
		return;
	}
	// Get the numberOfUpvotes of post
	let numberOfUpvotes = post.data().numberOfUpvotes;

	// Check if upvote is added
	if(!change.before.exists) {
		numberOfUpvotes = numberOfUpvotes + 1;
	}
	// Check if upvote is removed
	else if (!change.after.exists) {
		numberOfUpvotes = numberOfUpvotes - 1;
	}

	// Update number of upvotes of the post
	await db.collection('posts').doc(postId).update({
		numberOfUpvotes: numberOfUpvotes
	});
});

const updateNumberOfDownvotesOfPost = functions.firestore.document('postDownvotes/{downvoteId}').onWrite(async (change, context) => {
	// Get the downvote document
	const downvote = change.before.data() || change.after.data();
	// Get the post Id
	const postId = downvote.postId;
	// Get the post from the postId
	const post = await db.collection('posts').doc(postId).get();
	// Check if post exists
	if(!post.exists) {
		return;
	}

	// Get the numberOfUpvotes of post
	let numberOfDownvotes = post.data().numberOfDownvotes;

	// Check if upvote is added
	if(!change.before.exists) {
		numberOfDownvotes = numberOfDownvotes + 1;
	}
	// Check if upvote is removed
	else if (!change.after.exists) {
		numberOfDownvotes = numberOfDownvotes - 1;
	}

	// Update number of upvotes of the post
	await db.collection('posts').doc(postId).update({
		numberOfDownvotes: numberOfDownvotes
	});
});

const updateNumberOfCommentsOfPost = functions.firestore.document('comments/{commentId}').onWrite(async (change, context) => {
	
	// Check if a comment is only updated
	if(change.before.exists && change.after.exists) {
		return;
	}

	// Get the added or removed comment
	const comment = change.before.data() || change.after.data();
	// Get postId from the comment
	const postId = comment.postId;
	// Get the post from postId
	const post = await db.collection('posts').doc(postId).get();
	// Get number of comments from the post
	let numberOfComments = post.data().numberOfComments;

	// Check if the comment is added
	if(!change.before.exists) {
		numberOfComments = numberOfComments + 1;
		// Check if the comment is deleted
	} else if(!change.after.exists) {
		numberOfComments = numberOfComments - 1;
	}

	// Update number of comments
	await db.collection('posts').doc(postId).update({
		numberOfComments : numberOfComments
	});
});

const updatePostProfilePictureURL = functions.firestore.document('users/{userId}').onUpdate(async (change, context) => {
	// Check if profile picture is not changed
	if(change.before.data().imageURL === change.after.data().imageURL) {
		return;
	}

	// Get user's email
	const email = change.after.data().email;
	// Get changed profile picture URL
	const profilePictureURL = change.after.data().imageURL;
	// Get all posts
	const postsSnapshot = await db.collection('posts').where('email', '==', email).get();

	// Initialize batch
	const batch = db.batch();
	postsSnapshot.forEach(post => {
		// Get each documents reference
		const documentRef = post.ref;
		// Update a document
		batch.update(documentRef, {'userProfilePictureURL': profilePictureURL});
	});

	// Commit the batch
	await batch.commit();
});

// Trigger fires when a post is deleted
const deletePost = functions.firestore.document('posts/{postId}').onDelete(async (snap, context) => {
	// Remove image from storage
	// Get the imageURL of the deleted post
	const imageURL = snap.data().imageURL;
	// Get user's email
	const email = snap.data().email;
	// Get image name
	const imageName = imageURL.split('token=')[1];
	// Get image extension
	const extension = imageURL.split('_post')[1].split('?')[0].replace(imageName, ''); 

	// Initialize bucket
	const bucket = admin.storage().bucket();

	// Get file path
	const filePath = `posts/${email}/${email}_post${imageName + extension}`;

	try {
		await bucket.file(filePath).delete();
		console.log('Image deleted successfully');
	} catch(e) {
		console.log('Failed to delete image: ' + e);
	}

	// Removes image's upvotes and downvotes
	// Get postId
	const postId = snap.id;

	// Get all the upvotes of this post
	const upvotesSnapshot = await db.collection('postUpvotes').where('postId', '==', postId).get();
	// Get all downvotes of this post
	const downvotesSnapshot = await db.collection('postDownvotes').where('postId', '==', postId).get();
	// Get all comments (Not replies) of this post
	const commentsSnapshot = await db.collection('comments').where('postId', '==', postId).where('parent', '==', '').get();
	// Get the saves of this post
	const savedPostSnapshot = await db.collection('savedPosts').where('postId', '==', postId).get();

	// Initialize batch
	const batch = db.batch();

	// Delete upvotes
	upvotesSnapshot.forEach(upvote => {
		// Get documents reference
		const upvoteDocRef = upvote.ref;
		// Delete each document
		batch.delete(upvoteDocRef);
	});

	// Delete downvotes
	downvotesSnapshot.forEach(downvote => {
		// Get documents reference
		const downvoteDocRef = downvote.ref;
		// Delete each document
		batch.delete(downvoteDocRef);
	});

	// Delete direct commments (Not replies)
	commentsSnapshot.forEach(comment => {
		// Get documents reference
		const commentDocRef = comment.ref;
		// Delete each document
		batch.delete(commentDocRef);
	});

	// Delete saves 
	savedPostSnapshot.forEach(savedPost => {
		// Get documents reference
		const savedPostDocRef = savedPost.ref;
		// Delete each document
		batch.delete(savedPostDocRef);
	});

	// Commit batch
	await batch.commit();
});

module.exports = {
	updateNumberOfUpvotesOfPost,
	updateNumberOfDownvotesOfPost,
	updatePostProfilePictureURL,
	updateNumberOfCommentsOfPost,
	deletePost
}
