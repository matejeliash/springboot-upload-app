import {getMessages,setMessage} from "./common.js";

let messages = null;

async function register() {
    const usernameEl = document.getElementById("username");
    const emailEl = document.getElementById("email");
    const password1El = document.getElementById("password1");
    const password2El = document.getElementById("password2");

    const username = usernameEl.value.trim();
    const email = emailEl.value.trim();
    const password1 = password1El.value;
    const password2 = password2El.value;


    if (!username || !email || !password1 || !password2) {
        setMessage(messages["register.different.passwords"]);
        return;
    }

    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) {
        setMessage(messages["register.invalidemail"]);
        return;
    }

    if (password1 !== password2) {
        setMessage(messages["register.different.passwords"]);
         return ;

    }

    try {
        const res = await fetch("/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                username,
                email,
                password: password1,
            })
        });

            console.log(res)

        if (!res.ok) {

            const resObj= await res.json();
            console.log(resObj)
            if (resObj.errorCode === "USERNAME_ALREADY_USED"){
                setMessage(messages["error.username.already.used"],true);
            }else if (resObj.errorCode === "EMAIL_ALREADY_USED"){
                    setMessage(messages["error.email.already.used"],true);
            }

            return


        }

        setMessage(messages["register.success"], false);

    } catch (err) {
        console.error("Register error:", err);
         setMessage(messages["register.failure"],true);
    }
}



async function init(){
    document.getElementById("registerBtn").addEventListener("click", register);
    messages = await getMessages();
    console.log("Messages loaded:", messages);

}
init()




