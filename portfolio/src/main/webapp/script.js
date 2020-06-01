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


function getRandomMessage() {
    fetch('/data').then(response => response.text()).then((quote) => {
        document.getElementById('quote-container').innerText = quote;
    });
}

/**
 * Fetches stats from the servers and adds them to the DOM.
 */
function getComments() {	
  	fetch('/data').then(response => response.json()).then((comment) => {
    // stats is an object, not a string, so we have to
    // reference its fields to create HTML content

    console.log(comment.comments);
    const comentListElement = document.getElementById('comments-container');
	var i;
    comentListElement.innerHTML = '';
    
    for (i in comment.comments) {
        console.log(com);
        var com = comment.comments[i];

        comentListElement.appendChild(
            createListElement('Date: ' + com.date + ' Author: ' + com.author + ' Comment: ' + com.content));
    }
  });
}

/** Creates an <li> element containing text. */
function createListElement(text) {
    const liElement = document.createElement('li');
    liElement.innerText = text;
    return liElement;
}
