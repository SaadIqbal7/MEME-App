const express = require('express');
const admin = require('firebase-admin');

const db = admin.firestore();
const router = express.Router();

router.get('/', async (req, res) => {
	// Get templates
	const templatesSnapshot = await db.collection('templates').get();
	const templates = [];
	templatesSnapshot.forEach(template => {
		templates.push(template.data());
	});

	res.status(200).send(templates);
});

module.exports = router;
