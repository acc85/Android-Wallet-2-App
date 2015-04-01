package info.blockchain.wallet.ui;

import info.blockchain.api.ExchangeRates;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import info.blockchain.wallet.ui.Adapters.TransactionAdapter;
import info.blockchain.wallet.ui.Adapters.TransactionExpandableAdapter;
import info.blockchain.wallet.ui.Models.TransactionObject;
import info.blockchain.wallet.ui.Models.WalletObject;
import info.blockchain.wallet.ui.Utilities.BlockchainUtil;
import info.blockchain.wallet.ui.Utilities.TimeOutUtil;
import info.blockchain.wallet.ui.Utilities.TxNotifUtil;
import info.blockchain.wallet.ui.Utilities.TypefaceUtil;
import info.blockchain.wallet.ui.Utilities.WalletUtil;
import piuk.blockchain.android.EventListeners;
import piuk.blockchain.android.MyRemoteWallet;
import piuk.blockchain.android.MyTransaction;
import piuk.blockchain.android.R;
import piuk.blockchain.android.util.ConnectivityStatus;
import com.google.bitcoin.core.Transaction;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnTouchListener;

@SuppressLint("NewApi")
public class BalanceFragment extends Fragment   {

	private View rootView = null;
	private TextView tViewCurrencySymbol = null;
	private TextView tViewAmount1 = null;
	private TextView tViewAmount2 = null;
	private ListView transactionListView;
	private ExpandableListView balanceExpandView;
	private View layoutProgressContainer;

	private List<Bitmap> recyclingBitmaps;
	private MyRemoteWallet remoteWallet;

	private List<WalletObject> wallets;

	private TransactionExpandableAdapter transactionExpandableAdapter;
	private TransactionAdapter transactionAdapter;
	private boolean isBTC = true;
	private String strCurrentFiatSymbol = "$";
	private String strCurrentFiatCode = "USD";
	
	private static int QR_GENERATION = 1;
	private static int TX_ACTIVITY = 2;

	public static final String ACTION_INTENT = "info.blockchain.wallet.ui.BalanceFragment.REFRESH";
	
	private boolean isDefaultListView = true;
	private ImageView ivBalances = null;
	private ImageView ivTx = null;
	private TextView tListViewTitle = null;
	private Map<String,String> labels;

    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ACTION_INTENT.equals(intent.getAction())) {
        		refreshPayload();
        		ExchangeRates fxRates = new ExchangeRates();
                DownloadFXRatesTask task = new DownloadFXRatesTask(context, fxRates);
                task.execute(fxRates.getUrl());
				updateLists();
            }
        }
    };

	private EventListeners.EventListener eventListener = new EventListeners.EventListener() {
		@Override
		public String getDescription() {
			return "Wallet Balance Listener";
		}

		@Override
		public void onCoinsSent(final Transaction tx, final long result) {
			
    		try {
        		WalletUtil.getInstance(getActivity()).getWalletApplication().doMultiAddr(false, null);
    		}
    		catch(Exception e) {
        		Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
    		}
			updateLists();

		}

		@Override
		public void onCoinsReceived(final Transaction tx, final long result) {
			((MainActivity)getActivity()).navigateToTabContent(1);
			
    		try {
        		WalletUtil.getInstance(getActivity()).getWalletApplication().doMultiAddr(false, null);
    		}
    		catch(Exception e) {
        		Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
    		}
			updateLists();
		}

		@Override
		public void onTransactionsChanged() {
			updateLists();
		}

		@Override
		public void onWalletDidChange() {
			updateLists();
		}
		
		@Override
		public void onCurrencyChanged() {
			updateLists();
		}
	};

	public void updateLists(){
		try{
			setExandableList();
		}catch (Exception e){
			e.printStackTrace();
		}

		try{
			setTranactionList();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        IntentFilter filter = new IntentFilter(ACTION_INTENT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
		recyclingBitmaps = new ArrayList<>();
		wallets = new ArrayList<>();
        rootView = inflater.inflate(R.layout.fragment_balance, container, false);

		layoutProgressContainer = rootView.findViewById(R.id.layoutProgressContainer);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isDefaultListView = prefs.getBoolean("defaultTxView", true);
        
        strCurrentFiatCode = BlockchainUtil.getInstance(getActivity()).getFiatCode();
        strCurrentFiatSymbol = BlockchainUtil.getInstance(getActivity()).getFiatSymbol();


        tListViewTitle = (TextView)rootView.findViewById(R.id.listviewTitle);
        tListViewTitle.setTypeface(TypefaceUtil.getInstance(getActivity()).getRobotoTypeface());

        ivBalances = (ImageView)rootView.findViewById(R.id.balances);
        ivTx = (ImageView)rootView.findViewById(R.id.tx);
		balanceExpandView = (ExpandableListView)rootView.findViewById(R.id.balanceExpandView);
		transactionListView = (ListView) rootView.findViewById(R.id.txList);
        if(isDefaultListView) {
        	ivBalances.setImageResource(R.drawable.balances_icon_active);
        	ivTx.setImageResource(R.drawable.transactions_icon);
            tListViewTitle.setText(R.string.balances);
			balanceExpandView.setVisibility(View.VISIBLE);
			transactionListView.setVisibility(View.INVISIBLE);
        }
        else {
        	ivBalances.setImageResource(R.drawable.balances_icon);
        	ivTx.setImageResource(R.drawable.transactions_icon_active);
            tListViewTitle.setText(R.string.transactions);
			transactionListView.setVisibility(View.VISIBLE);
			balanceExpandView.setVisibility(View.INVISIBLE);
        }
        
        ivBalances.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	isDefaultListView = true;
                tListViewTitle.setText(R.string.balances);
            	ivBalances.setImageResource(R.drawable.balances_icon_active);
            	ivTx.setImageResource(R.drawable.transactions_icon);
				transactionListView.setVisibility(View.INVISIBLE);
				balanceExpandView.setVisibility(View.VISIBLE);
				Editor edit = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
				edit.putBoolean("defaultTxView", true);
				edit.commit();
				try {
					setExandableList();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
            }
        });

        ivTx.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	isDefaultListView = false;
                tListViewTitle.setText(R.string.transactions);
            	ivBalances.setImageResource(R.drawable.balances_icon);
            	ivTx.setImageResource(R.drawable.transactions_icon_active);
				transactionListView.setVisibility(View.VISIBLE);
				balanceExpandView.setVisibility(View.INVISIBLE);
				Editor edit = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
				edit.putBoolean("defaultTxView", false);
				edit.commit();
				setTranactionList();
				return false;
            }
        });

        tViewCurrencySymbol = (TextView)rootView.findViewById(R.id.currency_symbol);
        tViewCurrencySymbol.setTypeface(TypefaceUtil.getInstance(getActivity()).getBTCTypeface());
        tViewCurrencySymbol.setText(Character.toString((char) TypefaceUtil.getInstance(getActivity()).getBTCSymbol()));
        tViewCurrencySymbol.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				currencyToggle();
			}
		});

        tViewAmount1 = (TextView)rootView.findViewById(R.id.amount1);
        tViewAmount1.setTypeface(TypefaceUtil.getInstance(getActivity()).getRobotoLightTypeface());
        tViewAmount2 = (TextView)rootView.findViewById(R.id.amount2);
		remoteWallet = WalletUtil.getInstance(getActivity()).getRemoteWallet();
		if(remoteWallet != null) {
	        tViewAmount1.setText(BlockchainUtil.formatBitcoin(remoteWallet.getBalance()));
	        tViewAmount2.setText(strCurrentFiatSymbol + BlockchainUtil.BTC2Fiat(BlockchainUtil.formatBitcoin(remoteWallet.getBalance())));
		}
		else {
	        tViewAmount1.setText("0");
	        tViewAmount2.setText(strCurrentFiatSymbol + BlockchainUtil.BTC2Fiat("0"));
		}


		balanceExpandView.setGroupIndicator(null);
		balanceExpandView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
				return false;
			}
		});


		transactionExpandableAdapter = new TransactionExpandableAdapter(getActivity());
		balanceExpandView.setAdapter(transactionExpandableAdapter);
		transactionExpandableAdapter.setIsBTC(isBTC);
		balanceExpandView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
				if (i1 != 0) {
					TransactionObject transactionObject = (TransactionObject) balanceExpandView.getExpandableListAdapter().getChild(i, i1);
					MyTransaction myTransaction = transactionObject.getTransaction();
					TimeOutUtil.getInstance().updatePin();
					Intent intent;
					intent = new Intent(getActivity(), TxActivity.class);
					intent.putExtra("TX", myTransaction.getHashAsString());
					intent.putExtra("TS", myTransaction.getTime().getTime() / 1000);
					intent.putExtra("RESULT", BlockchainUtil.formatBitcoin(transactionObject.getTransactionValue().abs()));
					intent.putExtra("SENDING", transactionObject.getType() == TransactionObject.OUTPUT ? true : false);
					intent.putExtra("CURRENCY", strCurrentFiatCode);
					startActivityForResult(intent, TX_ACTIVITY);
				}
				return false;
			}
		});
			setExandableList();

		transactionAdapter = new TransactionAdapter(getActivity());
		transactionListView.setAdapter(transactionAdapter);
		transactionAdapter.setIsBTC(isBTC);
		transactionAdapter.setStrCurrentFiatCode(BlockchainUtil.getInstance(getActivity()).getFiatCode());

		transactionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				MyTransaction transaction = (MyTransaction)transactionListView.getAdapter().getItem(i);

				TimeOutUtil.getInstance().updatePin();
				Intent intent;
				intent = new Intent(getActivity(), TxActivity.class);
				intent.putExtra("TX", transaction.getHashAsString());
				intent.putExtra("TS", transaction.getTime().getTime() / 1000);
				intent.putExtra("RESULT", BlockchainUtil.formatBitcoin(transaction.getResult().abs()));
				intent.putExtra("SENDING", transaction.getResult().compareTo(BigInteger.ZERO) == 1 ? false : true);
				intent.putExtra("CURRENCY", strCurrentFiatCode);
				startActivityForResult(intent, TX_ACTIVITY);
			}
		});
		EventListeners.addEventListener(eventListener);

        return rootView;
    }

	public void setTranactionList(){
		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				try {
					final List<MyTransaction> transactionsList = remoteWallet.getTransactions();
					labels = remoteWallet.getLabelMap();
					transactionAdapter.setMyTransactions(transactionsList);
					transactionAdapter.setIsBTC(isBTC);
					transactionAdapter.setLabelMap(labels);
					transactionAdapter.setStrCurrentFiatCode(BlockchainUtil.getInstance(getActivity()).getFiatCode());
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							transactionAdapter.notifyDataSetChanged();
						}
					});
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});

	}

	public void setExandableList(){
		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				try {
					List<Object> transactionObjects = new ArrayList<>();
					for(WalletObject walletObject: wallets){
						transactionObjects.addAll(walletObject.getWalletTransactions());
					}
					wallets = remoteWallet.getWalletAddressesAndBalanceAndWatchType();
					transactionExpandableAdapter.setIsBTC(isBTC);
					transactionExpandableAdapter.setStrCurrentFiatCode(BlockchainUtil.getInstance(getActivity()).getFiatCode());
					transactionExpandableAdapter.setStrCurrentFiatSymbol(BlockchainUtil.getInstance(getActivity()).getFiatSymbol());
					transactionExpandableAdapter.setWallets(wallets);
					for (WalletObject wallet : wallets) {
						for (Object object : wallet.getWalletTransactions()) {
							TransactionObject transactionObject = null;
							if(object instanceof TransactionObject) {
								transactionObject = (TransactionObject) object;
								TxBitmap txBitmap = new TxBitmap(getActivity(), transactionObject.getAddressValueEntryList());
								Bitmap transActionBitmap = txBitmap.createArrowsBitmap(200, TxBitmap.SENDING, transactionObject.getAddressValueEntryList().size());
								if (transactionObject.getType() == TransactionObject.INPUT) {
									txBitmap = new TxBitmap(getActivity(), transactionObject.getAddressValueEntryList().subList(0, 1));
									transActionBitmap = txBitmap.createArrowsBitmap(200, TxBitmap.RECEIVING, 1);
								}
								transactionObject.setTxBitmap(transActionBitmap);
								transactionObject.setAddressBitmap(txBitmap.createListBitmap(200));
								((MainActivity) getActivity()).getRecyclingBitmaps().add(transactionObject.getAddressBitmap());
								((MainActivity) getActivity()).getRecyclingBitmaps().add(transactionObject.getAddressBitmap());
							}
						}
					}
					for(Object object: transactionObjects){
						TransactionObject transactionObject = null;
						if(object instanceof TransactionObject) {
							transactionObject = (TransactionObject) object;
							transactionObject.recycleBitmaps();
						}
					}
					transactionObjects = null;
					System.gc();

					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							transactionExpandableAdapter.notifyDataSetChanged();
						}
					});
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public void setRefreshView(boolean  bool) throws Exception{
		if(bool)
			layoutProgressContainer.setVisibility(View.VISIBLE);
		else
			layoutProgressContainer.setVisibility(View.GONE);
	}

	@Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser) {
        	System.gc();
            strCurrentFiatCode = BlockchainUtil.getInstance(getActivity()).getFiatCode();
            strCurrentFiatSymbol = BlockchainUtil.getInstance(getActivity()).getFiatSymbol();
    		if(TxNotifUtil.getInstance().getTx() != null) {
    			TxNotifUtil.getInstance().clear();
    		}
        }
    }

    @Override
    public void onResume() {
    	super.onResume();

        IntentFilter filter = new IntentFilter(ACTION_INTENT);

        strCurrentFiatCode = BlockchainUtil.getInstance(getActivity()).getFiatCode();
        strCurrentFiatSymbol = BlockchainUtil.getInstance(getActivity()).getFiatSymbol();
        
		if(TxNotifUtil.getInstance().getTx() != null) {
			TxNotifUtil.getInstance().clear();
		}
        BlockchainUtil.getInstance(getActivity());
		updateLists();
		setCurrencySymbol();
    	System.gc();

    }

    @Override
    public void onPause() {
    	super.onPause();
    }

	@Override
	public void onDestroy() {
		super.onDestroy();

		EventListeners.removeEventListener(eventListener);
		((MainActivity)getActivity()).recycleBitmaps();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == QR_GENERATION)	{
			try {
				setExandableList();
			}catch (Exception e){
				e.printStackTrace();
			}
	    }
		else if(requestCode == TX_ACTIVITY)	{
			try {
				setExandableList();
			}catch (Exception e){
				e.printStackTrace();
			}
	    }
		else {
		}
	}

    public boolean refreshPayload() {
    	
    	if(ConnectivityStatus.hasConnectivity(getActivity())) {
    		Toast.makeText(getActivity(), R.string.refreshing, Toast.LENGTH_LONG).show();

    		try {
        		WalletUtil.getRefreshedInstance(getActivity()).getWalletApplication().doMultiAddr(false, null);
    		}
    		catch(Exception e) {
        		Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
    		}
    	}
    	else {
    		Toast.makeText(getActivity(), R.string.network_error_description, Toast.LENGTH_LONG).show();
    	}
    	

		return false;
    }

	public void setCurrencySymbol(){
		strCurrentFiatCode = BlockchainUtil.getInstance(getActivity()).getFiatCode();
		strCurrentFiatSymbol = BlockchainUtil.getInstance(getActivity()).getFiatSymbol();
		if(isBTC) {
			tViewCurrencySymbol.setText(Character.toString((char) TypefaceUtil.getInstance(getActivity()).getBTCSymbol()));
			tViewAmount1.setText(BlockchainUtil.formatBitcoin(remoteWallet.getBalance()));
			tViewAmount2.setText(strCurrentFiatSymbol + BlockchainUtil.BTC2Fiat(BlockchainUtil.formatBitcoin(remoteWallet.getBalance())));
		}
		else {
			tViewCurrencySymbol.setText(strCurrentFiatSymbol);
			tViewAmount1.setText(BlockchainUtil.BTC2Fiat(BlockchainUtil.formatBitcoin(remoteWallet.getBalance())));
			tViewAmount2.setText(Character.toString((char) TypefaceUtil.getInstance(getActivity()).getBTCSymbol()) + BlockchainUtil.formatBitcoin(remoteWallet.getBalance()));
		}
	}

    public void currencyToggle() {
    	if(isBTC) {
    		tViewCurrencySymbol.setText(strCurrentFiatSymbol);
    		String tmp = tViewAmount1.getText().toString(); 
    		tViewAmount1.setText(tViewAmount2.getText().toString().substring(1));
    		tViewAmount2.setTypeface(TypefaceUtil.getInstance(getActivity()).getBTCTypeface());
            tViewAmount2.setText(Character.toString((char) TypefaceUtil.getInstance(getActivity()).getBTCSymbol()) + tmp);
    	}
    	else {
            tViewCurrencySymbol.setText(Character.toString((char)TypefaceUtil.getInstance(getActivity()).getBTCSymbol()));
    		String tmp = tViewAmount1.getText().toString(); 
            tViewAmount1.setText(tViewAmount2.getText().toString().substring(1));
            tViewAmount2.setText(strCurrentFiatSymbol + tmp);
    	}
    	isBTC = isBTC ? false : true;

		transactionExpandableAdapter.setIsBTC(isBTC);
		transactionExpandableAdapter.notifyDataSetChanged();
		transactionAdapter.setIsBTC(isBTC);
		transactionAdapter.notifyDataSetChanged();
    }

}
