/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.primefaces.ultima.view.c2p;

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

public class PollViewC2P implements Serializable {
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
                   String sql = "SELECT (SELECT COUNT(*) FROM dashboard.operations WHERE  messageTypeIdentifier=\"0210\" AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND processingCode='560009' AND responseCode=\"00\") AS \"Aprobadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND processingCode='560009' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"Rechazadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND processingCode='560009' AND messageTypeIdentifier =\"0410\") AS \"Reversadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND processingCode='560009' AND responseCode=\"91\") AS \"TimeOut\" ,\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"'AND processingCode='560009'  AND responseCode=\"00\") AS \"sumAprobadas\",\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"'AND processingCode='560009'  AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"sumRechazadas\",\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"'AND processingCode='560009'  AND messageTypeIdentifier =\"0410\") AS \"sumReversed\"";
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
        PollView.tempApprovedC2PTest = tempApproved;
        PollView.tempApprovedC2PTest++;

        tempReject = countRejectp2p;
        PollView.tempRejectC2PTest = tempReject;
        PollView.tempRejectC2PTest++;
        counterReject.set(Long.valueOf(tempReject));

        tempTimeOut = countTimeOutp2p;
        counterTimeOut.set(Long.valueOf(tempTimeOut));
        PollView.tempTimeOutC2PTest = tempTimeOut;
        PollView.tempTimeOutC2PTest++;

        tempReversed = countReversedp2p;
        counterReverse.set(Long.valueOf(tempReversed));
        PollView.tempReversedC2PTest = tempReversed;
        PollView.tempReversedC2PTest++;


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
               String sql = "SELECT (SELECT COUNT(*) FROM dashboard.operations WHERE  messageTypeIdentifier=\"0210\" AND transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND processingCode='560009' AND responseCode=\"00\") AS \"Aprobadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND processingCode='560009' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"Rechazadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND processingCode='560009' AND messageTypeIdentifier =\"0410\") AS \"Reversadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND processingCode='560009' AND responseCode=\"91\") AS \"TimeOut\" ,\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"'AND processingCode='560009'  AND responseCode=\"00\") AS \"sumAprobadas\",\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"'AND processingCode='560009'  AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"sumRechazadas\",\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"'AND processingCode='560009'  AND messageTypeIdentifier =\"0410\") AS \"sumReversed\"";
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
                    Logger.getLogger(PollViewC2P.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PollViewC2P.class.getName()).log(Level.SEVERE, null, ex);
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
  
       
       
}