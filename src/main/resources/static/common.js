
export function getTimeLeftInSecs(){
    const token = localStorage.getItem("jwt");
    if (!token ){
        return 0;
    }
    const payloadBase64 = token.split(".")[1];
    const payloadJson = atob(payloadBase64);
    const payload = JSON.parse(payloadJson);


    // Calculate time left in milliseconds
    const timeLeftMs = payload.exp * 1000 - Date.now();

    // Convert to minutes
    const timeLeftSecs = Math.floor(timeLeftMs / 1000 );
    console.log(timeLeftSecs)
    return timeLeftSecs;

}

export  function setMessage (msg, isError = true) {
    const messageEl = document.getElementById("message");
    messageEl.textContent = msg;
    messageEl.style.color = isError ? "red" : "green";
}






 export async function getMessages() {
    const messageUrl = 'http://localhost:8080/messages';

    try {
        const response = await fetch(messageUrl,{
            credentials : "include"
        });
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const messages = await response.json();   // store resolved data
        return messages;                           // return actual data

    } catch (error) {
        console.error("Failed to fetch messages:", error);
        return null;
    }
}
