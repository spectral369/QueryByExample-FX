/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spectral369.functionality;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;

/**
 *
 * @author spectral369
 */
public class UtilitiesQBE {

    private static FileHandler fh = null;
    public static boolean isLogAcctive = true;

    public final static synchronized Logger getLogger(Class c) throws SecurityException,
            IOException {
        if (isLogAcctive) {
            Logger logger = Logger.getLogger(c.toString());
            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.FINER);
            Logger.getAnonymousLogger().addHandler(consoleHandler);
            logger.setLevel(Level.FINER);
            if (fh == null) {
                fh = new FileHandler("Log.log", true);
                fh.setFormatter(new SimpleFormatter());
            }
            fh.setFormatter(fh.getFormatter());
            logger.addHandler(fh);
            return logger;
        } else {
            return null;
        }
    }

}
