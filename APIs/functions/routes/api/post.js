const express = require('express');
const admin = require('firebase-admin');
const validator = require('validator');
const UUID = require('uuid-v4');
const path = require('path');
const fs = require('fs');
const os = require('os');
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
	// Initialize postInfo
	// const postInfo = {
	// 	email: '',
	// 	description: '',
	// 	tags: [tag1, tag2, tag3],
	//	category: ''
	// 	image: '', // base64 image
	// }
	let postInfo = req.body;

	// Initalize errors that will be sent back to the user in case of any
	const errPostInfo = {
		errEmail : '',
		errDescription: '',
		errTags: '',
		errImage: '',
		errCategory: ''
	}

	// Check if email is provided
	if(validator.isEmpty(postInfo.email)) {
		errPostInfo.errEmail = 'Please provide an E-mail address';
	}

	// Check if description is provided
	if(validator.isEmpty(postInfo.description)) {
		errPostInfo.errDescription = 'Please enter a description for post';
	} else {
		// Remove white spaces
		postInfo.description = postInfo.description.trim();
	}

	// Check if at least one tag is provided
	if(postInfo.tags.length === 0) {
		errPostInfo.errTags = 'Post requires at least one tag';
	} else {
		for(let i = 0; i < postInfo.tags.length; i++){
			// Remove white spaces from tags
			postInfo.tags[i] = postInfo.tags[i].trim();
		}
	}

	// Check if image is provided
	if(validator.isEmpty(postInfo.image)) {
		errPostInfo.errImage = 'Post should have an image';
	}

	// Check if category is provided
	if(validator.default.equals(postInfo.category, 'Select category')) {
		errPostInfo.errCategory = 'Please choose a category';
	}

	// Check if error exists
	if(!validator.isEmpty(errPostInfo.errDescription)
	|| !validator.isEmpty(errPostInfo.errEmail)
	|| !validator.isEmpty(errPostInfo.errImage)
	|| !validator.isEmpty(errPostInfo.errTags)
	|| !validator.isEmpty(errPostInfo.errCategory)) {
		return res.status(400).json(errPostInfo);
	}

	// Generate a random ID for image
	const uuid = UUID();

	// Generate name for the image
	const imageName = postInfo.email +'_post' + uuid;

	// Split the base64 Image 2 parts
	const base64String = postInfo.image.split(',');

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

	try {
		// Upload image in "Profile pictures" folder on cloud storage
		const data = await bucket.upload(tempFilePath, {
			destination: 'posts/' + postInfo.email + "/" + path.basename(tempFilePath),
			uploadType: 'media',
			metadata: {
				contentType: 'image/' + extension,
				metadata: {
					firebaseStorageDownloadTokens: uuid
				}
			}
		});

		const file = data[0];
		// Generate image url
		const imageURL = "https://firebasestorage.googleapis.com/v0/b/" + bucket.name + "/o/" + encodeURIComponent(file.name) + "?alt=media&token=" + uuid;

		// Get users data 
		const user = await db.collection('users').doc(postInfo.email).get();

		// Create posts object to be stored in firestore
		const post = {
			email: postInfo.email,
			description: postInfo.description,
			tags: postInfo.tags,
			imageURL: imageURL,
			category: postInfo.category,
			timeCreated: helpers.getCurrentDateAndTime(),
			numberOfUpvotes: 0,
			numberOfDownvotes: 0,
			numberOfComments: 0,
			username: user.data().username,
			userProfilePictureURL: user.data().imageURL
		}

		// Add post
		const postDocumentRef = await db.collection('posts').add(post);
		// Store postId in the document as a field to enhance document searching
		await db.collection('posts').doc(postDocumentRef.id).update({
			id: postDocumentRef.id
		});

		return res.status(200).json({
			success: 'Post added'
		});

	} catch(e) {
		return res.status(400).json({
			error: "Could not upload image"
		});
	}
});

router.post('/hot', async (req, res) => {
	// postId will be used for pagination.
	// postId is the id of the last fetched post which will be used to get next 5 results
	// of the most upvoted post each time request is made to get most upvoted post.

	// Get postId
	const postId = req.body.postId;

	// Write query for fetching 5 most upvoted post
	const query = db.collection('posts')
	.orderBy('numberOfUpvotes', 'desc')
	.orderBy('timeCreated', 'desc')
	.limit(5);

	let postsSnapshot = null;

	// If postId is empty, return first 5 most up voted posts
	if(validator.default.isEmpty(postId)) {
		postsSnapshot = await query.get();
	} else {
		// Get the last fetched post document
		const postDoc = await db.collection('posts').doc(postId).get();

		// Get next 5 most upvoted posts after the last fetched post
		postsSnapshot = await query.startAfter(postDoc).get();
	}

	// Initialize an array for storing 5 or less posts that will be returned to user
	const posts = [];

	// Store fetched posts data
	postsSnapshot.docs.forEach((post) => {
		posts.push({
			postData: post.data(),
			postId: post.id
		});
	});

	// Return all the posts
	return res.status(200).json(posts);
});

router.post('/latest', async (req, res) => {
	// postId will be used for pagination.
	// postId is the id of the last fetched post which will be used to get next 5 results
	// of the most upvoted post each time request is made to get most upvoted post.

	// Get postId
	const postId = req.body.postId;

	// Write query for fetched 5 latest posts
	const query = db.collection('posts').orderBy('timeCreated', 'desc').limit(5);

	let postsSnapshot = null;

	// Check if postId is empty
	// If it is emtpy, fetch the first 5 latest posts
	if(validator.default.isEmpty(postId)) {
		postsSnapshot = await query.get();
	} else {
		// Get the last fetched post document
		const postDoc = await db.collection('posts').doc(postId).get();

		// Get next 5 latest posts after the last fetched post
		postsSnapshot = await query.startAfter(postDoc).get();
	}

	// Initialize an array for storing 5 or less posts that will be returned to user
	const posts = [];

	// Store fetched posts data
	postsSnapshot.docs.forEach((post) => {
		posts.push({
			postData: post.data(),
			postId: post.id
		});
	});

	// Return all the posts
	return res.status(200).json(posts);
});

router.post('/hot/category', async (req, res) => {
	// postId will be used for pagination.
	// postId is the id of the last fetched post which will be used to get next 5 results
	// of the most upvoted post each time request is made to get most upvoted post.

	// Get postId and category
	const postId = req.body.postId;
	const category = req.body.category;

	// Write query for fetching 5 most upvoted post
	const query = db.collection('posts')
	.where('category', '==', category)
	.orderBy('numberOfUpvotes', 'desc')
	.orderBy('timeCreated', 'desc')
	.limit(5);

	let postsSnapshot = null;

	// If postId is empty, return first 5 most up voted posts
	if(validator.default.isEmpty(postId)) {
		postsSnapshot = await query.get();
	} else {
		// Get the last fetched post document
		const postDoc = await db.collection('posts').doc(postId).get();

		// Get next 5 most upvoted posts after the last fetched post
		postsSnapshot = await query.startAfter(postDoc).get();
	}

	// Initialize an array for storing 5 or less posts that will be returned to user
	const posts = [];

	// Store fetched posts data
	postsSnapshot.docs.forEach((post) => {
		posts.push({
			postData: post.data(),
			postId: post.id
		});
	});

	// Return all the posts
	return res.status(200).json(posts);
});

router.post('/hot/tag', async (req, res) => {
	// postId will be used for pagination.
	// postId is the id of the last fetched post which will be used to get next 5 results
	// of the most upvoted post each time request is made to get most upvoted post.

	// Get postId and tag
	const postId = req.body.postId;
	const tag = req.body.tag;

	// Write query for fetching 5 most upvoted post
	const query = db.collection('posts')
	.where('tags', 'array-contains', tag)
	.orderBy('numberOfUpvotes', 'desc')
	.orderBy('timeCreated', 'desc')
	.limit(5);

	let postsSnapshot = null;

	// If postId is empty, return first 5 most up voted posts
	if(validator.default.isEmpty(postId)) {
		postsSnapshot = await query.get();
	} else {
		// Get the last fetched post document
		const postDoc = await db.collection('posts').doc(postId).get();

		// Get next 5 most upvoted posts after the last fetched post
		postsSnapshot = await query.startAfter(postDoc).get();
	}

	// Initialize an array for storing 5 or less posts that will be returned to user
	const posts = [];

	// Store fetched posts data
	postsSnapshot.docs.forEach((post) => {
		posts.push({
			postData: post.data(),
			postId: post.id
		});
	});

	// Return all the posts
	return res.status(200).json(posts);
});

router.post('/latest/category', async (req, res) => {
	// postId will be used for pagination.
	// postId is the id of the last fetched post which will be used to get next 5 results
	// of the most upvoted post each time request is made to get most upvoted post.

	// Get postId and category
	const postId = req.body.postId;
	const category = req.body.category;

	// Write query for fetching 5 most upvoted post
	const query = db.collection('posts')
	.where('category', '==', category)
	.orderBy('timeCreated', 'desc')
	.limit(5);

	let postsSnapshot = null;

	// If postId is empty, return first 5 most up voted posts
	if(validator.default.isEmpty(postId)) {
		postsSnapshot = await query.get();
	} else {
		// Get the last fetched post document
		const postDoc = await db.collection('posts').doc(postId).get();

		// Get next 5 most upvoted posts after the last fetched post
		postsSnapshot = await query.startAfter(postDoc).get();
	}

	// Initialize an array for storing 5 or less posts that will be returned to user
	const posts = [];

	// Store fetched posts data
	postsSnapshot.docs.forEach((post) => {
		posts.push({
			postData: post.data(),
			postId: post.id
		});
	});

	// Return all the posts
	return res.status(200).json(posts);
});

router.post('/latest/tag', async (req, res) => {
	// postId will be used for pagination.
	// postId is the id of the last fetched post which will be used to get next 5 results
	// of the most upvoted post each time request is made to get most upvoted post.

	// Get postId and tag
	const postId = req.body.postId;
	const tag = req.body.tag;

	// Write query for fetching 5 most upvoted post
	const query = db.collection('posts')
	.where('tags', 'array-contains', tag)
	.orderBy('timeCreated', 'desc')
	.limit(5);

	let postsSnapshot = null;

	// If postId is empty, return first 5 most up voted posts
	if(validator.default.isEmpty(postId)) {
		postsSnapshot = await query.get();
	} else {
		// Get the last fetched post document
		const postDoc = await db.collection('posts').doc(postId).get();

		// Get next 5 most upvoted posts after the last fetched post
		postsSnapshot = await query.startAfter(postDoc).get();
	}

	// Initialize an array for storing 5 or less posts that will be returned to user
	const posts = [];

	// Store fetched posts data
	postsSnapshot.docs.forEach((post) => {
		posts.push({
			postData: post.data(),
			postId: post.id
		});
	});

	// Return all the posts
	return res.status(200).json(posts);
});

router.post('/email', async (req, res) => {
	// Get posts of a user (in order of latest posts first)

	// Get email
	const email = req.body.email;
	// Get timeCreated for pagination
	const postId = req.body.postId;

	// Check if the email is valid
	if(!validator.default.isEmail(email)) {
		return res.status(400).json({
			error: 'Invalid E-mail address'
		});
	}	

	// Write query for fetching 5 latest posts for the specified user
	const query = db.collection('posts')
	.where('email', '==', email)
	.orderBy('timeCreated','desc')
	.limit(5);

	let postsSnapshot = null;

	// Check if postId is empty
	// If it is emtpy, fetch the first 5 latest posts
	if(validator.default.isEmpty(postId)) {
		postsSnapshot = await query.get();
	} else {
		// Get the last fetched post document
		const postDoc = await db.collection('posts').doc(postId).get();

		// Get next 5 latest posts after the last fetched post
		postsSnapshot = await query.startAfter(postDoc).get();
	}

	// Initialize an array for storing 5 or less posts that will be returned to user
	const posts = [];

	// Store fetched posts data
	postsSnapshot.docs.forEach((post) => {
		posts.push({
			postData: post.data(),
			postId: post.id
		});
	});

	// Return all the posts
	return res.status(200).json(posts);
});

router.post('/upvoted', async (req, res) => {
	// Get upvoted posts of a user (in order of latest posts first)

	// Get email
	const email = req.body.email;
	// Get timeCreated for pagination
	const postId = req.body.postId;

	// Check if the email is valid
	if(!validator.default.isEmail(email)) {
		return res.status(400).json({
			error: 'Invalid E-mail address'
		});
	}

	// Get the postId of the posts upvoted by the user
	const upvotedPostsSnapshot = await db.collection('postUpvotes').where('email', '==', email).get();	
	const postIds = [];
	upvotedPostsSnapshot.forEach(post => {
		postIds.push(post.data().postId);
	});

	const query = db.collection('posts')
	.where('id', 'in', postIds)
	.orderBy('timeCreated','desc')
	.limit(5);

	let postsSnapshot = null;

	// Check if postId is empty
	// If it is emtpy, fetch the first 5 latest posts
	if(validator.default.isEmpty(postId)) {
		postsSnapshot = await query.get();
	} else {
		// Get the last fetched post document
		const postDoc = await db.collection('posts').doc(postId).get();

		// Get next 5 latest posts after the last fetched post
		postsSnapshot = await query.startAfter(postDoc).get();
	}

	// Initialize an array for storing 5 or less posts that will be returned to user
	const posts = [];

	// Store fetched posts data
	postsSnapshot.docs.forEach((post) => {
		posts.push({
			postData: post.data(),
			postId: post.id
		});
	});

	// Return all the posts
	return res.status(200).json(posts);
});

router.post('/saved', async (req, res) => {
	// Get saved posts of a user (in order of latest posts first)

	// Get email
	const email = req.body.email;
	// Get timeCreated for pagination
	const postId = req.body.postId;

	// Check if the email is valid
	if(!validator.default.isEmail(email)) {
		return res.status(400).json({
			error: 'Invalid E-mail address'
		});
	}

	// Get the postId of the posts saved by the user
	const savedPostsSnapshot = await db.collection('savedPosts').where('email', '==', email).get();	
	const postIds = [];
	savedPostsSnapshot.forEach(post => {
		postIds.push(post.data().postId);
	});

	const query = db.collection('posts')
	.where('id', 'in', postIds)
	.orderBy('timeCreated','desc')
	.limit(5);

	let postsSnapshot = null;

	// Check if postId is empty
	// If it is emtpy, fetch the first 5 latest posts
	if(validator.default.isEmpty(postId)) {
		postsSnapshot = await query.get();
	} else {
		// Get the last fetched post document
		const postDoc = await db.collection('posts').doc(postId).get();

		// Get next 5 latest posts after the last fetched post
		postsSnapshot = await query.startAfter(postDoc).get();
	}

	// Initialize an array for storing 5 or less posts that will be returned to user
	const posts = [];

	// Store fetched posts data
	postsSnapshot.docs.forEach((post) => {
		posts.push({
			postData: post.data(),
			postId: post.id
		});
	});

	// Return all the posts
	return res.status(200).json(posts);
});

router.post('/upvote', async (req, res) => {
	// initialize upvote information
	// const upvoteInfo = {
	// 	postId: '',
	// 	email: ''
	// }
	const upvoteInfo = req.body;

	// Firstly check if the post is already upvoted by current user
	const upvoteDocument = await db.collection('postUpvotes')
	.where('postId', '==', upvoteInfo.postId)
	.where('email', '==', upvoteInfo.email)
	.get();

	if(!upvoteDocument.empty) {
		// Removed the upvote
		await db.collection('postUpvotes').doc(upvoteDocument.docs[0].id).delete();
		return res.status(200).json({
			success: "Upvote removed"
		});
	}

	// Check if the post is already downvoted
	// Remove downvote if it already is downvoted
	const downvoteDocument = await db.collection('postDownvotes')
	.where('postId', '==', upvoteInfo.postId)
	.where('email', '==', upvoteInfo.email)
	.get();
	
	if(!downvoteDocument.empty) {
		// Removed the downvote
		await db.collection('postDownvotes').doc(downvoteDocument.docs[0].id).delete();
	}

	// If upvote does not already exist, upvote the post
	await db.collection('postUpvotes').add({
		postId: upvoteInfo.postId,
		email: upvoteInfo.email
	});

	return res.status(200).json({
		success: "Upvote added"
	});
});

router.post('/downvote', async (req, res) => {
	// initialize downvote information
	// const downvoteInfo = {
	// 	postId: '',
	// 	email: ''
	// }
	const downvoteInfo = req.body;

	// Firstly check if the post is already downvote by current user
	const downvoteDocument = await db.collection('postDownvotes')
	.where('postId', '==', downvoteInfo.postId)
	.where('email', '==', downvoteInfo.email)
	.get();

	if(!downvoteDocument.empty) {
		// Removed the downvote
		await db.collection('postDownvotes').doc(downvoteDocument.docs[0].id).delete();
		return res.status(200).json({
			success: "Downvote removed"
		});
	}

	// Check if the post is already upvoted
	// Remove upvote if it already is upvoted
	const upvoteDocument = await db.collection('postUpvotes')
	.where('postId', '==', downvoteInfo.postId)
	.where('email', '==', downvoteInfo.email)
	.get();
	
	if(!upvoteDocument.empty) {
		// Removed the upvote
		await db.collection('postUpvotes').doc(upvoteDocument.docs[0].id).delete();
	}

	// If downvote does not already exist, downvote the post
	await db.collection('postDownvotes').add({
		postId: downvoteInfo.postId,
		email: downvoteInfo.email
	});

	return res.status(200).json({
		success: "Downvote added"
	});
});

router.post('/save', async (req, res) => {
	// Get postId and user's email
	const postId = req.body.postId;
	const email = req.body.email;

	// Check if the post is already saved by the user
	const savedPostSnapshot = await db.collection('savedPosts')
	.where('email', '==', email)
	.where('postId', '==', postId)
	.get();

	// If document exists, unsave the post
	if(!savedPostSnapshot.empty) {
		await db.collection('savedPosts').doc(savedPostSnapshot.docs[0].id).delete();
		return res.status(200).json({
			success: "Post unsaved"
		});
	}

	// Save post
	await db.collection('savedPosts').add({
		postId: postId,
		email: email
	});

	return res.status(200).json({
		success: "Post saved"
	});
});

router.post('/delete', async (req, res) => {
	// Get postId and user's email
	const postId = req.body.postId;
	const email = req.body.email;

	// Check if post is created by this user
	const postDocument = await db.collection('posts').doc(postId).get();

	if(postDocument.data().email === email) {
		await db.collection('posts').doc(postId).delete();
		return res.status(200).json({
			success:"Post deleted"
		});
	}

	return res.status(400).json({
		error: "Couldn't delete post"
	});
});

module.exports = router;
