package info.blockchain.wallet.ui;

import java.security.Security;
//import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
//import android.app.ActionBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
//import android.view.Menu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ImageView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.os.StrictMode;
//import android.util.Log;

import info.blockchain.wallet.ui.Adapters.NavDrawerListAdapter;
import info.blockchain.wallet.ui.Adapters.TabsPagerAdapter;
import info.blockchain.wallet.ui.Utilities.BlockchainUtil;
import info.blockchain.wallet.ui.Utilities.TimeOutUtil;
import info.blockchain.wallet.ui.Utilities.WalletUtil;
import piuk.blockchain.android.R;
//import piuk.blockchain.android.SharedCoin;
import piuk.blockchain.android.SuccessCallback;
import piuk.blockchain.android.WalletApplication;

import net.sourceforge.zbar.Symbol;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity implements ActionBar.TabListener, SendFragment.OnCompleteListener {

    private static int ABOUT_ACTIVITY 		= 1;
    private static int PICK_CONTACT 		= 2;
    private static int SETTINGS_ACTIVITY	= 3;
    private static int ADDRESSBOOK_ACTIVITY	= 4;
    private static int MERCHANT_ACTIVITY	= 5;

	private ViewPager viewPager = null;
    private TabsPagerAdapter mAdapter = null;
    private Toolbar newActionBar = null;
	private ActionBar actionBar = null;

	private boolean isDrawerOpen = false;

    private String[] tabs = null;

	private static int ZBAR_SCANNER_REQUEST = 2026;

	long lastMesssageTime = 0;

	private WalletApplication application;
	
	private boolean returningFromActivity = false;
	
	public static final String INTENT_EXTRA_ADDRESS = "address";

	private String strUri = null;

	private ImageView refresh_icon;

	private FragmentTabHost tabHost;

	private DrawerLayout mDrawerLayout = null;
	private ListView mDrawerList = null;
	private ActionBarDrawerToggle mDrawerToggle = null;
	private List<Bitmap> recyclingBitmaps;
	private boolean refreshing;

	private int selectedDrawerItem = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		recyclingBitmaps = new ArrayList<>();
		setContentView(R.layout.activity_main);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


	    
    	Locale locale = new Locale("en", "US");
        Locale.setDefault(locale);
        
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); 

        boolean isFirst = false;
        boolean isSecured = false;
        boolean isDismissed = false;
        Bundle extras = getIntent().getExtras();
        if(extras != null)	{
        	isFirst = extras.getBoolean("first");
        	isDismissed = extras.getBoolean("dismissed");
        	strUri = extras.getString("INTENT_URI");
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isValidated = false;
        isValidated = prefs.getBoolean("validated", false);
    	isSecured = prefs.getBoolean("PWSecured", false) && prefs.getBoolean("EmailBackups", false) ? true : false;
        boolean isPaired = prefs.getBoolean("paired", false);
        boolean isVirgin = prefs.getBoolean("virgin", false);

        if(isValidated || isSecured || isDismissed || isPaired || !isVirgin) {

        }
        else if(!isSecured && isFirst) {
			Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
			edit.putBoolean("first", false);
			edit.commit();

			Intent intent = new Intent(this, SecureWallet.class);
			intent.putExtra("first", true);
			startActivity(intent);
        }
        else if(!isSecured && !isFirst) {
			Intent intent = new Intent(this, SecureWallet.class);
			intent.putExtra("first", false);
			startActivity(intent);
        }
        else {
			Intent intent = new Intent(this, SetupActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
        }


		application = WalletUtil.getInstance(this).getWalletApplication();

//		setUpTabBar();

		setUpNewActionBar();
		setupNavigationDrawer();

//		setUpNewTabBar();
		setUpViewPager();
		setListeners();


	}

	public List<Bitmap> getRecyclingBitmaps() {
		return recyclingBitmaps;
	}

	public void setRecyclingBitmaps(List<Bitmap> recyclingBitmaps) {
		this.recyclingBitmaps = recyclingBitmaps;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		switch(viewPager.getCurrentItem()){
					case 0:
						menu.findItem(R.id.main_refresh).setVisible(false);
						break;
					case 1:
						menu.findItem(R.id.main_refresh).setVisible(true);
						if(refreshing)
							menu.findItem(R.id.main_refresh).setVisible(false);
						else
							menu.findItem(R.id.main_refresh).setVisible(true);
						try {
							((BalanceFragment)mAdapter.getFragment(1)).setRefreshView(refreshing);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case 2:
						menu.findItem(R.id.main_refresh).setVisible(false);
						break;

				}
		return super.onPrepareOptionsMenu(menu);
	}

	public void setListeners(){
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				invalidateOptionsMenu();
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
	}

	public void navigateToTabContent(int tabNumber){
		tabHost.setCurrentTab(tabNumber);
	}


	public void setUpViewPager(){
		List<Fragment> fragments = new ArrayList<>();
		BalanceFragment balanceFragment = new BalanceFragment();
		SendFragment sendFragment = new SendFragment();
		ReceiveFragment receiveFragment = new ReceiveFragment();

		fragments.add(sendFragment);
		fragments.add(balanceFragment);
		fragments.add(receiveFragment);
		viewPager = (ViewPager) findViewById(R.id.pager);
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), fragments);
		viewPager.setAdapter(mAdapter);
		viewPager.setCurrentItem(1);

	}

	public TabsPagerAdapter getTabPagerAdapter(){
		return mAdapter;
	}


	public void setupNavigationDrawer(){
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.drawer_list);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				invalidateOptionsMenu();
				isDrawerOpen = false;
				if(view.getTag() != null) {
					switch ((int) view.getTag()) {
						case 2:
							doMerchantDirectory();
							break;
						case 3:
							doAddressBook();
							break;
						case 4:
							doAddressBook();
							break;
						case 5:
							doSettings();
							break;
						default:
							break;
					}
				}
				view.setTag(null);
			}

			public void onDrawerOpened(View view) {
				invalidateOptionsMenu();
				isDrawerOpen = true;
			}

			public void onDrawerSlide(View drawerView, float slideOffset) {
			}

		};


		mDrawerLayout.setDrawerListener(mDrawerToggle);
		NavDrawerListAdapter adapter = new NavDrawerListAdapter(getBaseContext());
		mDrawerList.setAdapter(adapter);
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mDrawerList.setTag(position);
				mDrawerLayout.closeDrawer(mDrawerList);
			}
		});
	}



	public void setUpNewActionBar(){
		newActionBar = (Toolbar)findViewById(R.id.my_awesome_toolbar);
		new MenuInflater(this).inflate(R.menu.addressbook, newActionBar.getMenu());
		newActionBar.setTitleTextAppearance(this, android.R.style.TextAppearance_Large);
		newActionBar.setTitleTextColor(getResources().getColor(android.R.color.white));
		newActionBar.inflateMenu(R.menu.main_menu);
		setSupportActionBar(newActionBar);
		newActionBar.setBackgroundColor(getResources().getColor(R.color.blockchain_blue));
		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onComplete() {
		handleNavigateTo();
		
        if(strUri != null)	{
//			Toast.makeText(MainActivity.this, strUri, Toast.LENGTH_LONG).show();
			Intent intent = new Intent("info.blockchain.wallet.ui.SendFragment.BTC_ADDRESS_SCAN");
		    intent.putExtra("BTC_ADDRESS", strUri);
		    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		    intent = null;
		    strUri = null;
		    new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					viewPager.setCurrentItem(0, true);
				}
			}, 1000);
        }

	}

	void handleNavigateTo() {
		Intent intent = getIntent();
		String navigateTo = intent.getStringExtra("navigateTo");
		if (navigateTo != null) {
			if (navigateTo.equals("merchantDirectory")) {
				doMerchantDirectory();
			} else if (navigateTo.equals("scanReceiving")) {
    			Intent intent2 = new Intent(MainActivity.this, ZBarScannerActivity.class);
    			intent2.putExtra(ZBarConstants.SCAN_MODES, new int[] { Symbol.QRCODE } );
    			startActivityForResult(intent2, ZBAR_SCANNER_REQUEST);	
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		application.setIsPassedPinScreen(true);

		if(TimeOutUtil.getInstance().isTimedOut()) {
        	Intent intent = new Intent(MainActivity.this, StartActivity.class);
			String navigateTo = getIntent().getStringExtra("navigateTo");
			intent.putExtra("navigateTo", navigateTo);   
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        	intent.putExtra("verified", true);
    		startActivity(intent);
		}
		else {
			TimeOutUtil.getInstance().updatePin();
		}

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		application.setIsPassedPinScreen(false);
		recycleBitmaps();

	}



	public void recycleBitmaps(){
		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				for(Bitmap b: recyclingBitmaps){
					b.recycle();
				}
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		else if(item.getItemId() == R.id.main_refresh){
			refreshing = true;
			invalidateOptionsMenu();
			WalletUtil.getInstance(MainActivity.this).getWalletApplication().doMultiAddr(false, new SuccessCallback() {
				@Override
				public void onSuccess() {
					new Handler(getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							refreshing = false;
							invalidateOptionsMenu();
						}
					});

				}

				@Override
				public void onFail() {
					new Handler(getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							refreshing = false;
							invalidateOptionsMenu();
						}
					});
				}
			});
		}else if(item.getItemId() == R.id.main_qr_scanner){
			Intent intent = new Intent(MainActivity.this, ZBarScannerActivity.class);
			intent.putExtra(ZBarConstants.SCAN_MODES, new int[] { Symbol.QRCODE } );
			startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
		}

        return super.onOptionsItemSelected(item);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		application.setIsScanning(false);

		if(resultCode == Activity.RESULT_OK && requestCode == ZBAR_SCANNER_REQUEST)	{
			String strResult = data.getStringExtra(ZBarConstants.SCAN_RESULT);

        	if(strResult != null) {

		        viewPager.setCurrentItem(0);

				Intent intent = new Intent("info.blockchain.wallet.ui.SendFragment.BTC_ADDRESS_SCAN");
			    intent.putExtra("BTC_ADDRESS", strResult);
			    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        	}
			else {
				Toast.makeText(this, R.string.invalid_bitcoin_address, Toast.LENGTH_LONG).show();
			}

        }
		else if(resultCode == Activity.RESULT_CANCELED && requestCode == ZBAR_SCANNER_REQUEST) {
//          Toast.makeText(this, R.string.camera_unavailable, Toast.LENGTH_SHORT).show();
		}
		else {
    		//
    		// SecurityException fix
    		//
			Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
		}
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if(keyCode == KeyEvent.KEYCODE_BACK) {
        	
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.ask_you_sure_exit).setCancelable(false);
			AlertDialog alert = builder.create();

			alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					application.setIsPassedPinScreen(false);
					
//					finish();

					final Intent relaunch = new Intent(MainActivity.this, Exit.class)
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					startActivity(relaunch);
					
					dialog.dismiss();
				}}); 

			alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					
					dialog.dismiss();
				}});

			alert.show();
        	
            return true;
        }
        else	{
        	;
        }

        return false;
    }

//	@Override
//    public void onTabReselected(Tab tab, FragmentTransaction ft) { ; }
//
//    @Override
//    public void onTabSelected(Tab tab, FragmentTransaction ft) { viewPager.setCurrentItem(tab.getPosition()); }
//
//    @Override
//    public void onTabUnselected(Tab tab, FragmentTransaction ft) { ; }

    private void doExchangeRates()	{
        if(hasZeroBlock())	{
            Intent intent = getPackageManager().getLaunchIntentForPackage(BlockchainUtil.ZEROBLOCK_PACKAGE);
            startActivity(intent);
        }
        else	{
        	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BlockchainUtil.ZEROBLOCK_PACKAGE));
        	startActivity(intent);
        }
    }

    private boolean hasZeroBlock()	{
    	PackageManager pm = this.getPackageManager();
    	try	{
    		pm.getPackageInfo(BlockchainUtil.ZEROBLOCK_PACKAGE, 0);
    		return true;
    	}
    	catch(NameNotFoundException nnfe)	{
    		return false;
    	}
    }

    private void doMerchantDirectory()	{
    	if (!application.isGeoEnabled()) {
    		EnableGeo.displayGPSPrompt(this);
    	}
    	else {
    		/*
    		Provider[] providers = Security.getProviders();
    		for(int i = 0; i < providers.length; i++)	{
    			System.out.println(providers[i].getName());
    		}
    		*/
    		//
    		// SecurityException fix
    		//
    		Security.removeProvider("SC");

    		TimeOutUtil.getInstance().updatePin();
        	Intent intent = new Intent(MainActivity.this, info.blockchain.merchant.directory.MapActivity.class);
    		startActivityForResult(intent, MERCHANT_ACTIVITY);
    	}
    }

    private void doSettings()	{
		TimeOutUtil.getInstance().updatePin();
    	Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
		startActivityForResult(intent, SETTINGS_ACTIVITY);
    }

    private void doAddressBook()	{
		TimeOutUtil.getInstance().updatePin();
    	Intent intent = new Intent(MainActivity.this, AddressBookActivity.class);
		startActivityForResult(intent, ADDRESSBOOK_ACTIVITY);
    }

    private void doSend2Friends()	{
    	Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
    	intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
//    	intent.setType(ContactsContract.CommonDataKinds.Email.CONTENT_TYPE);
    	startActivityForResult(intent, PICK_CONTACT);
    }

	@Override
	public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

	}
}
