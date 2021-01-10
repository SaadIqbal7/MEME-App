const express = require('express');
const cors = require('cors');
const functions = require('firebase-functions');
const admin = require('firebase-admin');
const serviceAccount = require('./service-account.json');

// Initialize admin SDK
// admin.initializeApp();

admin.initializeApp({
	credential: admin.credential.cert(serviceAccount),
	databaseURL: "YOUR DATABASE URL",
	storageBucket: "YOUR STORAGE BUCKET URL"
});

// Initialize express app
const app = express();

// Initialize cors Middleware
app.use(cors());

// Initialize body parser Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: false }));

app.use('/api/register', require('./routes/api/register'));
app.use('/api/login', require('./routes/api/login'));
app.use('/api/user', require('./routes/api/user'));
app.use('/api/conversations', require('./routes/api/conversations'));
app.use('/api/message', require('./routes/api/message'));
app.use('/api/post', require('./routes/api/post'));
app.use('/api/comment', require('./routes/api/comments'));
app.use('/api/templates', require('./routes/api/templates'));

exports.webApi = functions.https.onRequest(app);

const userTriggers = require('./triggers/user triggers');
const postTriggers = require('./triggers/post triggers');
const commentTriggers = require('./triggers/comment triggers');
const memeScoreTriggers = require('./triggers/memescore triggers');

exports.updateNumberOfPosts = userTriggers.updateNumberOfPosts;
exports.updateNumberOfSavedPosts = userTriggers.updateNumberOfSavedPosts;
exports.updateNumberOfUpvotedPosts = userTriggers.updateNumberOfUpvotedPosts;
exports.updateNumberOfUpvotesOfPost = postTriggers.updateNumberOfUpvotesOfPost;
exports.updateNumberOfDownvotesOfPost = postTriggers.updateNumberOfDownvotesOfPost;
exports.updatePostProfilePictureURL = postTriggers.updatePostProfilePictureURL;
exports.updateNumberOfCommentsOfPost = postTriggers.updateNumberOfCommentsOfPost
exports.deletePost = postTriggers.deletePost;
exports.updateCommentProfilePictureURL = commentTriggers.updateCommentProfilePictureURL;
exports.updateNumberOfUpvotesOfComment = commentTriggers.updateNumberOfUpvotesOfComment;
exports.updateNumberOfDownvotesOfComment = commentTriggers.updateNumberOfDownvotesOfComment;
exports.incrementNumberOfReplies = commentTriggers.incrementNumberOfReplies;
exports.deleteComment = commentTriggers.deleteComment;
exports.IncrementMemeScoreOnPostUpvoteCreation = memeScoreTriggers.IncrementMemeScoreOnPostUpvoteCreation;
exports.DecrementMemeScoreOnPostUpvoteDeletion = memeScoreTriggers.DecrementMemeScoreOnPostUpvoteDeletion;
exports.IncrementMemeScoreOnPostDownvoteDeletion = memeScoreTriggers.IncrementMemeScoreOnPostDownvoteDeletion;
exports.DecrementMemeScoreOnPostDownvoteCreation = memeScoreTriggers.DecrementMemeScoreOnPostDownvoteCreation;
exports.IncrementMemeScoreOnPostCreation = memeScoreTriggers.IncrementMemeScoreOnPostCreation;
exports.DecrementMemeScoreOnPostDeletion = memeScoreTriggers.DecrementMemeScoreOnPostDeletion;
exports.IncrementMemeScoreOnCommentCreation = memeScoreTriggers.IncrementMemeScoreOnCommentCreation;
exports.DecrementMemeScoreOnCommentDeletion = memeScoreTriggers.DecrementMemeScoreOnCommentDeletion;
exports.updateMemeRank = memeScoreTriggers.updateMemeRank;
