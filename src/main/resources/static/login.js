import {getMessages, getTimeLeftInSecs, setMessage} from "./common.js";

let messages = null;

async function init(){

    document.getElementById("loginBtn").addEventListener("click", login);

    messages = await getMessages();

    const jwt = localStorage.getItem("jwt");
    if (jwt && getTimeLeftInSecs()>0) {
        window.location.href = "/upload";
    }else{
        if (getTimeLeftInSecs() <=0){
            console.log("issue with expiredIN ",getTimeLeftInSecs())
        }
    }

}
async function login(){


    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    try{

        const res = await fetch("/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body:JSON.stringify({
                username: username,
                password: password
            })
        });


        if (!res.ok){
            const errMsg = await res.text();

            setMessage(messages["login.failure"], true);
            return;
        }

        const data = await res.json();

        localStorage.setItem("jwt",data.token);
        localStorage.setItem("expiresIn",data.expiresIn);
        console.log("expiresIn " + localStorage.getItem("expiresIn")/1000);
        setMessage(messages["login.success"], false);
        // redirect to file upload page
        setTimeout(() => {
            window.location.href = "/upload";
        }, 100);


    }catch (error){
        console.log(error)
        setMessage(messages["login.failure"], true);
    }
}

init()


