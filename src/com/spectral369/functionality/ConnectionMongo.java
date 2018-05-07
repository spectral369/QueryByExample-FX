/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spectral369.functionality;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.ServerConnectionState;
import com.mongodb.event.ServerClosedEvent;
import com.mongodb.event.ServerDescriptionChangedEvent;
import com.mongodb.event.ServerListener;
import com.mongodb.event.ServerOpeningEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bson.Document;

/**
 *
 * @author spectral369
 */
public class ConnectionMongo extends ConnectionDB implements AutoCloseable {

    private MongoCredential mongoCredential = null;
    private MongoClientOptions mco = null;
    private MongoDatabase db = null;
    private List<String> table = null;
    private QueryData qdata = null;
    private List<String> colls = null;
    private List<String> databases = null;

    private boolean serverAvailable = false;
    private boolean isLogin = false;

    public ConnectionMongo(String userName, char[] password, String server, int port) {
        super(userName, password, server, port);
        this.userName = userName;
        this.password = password;
        this.server = server;
        this.port = port;

        mongoCredential = MongoCredential.createCredential(userName, "admin", password);//admin pentru ca este 
        //baza de date unde am cre4at  userul

        mco = new MongoClientOptions.Builder().addServerListener(new ServerListener() {
            @Override
            public void serverOpening(ServerOpeningEvent event) {

            }

            @Override
            public void serverClosed(ServerClosedEvent event) {
                mc = null;
            }

            @Override
            public void serverDescriptionChanged(ServerDescriptionChangedEvent event) {
                if (event.getNewDescription().isOk() && event.getNewDescription().getState().equals(ServerConnectionState.CONNECTED)) {
                    if (!serverAvailable || !isLogin) {
                        if (UtilitiesQBE.isLogAcctive) {
                            log.log(Level.INFO, "Success", mc);
                        }
                    }
                    serverAvailable = true;
                    isLogin = true;

                } else if (event.getNewDescription().getException() != null
                        && !event.getNewDescription().isOk()) {

                    if (event.getNewDescription().getException().getMessage().contains("socket")) {

                        serverAvailable = false;
                        isLogin = false;

                    } else if (event.getNewDescription().getException().getMessage().contains("authenticating")) {

                        serverAvailable = true;
                        isLogin = false;

                    }
                }
            }
        }).connectTimeout(1800).description("QBE Connection").build();

        mc = new MongoClient(new ServerAddress(server, port), mongoCredential, mco);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ConnectionMongo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setMongoDatabase(String database) {
        if (UtilitiesQBE.isLogAcctive) {
            log.log(Level.INFO, "Database Chosen = {0}", database);
        }
        try {
            db = mc.getDatabase(database);
        } catch (Exception e) {
            e.getMessage();
            if (UtilitiesQBE.isLogAcctive) {
                log.log(Level.WARNING, "failed to select DB", e);
            }
        }

    }

    public boolean checkMongoIsLogin() {
        if (UtilitiesQBE.isLogAcctive) {
            log.log(Level.WARNING, "Server Login success: " + isLogin, mc);
        }
        return isLogin;
    }

    public boolean checkMongoServerConnection() {
        if (UtilitiesQBE.isLogAcctive) {
            log.log(Level.SEVERE, "Server Connection success: " + serverAvailable, mc);
        }
        return serverAvailable;
    }

    protected List<String> getMongoDatabases() {

        List<String> list = new ArrayList<>();
        MongoCursor<String> dbsCursor = mc.listDatabaseNames().iterator();
        while (dbsCursor.hasNext()) {
            list.add(dbsCursor.next());
        }
        databases = list;

        return list;
    }

    protected List<String> getMongoCollections() {
        List<String> list = new ArrayList<>();
        MongoCursor<String> dbsCursor = db.listCollectionNames().iterator();
        while (dbsCursor.hasNext()) {
            list.add(dbsCursor.next());
        }
        table = list;

        return list;
    }

    protected void changeMongoDB(String database) {

        try {
            db = mc.getDatabase(database);
            if (UtilitiesQBE.isLogAcctive) {
                log.log(Level.INFO, "database change: {0}", database);
            }
        } catch (IllegalArgumentException e) {
            if (UtilitiesQBE.isLogAcctive) {
                log.log(Level.INFO, "database change error(not available): {0}", database);
            }
        }

    }

    protected List<String> getMongoColumns(String collection) {
        FindIterable<Document> cur = db.getCollection(collection).find();
        List<String> list = new ArrayList<>();
        if (cur.iterator().hasNext()) {
            Document doc = cur.first();
            Set<String> s = doc.keySet();
            list.addAll(s);
            colls = list;
        }

        return list;
    }

    protected QueryData mongoQBE(String mongoDatabase, String collection,
            String QueryString, List<String> collsSelected) {

        qdata = new QueryData();
        try {
            db = mc.getDatabase(mongoDatabase);

            MongoCollection<Document> col = db.getCollection(collection);

            BasicDBObject query = new BasicDBObject();
            BasicDBObject orQuery = new BasicDBObject();
            qdata.setQBECols(collsSelected);
            List<BasicDBObject> obj1 = new ArrayList<>();

            //    List<QueryBuilder> op = new ArrayList<>();
            for (String s : collsSelected) {

                // query.put(s, "/.*"+QueryString+".*/");
                Pattern regex = Pattern.compile(QueryString);
                obj1.add(new BasicDBObject(s, regex));

            }
            orQuery.put("$or", obj1);

            FindIterable<Document> cursor = col.find(orQuery);
            /*     List<String> subKey =  new ArrayList<>();
           List<String> subValue =  new ArrayList<>();
           List<String> subDoc =  new ArrayList<>();
             int i = 0;
            List<String> line = new ArrayList<>();
            List<List<String>> da = new ArrayList<>();
            MongoCursor<Document> cursor = col.find(orQuery).iterator();
            
         while ( cursor.hasNext() ) {
            Document doc = cursor.next();
      
         for (String s : collsSelected) {
        line.add(doc.get(s).toString());
         i++;
      
        try{
       
       
        Document something = doc.get(s, Document.class);
        subDoc.add(s);
    
      for(String smt: something.keySet()){
       
        subKey.add(smt);
        
      }
      for(Object val: something.values()){
     
        subValue.add(val.toString());
        
      }
     
        }catch(Exception e){
          
        }
             }
       
              da.add(line);
    }
         
         for(List<String> d:da){
             for(String l:d){
               //  System.out.println("---> "+l);
             }
         }
           qdata.setLength(i);
           qdata.setdata(da);
             */

            int i = 0;
            List<String> line = new ArrayList<>();
            List<List<String>> da = new ArrayList<>();
            for (Document doc : cursor) {

                for (String s : collsSelected) {

                    line.add(doc.get(s).toString());
                    i++;
                }
                da.add(line);
            }

            qdata.setdata(da);
            qdata.setLength(i);

        } catch (Exception e) {
            e.getMessage();
        }

        return qdata;
    }

    @Override
    public void close() throws Exception {
        mc.close();
    }

}
