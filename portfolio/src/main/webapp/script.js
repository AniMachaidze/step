// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


/**
 * Fetches comments from the servers and adds them to the DOM.
 */
function getComments() {
	const numberEl = document.getElementById("comments-number");
	const value = numberEl.options[numberEl.selectedIndex].value;
	const pageEl = document.getElementById("page");
	const page = pageEl.value;

	fetch('/data' + '?comments-number=' + value + '&page=' + page).
	then(response => response.json())
		.then((comments) => {
			const commentListElement = document
				.getElementById('comments-container');

			commentListElement.innerHTML = '';
			comments.forEach((comment) => {
				let date = new Date(comment.date);
				commentListElement.appendChild(
					createListElement(comment.userName, comment.userEmail,
						date.getMonth() + '/' + date.getDate() + '/' +
						date.getFullYear(), comment.content, comment.emotion,
						comment.isAbleToDelete, comment.id, comment.imageUrl));
			})

		});
}

/**
 * Fetches delete-data, deletes all commennts
 */
function deleteComments(id) {
	const pageEl = document.getElementById("page");
	const page = pageEl.value;
	const queryStr = 'page=' + page + '&' + 'id=' + id;
	fetch('/delete-data?' + queryStr, {
		method: 'POST',
	});

	if (!id) {
		document.getElementById('comments-container').innerHTML = '';
	} else {
		location.reload();
	}

}

/** 
 * Creates an <li> element containing author, date, comment and emotion emoji.
 */
function createListElement(userName, userEmail, date, text,
	emotion, isAbleToDelete, id, imageUrl) {
	const liElement = document.createElement('li');
	const containerDiv = document.createElement('div');
	const emotionEl = document.createElement('div');

	switch (emotion) {
		case 'happy':
			emotionEl.innerHTML = '&#128522; ';
			break;
		case 'laughing':
			emotionEl.innerHTML = '&#128516; ';
			break;
		case 'surprised':
			emotionEl.innerHTML = '&#128562; ';
			break;
		case 'sad':
			emotionEl.innerHTML = '&#128532; ';
			break;
		default:
			emotionEl.innerHTML = '&#128522; ';
	}

	containerDiv.className = 'comment-container';
	containerDiv.appendChild(emotionEl);
	const userNameText = document.createElement('b');
	const userEmailText = document.createElement('i');
	userNameText.innerText = userName + ' ';
	userEmailText.innerText = userEmail;
	emotionEl.appendChild(userNameText);
	emotionEl.appendChild(userEmailText);

	if (isAbleToDelete === true) {
		const deleteButton = document.createElement('button');
		deleteButton.innerHTML = '&#10005;';
		deleteButton.className = "delete-button";
		deleteButton.onclick = function () {
			deleteComments(id);
		}
		emotionEl.appendChild(deleteButton);
	}

	const dateNode = document.createElement('i');
	dateNode.innerText = date;
	containerDiv.appendChild(dateNode);
	liElement.appendChild(containerDiv);
	const textNode = document.createTextNode(text);
	liElement.appendChild(textNode);
    // TODO: Add style to image element
	const imageUrlEl = document.createElement('img');
	imageUrlEl.src = imageUrl;
	liElement.appendChild(imageUrlEl);

	return liElement;
}

/** 
 * Checks if the user is logged in and shows comment submission form.
 */
async function checkLogin() {
	var page = window.location.pathname;
	const queryStr = 'page=' + page;
	fetch('/user?' + queryStr).then(response => response.json())
		.then((user) => {
			if (user.loggedin === 'true') {
				const commentsForm = document.getElementById('comments-form');
				commentsForm.style.display = 'block';
				const userLoginForm = document.getElementById('user-login');
				userLoginForm.style.display = 'none';

				const userLogoutForm = document.getElementById('user-logout');
				userLogoutForm.style.display = 'block';
				const userLogoutButton = document.getElementById('logout-button');
				userLogoutButton.href = user.logoutUrl;
			}
		});
}

/** 
 * Redirects to the login page when the button is clicked
 */
function login() {
	var page = window.location.pathname;
	const queryStr = 'page=' + page;
	fetch('/user?' + queryStr).
	then(response => response.json())
		.then((user) => {
			if (user.loggedin === 'false') {
				window.location.href = user.loginUrl;
			}
		});
}

/** 
 * Calls functions when page is loaded.
 */
function start() {
	checkLogin();
	getComments();
	blobUpload();
}

function blobUpload() {
	fetch('/blobstore-upload-url')
		.then((response) => {
			return response.text();
		})
		.then((imageUploadUrl) => {
			const commentForm = document.getElementById('form');
			commentForm.action = imageUploadUrl;
		});
}
