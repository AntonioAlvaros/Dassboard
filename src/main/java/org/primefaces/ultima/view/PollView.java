/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.primefaces.ultima.view;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.model.chart.PieChartModel;
import org.primefaces.ultima.exception.EmptyListException;
import static org.primefaces.ultima.view.ChartDemoView.getBeginningDateTime;
import static org.primefaces.ultima.view.ChartDemoView.getCurrentDateTime;


/**
 *
 * @author usuario
 */
@Named
@ViewScoped

public class PollView implements Serializable {

    private static String OS = System.getProperty("os.name").toLowerCase();
    private static String fileName = "config.properties";
    private static Properties prop = new Properties();
    private PieChartModel pieModel1;
    Connection conn;
    
    private Float sumApproved;
    private Float sumReject;
    private Float sumReversed;
    
    private String information;
    private int approved;
    private int rejected;
    private int timeout;
    private int reversed;

    
   @PostConstruct
    public void init() {
       
        increment();
        
        
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
                String sql = "SELECT (SELECT COUNT(*) FROM dasshboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode=\"00\") AS \"Aprobadas\",\n" +
"(SELECT COUNT(*) FROM dasshboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"Rechazadas\",\n" +
"(SELECT COUNT(*) FROM dasshboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND messageTypeIdentifier =\"0410\") AS \"Reversadas\",\n" +
"(SELECT COUNT(*) FROM dasshboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode=\"91\") AS \"TimeOut\" ,\n" +
"(SELECT sum(amounTransaction) FROM dasshboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode=\"00\") AS \"sumAprobadas\",\n" +
"(SELECT sum(amounTransaction) FROM dasshboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"sumRechazadas\",\n" +
"(SELECT sum(amounTransaction) FROM dasshboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND messageTypeIdentifier =\"0410\") AS \"sumReversed\"";
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
                    Logger.getLogger(PollView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PollView.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("entro.........................");
        approved = countApproved;
        rejected = countReject;
        timeout = countTimeOut;
        reversed = countReversed;
        
        if(rejected>approved){
            getInfoAlert("La cantidad de transacciones rechazas supera las aprobadas");
        }
        
        try {
            showAlertTimeOut();
        } catch (EmptyListException ex) {
           ex.printStackTrace();
        }
                
    }
    
    
         public  Map<String,String> getRouteNameMaxUsed() throws EmptyListException{
        loadLocalProperties();
        Statement stmt3;
        Map<String, String> mapResult = new HashMap<String,String>();
        try {
            Class.forName(prop.getProperty("dbdriver"));
            try {
                conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
                stmt3 = conn.createStatement();
                
                String sql = "SELECT routeName,count(*) AS 'ct' FROM dasshboard.operations \n" +
                             "WHERE transmissionDateTime\n" +
                            " BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' GROUP BY routeName;";
                
                ResultSet rs3 = stmt3.executeQuery(sql);
                while (rs3.next()) {
                 
                    mapResult.put(rs3.getString("routeName"),rs3.getString("ct"));
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

                    String sql = "SELECT responseCode FROM dasshboard.operations \n"
                            + "WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND  '"+getCurrentDateTime()+"'\n"
                            + "AND routeName ='"+pair.getKey() +"' ORDER BY ID DESC LIMIT 5";

                    ResultSet rs3 = stmt3.executeQuery(sql);
                    while (rs3.next()) {

                        if(rs3.getString("responseCode").equals("91")){
                            counter++;
                        }
                        
                        if(counter>=5){
                           getInfoAlert("Gran Cantidad de timeOut en dirección a la red "+pair.getKey() ); 
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
        System.out.println(propertiesSource);
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
          
          
  
     public static Timestamp getBeginningDateTime(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return new Timestamp(cal.getTimeInMillis());
    }
    
     public static Timestamp getCurrentDateTime(){
        Calendar cal = Calendar.getInstance();
        return new Timestamp(cal.getTimeInMillis());
    }
 
    
}