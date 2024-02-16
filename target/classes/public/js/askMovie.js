function loadGetMsg() {
        let movieTitle = encodeURIComponent(document.getElementById("movie").value).replace(/%20/g, "+");
        console.log(movieTitle);
        const xhttp = new XMLHttpRequest();
        xhttp.onload = function() {
           console.log(this.status);
           if (this.status === 200) {
               window.location.href = "/public/movie?t="+movieTitle;
           }
        }
        xhttp.open("GET", "/public/movie?t="+movieTitle);
        xhttp.send();
}