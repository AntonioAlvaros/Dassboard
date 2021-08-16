/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.primefaces.ultima.view;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import org.primefaces.ultima.service.SessionValue;
import org.primefaces.ultima.servlet.Operation;
import org.primefaces.ultima.servlet.TCPServerRunner;
import static org.primefaces.ultima.servlet.TCPServerRunner.convertAmount;
import static org.primefaces.ultima.servlet.TCPServerRunner.loadProperties;
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
    private static String fileName = "configDASHBOARD.properties";
    private static Properties prop = new Properties();
    private PieChartModel pieModel1;


    /////////////CounterTemp
    public static Integer tempApproved = 0;
    public static Integer tempReversed = 0;
    public static Integer tempReject = 0;
    public static Integer tempTimeOut = 0;
    
    ////////////////////
     static   Integer countApproved = 0;
     static   Integer countReject = 0;
     static   Integer countTimeOut = 0;
     static    Integer countReversed = 0;

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
    @Push(channel = "counter")
    private PushContext push;

    public void toggle() {
        connected = true;
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
                String sql = "SELECT (SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '" + getBeginningDateTime() + "' AND '" + getCurrentDateTime() + "' AND responseCode=\"00\" AND messageTypeIdentifier=\"0210\") AS \"Aprobadas\",\n"
                        + "(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '" + getBeginningDateTime() + "' AND '" + getCurrentDateTime() + "' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"Rechazadas\",\n"
                        + "(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '" + getBeginningDateTime() + "' AND '" + getCurrentDateTime() + "' AND messageTypeIdentifier =\"0410\") AS \"Reversadas\",\n"
                        + "(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '" + getBeginningDateTime() + "' AND '" + getCurrentDateTime() + "' AND responseCode=\"91\") AS \"TimeOut\" ,\n"
                        + "(SELECT sum(amounTransaction) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '" + getBeginningDateTime() + "' AND '" + getCurrentDateTime() + "' AND responseCode=\"00\" AND messageTypeIdentifier=\"0210\") AS \"sumAprobadas\",\n"
                        + "(SELECT sum(amounTransaction) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '" + getBeginningDateTime() + "' AND '" + getCurrentDateTime() + "' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"sumRechazadas\",\n"
                        + "(SELECT sum(amounTransaction) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '" + getBeginningDateTime() + "' AND '" + getCurrentDateTime() + "' AND messageTypeIdentifier =\"0410\") AS \"sumReversed\"";
//                System.out.println("sql=" + sql);
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
        
        

//        System.out.println("::::::::::::::::::::::::::::::::::::::.");
        tempApproved = countApproved;
        counter.set(Long.valueOf(tempApproved));
        

        tempReject = countReject;
        counterReject.set(Long.valueOf(tempReject));

        tempTimeOut = countTimeOut;
        counterTimeOut.set(Long.valueOf(tempTimeOut));

        tempReversed = countReversed;
        counterReverse.set(Long.valueOf(tempReversed));

        if(rejected>approved){
            getInfoAlert("La cantidad de transacciones rechazas supera las aprobadas");
        }
        try {
            showAlertTimeOut();
        } catch (EmptyListException ex) {
            ex.printStackTrace();
        }
        
        TCPServerRunnerPollView tcpRun = new TCPServerRunnerPollView();
        tcpRun.setDaemon(true);
        tcpRun.start();

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

        loadLocalProperties();
        Statement stmt3;
        try {
            
            Class.forName(prop.getProperty("dbdriver"));
            
            try {
                conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
                stmt3 = conn.createStatement();
                String sql = "SELECT (SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode=\"00\" AND messageTypeIdentifier=\"0210\") AS \"Aprobadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode<>\"00\" AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"Rechazadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND messageTypeIdentifier =\"0410\") AS \"Reversadas\",\n" +
"(SELECT COUNT(*) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode=\"91\") AS \"TimeOut\" ,\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode=\"00\" AND messageTypeIdentifier=\"0210\") AS \"sumAprobadas\",\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND responseCode<>\"00\"  AND responseCode<>\"91\" AND messageTypeIdentifier =\"0210\") AS \"sumRechazadas\",\n" +
"(SELECT sum(amounTransaction) FROM dashboard.operations WHERE transmissionDateTime BETWEEN '"+getBeginningDateTime()+"' AND '"+getCurrentDateTime()+"' AND messageTypeIdentifier =\"0410\") AS \"sumReversed\"";
//                System.out.println("sql=" + sql);
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
        
 //       System.out.println("entro.........................");
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

        private void InitServer() {
  //          System.out.println("Entro en el hilo de inicialización");
            loadProperties();
            System.out.println("puert::"+prop.getProperty("port") );
         ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(Integer.valueOf(prop.getProperty("port")));
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client
            new EchoThread(socket).start();
        }
         

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
            System.out.println("Creando Hilo inicialización");
            InitServer();
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
       
          public class EchoThread extends Thread {
    protected Socket socket;

    public EchoThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {
        InputStream inp = null;
        BufferedReader brinp = null;
        DataOutputStream out = null;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        String line;
        while (true) {
            try {
                line = brinp.readLine();
                if ((line == null)) {
                    socket.close();
                    return;
                } else {
                    
                    
                     try {
                            Operation operation = contructObject(line);
                            System.out.println("operation=" + operation.getResponseCode39());
                            //Esto solo contempla para el contador si la transacción es del día
                            if (isToday(operation.getTransmissionDateTime7())) {
                                if (operation.getMessageTypeIdentifier().equals("0210") && operation.getResponseCode39().equals("00")) {
                                    tempApproved++;
                                    System.out.println("APROBADA::::::::::");
                                    push.send(tempApproved);

                                }
                                if (operation.getMessageTypeIdentifier().equals("0210") && operation.getResponseCode39().equals("91")) {
                                    tempTimeOut++;
                                    System.out.println("TIMEOUT::::::::::");
                                    pushTimeOut.send(tempTimeOut);
                                } else if (operation.getMessageTypeIdentifier().equals("0410")) {
                                    System.out.println("REVERSADA::::::::::");
                                    tempReversed++;
                                    pushReverse.send(tempReversed);
                                } else if ((operation.getMessageTypeIdentifier().equals("0210")) && (!operation.getResponseCode39().equals("00"))) {
                                    System.out.println("RECHAZADA::::::::::");
                                    tempReject++;
                                    pushReject.send(tempReject);
                                }
                            }

                            ThreadSaveTransactionInfo tcpSave = new ThreadSaveTransactionInfo(operation);
                            tcpSave.setDaemon(true);
                            tcpSave.start();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
    
     private Operation contructObject(String response) throws Exception {
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
                operation.setResponseCode39("Error leyendo el mensaje");
                throw new Exception();

            }
            return operation;
        }

    }
    
}

       
       
       
       
       

}
