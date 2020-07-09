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

const GREETING_CHOICES =
    ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!', 'How are you doing?', 'How do you do?'];
const CAT_IMAGES = ["images/kitten-in-bed.jpg", "images/sleepy-kitten.jpg", "images/kitten-covers.jpg", 
                    "images/silver-tabby.jpg", "images/teddy-cat.jpg"];
const POST_ID = "0cb628857f3c4c77bf7f9a879a6ec21d";
const DEFAULT_COMMENT_LIMIT = 5;
const PLACEHOLDER_URL = "images/squishycat.jpeg";
const LOGGED_IN_STATUS = "logged in";

/**
 * Adds a random greeting to the page.
 */
addRandomGreeting = () => {
  // Pick a random greeting.
  const greeting = GREETING_CHOICES[Math.floor(Math.random() * GREETING_CHOICES.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
 * Adds a random cat picture to the page.
 */
addRandomCat = () => {
  // Pick a random greeting.
  const cat = CAT_IMAGES[Math.floor(Math.random() * CAT_IMAGES.length)];

  // Create img element.
  let catimg = document.createElement("img");
  catimg.src = cat;
  catimg.style.width = "500px";
  
  // Add it to the page.
  let catContainer = document.getElementById('cat-container');
  const prev = catContainer.children[0];
  if (prev !== undefined)  catContainer.replaceChild(catimg, prev);
  else catContainer.appendChild(catimg);
}

/**
 * Use Potion API to create page content for blog post 1 from a Notion document.
 */
getBlogPost = () => {
  fetch("https://potion-api.now.sh/html?id=" + POST_ID)
    .then(res => res.text())
    .then(text => { document.getElementById("post1").innerHTML = text; 
  });
}

/** 
 * Fetch comments from server and insert them on blog page.
 */
getBlogComments = (commentLimit) => {
  if (commentLimit === null) commentLimit = DEFAULT_COMMENT_LIMIT;
  fetch("/data?comment-limit=" + commentLimit).then(response => response.json()).then((comments) => {
    const commentsContainer = document.getElementById("submitted-comments-container");
    commentsContainer.innerHTML = '';
    comments.forEach(comment => commentsContainer.appendChild(createCommentElement(comment)));
  });
}

/** Creates an element that represents a comment. */
createCommentElement = (comment) => {
  const commentBlock = document.createElement('div');
  commentBlock.className = "comment";

  const nameElement = document.createElement('p');
  nameElement.innerText = comment.name;

  const commentInputElement = document.createElement('p');
  commentInputElement.innerText = comment.commentInput;

  const fileElement = document.createElement('img');
  fileElement.style.width = '50px';
  if (comment.fileUrl != null) {
    fileElement.src = comment.fileUrl;
  } else {
    fileElement.src = PLACEHOLDER_URL;
  }

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment);
    commentBlock.remove();
  });

  commentBlock.appendChild(nameElement);
  commentBlock.appendChild(commentInputElement);
  commentBlock.appendChild(fileElement);
  commentBlock.appendChild(document.createElement('br'));
  commentBlock.appendChild(deleteButtonElement);
  commentBlock.appendChild(document.createElement('hr'));
  return commentBlock;
}

/** Tells the server to delete the comment. */
deleteComment = async (comment) => {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  let response = await fetch('/delete-data', {method: 'POST', body: params});
  getBlogComments(window.localStorage.getItem("comment-limit"));
}

/** Fetch Blobstore URL and set it to HTML form. */
fetchBlobstoreUrlAndSetForm = () => {
  fetch("/blobstore-upload-url").then(response => response.text()).then((urlstring) => {
    const commentsForm = document.getElementById('comment-submission-form');
    commentsForm.action = urlstring;
  });
}
                                                                        
/** Fetch login status and display comments form or login link accordingly. */
getLoginStatus = () => {
  fetch("/login").then(response => response.text()).then((result) => {
    results = result.split("\n");
    const commentsSubmissionForm = document.getElementById("comment-submission-form");    
    const loginContainer = document.getElementById("login-link-container");
    const logoutContainer = document.getElementById("logout-link-container");
    
    if (results[0] == LOGGED_IN_STATUS) {
      commentsSubmissionForm.style.display = "inline";

      const logoutElement = document.getElementById("logout-link");
      logoutElement.href = results[1];
      
      loginContainer.style.display = "none";
      logoutContainer.style.display = "inline";
    } else {
      commentsSubmissionForm.style.display = "none";

      const loginElement = document.getElementById("login-link");
      loginElement.href = results[1];

      loginContainer.style.display = "inline";
      logoutContainer.style.display = "none";
    }
  });
}

/**
 * Fetch page data and set up HTML elements on load.
 */
window.onload = () => {
  getBlogPost();
  getBlogComments(window.localStorage.getItem("comment-limit"));
  fetchBlobstoreUrlAndSetForm();
  getLoginStatus();

  // Refresh comment limit value in local storage.
  let commentInputContainer = document.getElementById("comment-limit");
  commentInputContainer.onchange = () => {
    const newLimit = document.getElementById("comment-limit").value;
    window.localStorage.setItem("comment-limit", newLimit);
  }

  // Add event listeners to buttons
  let greetButton = document.getElementById("greeting-button");
  greetButton.addEventListener("click", addRandomGreeting);
  let catButton = document.getElementById("random-cat-button");
  catButton.addEventListener("click", addRandomCat);
}

