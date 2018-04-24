/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spectral369.functionality;

import java.util.List;

/**
 *
 * @author spectral369
 */
public class QueryData {
    //Vector<?> QBECols = null;
    //Vector<?> data = null;

    List<String> QBECols = null;
    List<List<String>> data = null;
    int length = 0;

    public void setLength(int length) {
        this.length = length;
    }

    public void setQBECols(List<String> cols) {
        this.QBECols = cols;
    }

    public List<String> getQBECols() {
        if (length == 0) {
            return null;
        } else {
            return QBECols;
        }
    }

    public void setdata(List<List<String>> data) {
        this.data = data;
    }

    public List<List<String>> getData() {
        if (length == 0) {
            return null;
        } else {
            return data;
        }
    }

}
