package com.spozebra.fxp20.jpos.middleware;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Thread serverThread = new Thread(() -> {
            ZebraMiddlewareWS.getInstance().init();
        });

        System.out.println("ZebraEngineContext Started Successfully");

        serverThread.start();

        System.out.println("Type 'exit' to stop the server.");
        
        try (Scanner scanner = new Scanner(System.in)) {
            // Wait for user input to stop the server
            while (true) {
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    ZebraMiddlewareWS.getInstance().stopWebSocket();
                    break;
                }
            }
        }
        System.out.println("Server has been stopped.");
    }
}