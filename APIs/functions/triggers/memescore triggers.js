const functions = require('firebase-functions');
const admin = require('firebase-admin');

const db = admin.firestore();

const IncrementMemeScoreOnPostUpvoteCreation = functions.firestore.document('postUpvotes/{postUpvotesId}').onCreate(async (snap, context) => {

    // Get the upvote
	const upvoteDocument = snap.data();
	// Get the postId from the upvote document
    const postId = upvoteDocument.postId;
    //Get post from postId
    const post = await db.collection('posts').doc(postId).get();
    // Get the user's email from the post
	const email = post.data().email;
	// Get the user
	const userSnapshot = await db.collection('users').doc(email).get();
	// Get the meme score of the user 
	let userScore = userSnapshot.data().userScore;
	userScore = userScore + 2;

	// Update the user score of the user
	await db.collection('users').doc(email).update({
		userScore: userScore
	});	
});

const DecrementMemeScoreOnPostUpvoteDeletion = functions.firestore.document('postUpvotes/{postUpvotesId}').onDelete(async (snap, context) => {

    // Get the upvote
	const upvoteDocument = snap.data();
	// Get the postId from the upvote document
    const postId = upvoteDocument.postId;
    //Get post from postId
	const post = await db.collection('posts').doc(postId).get();
	// Check if the post is deleted
	if(!post.exists) {
		return;
	}
    // Get the user's email from the post
	const email = post.data().email;
	// Get the user
	const userSnapshot = await db.collection('users').doc(email).get();
	// Get the meme score of the user 
	let userScore = userSnapshot.data().userScore;
	userScore = userScore - 2;

	// Update the user score of the user
	await db.collection('users').doc(email).update({
		userScore: userScore
	});	
});

const IncrementMemeScoreOnPostDownvoteDeletion = functions.firestore.document('postDownvotes/{postDownvotesId}').onDelete(async (snap, context) => {

    // Get the downvote
	const downvoteDocument = snap.data();
	// Get the postId from the downvote document
    const postId = downvoteDocument.postId;
    //Get post from postId
	const post = await db.collection('posts').doc(postId).get();
	// Check if the post is deleted
	if(!post.exists) {
		return;
	}
    // Get the user's email from the post
	const email = post.data().email;
	// Get the user
	const userSnapshot = await db.collection('users').doc(email).get();
	// Get the meme score of the user 
	let userScore = userSnapshot.data().userScore;
	userScore = userScore + 1;

	// Update the user score of the user
	await db.collection('users').doc(email).update({
		userScore: userScore
	});	
});

const DecrementMemeScoreOnPostDownvoteCreation = functions.firestore.document('postDownvotes/{postDownvotesId}').onCreate(async (snap, context) => {

    // Get the downvote
	const downvoteDocument = snap.data();
	// Get the postId from the downvote document
    const postId = downvoteDocument.postId;
    //Get post from postId
    const post = await db.collection('posts').doc(postId).get();
    // Get the user's email from the post
	const email = post.data().email;
	// Get the user
	const userSnapshot = await db.collection('users').doc(email).get();
	// Get the meme score of the user 
	let userScore = userSnapshot.data().userScore;
	userScore = userScore - 1;

	// Update the user score of the user
	await db.collection('users').doc(email).update({
		userScore: userScore
	});	
});

const IncrementMemeScoreOnPostCreation = functions.firestore.document('posts/{postId}').onCreate(async (snap, context) => {

    // Get the post document 
	const post = snap.data();
    // Get the user's email from the post
	const email = post.email;
	// Get the user
	const userSnapshot = await db.collection('users').doc(email).get();
	// Get the meme score of the user 
	let userScore = userSnapshot.data().userScore;
	userScore = userScore + 5;

	// Update the user score of the user
	await db.collection('users').doc(email).update({
		userScore: userScore
	});	
});

const DecrementMemeScoreOnPostDeletion = functions.firestore.document('posts/{postId}').onDelete(async (snap, context) => {

    // Get the post document 
	const post = snap.data();
    // Get the user's email from the post
	const email = post.email;
	// Get the user
	const userSnapshot = await db.collection('users').doc(email).get();
	// Get the meme score of the user 
	let userScore = userSnapshot.data().userScore;
	userScore = userScore - 5;

	// Update the user score of the user
	await db.collection('users').doc(email).update({
		userScore: userScore
	});	
});

const IncrementMemeScoreOnCommentCreation = functions.firestore.document('comments/{commentId}').onCreate(async (snap, context) => {

    // Get the comment document 
    const comment = snap.data();
    //Get the postId from comment document
    const postId = comment.postId;
    // Get the post from postId 
	const post = await db.collection('posts').doc(postId).get();
    // Get the user's email from the post
	const email = post.data().email;
	// Get the user
	const userSnapshot = await db.collection('users').doc(email).get();
	// Get the meme score of the user 
	let userScore = userSnapshot.data().userScore;
	userScore = userScore + 3;

	// Update the user score of the user
	await db.collection('users').doc(email).update({
		userScore: userScore
	});	
});

const DecrementMemeScoreOnCommentDeletion = functions.firestore.document('comments/{commentId}').onDelete(async (snap, context) => {

    // Get the comment document 
    const comment = snap.data();
    //Get the postId from comment document
    const postId = comment.postId;
    // Get the post from postId 
	const post = await db.collection('posts').doc(postId).get();
    // Get the user's email from the post
	const email = post.data().email;
	// Get the user
	const userSnapshot = await db.collection('users').doc(email).get();
	// Get the meme score of the user 
	let userScore = userSnapshot.data().userScore;
	userScore = userScore - 3;

	// Update the user score of the user
	await db.collection('users').doc(email).update({
		userScore: userScore
	});	
});

const updateMemeRank = functions.firestore.document('users/{userId}').onWrite(async (change, context)=> {
	if(!change.after.exists || !change.before.exists) {
		return;
	}

    // Check if meme score is not changed
	if (change.before.data().userScore === change.after.data().userScore) {
		return;
    }
    //get user document
    const user = change.after.data();
    //get user meme score
    const userScore = user.userScore;
    //get user meme rank
    let userRank = user.userRank;

    if(userScore < 0){
        userRank = 'Poor Memer';
    }
    else if(userScore >=0 && userScore <20){
        userRank = 'Beginner';
    }
    else if(userScore >=20 && userScore <100){
        userRank = 'Intermediate';
    }
    else if(userScore >=100 && userScore <350){
        userRank = 'Time Killer';
    }
    else if(userScore >=350 && userScore <500){
        userRank = 'Meme Youngling';
    }
    else if(userScore >=500 && userScore <1000){
        userRank = 'Meme Designer';
    }
    else if(userScore >=1000 && userScore <2000){
        userRank = 'Meme Master';
    }
    else if(userScore >=2000 && userScore <3500){
        userRank = 'Expert Memer';
    }
    else if(userScore >=3500 && userScore <5000){
        userRank = 'PHD in Memes';
    }
    else if(userScore >=5000 && userScore <7500){
        userRank = 'Meme Lord';
    }
    else if(userScore >=7500){
        userRank = 'Meme God';
    }
	
	if(change.after.data().userRank === userRank) {
		return;
	}

    // Update the user rank of the user
	await db.collection('users').doc(change.after.id).update({
		userRank: userRank
	});
});

module.exports = {
    IncrementMemeScoreOnPostUpvoteCreation,
    DecrementMemeScoreOnPostUpvoteDeletion,
    IncrementMemeScoreOnPostDownvoteDeletion,
    DecrementMemeScoreOnPostDownvoteCreation,
    IncrementMemeScoreOnPostCreation,
    DecrementMemeScoreOnPostDeletion,
    IncrementMemeScoreOnCommentCreation,
    DecrementMemeScoreOnCommentDeletion,
    updateMemeRank
}