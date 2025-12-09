
import {getTimeLeftInSecs, getMessages, setMessage} from "./common.js";


let messages = null;
let jwt = null;

async function init() {
    messages = await getMessages();
    console.log("Messages loaded:", messages);

     jwt = localStorage.getItem("jwt");
    console.log(jwt)
// go back to login page if jw expired
    if (!jwt || getTimeLeftInSecs()<=0) {
        window.location.href = "/login";
    }else{
        // fill page with data from secure API endpoints
        getUsername();
        getFiles();
        getTimeLeftInSecs();
        putTimeLeft();
        setInterval(putTimeLeft,10000);// update time left on jwt every 10 seconds
    }


}


document.getElementById("refreshBtn").addEventListener("click", refresh);

async function refresh(){

    try{
        const jwt = localStorage.getItem("jwt");

        const res = await fetch("/auth/refresh", {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + jwt,
                "Content-Type": "application/json"
            }
        });

        if (!res.ok){
            setMessage( messages["refresh.failure"],true);
            return

        }

        const data = await res.json();

        localStorage.setItem("jwt",data.token);
        localStorage.setItem("expiresIn",data.expiresIn);
        console.log("expiresIn " + localStorage.getItem("expiresIn")/1000);
        putTimeLeft();

        setMessage( messages["refresh.success"],false);

    }catch (error){
       console.log(error) ;
        setMessage( messages["refresh.failure"],true);

    }

}




function downloadFile(fileObj) {

    fetch("http://localhost:8080/files/download", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + jwt

        },
        body: JSON.stringify(fileObj) // sending your object: {id, filename, size}
    })
        .then(response => {
            const disposition = response.headers.get("Content-Disposition");
            let filename = fileObj.filename;  // fallback to filename from file object

            // get filename  from header
            if (disposition && disposition.includes("filename=")) {
                filename = disposition.split("filename=")[1].replace(/"/g, "");
            }

            return response.blob().then(blob => ({ blob, filename }));
        })
        .then(({ blob, filename }) => {
            const url = window.URL.createObjectURL(blob);

            // create downloadable link
            const a = document.createElement("a");
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            a.remove();

            window.URL.revokeObjectURL(url);
        });
}


async function  deleteFile(fileObj) {

    let answer = confirm(messages["delete.ask"] + " " +  fileObj.filename + " ?") ;
    if (!answer){
        return
    }

    try {
        const res = await fetch("/files/delete", {
            method: "POST",
            headers: { "Content-Type": "application/json",
                    "Authorization": "Bearer " + jwt
            },
            body: JSON.stringify(fileObj) // sending your object: {id, filename, size}
        });

        if (!res.ok) {

            setMessage( messages["delete.failure"],true);
            return;
        }


        setMessage(messages["delete.success"], false);

        return true; // return true so we can then easily remove unwanted tr from table


    } catch (err) {
        setMessage(messages["delete.failure"]);
    }

}




// Attach the function to the button click
document.getElementById("uploadBtn").addEventListener("click", upload);

function upload() {
    const uploadBtn = document.getElementById('uploadBtn');
    const fileInput = document.getElementById("fileInput");
    const file = fileInput.files[0];
    const message = document.getElementById("message");

    if (!file) {
        alert("Select a file first.");
        return;
    }

    const jwt = localStorage.getItem("jwt");
    if (!jwt) {
        alert("You must log in first!");
        window.location.href = "login.html";
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    const xhr = new XMLHttpRequest();

    xhr.open("POST", "/files/upload", true);
    xhr.setRequestHeader("Authorization", "Bearer " + jwt);

    // Listen for progress
    xhr.upload.addEventListener("progress", (event) => {
        if (event.lengthComputable) {
            const percent = Math.round((event.loaded / event.total) * 100);
            message.textContent = `Uploading: ${percent}%`;
        }
    });

    // On success
    xhr.onload = function () {
        if (xhr.status >= 200 && xhr.status < 300) {
            message.textContent = messages["upload.success"];

            getFiles(); // refresh files list
        } else {

            message.textContent = messages["upload.failure"];
        }
    };

    // On error
    xhr.onerror = function () {
        message.textContent = uploadBtn.dataset.uploadfailure;
    };

    xhr.send(formData);
}



async function getFiles() {
    const jwt = localStorage.getItem("jwt");
    if (!jwt) {
        alert("You must log in first!");
        window.location.href = "/login";
        return;
    }

    try {
        const response = await fetch("/files", {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + jwt
            },
        });

        if (!response.ok) {
            setMessage( messages["files.load.failure"],true);
            return;
        }

        const files= await response.json();
        fill_files_table(files);

        console.log(files);

        setMessage( messages["files.load.success"],false);





    } catch (err) {
        console.error(err);
        setMessage( messages["files.load.failure"],true);

    }
}

function get_human_size(size){
    if (size > 1_000_000_000){
        return Math.floor(size / 1_000_000_000) + " GB";
    }else if (size > 1_000_000){
        return Math.floor(size / 1_000_000) + " MB";

    }else{
        return Math.floor(size / 1_000) + " KB";

    }
}
function fill_files_table(files){

    // get tbody to later append to
    document.querySelector("#filesTable tbody").innerHTML="";

    // make table invisible
    if (files.length === 0) {
        document.querySelector("#filesTable").style.display = "none";
    } else {
        document.querySelector("#filesTable").style.display = "table-row-group";
    }


    files.forEach(file =>{
        const tr = document.createElement("tr");
        const tdName = document.createElement("td");
        tdName .textContent = file.filename;
        const tdSize = document.createElement("td");
        tdSize.textContent = get_human_size(file.size);
        // const tdShareId = document.createElement("td");
        // tdShareId.textContent = file.shareId

        // create download button for every file/row
        const tdActions = document.createElement("td");
        
        const btnDownload =document.createElement("button");
        btnDownload.textContent = "⤓";
        btnDownload.addEventListener("click", ()=>{
            console.log(file)
            downloadFile(file);
        });

        tdActions.appendChild( btnDownload);

        // create delete button for every row
        const tdDelete = document.createElement("td");
        const btnDelete =document.createElement("button");
        btnDelete.textContent = "✖";
        btnDelete.addEventListener("click", ()=>{
            console.log(file)
            deleteFile(file).then(res =>{
                if (res){
                    tr.remove();
                }
            });
        });

        tdActions.appendChild( btnDelete);   // download and delete is in one cell


        tr.appendChild(tdName);
        tr.appendChild(tdSize);
        tr.appendChild(tdActions); 



        document.querySelector("#filesTable tbody").appendChild(tr);
    });





}

document.getElementById("logoutBtn").addEventListener("click", logout);


function logout(){
    let answer = confirm(messages["logout.ask"]) ;


    if ( answer && localStorage.getItem("jwt")){
        localStorage.removeItem("jwt");
        console.log("jwt removed");
        window.location.href="/login"

        //alert("you have been log out / jwt expired");
    }else{
        console.log("jwt is not present");

    }
}

async function getUsername(){

    const jwt = localStorage.getItem("jwt");
    if (!jwt) {
        alert("You must log in first!");
        window.location.href = "login.html";
        return;
    }

    try {
        const response = await fetch("/users/me", {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + jwt
            },
        });

        if (!response.ok) {
            throw new Error("Failed to get username");
        }

        const username = await response.text();
        console.log(username)

        document.getElementById("usernameSpan").textContent=username;


    } catch (err) {
        console.error(err);
        document.getElementById("usernameP").textContent = "Failed to get username";
    }

}

// put time left on jwt toke in minutes into span
function putTimeLeft(){
    const timeLeftMinutes = Math.floor(getTimeLeftInSecs()/60);

    console.log("Minutes left on token:", timeLeftMinutes);
    document.getElementById("tokenMinutesLeft").textContent=timeLeftMinutes  ;

}

// handle upload button click
fileInput.addEventListener("change", () => {

    const uploadBtn = document.getElementById('uploadBtn');
    if (fileInput.files.length === 0) {
        filenameSpan.textContent =  messages["upload.filenotchosen"];
    } else {
        filenameSpan.textContent = fileInput.files[0].name;
    }
});

init();

// Call the function
