package info.blockchain.wallet.ui.Models;

import android.graphics.Bitmap;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import piuk.blockchain.android.MyTransaction;

/**
 * Created by Raymond on 28/03/2015.
 */
public class TransactionObject {
    public static int INPUT = 0;
    public static int OUTPUT = 1;
    private MyTransaction transaction;
    private BigInteger transactionValue;
    private String date;
    private int type;
    private Bitmap txBitmap;
    private Bitmap addressBitmap;
    private List<Map.Entry<String, String>> addressValueEntryList = new ArrayList<Map.Entry<String, String>>();

    public Bitmap getAddressBitmap() {
        return addressBitmap;
    }

    public void setAddressBitmap(Bitmap addressBitmap) {
        this.addressBitmap = addressBitmap;
    }

    public Bitmap getTxBitmap() {
        return txBitmap;
    }

    public void setTxBitmap(Bitmap txBitmap) {
        this.txBitmap = txBitmap;
    }

    public TransactionObject(){
        this.transaction = null;
        this.type = INPUT;
        this.transactionValue = BigInteger.ZERO;
    }

    public TransactionObject(MyTransaction myTransaction, int type){
        this.transaction = myTransaction;
        this.type = type;
    }

    public TransactionObject(MyTransaction myTransaction){
        this.transaction = myTransaction;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public MyTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(MyTransaction transaction) {
        this.transaction = transaction;
    }


    public BigInteger getTransactionValue() {
        return transactionValue;
    }

    public void setTransactionValue(BigInteger transactionValue) {
        this.transactionValue = transactionValue;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Map.Entry<String, String>> getAddressValueEntryList() {
        return addressValueEntryList;
    }

    public void setAddressValueEntryList(List<Map.Entry<String, String>> addressValueEntryList) {
        this.addressValueEntryList = addressValueEntryList;
    }

    public void recycleBitmaps(){
        addressBitmap.recycle();
        txBitmap.recycle();
    }
}

