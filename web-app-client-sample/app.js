// Set up WebSocket connection
const socket = new WebSocket("ws://localhost:1997/fxp20");
const connectionStatusWS = document.getElementById("connection-status-ws");
const connectionStatusRFID = document.getElementById("connection-status-rfid");
const userInputDiv = document.getElementById("user-input");
const responseDiv = document.getElementById("response");
const messageList = document.getElementById("message-list");

let isConnectedWS = false;
let isRFIDAlive = false;

// Function to update connection status
function updateConnectionStatus(element, isConnected) {  
  const dot = element.querySelector('.dot');
  if (dot) {
    dot.className = 'dot ' + (isConnected ? 'green' : 'red');
  }
}

// Listen for WebSocket connection open/close
socket.addEventListener('open', () => {
  updateConnectionStatus(connectionStatusWS, true);
  isConnectedWS = true;
  // Send check health command
  sendCommand("0");
});

socket.addEventListener('close', () => {
  updateConnectionStatus(connectionStatusWS, false);
  isConnectedWS = false;
});

socket.addEventListener('error', (error) => {
  connectionStatusWS.textContent = "Error connecting to WS server";
  console.error("WebSocket error:", error);
});

socket.addEventListener('message', (event) => {
  const data = JSON.parse(event.data);
  appendMessage("Received", event.data);
  
  if (data.response === "CHECK_HEALTH") {
    if (data.parameters.isAlive === true) {
      updateConnectionStatus(connectionStatusRFID, true);
      isRFIDAlive = true;
    } else {
      updateConnectionStatus(connectionStatusRFID, false);
      isRFIDAlive = false;
    }
  }
  else if(data.response === "RETRY_CONNECT" && data.parameters.success === true) {
    updateConnectionStatus(connectionStatusRFID, true);
  }
});

// Function to send commands to the server
function sendCommand(command) {
  if (!isConnectedWS) {
    alert("Not connected to server!");
    return;
  }

  let message = {};
  switch (command) {
    case "0":
      message = { command: "CHECK_HEALTH", parameters: {} };
      break;
    case "1":
      message = { command: "RETRY_CONNECT", parameters: {} };
      break;
    case "2":
      message = { command: "START_INVENTORY", parameters: { "duration": 0 } };
      break;
    case "3":
      message = { command: "START_INVENTORY", parameters: getInventoryParams() };
      break;
    case "4":
      message = { command: "STOP_INVENTORY", parameters: {} };
      break;
    case "5":
      message = { command: "WRITE_TAG_ID", parameters: getWriteTagIdParams() };
      break;
    case "6":
      message = { command: "WRITE_TAG_DATA", parameters: getWriteTagDataParams() };
      break;
    case "7":
      message = { command: "KILL_TAG", parameters: getTagOperationParams() };
      break;
    case "8":
      message = { command: "LOCK_TAG", parameters: getTagOperationParams() };
      break;
    default:
      alert("Unknown command.");
      return;
  }

  appendMessage("Sent", JSON.stringify(message));
  // Send message as JSON
  socket.send(JSON.stringify(message));
}

// Helper functions to get input for various commands
function getInventoryParams() {
  const duration = prompt("Enter inventory duration (seconds):");
  return { "duration": parseInt(duration) };
}

function getWriteTagIdParams() {
  const inputTagId = prompt("Enter input tag ID:");
  const newTagId = prompt("Enter new tag ID:");
  const timeout = prompt("Enter timeout (milliseconds):");
  const password = prompt("Enter password:");
  return { "inputTagId": inputTagId, "newTagId": newTagId, "timeout": parseInt(timeout), "password": password };
}

function getWriteTagDataParams() {
  const inputTagId = prompt("Enter input tag ID:");
  const data = prompt("Enter data to write:");
  const startOffset = prompt("Enter start offset:");
  const timeout = prompt("Enter timeout (milliseconds):");
  const password = prompt("Enter password:");
  return { "inputTagId": inputTagId, "data": data, "startOffset": parseInt(startOffset), "timeout": parseInt(timeout), "password": password };
}

function getTagOperationParams() {
  const inputTagId = prompt("Enter input tag ID:");
  const timeout = prompt("Enter timeout (milliseconds):");
  const password = prompt("Enter password:");
  return { "inputTagId": inputTagId, "timeout": parseInt(timeout), "password": password };
}

// Function to append messages to the autoscroll list
function appendMessage(type, message) {
  const listItem = document.createElement("li");
  listItem.textContent = `${type}: ${message}`;
  messageList.insertBefore(listItem, messageList.firstChild);
}
