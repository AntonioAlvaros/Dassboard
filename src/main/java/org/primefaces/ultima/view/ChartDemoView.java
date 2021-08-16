/*
 * Copyright 2009-2014 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.PieChartModel;
import org.primefaces.ultima.exception.EmptyListException;
import static org.primefaces.ultima.view.PollView.loadLocalProperties;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LegendPlacement;
import static org.primefaces.ultima.view.PollView.getBeginningDateTime;
import static org.primefaces.ultima.view.PollView.getCurrentDateTime;
import static org.primefaces.ultima.view.PollView.loadLocalProperties;

@Named( "chartDemoView")
public class ChartDemoView implements Serializable {
   

    private String aproveedCountTransaction;
    private BarChartModel barModel;
    private BarChartModel barModel1;
    private boolean connected;
    private PieChartModel pieModel1;
    private PieChartModel pieModel2;
    

    
    private static String OS = System.getProperty("os.name").toLowerCase();
    private static String fileName = "configDASHBOARD.properties";
    private static Properties prop = new Properties();
    Connection conn;
    
    private int number;

    public void increment() {
        number++;
    }

    public int getNumber() {
        return number;
    }

    public BarChartModel getBarModel1() {
        
//        System.out.println("getBarModel1");
        
        barModel1 = new BarChartModel();
        MapperResponseMontTotal before2 = new MapperResponseMontTotal();
        MapperResponseMontTotal before = new MapperResponseMontTotal();        
        MapperResponseMontTotal current = new MapperResponseMontTotal();        
        
        before2 = getTransactionRejectAndAprovedByMonth(getAfterMonthInit(), getBeforeMonthFinish());
        before = getTransactionRejectAndAprovedByMonth(getAfterMonth2Init(), getBeforeMonth2Finish());
        current = getTransactionRejectAndAprovedByMonth(getBeforeMonthFinish(), getCurrentDateTime());
        
        
        ChartSeries aproveds = new ChartSeries();
        aproveds.setLabel("Aprobadas");
        aproveds.set(before.getMonth(), before.getApprovedCount());
        aproveds.set(before2.getMonth(), before2.getApprovedCount());
        aproveds.set(current.getMonth(), current.getApprovedCount());
    
            
        
        ChartSeries rejects = new ChartSeries();
        rejects.setLabel("Rechazadas");
        rejects.set(before.getMonth(), before.getRejectCount());
        rejects.set(before2.getMonth(), before2.getRejectCount());
        rejects.set(current.getMonth(), current.getRejectCount());
        
        
        
       
        barModel1.addSeries(aproveds);
        barModel1.addSeries(rejects);
        barModel1.setLegendPosition("ne");

        Axis xAxis = barModel1.getAxis(AxisType.X);
        xAxis.setLabel("Mes");

        Axis yAxis = barModel1.getAxis(AxisType.Y);
        yAxis.setLabel("Cantidad de Transacciones");
        yAxis.setMin(0);
        yAxis.setMax(200);
    
        
        return barModel1;
    }

    public void setBarModel1(BarChartModel barModel1) {
        this.barModel1 = barModel1;
    }
    
    
    
    

    @PostConstruct
    public void init() {
//   System.out.println("init");
        loadProperties();
        prop.getProperty("dbdriver");
        
        
        try {
            Class.forName(prop.getProperty("dbdriver"));
            try {
                conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
            } catch (SQLException ex) {
                Logger.getLogger(ChartDemoView.class.getName()).log(Level.SEVERE, null, ex);
            }
            setValueCountAprovedTransaction();
            createBarModels();
            createPieModels();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ChartDemoView.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

	public void itemSelect(ItemSelectEvent event) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Item selected",
                        "Item Index: " + event.getItemIndex() + ", Series Index:" + event.getSeriesIndex());
        
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}


  
    
    public PieChartModel getPieModel1() {
        System.out.println("getPieModel");
        Map<String, String> mapResult = new HashMap<String,String>();
        try {
        mapResult = getResponseCodeMaxUsed();
        Iterator it = mapResult.entrySet().iterator();
        while (it.hasNext()) {
        Map.Entry pair = (Map.Entry)it.next();
        String percent = pair.getValue().toString().split("l")[0];
        String totalTransaction = pair.getValue().toString().split("l")[1];
        pieModel1.set(pair.getKey().toString() + " (" +   new DecimalFormat("#.##").format(Float.valueOf(percent))+ "%)", Integer.valueOf(totalTransaction));
 //       System.out.println(pair.getKey().toString() + " = " + pair.getValue());
        it.remove(); // avoids a ConcurrentModificationException
        }
        return pieModel1;
        } catch (EmptyListException e) {
            e.printStackTrace();
        }
        return pieModel1;
    }

    
    public  Map<String,String> getResponseCodeMaxUsed() throws EmptyListException{
//               System.out.println("getResponseCodeMaxUse");
        loadLocalProperties();
        Statement stmt3;
        Map<String, String> mapResult = new HashMap<String,String>();
        try {
            Class.forName(prop.getProperty("dbdriver"));
            try {
                conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
                stmt3 = conn.createStatement();
                String sql = "(SELECT responseCode rcode,count(*) AS count,\n"
                        + "(SELECT COUNT(id) FROM dashboard.operations WHERE  transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND  messageTypeIdentifier=\"0210\") AS total ,\n"
                        + "(SELECT rc.traslate FROM response_code rc WHERE rc.code = rcode) \n"
                        + "AS name FROM dashboard.operations WHERE  messageTypeIdentifier=\"0210\"  AND  transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' \n"
                        + "GROUP BY responseCode ORDER BY COUNT(responseCode) DESC LIMIT 5)";
//                System.out.println("sql=" + sql);
                ResultSet rs3 = stmt3.executeQuery(sql);
                while (rs3.next()) {
                    if( rs3.getInt("total")== 0){
                     conn.close();
                     throw new EmptyListException("No result value from current Date time");
                    }
                    
                    DecimalFormat df = new DecimalFormat("#.##");
                    String formatted = df.format(2.456345); 
 //                   System.out.println(formatted);


       
                    mapResult.put(rs3.getString("rcode")+" "+ rs3.getString("name"), String.valueOf(Float.valueOf(rs3.getInt("count")) * (100) / Float.valueOf(rs3.getInt("total")))  + "l" + rs3.getInt("count"));
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
    
    
    public String getformatedValue(String decimalNumber){   
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return df.format(decimalNumber);
    }
    
    
    public PieChartModel getPieModel2() {
        return pieModel2;
    }

    
    
 
      
  

    


   

    public BarChartModel getBarModel() {
        
        barModel = new BarChartModel();
        List<MapperResponseTransactionDestination> responseList = getTransactionRejectAndAprovedByDestination();
//        System.out.println("lista long:"+responseList.size());
        
        ChartSeries aproveds = new ChartSeries();
        aproveds.setLabel("Aprobadas");
        for(MapperResponseTransactionDestination c: responseList){
            aproveds.set(c.getDestinationValue(), c.getApprovedCount());
            
        }
        ChartSeries rejects = new ChartSeries();
        rejects.setLabel("Rechazadas");
          for(MapperResponseTransactionDestination c: responseList){
            rejects.set(c.getDestinationValue(), c.getRejectCount());
        }
        barModel.addSeries(aproveds);
        barModel.addSeries(rejects);
        

        
        
        return barModel;
        
        
      
    }

  

    public String getAproveedCountTransaction() {
        return aproveedCountTransaction;
    }

    public void setAproveedCountTransaction(String aproveedCountTransaction) {
        this.aproveedCountTransaction = aproveedCountTransaction;
    }

    private void setValueCountAprovedTransaction() {
        Integer count = 2;
        Statement stmt3;
        try {
            stmt3 = conn.createStatement();
            ResultSet rs3 = stmt3.executeQuery(" SELECT COUNT(*) as 'count' FROM operations WHERE  transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"'");
            while (rs3.next()) {
                count = rs3.getInt("count");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
                   
            Logger.getLogger(ChartDemoView.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(PollView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        this.aproveedCountTransaction = String.valueOf(count);
    }
    
    
    

    
    
    
    
    
    
    public MapperResponseMontTotal getTransactionRejectAndAprovedByMonth(Timestamp beggingDate,Timestamp endingDate) {
        MapperResponseMontTotal responseMontTotalMapper  = new MapperResponseMontTotal();
        loadLocalProperties();
        Statement stmt3;
        try {
            
            Class.forName(prop.getProperty("dbdriver"));
            
            try {
                Calendar mCalendar = Calendar.getInstance(); 
                mCalendar.setTimeInMillis(endingDate.getTime());  
                String month = mCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());        
                responseMontTotalMapper.setMonth(month);
                
                
                
                conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
                stmt3 = conn.createStatement();
                String sql2 = "SELECT (SELECT COUNT(*) FROM dashboard.operations WHERE  transmissionDateTime BETWEEN '"+beggingDate +"' AND '"+ endingDate+"'AND responseCode=\"00\" AND messageTypeIdentifier=\"0210\") AS \"aprovved\" ,\n" + "(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+beggingDate+"' AND '"+endingDate+"' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\")  AS \"reject\"";
 //               System.out.println("sql=" + sql2);
                ResultSet rs3 = stmt3.executeQuery(sql2);
                while (rs3.next()) {
                     responseMontTotalMapper.setApprovedCount(rs3.getInt("aprovved"));
                    responseMontTotalMapper.setRejectCount(rs3.getInt("reject"));

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
        
                return responseMontTotalMapper; 
                
                
    }

    
    
     private  List<MapperResponseTransactionDestination> getRoutNameList() {
        
        List<MapperResponseTransactionDestination> list = new ArrayList<MapperResponseTransactionDestination>();
        loadLocalProperties();
        Statement stmt3;
        try {
            
            Class.forName(prop.getProperty("dbdriver"));
            
            try {
                conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
                stmt3 = conn.createStatement();
                String sql = "SELECT routeName FROM dashboard.operations WHERE  transmissionDateTime BETWEEN '" + getBeginningDateTime() + "' AND '" + getCurrentDateTime() + "' GROUP BY routeName";
//                System.out.println("sql=" + sql);
                ResultSet rs3 = stmt3.executeQuery(sql);
                while (rs3.next()) {
                          MapperResponseTransactionDestination destinationMapper = new MapperResponseTransactionDestination();
                          destinationMapper.setDestinationValue(rs3.getString("routeName"));
                          list.add(destinationMapper);
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
        return list;
                        
     }
     
     
     private  List<MapperResponseTransactionDestination> getTransactionRejectAndAprovedByDestination() {
         List<MapperResponseTransactionDestination> rouDestinations = getRoutNameList();
         
  //       System.out.println("Cantidad de rutas: "+ rouDestinations.size());
         
         List<MapperResponseTransactionDestination> destinationsList = new ArrayList<MapperResponseTransactionDestination>();           

         for(MapperResponseTransactionDestination rd : rouDestinations){
             
  //           System.out.println("iterando ruta = "+rd.getDestinationValue());
              MapperResponseTransactionDestination destinationMapper = new MapperResponseTransactionDestination();
              destinationMapper.setDestinationValue(rd.getDestinationValue());  
               Statement stmt3;
        try {
            
            Class.forName(prop.getProperty("dbdriver"));
            
            try {
                conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
                stmt3 = conn.createStatement();
                
                
                String sql2 = "SELECT (SELECT COUNT(*) FROM dashboard.operations WHERE  transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"'AND responseCode=\"00\" AND  messageTypeIdentifier=\"0210\" AND routeName=\""+  rd.getDestinationValue() +"\") AS \"aprovved\" ,\n"
                        + "(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\" AND routeName = \""+rd.getDestinationValue()+"\")  AS \"reject\"";
                
                
//                System.out.println("sql2="+ sql2);
                
                ResultSet rs4 = stmt3.executeQuery(sql2);
                while (rs4.next()) {
                    destinationMapper.setApprovedCount(rs4.getInt("aprovved"));
                    destinationMapper.setRejectCount(rs4.getInt("reject"));
                }
                destinationsList.add(destinationMapper);
                
//                
//                for(MapperResponseTransactionDestination mp: destinationsList){
//                    System.out.println("******************************************");
//                    System.out.println("..................+......"+mp.getDestinationValue());
//                    System.out.println("..................+......"+mp.getApprovedCount());
//                    System.out.println("..................+......"+mp.getRejectCount());
//                    System.out.println("******************************************");
//                }
//                
                
                
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
        return destinationsList;   
    }
    
    
    
    
    
//    private  List<MapperResponseTransactionDestination> getTransactionRejectAndAprovedByDestination() {
//        
//        System.out.println("Entro en getTransactionRejectAndAprovedByDestination");
//        List<MapperResponseTransactionDestination> destinationsList = new ArrayList<MapperResponseTransactionDestination>();
//        Statement stmt5;
//        Statement stmt6;
//          try {
//            Class.forName(prop.getProperty("dbdriver"));
//            try {
//            stmt5 = conn.createStatement();
//            String sql = "SELECT routeName FROM dashboard.operations WHERE  transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' GROUP BY routeName";
//            ResultSet rs3 = stmt5.executeQuery(sql);
//            while (rs3.next()) {
//                MapperResponseTransactionDestination destinationMapper = new MapperResponseTransactionDestination();
//                destinationMapper.setDestinationValue(rs3.getString("routeName"));
//                stmt6 = conn.createStatement();
//                
//                String sql2 = "SELECT (SELECT COUNT(*) FROM dashboard.operations WHERE  transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"'AND responseCode=\"00\" AND routeName=\""+  destinationMapper.getDestinationValue() +"\") AS \"aprovved\" ,\n"
//                        + "(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\" AND routeName = \""+destinationMapper.getDestinationValue()+"\")  AS \"reject\""; 
//                
//                ResultSet rs4 = stmt6.executeQuery(sql2);
//                while (rs4.next()) {
//                    destinationMapper.setApprovedCount(rs4.getInt("aprovved"));
//                    destinationMapper.setRejectCount(rs4.getInt("reject"));
//                }
//                destinationsList.add(destinationMapper);
//            }
//            } catch (SQLException ex) {
//                Logger.getLogger(ChartDemoView.class.getName()).log(Level.SEVERE, null, ex);
//            } finally {
//                try {
//                    conn.close();
//                } catch (SQLException ex) {    
//                    Logger.getLogger(PollView.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(PollView.class.getName()).log(Level.SEVERE, null, ex);
//        }   
//        return destinationsList;   
//    }
//    
    
    private void createBarModels() {
        createBarModel();
        createBarModel2();

    }
    
      private void createBarModel2() {
        barModel1 = new BarChartModel();
        MapperResponseMontTotal before2 = new MapperResponseMontTotal();
        MapperResponseMontTotal before = new MapperResponseMontTotal();        
        MapperResponseMontTotal current = new MapperResponseMontTotal();        
        
        before2 = getTransactionRejectAndAprovedByMonth(getAfterMonthInit(), getBeforeMonthFinish());
        before = getTransactionRejectAndAprovedByMonth(getAfterMonth2Init(), getBeforeMonth2Finish());
        current = getTransactionRejectAndAprovedByMonth(getBeforeMonthFinish(), getCurrentDateTime());
        
        
        ChartSeries aproveds = new ChartSeries();
        aproveds.setLabel("Aprobadas");
        aproveds.set(before.getMonth(), before.getApprovedCount());
        aproveds.set(before2.getMonth(), before2.getApprovedCount());
        aproveds.set(current.getMonth(), current.getApprovedCount());
    
            
        
        ChartSeries rejects = new ChartSeries();
        rejects.setLabel("Rechazadas");
        rejects.set(before.getMonth(), before.getRejectCount());
        rejects.set(before2.getMonth(), before2.getRejectCount());
        rejects.set(current.getMonth(), current.getRejectCount());
        
        
        
       
        barModel1.addSeries(aproveds);
        barModel1.addSeries(rejects);
    
    }
    
    private void createBarModel() {
        
        
        barModel = new BarChartModel();
        List<MapperResponseTransactionDestination> responseList = getTransactionRejectAndAprovedByDestination();
 //       System.out.println("lista long:"+responseList.size());
        
        ChartSeries aproveds = new ChartSeries();
        aproveds.setLabel("Aprobadas");
        for(MapperResponseTransactionDestination c: responseList){
            aproveds.set(c.getDestinationValue(), c.getApprovedCount());
            
        }
        ChartSeries rejects = new ChartSeries();
        rejects.setLabel("Rechazadas");
          for(MapperResponseTransactionDestination c: responseList){
            rejects.set(c.getDestinationValue(), c.getRejectCount());
        }
        barModel.addSeries(aproveds);
        barModel.addSeries(rejects);
    }

    private String getHoursPass(int hourPass) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -number);
  //      System.out.println(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));
        return String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
    }

    private void createPieModels() {
        createPieModel1();

    }

    private void createPieModel1() {
        pieModel1 = new PieChartModel();

        Map<String, String> mapResult = new HashMap<String, String>();
        try {
            mapResult = getResponseCodeMaxUsed();
            Iterator it = mapResult.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String percent = pair.getValue().toString().split("l")[0];
                String totalTransaction = pair.getValue().toString().split("l")[1];
                pieModel1.set(pair.getKey().toString() + " (" +   new DecimalFormat("#.##").format(Float.valueOf(percent))+ "%)", Integer.valueOf(totalTransaction));
                it.remove(); // avoids a ConcurrentModificationException
            }
            pieModel1.setTitle("Simple Pie");
            pieModel1.setShowDatatip(true);

            pieModel1.setShowDataLabels(true);
            pieModel1.setDataFormat("value");

            pieModel1.setDataLabelFormatString("%dK");
            pieModel1.setLegendPosition("e");
            pieModel1.setExtender("skinPie");
        } catch (EmptyListException ex) {
            ex.printStackTrace();
        }

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
     
     
     ///////////////////////////////////////////////////////
     
     
        public static Timestamp getBeforeMonthFinish(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return new Timestamp(cal.getTimeInMillis());
    }
    
    
    
     public static Timestamp getAfterMonthInit(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        cal.add(Calendar.MONTH, -1);
        cal.add(Calendar.DAY_OF_MONTH, 1);
     //    System.out.println(""+ new Date(cal.getTimeInMillis()).toString());
        return new Timestamp(cal.getTimeInMillis());
    }
     
     
     public static Timestamp getBeforeMonth2Finish(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.add(Calendar.MONTH, -1);
   //     System.out.println(""+ new Date(cal.getTimeInMillis()).toString());
        return new Timestamp(cal.getTimeInMillis());
    }
    
    
    
     public static Timestamp getAfterMonth2Init(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        cal.add(Calendar.MONTH, -1);
        cal.add(Calendar.DAY_OF_MONTH, 1);
           cal.add(Calendar.MONTH, -1);
  //         System.out.println(""+ new Date(cal.getTimeInMillis()).toString());
        return new Timestamp(cal.getTimeInMillis());
    }
    
     
     public static Timestamp getCurrentDate(){
         
        Calendar cal = Calendar.getInstance();
    //    System.out.println(""+ new Date(cal.getTimeInMillis()).toString());
        return new Timestamp(cal.getTimeInMillis());
    }
    
    
}
