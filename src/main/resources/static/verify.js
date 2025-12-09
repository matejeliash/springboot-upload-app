import {getMessages, setMessage} from "./common.js";
let messages = null;

async function verify() {
    const verificationCodeEl = document.getElementById("verificationCode");
    const emailEl = document.getElementById("email");

    const email = emailEl.value.trim();
    const verificationCode = verificationCodeEl.value.trim();


    if ( !email || !verificationCode) {
        setMessage(messages["register.different.passwords"]);
        return;
    }



    try {
        const res = await fetch("/auth/verify", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                verificationCode: verificationCode,
                email : email
            })
        });

        if (!res.ok) {
            setMessage(messages["verify.failure"],true);
            return
        }

        setMessage(messages["verify.success"], false);

        window.location.href = "/login";

    } catch (err) {
         setMessage(messages["verify.failure"]);
    }
}


async function init(){
    document.getElementById("verifyBtn").addEventListener("click", verify);
    messages = await getMessages();
    console.log("Messages loaded:", messages);

}
init()
