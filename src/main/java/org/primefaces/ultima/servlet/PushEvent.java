/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.primefaces.ultima.servlet;

/**
 *
 * @author kerwin
 */
public class PushEvent {
     private final String message;

    public PushEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
