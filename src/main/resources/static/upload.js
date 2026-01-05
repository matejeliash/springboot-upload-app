import { getTimeLeftInSecs, getMessages, setMessage } from "./common.js";

let messages = null; // language specific messages
let jwt = null; 

let curDirId = null;
let dirIds = []; // used as stack for dir ids
//let prevDirId = null;
let dirNames = []; // used as stack for dir names

let dirName="";
let fileName="";

// create current path from path and put it to HTML
function displayPath() {
  let relativeDirPath = "";
  if (dirNames.length > 0) {
    relativeDirPath = dirNames.join("/");
  }
  document.getElementById("curDir").innerText = "⌂ HOME/" + relativeDirPath;
}

// function executed on page load
async function init() {
  messages = await getMessages();
  console.log("Messages loaded:", messages);
  dirName=messages["table.type.dir"]
  fileName=messages["table.type.file"]

  jwt = localStorage.getItem("jwt");
  console.log(jwt);
  // go back to login page if jw expired
  if (!jwt || getTimeLeftInSecs() <= 0) {
    window.location.href = "/login";
  } else {
    // fill page with data from secure API endpoints
    showDirBackBtn();
    putUsernameToScreen();
    const files = await getFiles();
    const dirs = await getDirs();
    fill_files_table(files, dirs);

    putTimeLeftToScreen();
    setInterval(putTimeLeftToScreen, 10000); // update time left on jwt every 10 seconds
    displayPath();
  }
}


// put time left on jwt toke in minutes into span
function putTimeLeftToScreen() {
  const timeLeftMinutes = Math.floor(getTimeLeftInSecs() / 60);

  console.log("Minutes left on token:", timeLeftMinutes);
  document.getElementById("tokenMinutesLeft").textContent = timeLeftMinutes;
}

// fetches username and puts it to element on page
async function putUsernameToScreen() {
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
        Authorization: "Bearer " + jwt,
      },
    });

    if (!response.ok) {
      throw new Error("Failed to get username");
    }

    const username = await response.text();
    console.log(username);
    // put username to span
    document.getElementById("usernameSpan").textContent = username;
  } catch (err) {
    console.error(err);
    document.getElementById("usernameSpan").textContent = "error";
  }
}


// register refresh button
document.getElementById("refreshBtn").addEventListener("click", refresh);

// send and fetch refreshed jwt token
async function refresh() {
  try {
    const jwt = localStorage.getItem("jwt");

    const res = await fetch("/auth/refresh", {
      method: "POST",
      headers: {
        Authorization: "Bearer " + jwt,
        "Content-Type": "application/json",
      },
    });

    if (!res.ok) {
      setMessage(messages["refresh.failure"], true);
      return;
    }

    const data = await res.json();

    localStorage.setItem("jwt", data.token);
    localStorage.setItem("expiresIn", data.expiresIn);
    console.log("expiresIn " + localStorage.getItem("expiresIn") / 1000);

    putTimeLeftToScreen();

    setMessage(messages["refresh.success"], false);
  } catch (error) {
    console.log(error);
    setMessage(messages["refresh.failure"], true);
  }
}


// // button for going to HOME
// document.getElementById("homeBtn").addEventListener("click", async () => {
//   dirNames = [];
//   dirIds = [];

//   curDirId = null;


//   const files = await getFiles();
//   const dirs = await getDirs();
//   fill_files_table(files, dirs);

//   displayPath();
// });




// send request to download and than fetches file data and
// creates temporary link for downloading file
async function downloadFile(fileObj) {
  const jwt = localStorage.getItem("jwt");
  // no jwt go back to login
  if (!jwt) {
    window.location.href = "/login";
    return;
  }

  try {
    const res = await fetch("/files/download", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + jwt,
      },
      body: JSON.stringify(fileObj),
    });

    // throw error, propagate to catch
    if (!res.ok) {
      throw new Error(`Download failed: ${res.status} ${res.statusText}`);
    }

    // get filename from header
    let filename = fileObj.filename; // fallback
    const disposition = res.headers.get("Content-Disposition");
    if (disposition && disposition.includes("filename=")) {
      filename = disposition.split("filename=")[1].replace(/"/g, "");
    }

    const blob = await res.blob(); // blob representing files data
    const url = window.URL.createObjectURL(blob);

    // create link , present only after clicking
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    // make url unreachable
    a.remove();
    window.URL.revokeObjectURL(url);
  } catch (err) {
    console.error(err);
    setMessage(messages["download.failure"], true);
  }
}


// assign upload function to button click 
document.getElementById("uploadBtn").addEventListener("click", upload);



// this changes displayed filename if it is selected before upload
fileInput.addEventListener("change", () => {
  if (fileInput.files.length === 0) {
    filenameSpan.textContent = messages["upload.filenotchosen"];
  } else {
    filenameSpan.textContent = fileInput.files[0].name;
  }
});

//  send request to server and uplods file, it uses xhr, so 
// we can get upload progress
function upload() {
  const fileInput = document.getElementById("fileInput");
  const file = fileInput.files[0]; // 
  const message = document.getElementById("message");

  if (!file) {
    alert(messages["upload.select.file.first"]);
    return;
  }

  const jwt = localStorage.getItem("jwt");
  if (!jwt) {
    window.location.href = "/login";
    return;
  }

  // put data to form so server can retrieve theme
  const formData = new FormData();
  formData.append("file", file); // file object with data
  formData.append("dirId", curDirId); // id of dest dir

  const xhr = new XMLHttpRequest();

  xhr.open("POST", "/files/upload", true);
  xhr.setRequestHeader("Authorization", "Bearer " + jwt);

  // make listener on progress, and put progress to messages p
  xhr.upload.addEventListener("progress", (event) => {
    if (event.lengthComputable) {
      const percent = Math.round((event.loaded / event.total) * 100); // percentage
      message.textContent = `Progress: ${percent}%`;
      // later I may add upload speed
    }
  });

  xhr.onload = async function () {
    if (xhr.status >= 200 && xhr.status < 300) {
      message.textContent = messages["upload.success"];
      const files = await getFiles(); // refresh files list
      const dirs = await getDirs();
      fill_files_table(files, dirs);
    } else {
      // error during upload
      message.textContent = messages["upload.failure"];
    }
  };

  // error, before upload
  xhr.onerror = function () {
    message.textContent = messages["upload.failure"];
  };

  xhr.send(formData);
}

// get all files in current dir 
async function getFiles() {
  const jwt = localStorage.getItem("jwt");
  if (!jwt) {
    alert("You must log in first!");
    window.location.href = "/login";
    return;
  }

  try {
    // we put data in json, we could also use {id} as query param
    const response = await fetch("/files", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + jwt,
      },

      body: JSON.stringify({
        parentId: curDirId, // send current dir id in JSON
      }),
    });

    if (!response.ok) {
      setMessage(messages["files.load.failure"], true);
      return; // return nothing
    }

    const files = await response.json(); // return array of objects

    console.log(files);
    return files;
  } catch (err) {
    console.error(err);
    setMessage(messages["files.load.failure"], true);
  }
}
// get all dirs from current dir, similar to getFiles()
async function getDirs() {
  const jwt = localStorage.getItem("jwt");
  if (!jwt) {
    alert("You must log in first!");
    window.location.href = "/login";
    return;
  }

  try {
    const response = await fetch("/files/dirs", {
      method: "POST",
      headers: {
        Authorization: "Bearer " + jwt,
        "Content-Type": "application/json",
      },

      body: JSON.stringify({
        parentId: curDirId,
      }),
    });

    if (!response.ok) {
      setMessage(messages["files.load.failure"], true);
      return;
    }

    // return array of DirectoryResponse objects
    const dirs = await response.json();

    console.log(dirs);
    return dirs;
  } catch (err) {
    console.error(err);
    setMessage(messages["files.load.failure"], true);
  }
}

// convert bytes to more concise readable human format
function get_human_size(size) {
  if (size > 1_000_000_000) {
    return (size / 1_000_000_000).toFixed(2) + " GB";
  } else if (size > 1_000_000) {
    return (size / 1_000_000).toFixed(2) + " MB";
  } else {
    return (size / 1_000).toFixed(2) + " KB";
  }
}

//TODO
function fill_files_table(files, dirs) {
  // get tbody to later append to
  document.querySelector("#filesTable tbody").innerHTML = "";

  // make table invisible
  if (files.length === 0 && dirs.length === 0) {
    document.querySelector("#filesTable").style.display = "none";
  } else {
    document.querySelector("#filesTable").style.display = "table-row-group";
  }


 // create cols for row with dir
  dirs.forEach((dir) => {
    const tr = document.createElement("tr");
    tr.className = "dir";

    const tdId = document.createElement("td");
    tdId.textContent = dir.id;

    const tdType = document.createElement("td");
    tdType.textContent = dirName;
    const tdName = document.createElement("td");
    tdName.textContent = dir.name;
    const tdSize = document.createElement("td");
    tdSize.textContent = " ";

    // create download button for every file/row
    const tdActions = document.createElement("td");
    //
    const btnEnter = document.createElement("button");
    btnEnter.textContent = "⏎";
    btnEnter.classList.add("iconBtn");
    btnEnter.addEventListener("click", async () => {
      console.log(dir);
      if (dir.name ===".."){
        dirNames.pop();
        dirIds.pop();

        // set new current dir id 
        if (dirIds.length > 0){
          curDirId = dirIds.at(-1);
        }else{
          curDirId = null;
        }


      }else{
        dirIds.push(dir.id);
        dirNames.push(dir.name);
        curDirId = dir.id;

      }
      const files = await getFiles();
      const dirs = await getDirs();
      const relativeDirPath = dirNames.join("/");
      document.getElementById("curDir").innerText = "⌂ HOME/" + relativeDirPath;
      console.log(relativeDirPath);
      fill_files_table(files, dirs);
      showDirBackBtn();
    });

    const btnDelete = document.createElement("button");
    btnDelete.textContent = "✖";
    btnDelete.classList.add("iconBtn");
    btnDelete.addEventListener("click", async () => {
      handleDelete(dir.id,dir.name,false,tr);
    });

    const btnRename = document.createElement("button");
    btnRename.textContent = "aZ";
    btnRename.classList.add("iconBtn");
    btnRename.addEventListener("click", () => {
      handleRename(dir.id,dir.name,false,tr);
    });



    const tdCreatedAt = document.createElement("td");
    const date = new Date(dir.createdAt);

    tdCreatedAt.textContent= date.toLocaleString();

    // do not enter on current dir
    if (dir.name !== "."){
      tdActions.appendChild(btnEnter);
    }
    // do not allow delete and rename on previous dir
    if (dir.name !== "." && dir.name !== ".."){
      tdActions.appendChild(btnDelete);
      tdActions.appendChild(btnRename);

    }

    tr.appendChild(tdId);
    tr.appendChild(tdType);
    tr.appendChild(tdName);
    tr.appendChild(tdSize);
    tr.appendChild(tdActions);
    tr.appendChild(tdCreatedAt);

    attachDragToRow(tr);

    document.querySelector("#filesTable tbody").appendChild(tr);
  });

  files.forEach((file) => {
    const tr = document.createElement("tr");
    tr.className = "file";

    const tdId = document.createElement("td");
    tdId.textContent = file.id;
    
    const tdType = document.createElement("td");
    tdType.textContent = fileName;

    const tdName = document.createElement("td");
    tdName.textContent = file.filename;
    const tdSize = document.createElement("td");
    tdSize.textContent = get_human_size(file.size);
    // const tdShareId = document.createElement("td");
    // tdShareId.textContent = file.shareId

    // create download button for every file/row
    const tdActions = document.createElement("td");

    const btnDownload = document.createElement("button");
    btnDownload.textContent = "⤓";
    btnDownload.classList.add("iconBtn");
    btnDownload.addEventListener("click", () => {
      console.log(file);
      downloadFile(file);
    });

    tdActions.appendChild(btnDownload);



    const btnDelete = document.createElement("button");
    btnDelete.textContent = "✖";
    btnDelete.classList.add("iconBtn");
    btnDelete.addEventListener("click", async () => {
      handleDelete(file.id,file.filename,true,tr);
    });


    const btnRename = document.createElement("button");

    btnRename.textContent = "aZ";
    btnRename.classList.add("iconBtn");
    btnRename.addEventListener("click", () => {
      handleRename(file.id,file.filename,true,tr);
    });

    tdActions.appendChild(btnDelete); // download and delete is in one cell
    tdActions.appendChild(btnRename); // download and delete is in one cell

    const tdCreatedAt = document.createElement("td");

    const date = new Date(file.createdAt);

    tdCreatedAt.textContent= date.toLocaleString();

    tr.appendChild(tdId);
    tr.appendChild(tdType);
    tr.appendChild(tdName);
    tr.appendChild(tdSize);
    tr.appendChild(tdActions);
    tr.appendChild(tdCreatedAt);


    attachDragToRow(tr);

    document.querySelector("#filesTable tbody").appendChild(tr);
  });




}


//TODO
async function moveFile(movedId, destDirId, isFile) {

  try {
    const res = await fetch("/files/move", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + jwt,
      },
      body: JSON.stringify({
        movedId: movedId,
        destDirId: destDirId,
        isFile: isFile,
      }), // sending your object: {id, filename, size}
    });

    if (!res.ok) {

      if (res.status == 409){
        setMessage(messages["rename.failure.name.used"], true);
      }
      else{
      setMessage(messages["move.failure"], true);

      }

      return false;
    }

    setMessage(messages["move.success"], false);

    return true; // return true so we can then easily remove unwanted tr from table
  } catch (err) {
    setMessage(messages["move.failure"]);
  }
}


// ============================================
// Rename fetch + modal
// ============================================
// actual rename request
async function rename(id,name,isFile) {

  try {
    const res = await fetch("/files/rename", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + jwt,
      },
      body: JSON.stringify({
        id: id,
        name: name,
        isFile : isFile
      }),
    });

    if (!res.ok) {
      console.log( await res.json())
      //setMessage("error while creating dir", true);
      if (res.status == 409){
        setMessage(messages["rename.failure.name.used"], true);

      }else{
        setMessage(messages["rename.failure"], true);

      }
      return false;

    }
    //const data = await res.json();
      const files = await getFiles(); // refresh files list
      const dirs = await getDirs();
      fill_files_table(files, dirs);
    setMessage(messages["rename.success"], false);
    return true
  } catch (error) {
    console.log(error);
    setMessage(messages["rename.failure"], true);
    return false;
  }
}


function handleRename(id,name,isFile,tr){

  const modal = document.getElementById("renameModal");
  const input = document.getElementById("renameInput");

  document.getElementById("renameTitle").textContent = `${messages["rename.ask"]} ${name} ?`
  input.value = "";
  modal.classList.remove("hidden");
  input.focus();


  document.getElementById("cancelRename").onclick = closeModal;
  modal.onclick = (e) => {
    if (e.target === modal) closeModal();
  };

  document.getElementById("confirmRename").onclick = async () => {
    const newName = input.value.trim();
    if (!newName) return;

    const ok = await rename(id,newName,isFile);
      // remove row from HTML table
        if (ok) {
          tr.cells[1].textContent = newName;
        }



    closeModal();
  };

  function closeModal() {
    modal.classList.add("hidden");
  }

}

// ============================================
// Delete  + modal
// ============================================

// send request to delete file on server
async function deleteFile(id, isFile) {

  try {
    const res = await fetch("/files/delete", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + jwt,
      },
      body: JSON.stringify({
        id:id,
        isFile: isFile,
      }), // sending your object: {id, isFile}
    });

    if (!res.ok) {
      setMessage(messages["delete.failure"], true);
      return false;
    }

    setMessage(messages["delete.success"], false);
    return true; // return true so we can then easily remove unwanted tr from table
  } catch (err) {
    setMessage(messages["delete.failure"]);
    return false;
  }
}


 async function  handleDelete(id,name,isFile,tr){


  const modal = document.getElementById("deleteModal");

  document.getElementById("deleteTitle").textContent = `${messages["delete.ask"]} ${name} ?`

    modal.classList.remove("hidden");


  document.getElementById("cancelDelete").onclick = closeModal;
  modal.onclick = (e) => {
    if (e.target === modal) closeModal();
  };
  document.getElementById("confirmDelete").onclick =  async () => {

    const ok = await deleteFile(id,isFile);
    if (ok){
      tr.remove();
    }

    closeModal();
  };

  function closeModal() {
    modal.classList.add("hidden");
  }

}


// ============================================
// Logout  + modal
// ============================================

// attach logout() to button click
document.getElementById("logoutBtn").addEventListener("click", logout);

// whole logout process with jwt removal
function logout() {

  const modal = document.getElementById("logoutModal");
  document.getElementById("logoutTitle").textContent = `${messages["logout.ask"]}`
  modal.classList.remove("hidden");

  // close modal when cancel clicked
  document.getElementById("cancelLogout").onclick = closeModal;
  modal.onclick = (e) => {
    if (e.target === modal) closeModal();
  };
  
  // do logout on confirm click
  document.getElementById("confirmLogout").onclick =  async () => {

    // remove jwt if exist in localstorage
    if ( localStorage.getItem("jwt")) {
      localStorage.removeItem("jwt");
      console.log("jwt removed");

      //window.location.href="/login"
      window.location.replace("/login");

    } else {
      console.log("jwt is not present");
    }

    closeModal();
  };

  function closeModal() {
    modal.classList.add("hidden");
  }




}


// ============================================
// Back button navigation
// ============================================

// display back button and  change current dir id
document.getElementById("dirBackBtn").addEventListener("click", async () => {
  dirNames.pop();
  dirIds.pop();

  // set new current dir id 
  if (dirIds.length > 0){
    curDirId = dirIds.at(-1);
  }else{
    curDirId = null;
  }

  // get all dirs  and files for new current dir
  const files = await getFiles();
  const dirs = await getDirs();
  fill_files_table(files, dirs);

  displayPath();
  showDirBackBtn();
});

// display back button if we are not in root upload dir
function showDirBackBtn() {
  if (dirIds.length === 0) {
    document.getElementById("dirBackBtn").style.display = "none";
  } else {
    document.getElementById("dirBackBtn").style.display = "inline-block";
  }
}

// ============================================
// Create dir + modal
// ============================================

// register button with func to create dir
document.getElementById("createDirBtn").addEventListener("click", handleCreateDir);

async function createDir(id,name) {

  try {
    const res = await fetch("/files/createDir", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + jwt,
      },
      body: JSON.stringify({
        parentDirId: id,
        name: name,
      }),
    });

    if (!res.ok) {
      console.log( await res.json())

      //name is already used put error to UI
      if (res.status == 409){
        setMessage(messages["rename.failure.name.used"], true);

      }else{
        setMessage(messages["createDir.failure"], true);
      }
      return;

    }

    const files = await getFiles(); // refresh files list
    const dirs = await getDirs();
    fill_files_table(files, dirs);
    setMessage(messages["createDir.success"], false);
    return true;
  } catch (error) {
    console.log(error);
      setMessage(messages["createDir.failure"], true);
  }
}

function handleCreateDir(){

  const modal = document.getElementById("createDirModal");
  const input = document.getElementById("createDirInput");

  document.getElementById("createDirTitle").textContent = `${messages["createDir.ask"]}?`
  input.value = "";
  modal.classList.remove("hidden");
  input.focus();


  document.getElementById("cancelCreateDir").onclick = closeModal;
  modal.onclick = (e) => {
    if (e.target === modal) closeModal();
  };

  document.getElementById("confirmCreateDir").onclick = async () => {
    const newName = input.value.trim();
    if (!newName) return;

    const ok = await createDir(curDirId,newName)


    closeModal();
  };

  function closeModal() {
    modal.classList.add("hidden");
  }

}




let draggedRow = null;
let dragClone = null;
let offsetX = 0;
let offsetY = 0;

function attachDragToRow(row) {
  row.style.touchAction = "none"; // mobile support

  row.addEventListener("pointerdown", handlePointerDown);
  row.addEventListener("pointermove", handlePointerMove);
  row.addEventListener("pointerup", handlePointerUp);
  row.addEventListener("pointercancel", cleanupDrag);
}

// got from https://developer.mozilla.org/en-US/docs/Web/API/Element/pointerdown_event
function handlePointerDown(e) {
  // allows to select text and click on buttons
  if (e.target.closest("button, a, input, textarea, select")) return;

  draggedRow = this;

  if (draggedRow.cells[2].textContent === "." || draggedRow.cells[2].textContent ===".."){
    return;
  }

  document.body.classList.add("no-select");

  const rect = draggedRow.getBoundingClientRect();
  offsetX = e.clientX - rect.left;
  offsetY = e.clientY - rect.top;

  dragClone = createRowClone(draggedRow, rect);
  document.body.appendChild(dragClone);

  draggedRow.setPointerCapture(e.pointerId);
}

function handlePointerMove(e) {
  if (!dragClone) return;

  dragClone.style.left =
    e.clientX - offsetX + window.scrollX + "px";
  dragClone.style.top =
    e.clientY - offsetY + window.scrollY + "px";
}

function handlePointerUp(e) {
  if (!dragClone) return cleanupDrag();

  handleDrop();
  cleanupDrag(e.pointerId);
}

function createRowClone(row, rect) {
  const clone = document.createElement("div");
  clone.className = "dragging";
  clone.textContent = row.cells[2].innerText;

  Object.assign(clone.style, {
    position: "absolute",
    width: rect.width + "px",
    left: rect.left + window.scrollX + "px",
    top: rect.top + window.scrollY + "px",
    background: "#fff",
    border: "1px solid black",
    padding: "4px",
    pointerEvents: "none",
    opacity: 0.8,
    zIndex: 1000,
    color: "#000"
    
  });

  return clone;
}

async function handleDrop() {
  const cloneRect = dragClone.getBoundingClientRect();
  const cx = cloneRect.left + cloneRect.width / 2;
  const cy = cloneRect.top + cloneRect.height / 2;

  const dirs = document.querySelectorAll("#filesTable tr.dir");

  for (const dir of dirs) {
    if (dir === draggedRow) continue;

    // prevent dropping on current dir 
    if (dir.cells[2].textContent === "."){
      console.log("skipped cur dir");
      continue;
    } 

    const r = dir.getBoundingClientRect();
    if (cx >= r.left && cx <= r.right && cy >= r.top && cy <= r.bottom) {
      console.log(
        `${draggedRow.cells[0].innerText} dropped on ${dir.cells[0].innerText}`
      );

      const isFile  = draggedRow.cells[1].textContent === dirName ? false : true ; 

      // capture it before cleanup is done 
      const rowToRemove = draggedRow;
      
      const ok = await moveFile(
        draggedRow.cells[0].textContent,
        dir.cells[0].textContent,
        isFile
      );
      if (ok && rowToRemove?.isConnected ){

        rowToRemove.remove();

      }
       

      break;
    }
  }
}

function cleanupDrag(pointerId) {
  document.body.classList.remove("no-select");

  dragClone?.remove();
  dragClone = null;

  try {
    draggedRow?.releasePointerCapture(pointerId);
  } catch {}

  draggedRow = null;
}

window.addEventListener("blur", cleanupDrag);
document.addEventListener("pointercancel", cleanupDrag);



// ********* MAIN *************

init().then(() => console.log("page init finished !!!"));