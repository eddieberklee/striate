const firebase_tools = require('firebase-tools');
const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

exports.recursiveDelete = functions
    .runWith({
        timeoutSeconds: 540,
        memory: '2GB'
    })
    .https.onCall((data, context) => {
        const path = data.path;
        console.log(
            `User ${context.auth.token.email} has requested to delete path ${path}`
        );

        if (path.split('/')[2] !== context.auth.token.email) {
            console.log('Hitting error for deleting someone elses data: ' + path.split('/')[2] + ' ' + context.auth.token.email);
            throw new functions.https.HttpsError(
                'permission-denied',
                'You can only delete your own habits!'
            );
        }

        // Run a recursive delete on the given document or collection path.
        // The 'token' must be set in the functions config, and can be generated
        // at the command line by running 'firebase login:ci'.
        return firebase_tools.firestore
            .delete(path, {
                project: process.env.GCLOUD_PROJECT,
                recursive: true,
                yes: true,
                token: functions.config().fb.token
            })
            .then(() => {
                return {
                    path: path 
                };
            })
            .catch(error => {
                response.status(403).send(error);
            });
    })