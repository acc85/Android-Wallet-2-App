package info.blockchain.wallet.ui.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import info.blockchain.wallet.ui.Utilities.BlockchainUtil;
import info.blockchain.wallet.ui.Models.TransactionObject;
import info.blockchain.wallet.ui.Models.WalletObject;
import info.blockchain.wallet.ui.QRActivity;
import info.blockchain.wallet.ui.Utilities.TypefaceUtil;
import piuk.blockchain.android.R;
import piuk.blockchain.android.util.WalletUtils;

/**
 * Created by Raymond on 27/03/2015.
 */
public class TransactionExpandableAdapter extends BaseExpandableListAdapter {

    private Activity activity;
    private boolean isDefaultListView;
    private List<WalletObject> wallets;
    private boolean isBTC = true;
    private String strCurrentFiatCode = "USD";
    private String strCurrentFiatSymbol = "$";
    private static int QR_GENERATION = 1;
    private List<String> activeAddresses;

    public TransactionExpandableAdapter(Activity activity, List<WalletObject> wallets) {
        this.activity = activity;
        this.wallets = wallets;
        String strCurrentFiatCode = BlockchainUtil.getInstance(activity).getFiatCode();
        String strCurrentFiatSymbol = BlockchainUtil.getInstance(activity).getFiatSymbol();
    }

    public TransactionExpandableAdapter() {
        this.activity = null;
        this.wallets = null;
        String strCurrentFiatCode = BlockchainUtil.getInstance(activity).getFiatCode();
        String strCurrentFiatSymbol = BlockchainUtil.getInstance(activity).getFiatSymbol();
    }

    public TransactionExpandableAdapter(Activity activity) {
        this.activity = activity;
        this.wallets = null;
        String strCurrentFiatCode = BlockchainUtil.getInstance(activity).getFiatCode();
        String strCurrentFiatSymbol = BlockchainUtil.getInstance(activity).getFiatSymbol();
    }

    public void setIsBTC(boolean bool){
        isBTC = bool;
    }


    public void setActiveAddresses(List<String> activeAddresses){
        this.activeAddresses = activeAddresses;
    }

    public boolean isBTC() {
        return isBTC;
    }

    public String getStrCurrentFiatCode() {
        return strCurrentFiatCode;
    }

    public void setStrCurrentFiatCode(String strCurrentFiatCode) {
        this.strCurrentFiatCode = strCurrentFiatCode;
    }

    public String getStrCurrentFiatSymbol() {
        return strCurrentFiatSymbol;
    }

    public void setStrCurrentFiatSymbol(String strCurrentFiatSymbol) {
        this.strCurrentFiatSymbol = strCurrentFiatSymbol;
    }

    public List<WalletObject> getWallets() {
        return wallets;
    }

    public void setWallets(List<WalletObject> wallets) {
        this.wallets = wallets;
    }

    @Override
    public int getGroupCount() {
        return wallets != null ? wallets.size() : 0;
    }

    @Override
    public int getChildrenCount(int i) {
        return wallets.get(i).getWalletTransactions().size();
    }

    @Override
    public WalletObject getGroup(int i) {
        return wallets.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return wallets.get(i).getWalletTransactions().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder = null;
        if(view == null){
            view = LayoutInflater.from(activity).inflate(R.layout.balance_wallet_header, null, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)view.getTag();
        }
        WalletObject walletObject = getGroup(i);
        String amount = "";
        if(isBTC) {
            DecimalFormat df = new DecimalFormat("######0.0000");
            if(walletObject.getWalletAmount() != null) {
                try {
                    amount = df.format(NumberFormat.getInstance().parse(walletObject.getWalletAmountToString()).doubleValue());
                } catch (ParseException e) {
                    e.printStackTrace();
                    amount = "ERROR";
                }
            }
            else {
                amount = "0.0000";
            }
        }
        else {
            if(walletObject.getWalletAmount() != null) {
                amount = BlockchainUtil.BTC2Fiat(walletObject.getWalletAmountToString());
            }
            else {
                amount = "0.00";
            }
        }

        viewHolder.amount.setText(amount);

        if (walletObject.isWatchOnly()) {
            if (b)
                viewHolder.addressType.setImageResource(R.drawable.address_watch);
            else
                viewHolder.addressType.setImageResource(R.drawable.address_watch_inactive);
        }else {
            if (b)
                viewHolder.addressType.setImageResource(R.drawable.address_active);
            else
                viewHolder.addressType.setImageResource(R.drawable.address_inactive);
        }
        if(walletObject.getWalletName() != null) {
            viewHolder.address.setText(walletObject.getWalletName().length() > 15 ? walletObject.getWalletName().substring(0, 15) + "..." : walletObject.getWalletName());
        }else{
            viewHolder.address.setText(walletObject.getWalletAddress().length() > 15 ? walletObject.getWalletAddress().substring(0, 15) + "..." : walletObject.getWalletAddress());
        }
        viewHolder.currencyCode.setText(isBTC ? "BTC" : strCurrentFiatCode);
        return view;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);


    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        final WalletObject walletObject = wallets.get(i);
        Object object = walletObject.getWalletTransactions().get(i1);
        ViewHolderOne viewHolderOne;
        if (view == null) {
            view = LayoutInflater.from(activity).inflate(R.layout.balance_expandable_view_one, null);
            viewHolderOne = new ViewHolderOne(view);
            view.setTag(viewHolderOne);
        } else {
            viewHolderOne = (ViewHolderOne) view.getTag();
        }
        if(i1 == 0) {
//            viewHolderOne.progression_received_total_type.setTypeface(TypefaceUtil.getInstance(getActivity()).getRobotoTypeface());
            viewHolderOne.transactionListItemLayout.setVisibility(View.GONE);
            viewHolderOne.balanceLayout.setVisibility(View.VISIBLE);
            viewHolderOne.progression_sent_total_type.setTextColor(0xFF9b9b9b);
            viewHolderOne.progression_sent_total_type.setText("SENT");
//            viewHolderOne.progression_sent_amount.setTypeface(TypefaceUtil.getInstance(getActivity()).getRobotoTypeface());
            viewHolderOne.progression_sent_amount.setTextColor(0xFF9b9b9b);
            viewHolderOne.progression_sent_amount.setText(BlockchainUtil.formatBitcoin(walletObject.getTotalSent()) + " BTC");
            viewHolderOne.progression_sent_bar.setMax(100);

//            ((TextView)progression_received.findViewById(R.id.total_type)).setTypeface(TypefaceUtil.getInstance(getActivity()).getRobotoTypeface());
            viewHolderOne.progression_received_total_type.setTextColor(0xFF9b9b9b);
            viewHolderOne.progression_received_total_type.setText("RECEIVED");
//            ((TextView)progression_received.findViewById(R.id.amount)).setTypeface(TypefaceUtil.getInstance(getActivity()).getRobotoTypeface());
            viewHolderOne.progression_received_amount.setTextColor(0xFF9b9b9b);
            viewHolderOne.progression_received_amount.setText(BlockchainUtil.formatBitcoin(walletObject.getTotalReceived()) + " BTC");
            viewHolderOne.progression_received_bar.setMax(100);

            viewHolderOne.progression_sent_bar.setProgress((int) ((walletObject.getTotalSent().doubleValue() / (walletObject.getTotalSent().doubleValue() + walletObject.getTotalReceived().doubleValue())) * 100));
            viewHolderOne.progression_sent_bar.setProgressDrawable(activity.getResources().getDrawable(R.drawable.progress_red2));
            viewHolderOne.progression_received_bar.setProgress((int) ((walletObject.getTotalReceived().doubleValue() / (walletObject.getTotalSent().doubleValue() + walletObject.getTotalReceived().doubleValue())) * 100));
            viewHolderOne.progression_received_bar.setProgressDrawable(activity.getResources().getDrawable(R.drawable.progress_green2));

            viewHolderOne.balance_qr_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Address", walletObject.getWalletAddress());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(activity, R.string.address_copied_clipboard, Toast.LENGTH_LONG).show();
                    Intent intent;
                    intent = new Intent(activity, QRActivity.class);
                    intent.putExtra("BTC_ADDRESS", walletObject.getWalletAddress());
                    activity.startActivityForResult(intent, QR_GENERATION);
                }
            });
        }else {
            TransactionObject myTransaction = null;
            if (object instanceof TransactionObject) {
                myTransaction = (TransactionObject) object;
                final ViewHolderOne finalViewHolderTwo = viewHolderOne;
                viewHolderOne.transactionListItemLayout.setVisibility(View.VISIBLE);
                viewHolderOne.balanceLayout.setVisibility(View.GONE);
                finalViewHolderTwo.ts.setText(myTransaction.getDate());

                finalViewHolderTwo.txbitmap.setImageBitmap(myTransaction.getTxBitmap());
                finalViewHolderTwo.address.setImageBitmap(myTransaction.getAddressBitmap());
                finalViewHolderTwo.transactionAmount.setTypeface(TypefaceUtil.getInstance(activity).getGravityBoldTypeface());
                finalViewHolderTwo.transactionAmount.setTextColor(BlockchainUtil.BLOCKCHAIN_RED);
                if(myTransaction.getType() == TransactionObject.INPUT)
                    finalViewHolderTwo.transactionAmount.setTextColor(BlockchainUtil.BLOCKCHAIN_GREEN);

                if(isBTC) {
                    finalViewHolderTwo.transactionAmount.setText(BlockchainUtil.formatBitcoin(myTransaction.getTransactionValue()) + " BTC");
                }
                else {
                    finalViewHolderTwo.transactionAmount.setText((BlockchainUtil.BTC2Fiat(WalletUtils.formatValue(myTransaction.getTransactionValue())) + " " + strCurrentFiatCode));
                }
            }

        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        if(i1 == 0)
            return false;
        else
            return true;
    }

    private class ViewHolder{

        ImageView addressType;
        TextView address;
        TextView amount;
        TextView currencyCode;

        public ViewHolder(View view){
            addressType = (ImageView)view.findViewById(R.id.address_type);
            address = (TextView)view.findViewById(R.id.address);
            amount = (TextView)view.findViewById(R.id.amount);
            currencyCode = (TextView)view.findViewById(R.id.currency_code);

        }
    }

    private class ViewHolderOne{
        View view;
        View balanceLayout;
        View transactionListItemLayout;
        LinearLayout progression_sent;
        TextView progression_sent_total_type;
        TextView progression_sent_amount;
        ProgressBar progression_sent_bar;
        LinearLayout progression_received;
        TextView progression_received_total_type;
        TextView progression_received_amount;
        ProgressBar progression_received_bar;
        ImageView balance_qr_icon;
        ImageView txbitmap;
        ImageView address;
        TextView transactionAmount;
        TextView ts;

        public ViewHolderOne(View view){
           balanceLayout = view.findViewById(R.id.balance_ext);
           transactionListItemLayout = view.findViewById(R.id.transaction_list_item);
           txbitmap = (ImageView)view.findViewById(R.id.txbitmap);
           address = (ImageView)view.findViewById(R.id.address);
           transactionAmount = (TextView)view.findViewById(R.id.transactionAmount);
           progression_received = (LinearLayout) view.findViewById(R.id.progression_received);
           progression_received_amount = (TextView) progression_received.findViewById(R.id.amount);
           progression_received_total_type = (TextView) progression_received.findViewById(R.id.total_type);
           progression_received_bar = (ProgressBar) progression_received.findViewById(R.id.bar);
           progression_sent = (LinearLayout) view.findViewById(R.id.progression_sent);
           progression_sent_amount = (TextView) progression_sent.findViewById(R.id.amount);
           progression_sent_total_type = (TextView) progression_sent.findViewById(R.id.total_type);
           progression_sent_bar = (ProgressBar) progression_sent.findViewById(R.id.bar);
           balance_qr_icon = (ImageView)view.findViewById(R.id.balance_qr_icon);
           ts= (TextView)view.findViewById(R.id.ts);
        }
    }

}
