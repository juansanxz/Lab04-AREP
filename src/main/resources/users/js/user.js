function loadPostMsg() {
    // Obtén los datos del formulario
    var name = document.getElementById("name").value;
    var lastName = document.getElementById("lastName").value;
    var age = document.getElementById("age").value;

    // Crea una cadena de consulta con los datos del formulario
    var queryString = "name=" + encodeURIComponent(name) + "&lastName=" + encodeURIComponent(lastName) + "&age=" + encodeURIComponent(age);

    // Crea una nueva solicitud XMLHttpRequest
    var xhttp = new XMLHttpRequest();

    // Define la función de respuesta cuando la solicitud se completa
    xhttp.onreadystatechange = function() {
        console.log(this.readyState + " ---- " + this.status);
        if (this.readyState === 4 && this.status === 201) {
            // La solicitud se completó con éxito
            console.log("User created successfully!");
            loadGetMsg(function(data) {
                console.log(data);
                var userList = document.getElementById("userList");
                userList.innerHTML = ""; // Limpia la lista anterior

                data.forEach(function(user) {
                    var listItem = document.createElement("li");
                    listItem.textContent = "Name: " + user.name + ", Last Name: " + user.lastName + ", Age: " + user.age;
                    userList.appendChild(listItem);
                });
            });

        }
    };

    // Abre una solicitud POST a la URL "/createUser" con la cadena de consulta como datos
    xhttp.open("POST", "/users", true);

    // Establece el encabezado Content-Type para datos de formulario
    xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

    // Envía la cadena de consulta como datos de la solicitud
    xhttp.send(queryString);
}

function loadGetMsg(callback) {
    var xhr = new XMLHttpRequest(); // Crea una nueva instancia de XMLHttpRequest

    // Configura la función de devolución de llamada cuando la solicitud se completa
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) { // Cuando la solicitud está completa
            if (xhr.status === 200) { // Si el estado de la respuesta es exitoso
                // Parsea la respuesta JSON si es necesario
                console.log(xhr.responseText);
                var responseData = JSON.parse(xhr.responseText);
                // Llama a la función de devolución de llamada con los datos de respuesta
                callback(responseData);
            } else {
                // Si la respuesta no es exitosa, llama a la función de devolución de llamada con un error
                callback(new Error('Error en la solicitud: ' + xhr.status));
            }
        }
    };

    xhr.open('GET', "/users", true); // Abre una solicitud GET a la URL especificada
    xhr.send(); // Envía la solicitud
}

function loadGetOneMsg() {
    var nameToSearch = document.getElementById("nameToSearch").value;
    var xhr = new XMLHttpRequest(); // Crea una nueva instancia de XMLHttpRequest

    // Configura la función de devolución de llamada cuando la solicitud se completa
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) { // Cuando la solicitud está completa
            if (xhr.status === 200) { // Si el estado de la respuesta es exitoso
                // Parsea la respuesta JSON si es necesario
                console.log(xhr.responseText);
                var responseData = JSON.parse(xhr.responseText);
                var userInfo = document.getElementById("userInfo");
                userInfo.innerHTML = ""; // Limpia la lista anterior
                var listItem = document.createElement("info");
                listItem.textContent = "Name: " + responseData.name + ", Last Name: " + responseData.lastName + ", Age: " + responseData.age;
                userInfo.appendChild(listItem);


            } else {
                // Si la respuesta no es exitosa, llama a la función de devolución de llamada con un error
                callback(new Error('Error en la solicitud: ' + xhr.status));
            }
        }
    };

    xhr.open('GET', "/users/user?id=" + nameToSearch , true); // Abre una solicitud GET a la URL especificada
    xhr.send(); // Envía la solicitud
}