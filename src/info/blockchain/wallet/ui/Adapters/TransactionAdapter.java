package info.blockchain.wallet.ui.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.script.Script;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import info.blockchain.wallet.ui.Utilities.BlockchainUtil;
import info.blockchain.wallet.ui.Utilities.DateUtil;
import info.blockchain.wallet.ui.Utilities.TypefaceUtil;
import piuk.blockchain.android.MyRemoteWallet;
import piuk.blockchain.android.MyTransaction;
import piuk.blockchain.android.R;
import piuk.blockchain.android.util.WalletUtils;

/**
 * Created by Raymond on 28/03/2015.
 */
public class TransactionAdapter extends BaseAdapter {

    private Activity activity;
    private List<MyTransaction> myTransactions;
    private Map<String, String> labelMap;
    private boolean isBTC;
    private String strCurrentFiatCode = "USD";

    public TransactionAdapter(Activity activity) {
        this.activity = activity;
    }


    public Map<String, String> getLabelMap() {
        return labelMap;
    }

    public void setLabelMap(Map<String, String> labelMap) {
        this.labelMap = labelMap;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public List<MyTransaction> getMyTransactions() {
        return myTransactions;
    }

    public void setMyTransactions(List<MyTransaction> myTransactions) {
        this.myTransactions = myTransactions;
    }


    public boolean isBTC() {
        return isBTC;
    }

    public void setIsBTC(boolean isBTC) {
        this.isBTC = isBTC;
    }



    @Override
    public int getCount() {
        return myTransactions != null? myTransactions.size() : 0;
    }

    @Override
    public MyTransaction getItem(int position) {
        return myTransactions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.txs_layout_simple, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }


        BigInteger result = BigInteger.ZERO;
        String addr = null;

        MyTransaction transaction = getItem(position);
        List<TransactionOutput> transactionOutputs = transaction.getOutputs();
        List<TransactionInput> transactionInputs = transaction.getInputs();

        int height = transaction.getHeight();

        viewHolder.ts.setTypeface(TypefaceUtil.getInstance(activity).getRobotoTypeface());
        viewHolder.address.setTypeface(TypefaceUtil.getInstance(activity).getRobotoTypeface());
        viewHolder.result.setTypeface(TypefaceUtil.getInstance(activity).getRobotoTypeface());

//        viewHolder.result.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                currencyToggle();
//            }
//        });

        result = transaction.getResult();
        boolean isSending = true;
        if(result.compareTo(BigInteger.ZERO) == 1) {
            isSending = false;
            viewHolder.result.setBackgroundResource(R.drawable.rounded_view_green);
            if(transactionInputs != null && transactionInputs.size() > 0) {
                addr = transactionInputs.get(0).getFromAddress().toString();
            }
            else {
                addr = "";
            }
        }
        else {
            isSending = true;
            viewHolder.result.setBackgroundResource(R.drawable.rounded_view_red);
            if(transactionOutputs != null && transactionOutputs.size() > 0) {
                TransactionOutput txo = transactionOutputs.get(0);
                Script script = txo.getScriptPubKey();
                if (script != null) {
                    addr = script.getToAddress(MyRemoteWallet.getParams()).toString();
                }
                else {
                    addr = "";
                }
            }
            else {
                addr = "";
            }
        }



        if(labelMap != null && labelMap.size() > 0 && labelMap.get(addr) != null) {
            addr = labelMap.get(addr);
        }

        viewHolder.ts.setText(DateUtil.getInstance(getActivity()).formatted(transaction.getTime().getTime() / 1000L));
//		        tvDirection.setText(isSending ? getActivity().getResources().getString(R.string.SENT) : getActivity().getResources().getString(R.string.RECEIVED) );
        viewHolder.address.setText(addr);

        String amount = null;
        DecimalFormat df = null;
        if(isBTC) {
            df = new DecimalFormat("######0.00######");
            if(result != null) {
                try {
                    amount = df.format(Double.parseDouble(WalletUtils.formatValue(result.abs())));
                } catch (Exception e) {
                    e.printStackTrace();
                    amount = "ERROR";
                }
            }
            else {
                amount = "0.0000";
            }
        }
        else {
            if(result != null) {
                amount = BlockchainUtil.BTC2Fiat(BlockchainUtil.formatBitcoin(result.abs()));
            }
            else {
                amount = "0.00";
            }
        }
        amount += " ";
        amount += (isBTC) ? "BTC" : strCurrentFiatCode;
        viewHolder.result.setText(amount);
        return convertView;

    }

    public class ViewHolder{

        TextView ts;
        TextView address;
        TextView result;

        public ViewHolder(View view){
            ts = (TextView)view.findViewById(R.id.ts);
            address = (TextView)view.findViewById(R.id.address);
            result = (TextView)view.findViewById(R.id.result);
        }
    }

}
