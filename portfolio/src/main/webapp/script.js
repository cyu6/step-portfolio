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
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!', 'How are you doing?', 'How do you do?'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
 * Adds a random cat picture to the page.
 */
function addRandomCat() {
  const cats =
      ["images/kitten-in-bed.jpg", "images/sleepy-kitten.jpg"];

  // Pick a random greeting.
  const cat = cats[Math.floor(Math.random() * cats.length)];

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

const postId1 = "0cb628857f3c4c77bf7f9a879a6ec21d";
fetch("https://potion-api.now.sh/html?id=" + postId1)
    .then(res => res.text())
    .then(text => { document.getElementById("post1").innerHTML = text; 
});
