/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.primefaces.ultima.view.p2c;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import org.primefaces.ultima.view.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;
import org.primefaces.model.chart.PieChartModel;
import org.primefaces.ultima.exception.EmptyListException;
import org.primefaces.ultima.servlet.Operation;
import static org.primefaces.ultima.servlet.TCPServerRunner.convertAmount;

/**
 *
 * @author usuario
 */
@Named
@ViewScoped

public class PollViewP2C implements Serializable {

     private static String OS = System.getProperty("os.name").toLowerCase();
    private static String fileName = "configDASHBOARD.properties";
    private static Properties prop = new Properties();
    private PieChartModel pieModel1;




    /////////////CounterTemp
    public static Integer tempApproved = 0;
    public static Integer tempReversed = 0;
    public static Integer tempReject = 0;
    public static Integer tempTimeOut = 0;
    
    ////////////////////countTimeOut
    public static   Integer countApprovedp2p = 0;
    public static   Integer countRejectp2p = 0;
    public static   Integer countTimeOutp2p = 0;
    public static    Integer countReversedp2p = 0;

    Connection conn;

    private Float sumApproved;
    private Float sumReject;
    private Float sumReversed;

    private String information;
    private int approved;
    private int rejected;
    private int timeout;
    private int reversed;

    private boolean connected;

    ///////////////////////////////////Aprovadas////////////////////////////////////////////////
    private static AtomicLong counter = new AtomicLong();
    @Inject
    @Push(channel = "counterP2P")
    private PushContext push;

    public void toggle() {
        connected = true;
    }
    
   public static void pushValueP2P(){
       
   }


    public Long getCount() {
        return counter.get();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////reversadas////////////////////////////////////////////////
    private static AtomicLong counterReverse = new AtomicLong();
    @Inject
    @Push(channel = "counterReverse")
    private PushContext pushReverse;

    public void toggleReverse() {
        connected = true;
    }

    public void incrementerReverse(int i) {
        pushReverse.send(i);
    }

    public Long getCountReverse() {

        return counterReverse.get();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////rechazadas////////////////////////////////////////////////
    private static AtomicLong counterReject = new AtomicLong();
    @Inject
    @Push(channel = "counterReject")
    private PushContext pushReject;

    public void toggleRejecte() {
        connected = true;
    }

    public void incrementerReject(int i) {
        pushReject.send(i);
    }

    public Long getCountReject() {
        return counterReject.get();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////timeOut////////////////////////////////////////////////
    private static AtomicLong counterTimeOut = new AtomicLong();
    @Inject
    @Push(channel = "counterTimeOut")
    private PushContext pushTimeOut;

    public void toggleTimeOut() {
        connected = true;
    }

    public void incrementerTimeOut(int i) {
        pushTimeOut.send(i);
    }

    public Long getCountTimeOut() {
        return counterTimeOut.get();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////

    ServerSocket ss;

    @PostConstruct
    public void init() {

        loadLocalProperties();
        Statement stmt3;
        try {
            Class.forName(prop.getProperty("dbdriver"));
            try {
                conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
                stmt3 = conn.createStatement();
                String sql = "SELECT (SELECT COUNT(*) FROM dashboard.operations WHERE processingCode='560050' AND messageTypeIdentifier=\"0210\" AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode=\"00\") AS \"Aprobadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE processingCode='560050' AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"Rechazadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE processingCode='560050' AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND messageTypeIdentifier =\"0410\") AS \"Reversadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE processingCode='560050' AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode=\"91\") AS \"TimeOut\" ,\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE processingCode='560050' AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode=\"00\") AS \"sumAprobadas\",\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE processingCode='560050' AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"sumRechazadas\",\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE processingCode='560050' AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND messageTypeIdentifier =\"0410\") AS \"sumReversed\"";
                System.out.println("sql=" + sql);
                ResultSet rs3 = stmt3.executeQuery(sql);
                while (rs3.next()) {
                    countApprovedp2p = rs3.getInt("Aprobadas");
                    countRejectp2p = rs3.getInt("Rechazadas");
                    countTimeOutp2p = rs3.getInt("TimeOut");
                    countReversedp2p = rs3.getInt("Reversadas");

                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);

                    sumApproved = rs3.getFloat("sumAprobadas");;
                    sumReject = rs3.getFloat("sumRechazadas");;
                    sumReversed = rs3.getFloat("sumReversed");;

                }
            } catch (SQLException ex) {
                Logger.getLogger(ChartDemoView.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(PollView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PollView.class.getName()).log(Level.SEVERE, null, ex);
        }

        tempApproved = countApprovedp2p;
        counter.set(Long.valueOf(tempApproved));
        PollView.tempApprovedP2CTest = tempApproved;
        PollView.tempApprovedP2CTest++;

        tempReject = countRejectp2p;
        PollView.tempRejectP2CTest = tempReject;
        PollView.tempRejectP2CTest++;
        counterReject.set(Long.valueOf(tempReject));

        tempTimeOut = countTimeOutp2p;
        counterTimeOut.set(Long.valueOf(tempTimeOut));
        PollView.tempTimeOutP2CTest = tempTimeOut;
        PollView.tempTimeOutP2CTest++;

        tempReversed = countReversedp2p;
        counterReverse.set(Long.valueOf(tempReversed));
        PollView.tempReversedP2CTest = tempReversed;
        PollView.tempReversedP2CTest++;


        if(rejected>approved){
            getInfoAlert("La cantidad de transacciones rechazas supera las aprobadas");
        }
        try {
            showAlertTimeOut();
        } catch (EmptyListException ex) {
            ex.printStackTrace();
        }
        
 
    }

    public void getInfoAlert(String value) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", value));
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public void increment() {
    
        Integer countApproved=0;
        Integer countReject=0;
        Integer countTimeOut=0;
        Integer countReversed=0;
        loadLocalProperties();
        Statement stmt3;
        try {
            
            Class.forName(prop.getProperty("dbdriver"));
            
            try {
                conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
                stmt3 = conn.createStatement();
                  String sql = "SELECT (SELECT COUNT(*) FROM dashboard.operations WHERE processingCode='560050' AND messageTypeIdentifier=\"0210\" AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode=\"00\") AS \"Aprobadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE processingCode='560050' AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"Rechazadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE processingCode='560050' AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND messageTypeIdentifier =\"0410\") AS \"Reversadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE processingCode='560050' AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode=\"91\") AS \"TimeOut\" ,\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE processingCode='560050' AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode=\"00\") AS \"sumAprobadas\",\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE processingCode='560050' AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"sumRechazadas\",\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE processingCode='560050' AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND messageTypeIdentifier =\"0410\") AS \"sumReversed\"";
                System.out.println("sql=" + sql);
                ResultSet rs3 = stmt3.executeQuery(sql);
                while (rs3.next()) {
                    countApproved = rs3.getInt("Aprobadas");
                    countReject = rs3.getInt("Rechazadas");
                    countTimeOut = rs3.getInt("TimeOut");
                    countReversed = rs3.getInt("Reversadas");
                   
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    
                    sumApproved = rs3.getFloat("sumAprobadas");;
                    sumReject = rs3.getFloat("sumRechazadas");;
                    sumReversed = rs3.getFloat("sumReversed");;
    
                    
                    
                }
            } catch (SQLException ex) {
                Logger.getLogger(ChartDemoView.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(PollViewP2C.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PollViewP2C.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("entro.........................");
        approved = countApproved;
        rejected = countReject;
        timeout = countTimeOut;
        reversed = countReversed;
        
                
                
    }

    public Map<String, String> getRouteNameMaxUsed() throws EmptyListException {
        loadLocalProperties();
        Statement stmt3;
        Map<String, String> mapResult = new HashMap<String, String>();
        try {
            Class.forName(prop.getProperty("dbdriver"));
            try {
                conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
                stmt3 = conn.createStatement();

                String sql = "SELECT routeName,count(*) AS 'ct' FROM dashboard.operations \n"
                        + "WHERE transmissionDateTime\n"
                        + " BETWEEN '" + getBeginningDateTime() + "' AND '" + getCurrentDateTime() + "' GROUP BY routeName;";

                ResultSet rs3 = stmt3.executeQuery(sql);
                while (rs3.next()) {

                    mapResult.put(rs3.getString("routeName"), rs3.getString("ct"));
                }
            } catch (SQLException ex) {
                Logger.getLogger(ChartDemoView.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(PollView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PollView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mapResult;
    }

    public void showAlertTimeOut() throws EmptyListException {

        Statement stmt3;
        Map<String, String> mapResult = new HashMap<String, String>();
        mapResult = getRouteNameMaxUsed();
        Iterator it = mapResult.entrySet().iterator();
        while (it.hasNext()) {
            Integer counter = 0;
            Map.Entry pair = (Map.Entry) it.next();

            try {
                Class.forName(prop.getProperty("dbdriver"));
                try {
                    conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
                    stmt3 = conn.createStatement();

                    String sql = "SELECT responseCode FROM dashboard.operations \n"
                            + "WHERE transmissionDateTime BETWEEN '" + getBeginningDateTime() + "' AND  '" + getCurrentDateTime() + "'\n"
                            + "AND routeName ='" + pair.getKey() + "' ORDER BY ID DESC LIMIT 5";

                    ResultSet rs3 = stmt3.executeQuery(sql);
                    while (rs3.next()) {

                        if (rs3.getString("responseCode").equals("91")) {
                            counter++;
                        }

                        if (counter >= 5) {
                            getInfoAlert("Gran Cantidad de timeOut en dirección a la red " + pair.getKey());
                        }
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(ChartDemoView.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(PollView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(PollView.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    public int getApproved() {
        return approved;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }

    public int getRejected() {
        return rejected;
    }

    public void setRejected(int rejected) {
        this.rejected = rejected;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getReversed() {
        return reversed;
    }

    public void setReversed(int reversed) {
        this.reversed = reversed;
    }

    public Float getSumApproved() {
        return sumApproved;
    }

    public void setSumApproved(Float sumApproved) {
        this.sumApproved = sumApproved;
    }

    public Float getSumReject() {
        return sumReject;
    }

    public void setSumReject(Float sumReject) {
        this.sumReject = sumReject;
    }

    public Float getSumReversed() {
        return sumReversed;
    }

    public void setSumReversed(Float sumReversed) {
        this.sumReversed = sumReversed;
    }

    public static void loadLocalProperties() {
        String propertiesSource = "/home/" + fileName;
        if (isWindows()) {
            propertiesSource = "c://" + fileName;
        }
        //System.out.println(propertiesSource);
        try (InputStream input = new FileInputStream(propertiesSource)) {
            // load a properties file
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static Timestamp getBeginningDateTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return new Timestamp(cal.getTimeInMillis());
    }

    public static Timestamp getCurrentDateTime() {
        Calendar cal = Calendar.getInstance();
        return new Timestamp(cal.getTimeInMillis());
    }

    public class TCPServerRunnerPollView extends Thread {

        public TCPServerRunnerPollView() {

        }


        public void CloseServer() {
            try {
                ss.close();
            } catch (IOException ex) {
                System.out.println("Cerrando el socket");
            }
        }

        public boolean isToday(String field7Value) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, Integer.valueOf(field7Value.substring(0, 2)) - 1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(field7Value.substring(2, 4)));
            cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(field7Value.substring(4, 6)));
            cal.set(Calendar.MINUTE, Integer.valueOf(field7Value.substring(6, 8)));
            cal.set(Calendar.SECOND, Integer.valueOf(field7Value.substring(8, 10)));

            if (cal.getTimeInMillis() >= getBeginningDateTime().getTime() && cal.getTimeInMillis() <= getEndingDate().getTime()) {
                return true;
            } else {
                return false;
            }
        }

        public Timestamp getBeginningDateTime() {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            return new Timestamp(cal.getTimeInMillis());
        }

        public Timestamp getEndingDate() {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            return new Timestamp(cal.getTimeInMillis());
        }

        @Override
        public void run() {
           
           
        }

       

    public class ThreadSaveTransactionInfo extends Thread {

        private Operation operation;

        public ThreadSaveTransactionInfo(Operation operation_) {
            this.operation = operation_;
        }

        private void saveTrantactionInfo() {
            try {
                saveOperation(operation);
            } catch (Exception ex) {
                ex.printStackTrace();
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
            saveTrantactionInfo();
        }

        public void saveOperation(Operation operation) throws Exception {
            try {
                System.out.println("Armando el guardado en base de datos de la transacción");
                Class.forName(prop.getProperty("dbdriver"));
                Connection conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
                // the mysql insert statement
                String query = " INSERT INTO `dashboard`.`operations`  (`messageTypeIdentifier`, `primaryAccountNumber`, `processingCode`, `amounTransaction`, `transmissionDateTime`, `systemTraceAuditNumber`, `timeLocalTransaction`,`localTransactionDate`,`settlementDate`, `dateCapture`, `merchantCategoryCode`, `posEntryMode`,"
                        + " `codeAcquiringInstitution`, `forwardingInstitutionCode`, `track2Data`, `retrievalReferenceNumber`, `authorizationCode`, `responseCode`, `identificationReceivingTerminalCard`, `nameAndLocationReceiverCard`, `currencyCode`, `reserved58`, "
                        + "`accountIdentificationSource`, `accountIdentificationDestination`," + " `description`, `reversed`,`routeName`) VALUES "
                        + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

                java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());
                // create the mysql insert preparedstatement
                PreparedStatement preparedStmt = conn.prepareStatement(query);
                preparedStmt.setString(1, operation.getMessageTypeIdentifier());
                preparedStmt.setString(2, operation.getPrimaryAccountNumber2());
                preparedStmt.setString(3, operation.getProcessingCode3());
                preparedStmt.setFloat(4, operation.getAmounTransaction4());
                preparedStmt.setTimestamp(5, getDateField7(operation.getTransmissionDateTime7()));
                preparedStmt.setLong(6, Long.valueOf(operation.getSystemTraceAuditNumber11()));
                preparedStmt.setTimestamp(7, getDateField12(operation.getTimeLocalTransaction12()));
                preparedStmt.setString(8, operation.getLocalTransactiondate13());
                preparedStmt.setString(9, operation.getSettlementDate15());
                preparedStmt.setString(10, operation.getDateCapture17());
                preparedStmt.setString(11, operation.getMerchantCategoryCode18());
                preparedStmt.setString(12, operation.getPosEntryMode22());
                preparedStmt.setString(13, operation.getCodeAcquiringInstitution32());
                preparedStmt.setString(14, operation.getForwardingInstitutionCode33());
                preparedStmt.setString(15, operation.getTrack2Data35());
                preparedStmt.setString(16, operation.getRetrievalReferenceNumber37());
                preparedStmt.setString(17, operation.getAuthorizationCode38());
                preparedStmt.setString(18, operation.getResponseCode39());
                preparedStmt.setString(19, operation.getIdentificationReceivingTerminalCard41());
                preparedStmt.setString(20, operation.getNameAndLocationReceiverCard43());
                preparedStmt.setString(21, operation.getTransactionCurrencyCode49());
                preparedStmt.setString(22, operation.getReserved58());
                preparedStmt.setString(23, operation.getAccountIdentification102());
                preparedStmt.setString(24, operation.getAccountIdentification103());
                preparedStmt.setString(25, operation.getTransactionDescription104());
                preparedStmt.setString(26, operation.getReserved123());
                preparedStmt.setString(27, operation.getDestinationRoute());
                // execute the preparedstatement
                preparedStmt.execute();

                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception();
            }
        }

        public Timestamp getDateField7(String field7Value) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, Integer.valueOf(field7Value.substring(0, 2)) - 1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(field7Value.substring(2, 4)));
            cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(field7Value.substring(4, 6)));
            cal.set(Calendar.MINUTE, Integer.valueOf(field7Value.substring(6, 8)));
            cal.set(Calendar.SECOND, Integer.valueOf(field7Value.substring(8, 10)));
            return new Timestamp(cal.getTimeInMillis());
        }

        public Timestamp getDateField12(String field12Value) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(field12Value.substring(0, 2)));
            cal.set(Calendar.MINUTE, Integer.valueOf(field12Value.substring(2, 4)));
            cal.set(Calendar.SECOND, Integer.valueOf(field12Value.substring(4, 6)));
            return new Timestamp(cal.getTimeInMillis());
        }
    }
    
       public  void loadProperties() {
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
       
        

       
    }       
   
}