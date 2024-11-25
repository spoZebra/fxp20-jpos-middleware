package com.spozebra.fxp20.jpos.middleware.services;

import java.net.UnknownHostException;

import com.zebra.log.rfid.scanner.JCoreLogger;

import java.util.ArrayList;
import java.util.List;

import jpos.JposException;
import jpos.RFIDScanner;
import jpos.events.*;

public class ZebraReaderService {
    private RFIDScanner reader = new RFIDScanner();
    private IZebraReaderListener zebraReaderListener;
    public ZebraReaderService(IZebraReaderListener zebraReaderListener) {
        this.zebraReaderListener = zebraReaderListener;
    } 

     public void initReader() throws JposException {
        reader.open("ZebraRFIDScanners");

        if(!reader.getClaimed()){
            reader.claim(1000);

            // Setup listener
            reader.setDeviceEnabled(true);
            reader.addDataListener(dataEventListener);
            reader.addStatusUpdateListener(statusUpdateListener);
            reader.addOutputCompleteListener(outputCompleteListener);
            reader.addErrorListener(errorEventListener);
        }
    }

    public void retryConnect() throws JposException {
        reader.release();
        reader.close();
        initReader();
    }

    public void startReadingTags(int duration){
        try {
            if(duration == 0)
                reader.startReadTags(16, new byte[0], new byte[0], 0, 0, new byte[0]);
            else{
                reader.readTags(16, new byte[0], new byte[0], 0, 0, duration, new byte[0]);
                parseTags(); //invoked after the duration is reached
            }
        } catch (JposException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void stopReadingTags(){
        try {
            reader.stopReadTags(new byte[0]);
            parseTags();
        } catch (JposException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean checkHealth() {
        postLog("CheckHealth", " TestIsAlive (in)");
        try {

            reader.checkHealth(1);
            postLog("CheckHealth", " TestIsAlive (in)" + reader.getCheckHealthText());
            reader.checkHealth(2);
            postLog("CheckHealth", " TestIsAlive (in)" + reader.getCheckHealthText());
            reader.checkHealth(3);
            postLog("CheckHealth", " TestIsAlive (in)" + reader.getCheckHealthText());

            return true;
        } catch (JposException e) {
            postLog("CheckHealth", e.getErrorCode() + " TestInventoryCalls (in) " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            postLog("CheckHealth", " TestInventoryCalls (in)" + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    public void writeTagIdOperation(String inputTagId, String newTagId, int timeout, String password) throws JposException {
        postLog("OPERATION MENU","writeTagIdOperation Destination ID: "+ inputTagId);

        var passwordBytes = new byte[0];
        if(password != null && password != "")
            passwordBytes = password.getBytes();

        reader.writeTagID(inputTagId.getBytes(), Utils.hexStringToByteArray(newTagId), timeout, passwordBytes);
    }

    public void writeTagDataOperation(String inputTagId, String data,int startOffset, int timeout, String password) throws JposException {
        postLog("OPERATION MENU","writeTagDataOperation Destination ID: "+ inputTagId);

        var passwordBytes = new byte[0];
        if(password != null && password != "")
            passwordBytes = password.getBytes();

        reader.writeTagData(inputTagId.getBytes(), Utils.hexStringToByteArray(data), startOffset, timeout, passwordBytes);
    }

    public void killTagOperation(String inputTagId, int timeout, String password) throws JposException {
        postLog("OPERATION MENU","killTagOperation Destination ID: "+ inputTagId);
        
        var passwordBytes = new byte[0];
        if(password != null && password != "")
            passwordBytes = password.getBytes();

        reader.disableTag(inputTagId.getBytes(), timeout, passwordBytes);
    }

    public void lockTagOperation(String inputTagId, int timeout, String password) throws JposException {
        postLog("OPERATION MENU","lockTagOperation Destination ID: "+ inputTagId);
        
        var passwordBytes = new byte[0];
        if(password != null && password != "")
            passwordBytes = password.getBytes();
            
        reader.lockTag(inputTagId.getBytes(), timeout, passwordBytes);
    }

    private void parseTags(){
        try {
            List<String> tags = new ArrayList<String>();
            reader.firstTag();
            System.out.println(new String(reader.getCurrentTagID()));
            tags.add(new String(reader.getCurrentTagID()));

            for(int i = 0; i < reader.getTagCount() - 1; i++){
                reader.nextTag();
                System.out.println(new String(reader.getCurrentTagID()));
                tags.add(new String(reader.getCurrentTagID()));
            }

            zebraReaderListener.onTagsRead(tags);
            
        } catch (JposException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    OutputCompleteListener outputCompleteListener = new OutputCompleteListener() {

        @Override
        public void outputCompleteOccurred(OutputCompleteEvent outputCompleteEvent) {
            System.out.println("outputCompleteOccurred "+outputCompleteEvent.getOutputID());
            postLog("outputCompleteOccurred"," getOutputID : "+outputCompleteEvent.getOutputID());
        }
    };

    StatusUpdateListener statusUpdateListener = new StatusUpdateListener() {
        @Override
        public void statusUpdateOccurred(StatusUpdateEvent statusUpdateEvent) {
            System.out.println("statusUpdateOccurred STATUS : " + statusUpdateEvent.getStatus());
            postLog("statusUpdateOccurred"," STATUS : "+statusUpdateEvent.getStatus());
            if (statusUpdateEvent.getStatus() == 100) {
                System.out.println("Firmware updated successfully");
            }
        }
    };

    DataListener dataEventListener = new DataListener() {
        @Override
        public void dataOccurred(DataEvent dataEvent) {
            postLog("dataOccurred"," dataEvent STATUS : "+dataEvent.getStatus());
        }
    };

    ErrorListener errorEventListener = new ErrorListener() {
        @Override
        public void errorOccurred(ErrorEvent errorEvent) {
            System.out.println("Error Code : " + errorEvent.getErrorCode());
            System.out.println("Error Response : " + errorEvent.getErrorResponse());
            postLog("dataOccurred"," errorOccurred Code : "+errorEvent.getErrorCode());
            postLog("dataOccurred"," errorOccurred Response : "+errorEvent.getErrorResponse());
        }
    };


    
    static void postLog(String key, String value) {
            try {
                JCoreLogger.SetLogEntries("JavaPosMain","TestApp");
                JCoreLogger.Log(true,1,0,key,value);
            } catch (JposException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println(key+" - "+value);
    }
}
