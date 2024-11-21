# FXP20 JPOS Middleware

## Introduction
Currently, the only way to integrate the Zebra FXP20 is through the JPOS driver. 
This can be challenging for developers unfamiliar with Java and JPOS standards. 
To address this, here is a middleware that simplifies the integration process by using standard WebSocket communication, enabling seamless integration with any framework.

## Requirements
- Zebra FXP20 
- FXP20 JPOS Driver (early access version): Please contact your Zebra Sales representative to get it.

## Setup & Testing

### Run Artifact
- Download the latest release and unzip the file
- Run `run-service.bat` file to run the middleware
- Open the file `index.html` under ./web-app-client-sample
- Test & Enjoy

### ...Or run the full source code
- Copy the FXP20 JPOS Driver under /lib folder
- Ensure that port 1997 is open. You can change the port on the init function of ZebraMiddlewareWS.java
- Run the middleware using your favourite IDE
- Sample web-app is located in /web-app-client-sample

## Supported Commands

The app supports the following commands for managing RFID operations. Each command has a specific structure and purpose.

| **Command**           | **Description**                                                                                         | **Parameters**                                                                                                                                                                                                                                          | **Sample Request**                                                                                                                                                                                                                                                                                                                                                                   | **Sample Response**                                                                                                                                                                                                                                           |
|------------------------|---------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CHECK_HEALTH`         | Checks the health status of the RFID reader service.                                                   | None                                                                                                                                                                                                                                                   | ```json { "command": "CHECK_HEALTH" } ```                                                                                                                                                                                                                                                                                                                                           | ```json { "response": "CHECK_HEALTH", "parameters": { "isAlive": true } } ```                                                                                                                                                                               |
| `START_INVENTORY`      | Starts an RFID inventory for a specified duration.                                                     | - `duration` (int): Duration of the inventory in milliseconds. If duration value is 0, the inventory won't stop until the STOP_INVENTORY command is ussed                                                                                                                                                                                        | ```json { "command": "START_INVENTORY", "parameters": { "duration": 30000 } } ```                                                                                                                                                                                                                                                                                                 | ```json { "response": "START_INVENTORY", "parameters": { "success": true } } ```                                                                                                                                                                            |
| `STOP_INVENTORY`       | Stops the ongoing RFID inventory operation - Needed only when the START_INVENTORY is invoked with value 0 as duration.                                                            | None                                                                                                                                                                                                                                                   | ```json { "command": "STOP_INVENTORY" } ```                                                                                                                                                                                                                                                                                                                                       | ```json { "response": "STOP_INVENTORY", "parameters": { "success": true } } ```                                                                                                                                                                            |
| `WRITE_TAG_ID`         | Writes a new tag ID to an RFID tag.                                                                    | - `inputTagId` (String): Existing tag ID. <br> - `newTagId` (String): New tag ID to write. <br> - `timeout` (int): Timeout for the operation in milliseconds. <br> - `password` (String): Tag access password.                                           | ```json { "command": "WRITE_TAG_ID", "parameters": { "inputTagId": "E2000016580301741890F00A", "newTagId": "E2000016580301741890F00B", "timeout": 5000, "password": "12345678" } } ```                                                                                                                                                       | ```json { "response": "WRITE_TAG_ID", "parameters": { "success": true } } ```                                                                                                                                                                              |
| `WRITE_TAG_DATA`       | Writes data to an RFID tag.                                                                             | - `inputTagId` (String): Existing tag ID. <br> - `data` (String): Hexadecimal string data to write. <br> - `startOffset` (int): Offset to start writing data. <br> - `timeout` (int): Timeout in milliseconds. <br> - `password` (String): Tag access password. | ```json { "command": "WRITE_TAG_DATA", "parameters": { "inputTagId": "E2000016580301741890F00A", "data": "DEADBEEF", "startOffset": 0, "timeout": 5000, "password": "12345678" } } ```                                                                                                                | ```json { "response": "WRITE_TAG_DATA", "parameters": { "success": true } } ```                                                                                                                                                                            |
| `KILL_TAG`             | Permanently disables an RFID tag.                                                                      | - `inputTagId` (String): Tag ID to disable. <br> - `timeout` (int): Timeout for the operation in milliseconds. <br> - `password` (String): Tag access password.                                                 | ```json { "command": "KILL_TAG", "parameters": { "inputTagId": "E2000016580301741890F00A", "timeout": 5000, "password": "12345678" } } ```                                                                                                                                                                                                 | ```json { "response": "KILL_TAG", "parameters": { "success": true } } ```                                                                                                                                                                                 |
| `LOCK_TAG`             | Locks an RFID tag, preventing further modifications.                                                   | - `inputTagId` (String): Tag ID to lock. <br> - `timeout` (int): Timeout for the operation in milliseconds. <br> - `password` (String): Tag access password.                                                   | ```json { "command": "LOCK_TAG", "parameters": { "inputTagId": "E2000016580301741890F00A", "timeout": 5000, "password": "12345678" } } ```                                                                                                                                                                                                 | ```json { "response": "LOCK_TAG", "parameters": { "success": true } } ```                                                                                                                                                                                 |

---

### Error Handling

If an error occurs during any operation, the server responds with the following structure:

```json
{
    "response": "<COMMAND_NAME>",
    "parameters": {
        "success": false,
        "error": "Error message describing the issue"
    }
}
