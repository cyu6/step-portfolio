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

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment);
    commentBlock.remove();
  });

  commentBlock.appendChild(nameElement);
  commentBlock.appendChild(commentInputElement);
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

/**
 * Fetch page data and set up HTML elements on load.
 */
window.onload = () => {
  getBlogPost();
  
  let commentLimit = window.localStorage.getItem("comment-limit");
  if (!commentLimit) commentLimit = 5;
  getBlogComments(commentLimit);

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

/**
 * Create a hard-coded chart about plants and add it to the random.html page.
 */
drawPlantChart = () => {
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'Plant');
  data.addColumn('number', 'Amount');
  data.addRows([
    ['Clover', 3],
    ['Lavender', 9],
    ['Poppies', 6],
    ['Basil', 2]
  ]);

  const options = {
    'title': 'Plants in my garden', 
    'width': 400, 
    'height': 300
  };

  const chart = new google.visualization.PieChart(
    document.getElementById('plant-chart-container')
  );
  chart.draw(data, options);
}

/**
 * Fetch data about COVID-19 and use it to create a chart.
 */
drawCovidChart = () => {
  fetch('/covid-data').then(response => response.json())
  .then((covidTotals) => {
    const data = new google.visualization.DataTable();
    data.addColumn('date', 'Date');
    data.addColumn('number', 'Total Cases');
    data.addColumn('number', 'New Cases');
    Object.keys(covidTotals).forEach((date) => {
      data.addRow([new Date(date), covidTotals[date][0], covidTotals[date][1]]);
    });

    const options = {
      'title': 'COVID-19 total and new cases worldwide', 
      'width': 600, 
      'height': 500
    };

    const chart = new google.visualization.AreaChart(
        document.getElementById('covid-chart-container'));
    chart.draw(data, options);
  });
}

/**
 * Fetch data of submitted comments and create a line chart.
 */
drawCommentsChart = () => {
  fetch('/comment-data')
  .then(response => response.json()).then((totalDailyComments) => {
    const data = new google.visualization.DataTable();
    data.addColumn('datetime', 'Date');
    data.addColumn('number', 'Amount');
    Object.keys(totalDailyComments).forEach((date) => {
      data.addRow([new Date(date), totalDailyComments[date]]);
    });

    const options = {
      'title': 'Number of submitted comments', 
      'width': 400, 
      'height': 300
    };

    const chart = new google.visualization.LineChart(
      document.getElementById('comment-chart-container')
    );
    
    chart.draw(data, options);
  });
}

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawPlantChart);
google.charts.setOnLoadCallback(drawCovidChart);
google.charts.setOnLoadCallback(drawCommentsChart);

