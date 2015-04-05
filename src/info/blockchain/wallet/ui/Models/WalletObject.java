package info.blockchain.wallet.ui.Models;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.google.bitcoin.core.ScriptException;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.script.Script;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import info.blockchain.wallet.ui.Utilities.BlockchainUtil;
import info.blockchain.wallet.ui.Utilities.DateUtil;
import piuk.blockchain.android.MyRemoteWallet;
import piuk.blockchain.android.MyTransaction;
import piuk.blockchain.android.MyTransactionInput;
import piuk.blockchain.android.WalletApplication;

/**
 * Created by Raymond on 27/03/2015.
 */
public class WalletObject {

    private String walletName;
    private String walletAddress;
    private BigInteger walletAmount;
    private List<Object> myTransactions;
    private List<Object> walletTransactions;
    private List<Bitmap> bitmapReferences;

    private boolean watchOnly;
    private BigInteger totalReceived;
    private BigInteger totalSent;
    private Map<String, String> walletLabelMap;

    public WalletObject(String walletName,String walletAddress, BigInteger walletAmount, List<Object>myTransactions, boolean watchOnly){
        this.walletAddress = walletAddress;
        this.walletName = walletName;
        this.walletAmount = walletAmount;
        this.myTransactions = myTransactions;
        this.myTransactions.add(0,new Object());
        this.walletTransactions = new ArrayList<>();
        this.watchOnly = watchOnly;
        this.bitmapReferences = new ArrayList<>();
    }


    public WalletObject(String walletName, String walletAddress,BigInteger walletAmount, boolean watchOnly){
        this.walletName = walletName;
        this.walletAddress = walletAddress;
        this.walletAmount = walletAmount;
        this.myTransactions = new ArrayList<>();
        this.walletTransactions = new ArrayList<>();
        this.myTransactions.add(new Object());
        this.watchOnly = watchOnly;
        this.bitmapReferences = new ArrayList<>();
    }


    public WalletObject(String walletName){
        this.walletName = walletName;
        this.walletAddress = "";
        this.walletAmount = BigInteger.ZERO;
        this.myTransactions = new ArrayList<>();
        this.myTransactions.add(new Object());
        this.walletTransactions = new ArrayList<>();
        this.watchOnly = false;
        this.bitmapReferences = new ArrayList<>();
    }

    public WalletObject(){
        this.walletName = "";
        this.walletAddress = "";
        this.walletAmount = BigInteger.ZERO;
        this.myTransactions = new ArrayList<>();
        this.myTransactions.add(new Object());
        this.walletTransactions = new ArrayList<>();
        this.watchOnly = false;
        this.bitmapReferences = new ArrayList<>();
    }



    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public BigInteger getWalletAmount() {
        return walletAmount;
    }

    public void setWalletAmount(BigInteger walletAmount) {
        this.walletAmount = walletAmount;
    }

    public String getWalletAmountToString(){
        if(walletAmount != BigInteger.ZERO)
            return BlockchainUtil.formatBitcoin(walletAmount);
        else{
            return "0.000";
        }
    }

    public List<Object> getMyTransactions() {
        return myTransactions;
    }

    public void setMyTransactions(List<MyTransaction> myTransactions, Context context) {
        if(myTransactions.size() > 1) {
            this.myTransactions.clear();
        }
        this.myTransactions.add(new Object());
        this.walletTransactions.add(new Object());
        this.myTransactions.addAll(myTransactions);

        HashMap<String,BigInteger> txAmounts = new HashMap<String,BigInteger>();
        final List<MyTransaction> transactionsList = myTransactions;
        final List<MyTransaction> filteredTxList = new ArrayList<MyTransaction>();
        List<Map.Entry<String, String>> addressValueEntryList = new ArrayList<Map.Entry<String, String>>();
        boolean isPartOfTx = false;

        List<MyTransaction> transactionsListClone = transactionsList;
        MyTransaction transaction = null;
        for (Iterator<MyTransaction> it = transactionsListClone.iterator(); it.hasNext();) {
            transaction = it.next();
            List<TransactionOutput> transactionOutputs = transaction.getOutputs();
            List<TransactionInput> transactionInputs = transaction.getInputs();
            isPartOfTx = false;

            for (Iterator<TransactionInput> iti = transactionInputs.iterator(); iti.hasNext();) {
                TransactionInput transactionInput = iti.next();
                try {
                    String addr = transactionInput.getFromAddress().toString();
                    if(addr != null && addr.equals(getWalletAddress())) {
                        filteredTxList.add(transaction);
                        isPartOfTx = true;
                        break;
                    }
                } catch (ScriptException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(isPartOfTx) {
                continue;
            }

            for (Iterator<TransactionOutput> ito = transactionOutputs.iterator(); ito.hasNext();) {
                TransactionOutput transactionOutput = ito.next();
                try {
                    Script script = transactionOutput.getScriptPubKey();
                    String addr = null;
                    if (script != null) {
                        addr = script.getToAddress(MyRemoteWallet.getParams()).toString();
                        if (addr != null && addr.equals(getWalletAddress())) {
                            filteredTxList.add(transaction);
                            break;
                        }
                    }
                } catch (ScriptException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        //
        // build map of addresses <-> amounts for retained txs
        //

        Map<String,String> labels = getWalletLabelMap();


        for (Iterator<MyTransaction> it = filteredTxList.iterator(); it.hasNext();) {

            transaction = it.next();
            txAmounts.clear();
            addressValueEntryList = new ArrayList<Map.Entry<String, String>>();
            BigInteger result = BigInteger.ZERO;
            List<TransactionInput> transactionInputs = transaction.getInputs();

            for (Iterator<TransactionInput> iti = transactionInputs.iterator(); iti.hasNext(); ) {
                TransactionInput transactionInput = iti.next();
                try {
                    String addr = transactionInput.getFromAddress().toString();
                    if (addr != null) {
                        MyTransactionInput ti = (MyTransactionInput) transactionInput;
                        if (addr.equals(getWalletAddress())) {
                            result = result.subtract(ti.getValue());
                        }
                        if (txAmounts.get(addr) != null) {
                            txAmounts.put(addr, txAmounts.get(addr).subtract(ti.getValue()));
                        } else {
                            txAmounts.put(addr, BigInteger.ZERO.subtract(ti.getValue()));
                        }
                    }
                } catch (ScriptException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            List<TransactionOutput> transactionOutputs = transaction.getOutputs();

            for (Iterator<TransactionOutput> ito = transactionOutputs.iterator(); ito.hasNext(); ) {
                TransactionOutput transactionOutput = ito.next();
                try {
                    Script script = transactionOutput.getScriptPubKey();
                    String addr = null;
                    if (script != null) {
                        addr = script.getToAddress(MyRemoteWallet.getParams()).toString();
                        if (addr != null) {
                            if (addr.equals(getWalletAddress())) {
                                result = result.add(transactionOutput.getValue());
                            }
                            if (txAmounts.get(addr) != null) {
                                txAmounts.put(addr, txAmounts.get(addr).add(transactionOutput.getValue()));
                            } else {
                                txAmounts.put(addr, transactionOutput.getValue());
                            }
                        }
                    }
                } catch (ScriptException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            boolean isSending = true;
            if(result.compareTo(BigInteger.ZERO) == 1) {
                isSending = false;
            }
            else {
                isSending = true;
            }


            addressValueEntryList.clear();
            Map.Entry<String, String> addressValueEntry = null;
            for (String key : txAmounts.keySet()) {
                if (key.equals(getWalletAddress())) {
                    continue;
                }

                if (labels.get(key) != null) {
                    addressValueEntry = new AbstractMap.SimpleEntry<String, String>(labels.get(key), BlockchainUtil.formatBitcoin(txAmounts.get(key)) + " BTC");
                } else {
                    addressValueEntry = new AbstractMap.SimpleEntry<String, String>(key, BlockchainUtil.formatBitcoin(txAmounts.get(key)) + " BTC");
                }
                addressValueEntryList.add(addressValueEntry);
            }

            if(addressValueEntryList.size() > 0){
                TransactionObject transactionObject = new TransactionObject(transaction);
                transactionObject.setType(isSending ? TransactionObject.OUTPUT : TransactionObject.INPUT);
                transactionObject.setAddressValueEntryList(addressValueEntryList);
                transactionObject.setTransactionValue(result);
                transactionObject.setDate(DateUtil.getInstance(context).dateFormatted(transaction.getTime().getTime() / 1000));
                walletTransactions.add(transactionObject);
            }



            transactionInputs = null;
            transactionOutputs = null;
            addressValueEntry = null;
        }
        txAmounts = null;
        myTransactions = null;
        addressValueEntryList = null;



//        HashMap<String,BigInteger> txAmounts = new HashMap<String,BigInteger>();
//        List<Map.Entry<String, String>> addressValueEntryList = new ArrayList<Map.Entry<String, String>>();
//
//        for(MyTransaction transaction: myTransactions){
//            BigInteger result = BigInteger.ZERO;
//            txAmounts = new HashMap<String,BigInteger>();
//            addressValueEntryList =  new ArrayList<Map.Entry<String, String>>();
//            List<TransactionInput> transactionInputs = transaction.getInputs();
//            List<TransactionOutput> transactionOutputs = transaction.getOutputs();
//            for (TransactionInput transactionInput: transactionInputs) {
//                try {
//                    String addr = transactionInput.getFromAddress().toString();
//                    if (addr != null && addr.equals(getWalletAddress())) {
//                        result = result.subtract(((MyTransactionInput)transactionInput).getValue());
//                    }
//                    if(!addr.equalsIgnoreCase(walletAddress)){
//                        if(txAmounts.get(addr) != null) {
//                            txAmounts.put(addr, txAmounts.get(addr).subtract(((MyTransactionInput) transactionInput).getValue()));
//                        }
//                        else {
//                            txAmounts.put(addr, ((MyTransactionInput)transactionInput).getValue());
//                        }
//                    }
//
//                } catch (ScriptException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            for (TransactionOutput transactionOutput : transactionOutputs) {
//                try {
//                    Script script = transactionOutput.getScriptPubKey();
//                    String addr = null;
//                    if (script != null) {
//                        addr = script.getToAddress(MyRemoteWallet.getParams()).toString();
//                        if (addr != null && addr.equals(getWalletAddress())) {
//                            result = result.add(transactionOutput.getValue());
//
//                        }
//                        if(!addr.equalsIgnoreCase(walletAddress)) {
//                            if (txAmounts.get(addr) != null) {
//                                txAmounts.put(addr, txAmounts.get(addr).add(transactionOutput.getValue()));
//                            } else {
//                                txAmounts.put(addr, transactionOutput.getValue());
//                            }
//                        }
//
//                    }
//                } catch (ScriptException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            boolean isSending = true;
//            if(result.compareTo(BigInteger.ZERO) == 1) {
//                isSending = false;
//            }
//
//            addressValueEntryList.clear();
//            Map.Entry<String, String> addressValueEntry = null;
//            for (String key : txAmounts.keySet()){
//                if(walletLabelMap.get(key) != null) {
//                    addressValueEntry = new AbstractMap.SimpleEntry<String, String>(walletLabelMap.get(key), BlockchainUtil.formatBitcoin(txAmounts.get(key)) + " BTC");
//                }
//                else {
//                    addressValueEntry = new AbstractMap.SimpleEntry<String, String>(key, BlockchainUtil.formatBitcoin(txAmounts.get(key)) + " BTC");
//                }
//
//                addressValueEntryList.add(addressValueEntry);
//            }
//
//            if(addressValueEntryList.size() > 0) {
//                TransactionObject transactionObject = new TransactionObject(transaction,TransactionObject.OUTPUT);
//                transactionObject.setAddressValueEntryList(addressValueEntryList);
//                transactionObject.setTransactionValue(result);
//                transactionObject.setType(isSending?TransactionObject.OUTPUT:TransactionObject.INPUT);
//                transactionObject.setDate(DateUtil.dateFormatted(transaction.getTime().getTime() / 1000));
//                walletTransactions.add(transactionObject);
//            }
//
//            transactionInputs = null;
//            transactionOutputs = null;
//            myTransactions = null;
//            addressValueEntryList = null;
//            txAmounts = null;
//            addressValueEntry = null;
//        }
    }

    public List<Bitmap> getBitmapReferences() {
        return bitmapReferences;
    }

    public void setBitmapReferences(List<Bitmap> bitmapReferences) {
        this.bitmapReferences = bitmapReferences;
    }

    public boolean isWatchOnly() {
        return watchOnly;
    }

    public void setWatchOnly(boolean watchOnly) {
        this.watchOnly = watchOnly;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public BigInteger getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(BigInteger totalReceived) {
        this.totalReceived = totalReceived;
    }

    public BigInteger getTotalSent() {
        return totalSent;
    }

    public void setTotalSent(BigInteger totalSent) {
        this.totalSent = totalSent;
    }

    public List<Object> getWalletTransactions() {
        return walletTransactions;
    }

    public void setWalletTransactions(List<Object>walletTransactions) {
        this.walletTransactions = walletTransactions;
    }

    public Map<String, String> getWalletLabelMap() {
        return walletLabelMap;
    }

    public void setWalletLabelMap(Map<String, String> walletLabelMap) {
        this.walletLabelMap = walletLabelMap;
    }

}
