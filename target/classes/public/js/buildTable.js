var movieData;
const xhttp = new XMLHttpRequest();
xhttp.onload = function() {
    console.log(this.status);
    console.log(this.responseText);
    movieData = JSON.parse(this.responseText);

    document.title = movieData.Title;

     // Get the table
    var table = document.getElementById("movieTable");

     // Add rows to the table
    for (var property in movieData) {
         if (movieData.hasOwnProperty(property)) {
            addRow(table, property, movieData[property]);
         }
    }
}
xhttp.open("GET", "/public/movieData");
xhttp.send();
  // Function to add a row
function addRow(table, key, value) {
      var row = table.insertRow();
      var cell1 = row.insertCell(0);
      var cell2 = row.insertCell(1);
      cell1.innerHTML = key;
 // case of "Ratings"
      if (key === "Ratings" && Array.isArray(value)) {
         var ratingsHTML = "<ul>";
         value.forEach(function(rating) {
             ratingsHTML += "<li>" + rating.Source + ": " + rating.Value + "</li>";
         });
         ratingsHTML += "</ul>";
         cell2.innerHTML = ratingsHTML;
      } else if (key === "Poster" && !(value === "N/A")) {
         var posterHTML = "<img src=" + value + ">";
         cell2.innerHTML = posterHTML;
      } else {
         cell2.innerHTML = value;
      }
 }
