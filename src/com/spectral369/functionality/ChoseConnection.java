/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spectral369.functionality;

import com.mongodb.MongoClient;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author spectral369
 */
public class ChoseConnection {

    private Logger logChose;
    protected ConnectionMysql sql = null;
    protected ConnectionOracle oracle = null;
    protected ConnectionMongo mongo = null;
    protected ConnectionDB connDB = null;
    protected Object ins = null;

    protected String userName = null;
    protected char[] password = null;
    protected String server = null;
    protected int port;
    protected String SID = null;
    protected String database = null;

    private void SetLog() {
        try {
            if (UtilitiesQBE.isLogAcctive) {
                logChose = UtilitiesQBE.getLogger(ConnectionMysql.class);
            }
        } catch (SecurityException | IOException e) {
            // TODO Auto-generated catch block
            e.getMessage();
        }
    }

    public ChoseConnection() {
        SetLog();
    }

    protected void setInfo(String userName, String password, String server,
            int port, String SID) {
        this.userName = userName;
        this.password = password.toCharArray();
        this.server = server;
        this.port = port;
        this.SID = SID;

    }

    protected void setInfo(String userName, String password, String server,
            int port) {
        this.userName = userName;
        this.password = password.toCharArray();
        this.server = server;
        this.port = port;

    }

    public Connection getConSQLOROracle() {
        // return connDB.connection;
        if (sql != null) {
            return sql.connection;
        } else if (oracle != null) {
            return oracle.connection;
        } else {
            return null;
        }

    }

    public MongoClient getMongoConn() {
        return mongo.mc;
    }

    public void ChoseConn(int i) {
        switch (i) {
            case 1:
                if (UtilitiesQBE.isLogAcctive) {
                    logChose.log(Level.INFO, "SQL Server Chosen");
                }
                sql = new ConnectionMysql(userName, password, server, port);
                break;
            case 2:
                if (UtilitiesQBE.isLogAcctive) {
                    logChose.log(Level.INFO, "Oracle Server Chosen");
                }
                oracle = new ConnectionOracle(userName, password, server, port, SID);
                break;
            case 3:
                if (UtilitiesQBE.isLogAcctive) {
                    logChose.log(Level.INFO, "Mongo Server Chosen");
                }
                mongo = null;
                mongo = new ConnectionMongo(userName, password, server, port);
                break;

            default:
                if (UtilitiesQBE.isLogAcctive) {
                    logChose.log(Level.WARNING, "Bad Choise");
                }
                break;
        }
    }

    /* public void changeDatabase(String database) throws SQLException {
        sql.changedb(database);
    }*/
}
