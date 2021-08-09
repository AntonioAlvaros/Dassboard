/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.primefaces.ultima.servlet;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.ultima.service.SessionValue;
import org.primefaces.ultima.view.PollView;

    
public class TCPServerRunner extends Thread {

  
    
    private static final int PORT = 54780;
    private static String OS = System.getProperty("os.name").toLowerCase();
    private static String fileName = "config.properties";
    private static Properties prop = new Properties();
    ServerSocket ss;
          

    

    

    public TCPServerRunner() {

    }

    private void InitServer() {
      loadProperties();
        System.out.println("iniciado*********************************************************");
        try {
//            ss = new ServerSocket(Integer.valueOf(prop.getProperty("port")));
            ss = new ServerSocket(Integer.valueOf(9949));
            System.out.println("socket iniciado*********************************************************");
//            System.out.println("Source inicializado " + prop.getProperty("port") + " [OK]");
              System.out.println("Source inicializado " + 9949 + " [OK]");
            while (true) {
                Socket socket = ss.accept();
                System.out.println("conectado from [" + socket.getInetAddress().getHostName() + "]");
                OutputStream os = socket.getOutputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response;
                while ((response = in.readLine()) != null) {
                   System.out.println("It is response plot: " + response);
                   SessionValue.aproved++;
                    try {

                        
                        
                        
                        System.out.println("Tratando de incrementar...");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }   
                }
                os.flush();
            }
        } catch (IOException ex) {
           System.out.println("IOException*********************************************************");

            ex.printStackTrace();
            System.out.println(ex);
        } catch (Exception e) {
            System.out.println("IOException*********************************************************");
            e.printStackTrace();
            System.out.println("\n\tERROR[" + e.getMessage() + "]");
        }
   
    }
    
    
    public void CloseServer() {
        try {
            ss.close();
        } catch (IOException ex) {
            System.out.println("Cerrando el socket");
        }
    }
    
    
        @Override
    public void run() {
        InitServer();
    }
    
  private static Operation contructObject(String response) {
    String[] plot = response.split(";");
    Operation operation = new Operation();
    try {
      operation.setMessageTypeIdentifier(plot[0].trim());
      operation.setPrimaryAccountNumber2(plot[1].trim());
      operation.setProcessingCode3(plot[2].trim());
      operation.setAmounTransaction4(convertAmount(plot[3].trim()));
      operation.setTransmissionDateTime7(plot[4].trim());
      operation.setSystemTraceAuditNumber11(plot[5].trim());
      operation.setTimeLocalTransaction12(plot[6].trim());
      operation.setLocalTransactiondate13(plot[7].trim());
      operation.setSettlementDate15(plot[8].trim());
      operation.setDateCapture17(plot[9].trim());
      operation.setMerchantCategoryCode18(plot[10].trim());
      operation.setPosEntryMode22(plot[11].trim());
      operation.setCodeAcquiringInstitution32(plot[12].trim());
      operation.setForwardingInstitutionCode33(plot[13].trim());
      operation.setTrack2Data35(plot[14].trim());
      operation.setRetrievalReferenceNumber37(plot[15].trim());
      operation.setAuthorizationCode38(plot[16].trim());
      operation.setResponseCode39(plot[17].trim());
      operation.setIdentificationReceivingTerminalCard41(plot[18].trim());
      operation.setNameAndLocationReceiverCard43(plot[19].trim());
      operation.setTransactionCurrencyCode49(plot[20].trim());
      operation.setReserved58(plot[21].trim());
      operation.setAccountIdentification102(plot[22].trim());
      operation.setAccountIdentification103(plot[23].trim());
      operation.setTransactionDescription104(plot[24]);
      operation.setReserved123(plot[25].trim());
      operation.setDestinationRoute(plot[26].trim());
      
    } catch (Exception ex) {
        ex.printStackTrace();
      operation.setResponseCode39("Error");
    } 
    return operation;
  }


    
     public static Float convertAmount(String val) {
        char char0 = '0';
        String decimalValue = val.substring(val.length() - 2, val.length());
        String enterValue = val.substring(0, val.length() - 2);
        String finalenterValue = "";
        for (int i = 0; i <= enterValue.length(); i++) {
            if (enterValue.charAt(i) == char0) {
                continue;
            } else {
                finalenterValue = enterValue.substring(i, enterValue.length());
                 break;
                
            }
           
        }
        return Float.valueOf(finalenterValue + "." + decimalValue);
    }
    
    
      public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static void loadProperties() {
        String propertiesSource = "/home/" + fileName;
        if (isWindows()) {
            propertiesSource = "c://" + fileName;
        }
        System.out.println(propertiesSource);
        try (InputStream input = new FileInputStream(propertiesSource)) {
            // load a properties file
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    
    
    public static void saveOperation(Operation operation) throws Exception {
        try {

            Class.forName(prop.getProperty("dbdriver"));
            Connection conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
            // the mysql insert statement
            String query = " INSERT INTO `dasshboard`.`operations`  (`messageTypeIdentifier`, `primaryAccountNumber`, `processingCode`, `amounTransaction`, `transmissionDateTime`, `systemTraceAuditNumber`, `timeLocalTransaction`,`localTransactionDate`,`settlementDate`, `dateCapture`, `merchantCategoryCode`, `posEntryMode`,"
                    + " `codeAcquiringInstitution`, `forwardingInstitutionCode`, `track2Data`, `retrievalReferenceNumber`, `authorizationCode`, `responseCode`, `identificationReceivingTerminalCard`, `nameAndLocationReceiverCard`, `currencyCode`, `reserved58`, "
                    + "`accountIdentificationSource`, `accountIdentificationDestination`,"+ " `description`, `reversed`,`routeName`) VALUES "
                    + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

            java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1,operation.getMessageTypeIdentifier());
            preparedStmt.setString(2,operation.getPrimaryAccountNumber2());
            preparedStmt.setString(3,operation.getProcessingCode3());
            preparedStmt.setFloat(4,operation.getAmounTransaction4());
            preparedStmt.setTimestamp(5,getDateField7(operation.getTransmissionDateTime7()));
            preparedStmt.setLong(6,Long.valueOf(operation.getSystemTraceAuditNumber11()));
            preparedStmt.setTimestamp(7, getDateField12(operation.getTimeLocalTransaction12()));
            preparedStmt.setString(8,operation.getLocalTransactiondate13());
            preparedStmt.setString(9,operation.getSettlementDate15());
            preparedStmt.setString(10,operation.getDateCapture17());
            preparedStmt.setString(11,operation.getMerchantCategoryCode18());
            preparedStmt.setString(12,operation.getPosEntryMode22());
            preparedStmt.setString(13,operation.getCodeAcquiringInstitution32());
            preparedStmt.setString(14,operation.getForwardingInstitutionCode33());
            preparedStmt.setString(15,operation.getTrack2Data35());
            preparedStmt.setString(16,operation.getRetrievalReferenceNumber37());
            preparedStmt.setString(17,operation.getAuthorizationCode38());
            preparedStmt.setString(18,operation.getResponseCode39());
            preparedStmt.setString(19,operation.getIdentificationReceivingTerminalCard41());
            preparedStmt.setString(20,operation.getNameAndLocationReceiverCard43());
            preparedStmt.setString(21,operation.getTransactionCurrencyCode49());
            preparedStmt.setString(22,operation.getReserved58());
            preparedStmt.setString(23,operation.getAccountIdentification102());
            preparedStmt.setString(24,operation.getAccountIdentification103());
            preparedStmt.setString(25,operation.getTransactionDescription104());
            preparedStmt.setString(26,operation.getReserved123());
            preparedStmt.setString(27,operation.getDestinationRoute());
            // execute the preparedstatement
            preparedStmt.execute();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    public static Timestamp getDateField7(String field7Value){
          Calendar cal = Calendar.getInstance();
          cal.set(Calendar.MONTH, Integer.valueOf(field7Value.substring(0,2))-1);
          cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(field7Value.substring(2,4)));
          cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(field7Value.substring(4,6)));
          cal.set(Calendar.MINUTE, Integer.valueOf(field7Value.substring(6,8)));
          cal.set(Calendar.SECOND, Integer.valueOf(field7Value.substring(8,10)));
        return new Timestamp(cal.getTimeInMillis());
    }
    
    public static Timestamp getDateField12(String field12Value ){
          Calendar cal = Calendar.getInstance();
               cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(field12Value.substring(0,2)));
          cal.set(Calendar.MINUTE, Integer.valueOf(field12Value.substring(2,4)));
          cal.set(Calendar.SECOND, Integer.valueOf(field12Value.substring(4,6)));
        return new Timestamp(cal.getTimeInMillis());
    }
    


    
    

}
