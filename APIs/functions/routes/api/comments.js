const express = require('express');
const admin = require('firebase-admin');
const validator = require('validator');//provides string functions
const helpers = require('../../helpers/helpers');

const db = admin.firestore();
const router = express.Router();

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

router.post('/', async (req, res) => {
	//request se data get karna hai
	// const commentInformation = {
	//	comment : '',
    //	email : '',
	//	parent : '',
	//	postId
	// }
	const commentInformation = req.body; //getting it from front end
	if (validator.default.isEmpty(commentInformation.comment)) {
		return res.status(400).json({
			error: "Please provide a comment"
		});
	}
    
    // Get users data 
	const user = await db.collection('users').doc(commentInformation.email).get();
    
    const comment = {
        comment: commentInformation.comment,
        postId: commentInformation.postId,
        email: commentInformation.email,
        parent: commentInformation.parent,
        numberOfUpvotes: 0,
        numberOfDownvotes: 0,
        numberOfReplies: 0,
        timeCreated: helpers.getCurrentDateAndTime(),
        username: user.data().username,
        userProfilePictureURL: user.data().imageURL
    }

    await db.collection('comments').add(comment);
    
	return res.status(200).json({
		success: "Comment added"
	});
});

router.post('/upvote', async (req, res) =>{
    // upvoteInformation = {
    //     email: '',
    //     commentId: ''
    // }
    const upvoteInformation = req.body;
     
    const upvoteDocument = await db.collection('commentUpvotes')
    .where('commentId', '==', upvoteInformation.commentId)
    .where('email', '==', upvoteInformation.email)
    .get();
    if(!upvoteDocument.empty){
        await db.collection('commentUpvotes').doc(upvoteDocument.docs[0].id).delete();
        return res.status(200).json({
            success: "Upvote Removed."
        });
    }
    const downvoteDocument = await db.collection('commentDownvotes')
    .where('commentId', '==', upvoteInformation.commentId)
    .where('email', '==', upvoteInformation.email)
    .get();
    if(!downvoteDocument.empty){
        await db.collection('commentDownvotes').doc(downvoteDocument.docs[0].id).delete();
    }

    await db.collection('commentUpvotes').add({
        email: upvoteInformation.email,
        commentId: upvoteInformation.commentId
    });
    return res.status(200).json({
        success: "Upvote Added."
    });
});

router.post('/downvote', async (req, res) => {
    // DownvoteInformation = {
    //     email: '',
    //     commentId: ''
    // }
    const downvoteInformation = req.body;

    const downvoteDocument = await db.collection('commentDownvotes')
    .where('email', '==', downvoteInformation.email)
    .where('commentId', '==', downvoteInformation.commentId)
    .get();
    if(!downvoteDocument.empty){
        await db.collection('commentDownvotes').doc(downvoteDocument.docs[0].id).delete();
        return res.status(200).json({
            success: "Downvote Removed."
        });
    }
    const upvoteDocument = await db.collection('commentUpvotes')
    .where('email', '==', downvoteInformation.email)
    .where('commentId', '==', downvoteInformation.commentId)
    .get();
    if(!upvoteDocument.empty){
        await db.collection('commentUpvotes').doc(upvoteDocument.docs[0].id).delete();
    }
    
    await db.collection('commentDownvotes').add({
        email: downvoteInformation.email,
        commentId: downvoteInformation.commentId
    });
    return res.status(200).json({
        success: "Downvote Added"
    });
});

router.post('/latest', async (req, res) => {
	// commentId will be used for pagination.
	// commentId is the id of the last fetched comment which will be used to get next 5 results
	// of the most upvoted comment each time request is made to get most upvoted comment.

	// Get commentId
	const commentId = req.body.commentId;

	// Write query for fetched 5 latest comments
	const query = db.collection('comments').orderBy('timeCreated', 'desc').limit(10);

	let commentsSnapshot = null;

	// Check if commentId is empty
	// If it is emtpy, fetch the first 5 latest comments
	if(validator.default.isEmpty(commentId)) {
		commentsSnapshot = await query.get();
	} else {
		// Get next 5 latest comments after the last fetched comment
		commentsSnapshot = await query.startAfter(commentId).get();
	}

	// Initialize an array for storing 5 or less comments that will be returned to user
	const comments = [];

	// Store fetched comments data
	commentsSnapshot.docs.forEach((comment) => {
		comments.push({
			commentData: comment.data(),
			commentId: comment.id
		});
	});

	// Return all the comments
	return res.status(200).json(comments);
});

router.post('/delete', async (req, res) => {
	// Get commentId and user's email
	const commentId = req.body.commentId;
    const email = req.body.email;

	const commentDocument = await db.collection('comments').doc(commentId).get();
	// Check if comment is created by this user
	if(commentDocument.data().email === email) {
		await db.collection('comments').doc(commentId).delete();
		return res.status(200).json({
			success:"Comment deleted"
		});
	}

	return res.status(400).json({
		error: "Couldn't delete comment"
	});
});

module.exports = router;