/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.primefaces.ultima.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TCPServerRunner extends Thread {

    private static final int PORT = 54780;
    ServerSocket ss;

    public TCPServerRunner() {

    }

    private void InitServer() {

   
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

}
