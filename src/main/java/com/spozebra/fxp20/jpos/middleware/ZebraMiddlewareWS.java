package com.spozebra.fxp20.jpos.middleware;

import java.util.List;
import java.util.Map;

import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;

import org.glassfish.tyrus.server.Server;
import org.json.JSONObject;

import com.spozebra.fxp20.jpos.middleware.services.*;

import jpos.JposException;

public class ZebraMiddlewareWS implements IWebSocketListener, IZebraReaderListener {

    private static ZebraMiddlewareWS instance = null; 
    private ZebraMiddlewareWS() {} 
 
    public static ZebraMiddlewareWS getInstance() {
        if (instance == null) {
            instance = new ZebraMiddlewareWS();
        }
        return instance;
    }

    private ZebraReaderService zebraReaderService;
    private Server server;

    public void init(){
        
        // Init JPOS driver
        initDriver();

        // Init WS
        try {
            Server server = new Server("localhost", 1997, "/", null, WebSocketServer.class);
            WebSocketServer.setListener(this);
            server.start();
            System.out.println("Websocket Server started successfully");
        } catch (DeploymentException e) {
            e.printStackTrace();
        }

    }

    private void initDriver(){
        zebraReaderService = new ZebraReaderService(this);

        try {
            zebraReaderService.initReader();
            System.out.println("Reader Service Initialized");
        } catch (JposException e) {
            e.printStackTrace();
        }
    }


    public void stopWebSocket() {
        server.stop();
    }

    @Override
    public void onClientConnected(Session client) {
        System.out.println("Listener: Client connected from " + client.getId());
    }

    @Override
    public void onMessageReceived(Session client, String message) {
        String command = "";
        try {
            System.out.println("Listener: Message from client " + client.getId() + ": " + message);
            JSONObject jsonObject = new JSONObject(message);
            command = jsonObject.getString("command");
    
            switch (command) {
                case "CHECK_HEALTH":
                    boolean isAlive = zebraReaderService.checkHealth();
                    sendResponse(client, command, Map.of("success", true, "isAlive", isAlive));
                    break;
    
                case "RETRY_CONNECT":
                    zebraReaderService.initReader();
                    sendResponse(client, command, Map.of("success", true));
                break;
    
                case "START_INVENTORY":
                    int duration = jsonObject.getJSONObject("parameters").getInt("duration");
                    zebraReaderService.startReadingTags(duration);
                    sendResponse(client, command, Map.of("success", true));
                    break;
    
                case "STOP_INVENTORY":
                    zebraReaderService.stopReadingTags();
                    sendResponse(client, command, Map.of("success", true));
                    break;
    
                case "WRITE_TAG_ID":
                    String inputTagId = jsonObject.getJSONObject("parameters").getString("inputTagId");
                    String newTagId = jsonObject.getJSONObject("parameters").getString("newTagId");
                    int timeout = jsonObject.getJSONObject("parameters").getInt("timeout");
                    String password = jsonObject.getJSONObject("parameters").getString("password");
    
                    zebraReaderService.writeTagIdOperation(inputTagId, newTagId, timeout, password);
                    sendResponse(client, command, Map.of("success", true));
                    break;
    
                case "WRITE_TAG_DATA":
                    inputTagId = jsonObject.getJSONObject("parameters").getString("inputTagId");
                    String data = jsonObject.getJSONObject("parameters").getString("data");
                    int startOffset = jsonObject.getJSONObject("parameters").getInt("startOffset");
                    timeout = jsonObject.getJSONObject("parameters").getInt("timeout");
                    password = jsonObject.getJSONObject("parameters").getString("password");
    
                    zebraReaderService.writeTagDataOperation(inputTagId, data, startOffset, timeout, password);
                    sendResponse(client, command, Map.of("success", true));
                    break;
    
                case "KILL_TAG":
                    inputTagId = jsonObject.getJSONObject("parameters").getString("inputTagId");
                    timeout = jsonObject.getJSONObject("parameters").getInt("timeout");
                    password = jsonObject.getJSONObject("parameters").getString("password");
    
                    zebraReaderService.killTagOperation(inputTagId, timeout, password);
                    sendResponse(client, command, Map.of("success", true));
                    break;
    
                case "LOCK_TAG":
                    inputTagId = jsonObject.getJSONObject("parameters").getString("inputTagId");
                    timeout = jsonObject.getJSONObject("parameters").getInt("timeout");
                    password = jsonObject.getJSONObject("parameters").getString("password");
    
                    zebraReaderService.lockTagOperation(inputTagId, timeout, password);
                    sendResponse(client, command, Map.of("success", true));
                    break;
    
                default:
                    System.err.println("Unknown command: " + command);
                    sendResponse(client, command, Map.of("success", false, "error", "Unknown command"));
                    break;
            }
        } catch (Exception ex) {
            sendResponse(client, command, Map.of("success", false, "error", ex.getMessage()));
            System.err.println("Error processing command: " + ex.getMessage());
        }
    }
    

    private void sendResponse(Session client, String responseAction, Map<String, Object> responseParams) {
        try {
            // Create the main response JSON object
            JSONObject response = new JSONObject();
            response.put("response", responseAction);

            // Create a "parameters" JSON object to hold all key-value pairs
            JSONObject parameters = new JSONObject();
            for (Map.Entry<String, Object> entry : responseParams.entrySet()) {
                parameters.put(entry.getKey(), entry.getValue());
            }

            // Add the parameters object to the response
            response.put("parameters", parameters);

            // Send the message to the client
            WebSocketServer.sendMessageToClient(client, response.toString());
        } catch (Exception e) {
            System.err.println("Error sending response to client: " + e.getMessage());
        }
    }

    @Override
    public void onClientDisconnected(Session client) {
        System.out.println("Listener: Client disconnected " + client.getId());
    }

    @Override
    public void onTagsRead(List<String> readTags) {
        JSONObject message = new JSONObject();
        message.put("response", "TAG_DATA");
        message.put("parameters", new JSONObject().put("tags", readTags));
        WebSocketServer.broadcastMessage(message.toString());
    }
}
