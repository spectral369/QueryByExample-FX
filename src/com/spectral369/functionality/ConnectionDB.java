/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spectral369.functionality;

import com.spectral369.functionality.UtilitiesQBE;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author spectral369
 */
public abstract class ConnectionDB {
    // Connection requirements

    protected String userName = null;
    protected char[] password = null;
    protected String server = null;
    protected int port = 0;
    /* oracle specific */
    protected String SID = null;
    protected String[] schemas = null;
    /* Oracle Specific */
    Logger log;
    protected boolean workingState = false;
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    protected List<String> QBECols;
    protected List<List<String>> data;

    // sql constructor
    public ConnectionDB(String userName, char[] password, String server,
            int port) {
        this.userName = userName;
        this.password = password;
        this.server = server;
        this.port = port;
        try {
            if (UtilitiesQBE.isLogAcctive) {
                log = UtilitiesQBE.getLogger(ConnectionDB.class);
            }
        } catch (SecurityException | IOException e) {
            // TODO Auto-generated catch block
            e.getMessage();
        }
    }

    // oracle Contructor
    public ConnectionDB(String userName, char[] password, String server,
            int port, String SID) {
        this.userName = userName;
        this.password = password;
        this.server = server;
        this.port = port;
        this.SID = SID;
        try {
            if (UtilitiesQBE.isLogAcctive) {
                log = UtilitiesQBE.getLogger(ConnectionDB.class);
            }
        } catch (SecurityException | IOException e) {
            // TODO Auto-generated catch block
            e.getMessage();
        }
    }

}
