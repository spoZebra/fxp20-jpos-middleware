// Set up WebSocket connection
const socket = new WebSocket("ws://localhost:1997/fxp20");
const connectionStatus = document.getElementById("connection-status");
const userInputDiv = document.getElementById("user-input");
const responseDiv = document.getElementById("response");
const messageList = document.getElementById("message-list");

let isConnected = false;

// Listen for WebSocket connection open/close
socket.addEventListener('open', () => {
  connectionStatus.textContent = "Connected to server!";
  isConnected = true;
});

socket.addEventListener('close', () => {
  connectionStatus.textContent = "Disconnected from server";
  isConnected = false;
});

socket.addEventListener('error', (error) => {
  connectionStatus.textContent = "Error connecting to server";
  console.error("WebSocket error:", error);
});

socket.addEventListener('message', (event) => {
  appendMessage("Received", event.data);
});

// Function to send commands to the server
function sendCommand(command) {
  if (!isConnected) {
    alert("Not connected to server!");
    return;
  }

  let message = {};
  switch (command) {
    case "1":
      message = { command: "START_INVENTORY", parameters: { "duration":0} };
      break;
    case "2":
      message = { command: "START_INVENTORY", parameters: getInventoryParams() };
      break;
    case "3":
      message = { command: "STOP_INVENTORY", parameters: {} };
      break;
    case "4":
      message = { command: "WRITE_TAG_ID", parameters: getWriteTagIdParams() };
      break;
    case "5":
      message = { command: "WRITE_TAG_DATA", parameters: getWriteTagDataParams() };
      break;
    case "6":
      message = { command: "KILL_TAG", parameters: getTagOperationParams() };
      break;
    case "7":
      message = { command: "LOCK_TAG", parameters: getTagOperationParams() };
      break;
    default:
      alert("Unknown command.");
      return;
  }

  appendMessage("Sent", JSON.stringify(message));
  // Send message as JSON
  socket.send(JSON.stringify(message));
  clearUserInput();
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
  return { "inputTagId": inputTagId, "newTagId" : newTagId, "timeout" : parseInt(timeout), "password" : password };
}

function getWriteTagDataParams() {
  const inputTagId = prompt("Enter input tag ID:");
  const data = prompt("Enter data to write:");
  const startOffset = prompt("Enter start offset:");
  const timeout = prompt("Enter timeout (milliseconds):");
  const password = prompt("Enter password:");
  return { "inputTagId": inputTagId, "data" : data, "startOffset":  parseInt(startOffset), "timeout" : parseInt(timeout), "password" : password };
}

function getTagOperationParams() {
  const inputTagId = prompt("Enter input tag ID:");
  const timeout = prompt("Enter timeout (milliseconds):");
  const password = prompt("Enter password:");
  return { "inputTagId" : inputTagId, "timeout" : parseInt(timeout), "password" : password };
}

// Function to append messages to the autoscroll list
function appendMessage(type, message) {
  const listItem = document.createElement("li");
  listItem.textContent = `${type}: ${message}`;
  messageList.appendChild(listItem);
  messageList.scrollTop = messageList.scrollHeight; // Auto-scroll to the latest message
}

// Clear input form after sending a command
function clearUserInput() {
  userInputDiv.innerHTML = "";
}
