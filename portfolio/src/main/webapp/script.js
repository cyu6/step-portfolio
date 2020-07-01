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
const CAT_IMAGES = ["images/kitten-in-bed.jpg", "images/sleepy-kitten.jpg"];
const POST_ID = "0cb628857f3c4c77bf7f9a879a6ec21d";

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

/*
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
getBlogComments = () => {
  fetch("/data").then(response => response.json()).then((commentParts) => {
    const commentsContainer = document.getElementById("submitted-comments-container");
    commentsContainer.innerHTML = '';
    commentParts.forEach(element => 
      commentsContainer.appendChild(createParagraphElement(element))
    );
  });
}

/*
 * Fetch page data and set up HTML elements on load.
 */
window.onload = () => {
  getBlogPost();
  getBlogComments();
}

// Creates a <p> element containing text.
createParagraphElement = (text) => {
  const pElement = document.createElement('p');
  pElement.innerText = text;
  return pElement;
}
