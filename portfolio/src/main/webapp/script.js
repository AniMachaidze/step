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

	fetch('/data' + '?comments-number=' + value + '&page=' + page).then(response => response.json())
		.then((comments) => {
			const commentListElement = document.getElementById('comments-container');

			commentListElement.innerHTML = '';
			comments.forEach((comment) => {
				let date = new Date(comment.date);
				commentListElement.appendChild(
					createListElement(comment.author, 
						date.getMonth() + '/' + date.getDate() + '/' +
						date.getFullYear(), comment.content, comment.emotion));
			})
	    });
}

/**
 * Fetches delete-data, deletes all commennts
 */
function deleteComments() {
	const pageEl = document.getElementById("page");
	const page = pageEl.value;
	fetch('/delete-data?page=' + page, {
		method: 'POST'
	});

	document.getElementById('comments-container').innerHTML = '';
}

/** Creates an <li> element containing text. */
function createListElement(author, date, text, emotion) {
	const liElement = document.createElement('li');

    const emotionEl = document.createElement('div');
    switch(emotion) {
    case 'happy':
        emotionEl.innerHTML = '&#128522; ';
        break;
    case 'laughting':
        emotionEl.innerHTML =  '&#128516; ';
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


    const bold = document.createElement('b');
    bold.innerText = author;
    emotionEl.appendChild(bold);
    const linebreak = document.createElement('br');
    liElement.appendChild(emotionEl);
    liElement.appendChild(linebreak);
    const dateNode = document.createElement('i');
    dateNode.innerText = date;
    liElement.appendChild(dateNode);
    liElement.appendChild(linebreak);
    const textNode = document.createTextNode(text);
    liElement.appendChild(textNode);

	return liElement;
}
