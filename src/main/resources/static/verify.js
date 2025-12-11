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
            const resObj= await res.json();
            console.log(resObj)
            if (resObj.errorCode === "ACCOUNT_ALREADY_VERIFIED"){
                setMessage(messages["error.verification.already.done"],true);
            }else if (resObj.errorCode === "CODE_EXPIRED"){
                setMessage(messages["error.verification.code.expired"],true);
            } else if (resObj.errorCode === "WRONG_CODE"){
            setMessage(messages["error.verification.wrong.code"],true);
        }

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
