/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spectral369.functionality;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author spectral369
 */
public class QueryByExampleAPI {

    ChoseConnection choise = null;
    public Connection connection;
    protected int ConnectonCounter = 0;
    private Logger logger;
    private DatabasesAvailable selectedDB;
    private boolean TCQ = false;
    private QueryData queryData = null;
    private String tableInUse = null;
    private List<String> columnsSelected = null;
    private String QueryString = null;

    public QueryByExampleAPI() {
        if (choise == null) {
            choise = new ChoseConnection();
        }
        try {
            if (UtilitiesQBE.isLogAcctive) {
                logger = UtilitiesQBE.getLogger(QueryByExampleAPI.class);
            }
        } catch (SecurityException | IOException e) {
            // TODO Auto-generated catch block
            e.getMessage();
        }
    }

    public void setDatabase(DatabasesAvailable database) {
        this.selectedDB = database;
    }

    // set database (SelectDataBase)
    public void beginSession() {

        if (selectedDB.equals(DatabasesAvailable.MYSQL)) {
            if (check() == true) {
                // choise.setInfo(userName, String.copyValueOf(password),
                // server, port, SID, database);
                choise.ChoseConn(1);
                queryData = new QueryData();

            }

        }
        if (selectedDB.equals(DatabasesAvailable.ORACLE)) {
            choise.ChoseConn(2);
            queryData = new QueryData();
            choise.oracle.setConnection();
        }

    }

    // check if info is setted
    private boolean check() {
        if (choise.userName == null) {
            throw new IllegalArgumentException("User Name not set!");
        } else if (choise.password == null) {
            throw new IllegalArgumentException("Password not set!");
        } else if (choise.server == null) {
            throw new IllegalArgumentException("Server address not set!");
        } else if (choise.port == 0) {
            throw new IllegalArgumentException("Server port not set!");
        } else {
            return true;
        }
    }

    // set user
    public void setUser(String userName) {

        this.choise.userName = userName;
    }

    // set password
    public void setPassword(String password) {
        this.choise.password = password.toCharArray();
    }

    // set server
    public void setServer(String server) {
        this.choise.server = server;
    }

    // set port
    public void setPort(int port) {
        this.choise.port = port;
    }

    public void setSID(String SID) {
        this.choise.SID = SID;
    }

    // set set database for mysql
    public void setDatabase(String database) {
        // this.database = database;
        this.choise.database = database;

    }

    // set schema for oracle s
    public void setSchema(String schema) {
        this.choise.database = schema;
    }

    // set complete mysql server
    public void setMYSQLServerInformation(String userName, String password,
            String server, int port) {
        /*
		 * this.choise.userName = userName; this.choise.password =
		 * password.toCharArray(); this.choise.server = server; this.choise.port
		 * = port; this.choise.database = database;
         */
        // choise.setInfo(userName, password, server, port, null, null);
        choise.setInfo(userName, password, server, port);

    }

    // test
    public void setTableInUse(String table) {
        this.tableInUse = table;
        this.TCQ = true;
    }

    public void setColumnsInInUse(List<String> columns) {
        this.columnsSelected = columns;
    }

    public void setCurrentQuery(String query) {
        this.QueryString = query;
    }

    // not tested
    public void setTCQ(String Table, String Query, List<String> columns) {
        this.tableInUse = Table;
        this.QueryString = Query;
        this.columnsSelected.addAll(columns);
        TCQ = true;
    }

    // setOracle server
    public void setOracleServerInformation(String userName, String password,
            String server, int port, String SID) {
        choise.setInfo(userName, password, server, port, SID);
    }

    // get Connection
    public Connection getConnection() {
        ConnectonCounter++;
        return choise.getCon();
    }

    public DatabasesAvailable getDatabaseAvailable() {
        return selectedDB;
    }

    public void changeSQLDatabase(String database) throws SQLException {
        choise.sql.changedb(database);
    }

    public ArrayList<Object> getSQLArrayListTables() {
        return choise.sql.getDBTAbles();

    }

    public List<String> getSQLStringArraytables() {
        return choise.sql.getTables();
    }

    public String[] getOracleStringArraySchemas() {
        return choise.oracle.schemas;
    }

    public ArrayList<String> getOracleTables(String Schema) {
        return choise.oracle.getOracleTables(Schema);
    }

    public ArrayList<Object> getSQLColumnsFromTable(String table) {
        return choise.sql.getColumns(table);
    }

    public QueryData SQLQBE(String sqlDatabase, String Table,
            String QueryString, List<String> collsSelected) {
        return choise.sql.QBEQuerySQL(sqlDatabase, Table, QueryString,
                collsSelected);
    }

    public ArrayList<String> getOracleColumns(String Table) {
        return choise.oracle.getOracleColumns(Table);
    }

    public QueryData OracleSimpleQuery(String Query) {
        return choise.oracle.SimpleQuery(Query);
    }

    public QueryData OracleQBE(String schema, String table, String Query,
            String[] columns) {
        return choise.oracle.oracleQBE(schema, table, Query, columns);
    }

    public QueryData SQLSimpleQuery(String Query) {
        return choise.sql.SimpleQuery(Query);
    }

    public List<String> SQLQBEColumns(String sqlDatabase, String Table,
            String QueryString, List<String> collsSelected) {
        return choise.sql.QBEQuerySQL(sqlDatabase, Table, QueryString,
                collsSelected).QBECols;
    }

    // not tested
    public QueryData SQLgetQueryData() {
        if (TCQ == false) {
            return null;
        }
        queryData = choise.sql.QBEQuerySQL(selectedDB.name(),
                tableInUse, QueryString, columnsSelected);
        return queryData;
    }

    public List<List<String>> SQLQBEData(String sqlDatabase, String Table,
            String QueryString, List<String> collsSelected) {
        return choise.sql.QBEQuerySQL(sqlDatabase, Table, QueryString,
                collsSelected).data;
    }

    public QueryData SQLQBE(String sqlDatabase, String Table,
            String QueryString, String collsSelected) {

        //String[] cols = new String[1];
        //cols[0] = collsSelected;
        return choise.sql.QBEQuerySQL(sqlDatabase, Table, QueryString, Arrays.asList(collsSelected));
    }

    public QueryData SQLQBE(String sqldatabase, String table, String Querystring) {

        // TODO Auto-generated method stub
        return choise.sql.QBEQuerySQL(sqldatabase, table, Querystring, null);
    }

    public String[] showSQLAvailableSchemas() {
        return choise.sql.getDatabases();
    }

    public boolean checkSQLConnection() {
        return choise.sql.checkConnection();
    }

    public boolean checkOracleConnection() {
        return choise.oracle.CheckConnectionOracle();
    }

    public void ExportToPDFDefault(QueryData queryData) {
        PDFExport pdf = new PDFExport();
        pdf.defaultExportToPDF(queryData);
    }

    public void ExportToXMLDefault(QueryData queryData) {
        XMLExport xml = new XMLExport();
        try {
            xml.defaultXMLExport(queryData);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(QueryByExampleAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //settings for logger
    public static void setLogger(boolean acctive) {
        UtilitiesQBE.isLogAcctive = acctive;
    }

}
