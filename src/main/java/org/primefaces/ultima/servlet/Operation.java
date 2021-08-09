package org.primefaces.ultima.servlet;


import java.lang.reflect.Field;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author usuario
 */
public class Operation {
    
     private static final long serialVersionUID = 1L;
  
  private String messageTypeIdentifier;
  private String primaryAccountNumber2;
  private String processingCode3;
  private Float amounTransaction4;
  private String transmissionDateTime7;
  private String systemTraceAuditNumber11;
  private String timeLocalTransaction12;
  private String localTransactiondate13;
  private String settlementDate15;
  private String dateCapture17;
  private String merchantCategoryCode18;
  private String posEntryMode22;
  private String applicationPANnumber23;
  private String codeAcquiringInstitution32;
  private String forwardingInstitutionCode33;
  private String track2Data35;
  private String retrievalReferenceNumber37;
  private String authorizationCode38;
  private String responseCode39;
  private String identificationReceivingTerminalCard41;  
  private String nameAndLocationReceiverCard43;
  private String transactionCurrencyCode49;
  private String reserved58;
  private String accountIdentification102;
  private String accountIdentification103;
  private String transactionDescription104;
  private String reserved123;
  private String reserved127;
  private String destinationRoute;

    public String getMessageTypeIdentifier() {
        return messageTypeIdentifier;
    }

    public void setMessageTypeIdentifier(String messageTypeIdentifier) {
        this.messageTypeIdentifier = messageTypeIdentifier;
    }

    public String getPrimaryAccountNumber2() {
        return primaryAccountNumber2;
    }

    public void setPrimaryAccountNumber2(String primaryAccountNumber2) {
        this.primaryAccountNumber2 = primaryAccountNumber2;
    }

    public String getProcessingCode3() {
        return processingCode3;
    }

    public void setProcessingCode3(String processingCode3) {
        this.processingCode3 = processingCode3;
    }

    public Float getAmounTransaction4() {
        return amounTransaction4;
    }

    public void setAmounTransaction4(Float amounTransaction4) {
        this.amounTransaction4 = amounTransaction4;
    }

    public String getTransmissionDateTime7() {
        return transmissionDateTime7;
    }

    public void setTransmissionDateTime7(String transmissionDateTime7) {
        this.transmissionDateTime7 = transmissionDateTime7;
    }

    public String getSystemTraceAuditNumber11() {
        return systemTraceAuditNumber11;
    }

    public void setSystemTraceAuditNumber11(String systemTraceAuditNumber11) {
        this.systemTraceAuditNumber11 = systemTraceAuditNumber11;
    }

    public String getTimeLocalTransaction12() {
        return timeLocalTransaction12;
    }

    public void setTimeLocalTransaction12(String timeLocalTransaction12) {
        this.timeLocalTransaction12 = timeLocalTransaction12;
    }

    public String getLocalTransactiondate13() {
        return localTransactiondate13;
    }

    public void setLocalTransactiondate13(String localTransactiondate13) {
        this.localTransactiondate13 = localTransactiondate13;
    }

    public String getSettlementDate15() {
        return settlementDate15;
    }

    public void setSettlementDate15(String settlementDate15) {
        this.settlementDate15 = settlementDate15;
    }

    public String getDateCapture17() {
        return dateCapture17;
    }

    public void setDateCapture17(String dateCapture17) {
        this.dateCapture17 = dateCapture17;
    }

    public String getMerchantCategoryCode18() {
        return merchantCategoryCode18;
    }

    public void setMerchantCategoryCode18(String merchantCategoryCode18) {
        this.merchantCategoryCode18 = merchantCategoryCode18;
    }

    public String getPosEntryMode22() {
        return posEntryMode22;
    }

    public void setPosEntryMode22(String posEntryMode22) {
        this.posEntryMode22 = posEntryMode22;
    }

    public String getApplicationPANnumber23() {
        return applicationPANnumber23;
    }

    public void setApplicationPANnumber23(String applicationPANnumber23) {
        this.applicationPANnumber23 = applicationPANnumber23;
    }

    public String getCodeAcquiringInstitution32() {
        return codeAcquiringInstitution32;
    }

    public void setCodeAcquiringInstitution32(String codeAcquiringInstitution32) {
        this.codeAcquiringInstitution32 = codeAcquiringInstitution32;
    }

    public String getForwardingInstitutionCode33() {
        return forwardingInstitutionCode33;
    }

    public void setForwardingInstitutionCode33(String forwardingInstitutionCode33) {
        this.forwardingInstitutionCode33 = forwardingInstitutionCode33;
    }

    public String getTrack2Data35() {
        return track2Data35;
    }

    public void setTrack2Data35(String track2Data35) {
        this.track2Data35 = track2Data35;
    }

    public String getRetrievalReferenceNumber37() {
        return retrievalReferenceNumber37;
    }

    public void setRetrievalReferenceNumber37(String retrievalReferenceNumber37) {
        this.retrievalReferenceNumber37 = retrievalReferenceNumber37;
    }

    public String getAuthorizationCode38() {
        return authorizationCode38;
    }

    public void setAuthorizationCode38(String authorizationCode38) {
        this.authorizationCode38 = authorizationCode38;
    }

    public String getResponseCode39() {
        return responseCode39;
    }

    public void setResponseCode39(String responseCode39) {
        this.responseCode39 = responseCode39;
    }

    public String getIdentificationReceivingTerminalCard41() {
        return identificationReceivingTerminalCard41;
    }

    public void setIdentificationReceivingTerminalCard41(String identificationReceivingTerminalCard41) {
        this.identificationReceivingTerminalCard41 = identificationReceivingTerminalCard41;
    }

    public String getNameAndLocationReceiverCard43() {
        return nameAndLocationReceiverCard43;
    }

    public void setNameAndLocationReceiverCard43(String nameAndLocationReceiverCard43) {
        this.nameAndLocationReceiverCard43 = nameAndLocationReceiverCard43;
    }

    public String getTransactionCurrencyCode49() {
        return transactionCurrencyCode49;
    }

    public void setTransactionCurrencyCode49(String transactionCurrencyCode49) {
        this.transactionCurrencyCode49 = transactionCurrencyCode49;
    }


    public String getReserved58() {
        return reserved58;
    }

    public void setReserved58(String reserved58) {
        this.reserved58 = reserved58;
    }

    public String getAccountIdentification102() {
        return accountIdentification102;
    }

    public void setAccountIdentification102(String accountIdentification102) {
        this.accountIdentification102 = accountIdentification102;
    }

    public String getAccountIdentification103() {
        return accountIdentification103;
    }

    public void setAccountIdentification103(String accountIdentification103) {
        this.accountIdentification103 = accountIdentification103;
    }

    public String getTransactionDescription104() {
        return transactionDescription104;
    }

    public void setTransactionDescription104(String transactionDescription104) {
        this.transactionDescription104 = transactionDescription104;
    }

    public String getReserved123() {
        return reserved123;
    }

    public void setReserved123(String reserved123) {
        this.reserved123 = reserved123;
    }

    public String getReserved127() {
        return reserved127;
    }

    public void setReserved127(String reserved127) {
        this.reserved127 = reserved127;
    }  

    public String getDestinationRoute() {
        return destinationRoute;
    }

    public void setDestinationRoute(String destinationRoute) {
        this.destinationRoute = destinationRoute;
    }
    
    
    
    
  public String toString() {
    StringBuilder sb = new StringBuilder();
    Class<?> thisClass = null;
    try {
      thisClass = Class.forName(getClass().getName());
      Field[] aClassFields = thisClass.getDeclaredFields();
      for (Field f : aClassFields) {
        if (f.get(this) != null) {
          sb.append(f.get(this));
          sb.append(";");
        } 
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return sb.toString().substring(2);
  }
    
}
