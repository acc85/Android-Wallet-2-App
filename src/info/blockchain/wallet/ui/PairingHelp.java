package info.blockchain.wallet.ui;

import info.blockchain.api.ExchangeRates;

import java.util.regex.Pattern;

import net.sourceforge.zbar.Symbol;

import org.spongycastle.util.encoders.Hex;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.google.android.gcm.GCMRegistrar;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.res.Resources;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.Display;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Point;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.Toast;
//import android.util.Log;

import info.blockchain.wallet.ui.Utilities.TypefaceUtil;
import piuk.blockchain.android.Constants;
import piuk.blockchain.android.MyRemoteWallet;
import piuk.blockchain.android.MyWallet;
import piuk.blockchain.android.R;
import piuk.blockchain.android.SuccessCallback;
import piuk.blockchain.android.WalletApplication;
import piuk.blockchain.android.util.ConnectivityStatus;

public class PairingHelp extends FragmentActivity {
	
	private TextView tvHeader = null;
	private TextView tvFooter1 = null;
	private TextView tvFooter2 = null;
	private TextView tvWarning1 = null;
	private TextView tvWarning2 = null;
	private TextView tvBack = null;
	private TextView tvNext = null;
	private ImageView ivImage = null;
	private RelativeLayout layoutScan = null;
	private LinearLayout layoutManual = null;
	private static int LEFT = 0;
	private static int RIGHT = 1;
	private int fragmentContainerHeight;
	PairingHelpStageOneFragment fragment;
	private static String HELP_STAGE_ONE_FRAGMENT = "help_stage_one_fragment";
	private static String HELP_STAGE_TWO_FRAGMENT = "help_stage_two_fragment";
	private static String HELP_STAGE_THREE_FRAGMENT = "help_stage_three_fragment";
	int stage;
	
	private int level = 0;

	private static int ZBAR_SCANNER_REQUEST = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_pairing_help);

	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Bundle extras = getIntent().getExtras();
        if(extras != null)	{
			if(extras.containsKey("STAGE"))
				stage = extras.getInt("STAGE");
        }


		layoutScan = (RelativeLayout)findViewById(R.id.scan);
		layoutScan.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				Intent intent = new Intent(PairingHelp.this, ZBarScannerActivity.class);
				intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
				startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
				return false;
			}
		});

		final FrameLayout helpContainer = (FrameLayout)findViewById(R.id.helpContainer);
		helpContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public void onGlobalLayout() {
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
					helpContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				else
					helpContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				fragmentContainerHeight = helpContainer.getHeight();
			}
		});
		tvHeader = (TextView)findViewById(R.id.header);

		tvHeader.setTypeface(TypefaceUtil.getInstance(this).getGravityLightTypeface());
		tvBack = (TextView)findViewById(R.id.back);
		tvBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(stage-1 >= 0)
					displayHelpFragment(stage-1,LEFT);
			}
		});

		tvNext = (TextView)findViewById(R.id.next);

		tvNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (stage + 1 <= 3)
					displayHelpFragment(stage + 1, RIGHT);
			}
		});


		if(stage == 0)
			displayStartHelpFragment(1);
		else{
			displayStartHelpFragment(stage);
		}


		if(ConnectivityStatus.hasConnectivity(this)) {
			CurrencyExchange.getInstance(this).localUpdate();
			ExchangeRates fxRates = new ExchangeRates();
			DownloadFXRatesTask task = new DownloadFXRatesTask(this, fxRates);
			task.execute(new String[] { fxRates.getUrl() });
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("STAGE",stage);
		super.onSaveInstanceState(outState);
	}

	public void displayStartHelpFragment(int stage){
		switch(stage) {
			case 1:
				tvBack.setVisibility(View.INVISIBLE);
				PairingHelpStageOneFragment stageOne = new PairingHelpStageOneFragment();
				this.stage = 1;
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.helpContainer, stageOne, HELP_STAGE_ONE_FRAGMENT)
						.commit();
				break;
			case 2:
				tvBack.setVisibility(View.VISIBLE);
				tvNext.setVisibility(View.VISIBLE);
				PairingHelpStageTwoFragment stageTwo = new PairingHelpStageTwoFragment();
				this.stage = 2;
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.helpContainer, stageTwo, HELP_STAGE_TWO_FRAGMENT)
						.commit();
				break;
			case 3:
				tvNext.setVisibility(View.INVISIBLE);
				PairingHelpStageThreeFragment stageThree = new PairingHelpStageThreeFragment();
				this.stage = 3;
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.helpContainer, stageThree, HELP_STAGE_THREE_FRAGMENT)
						.commit();
				break;
		}
	}

	public void displayHelpFragment(int stage, int direction){
		int animateOutCurrentFragment = 0;
		int animateInNewFragment = 0;
		switch(direction){
			case 0:
				animateOutCurrentFragment = R.anim.slide_alpha_out_from_left_to_right;
				animateInNewFragment = R.anim.slide_alpha_in_from_left_to_right;
				break;
			case 1:
				animateOutCurrentFragment = R.anim.slide_alpha_out_from_right_to_left;
				animateInNewFragment = R.anim.slide_alpha_in_from_right_to_left;
				break;

		}
		switch(stage){
			case 1:
				tvBack.setVisibility(View.INVISIBLE);
				PairingHelpStageOneFragment stageOne = new PairingHelpStageOneFragment();
				this.stage = 1;
				getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(animateInNewFragment, animateOutCurrentFragment)
						.replace(R.id.helpContainer, stageOne, HELP_STAGE_ONE_FRAGMENT)
						.commit();
				break;
			case 2:
				tvBack.setVisibility(View.VISIBLE);
				tvNext.setVisibility(View.VISIBLE);
				PairingHelpStageTwoFragment stageTwo = new PairingHelpStageTwoFragment();
				this.stage = 2;
				getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(animateInNewFragment,animateOutCurrentFragment)
						.replace(R.id.helpContainer, stageTwo, HELP_STAGE_TWO_FRAGMENT)
						.commit();
				break;
			case 3:
				tvNext.setVisibility(View.INVISIBLE);
				PairingHelpStageThreeFragment stageThree = new PairingHelpStageThreeFragment();
				this.stage = 3;
				getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(animateInNewFragment, animateOutCurrentFragment)
						.replace(R.id.helpContainer, stageThree, HELP_STAGE_THREE_FRAGMENT)
						.commit();
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode == Activity.RESULT_OK && requestCode == ZBAR_SCANNER_REQUEST)	{
			if(data != null && data.getStringExtra(ZBarConstants.SCAN_RESULT) != null)	{
				String strResult = data.getStringExtra(ZBarConstants.SCAN_RESULT);
	        	handleQRCode(strResult);
			}
        }

	}

	public void handleQRCode(String raw_code) {
		final WalletApplication application = (WalletApplication) getApplication();
		
		try {
			if (raw_code == null || raw_code.length() == 0) {
				throw new Exception("Invalid Pairing QR Code");
			}

			if (raw_code.charAt(0) != '1') {
				throw new Exception("Invalid Pairing Version Code " + raw_code.charAt(0));
			}

			final Handler handler = new Handler();

			{
				String[] components = raw_code.split("\\|", Pattern.LITERAL);

				if (components.length < 3) {
					throw new Exception("Invalid Pairing QR Code. Not enough components.");
				}

				final String guid = components[1];
				if (guid.length() != 36) {
					throw new Exception("Invalid Pairing QR Code. GUID wrong length.");
				}

				final String encrypted_data = components[2];

				new Thread(new Runnable() {

					@Override
					public void run() {
						
						Looper.prepare();

						try {
							String temp_password = MyRemoteWallet.getPairingEncryptionPassword(guid);

							String decrypted = MyWallet.decrypt(encrypted_data, temp_password, 10);

							String[] sharedKeyAndPassword = decrypted.split("\\|", Pattern.LITERAL);

							if (sharedKeyAndPassword.length < 2) {
								throw new Exception("Invalid Pairing QR Code. sharedKeyAndPassword Incorrect number of components.");
							}

							final String sharedKey = sharedKeyAndPassword[0];
							if (sharedKey.length() != 36) {
								throw new Exception("Invalid Pairing QR Code. sharedKey wrong length.");
							}

							final String password = new String(Hex.decode(sharedKeyAndPassword[1]), "UTF-8");
//							Toast.makeText(application, password, Toast.LENGTH_LONG).show();

							application.clearWallet();

//							PinEntryActivity.clearPrefValues(application);

							Editor edit = PreferenceManager.getDefaultSharedPreferences(PairingHelp.this).edit();

							edit.putString("guid", guid);
							edit.putString("sharedKey", sharedKey);
							
							edit.commit();

							handler.post(new Runnable() {

								@Override
								public void run() {
									application.checkIfWalletHasUpdated(password, guid, sharedKey, true, new SuccessCallback(){

										@Override
										public void onSuccess() {	
//											registerNotifications();

									        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PairingHelp.this);
											Editor edit = prefs.edit();
											edit.putBoolean("validated", true);
											edit.putBoolean("paired", true);
											edit.commit();

											try {
												final String regId = GCMRegistrar.getRegistrationId(PairingHelp.this);
												if (regId == null || regId.equals("")) {
													GCMRegistrar.register(PairingHelp.this, Constants.SENDER_ID);
												} else {
													application.registerForNotificationsIfNeeded(regId);
												}
											} catch (Exception e) {
												e.printStackTrace();
											}

//								        	Intent intent = new Intent(PairingHelp.this, PinEntryActivity.class);
//								        	intent.putExtra("S", "1");
//											Intent intent = new Intent(PairingHelp.this, PinActivity.class);
											Intent intent = new Intent(PairingHelp.this, StartActivity.class);
											intent.putExtra("S", "1");
											intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
								    		startActivity(intent);

											finish();
										}

										@Override
										public void onFail() {
											finish();

											Toast.makeText(application, R.string.toast_error_syncing_wallet, Toast.LENGTH_LONG).show();
										}
									});
								}
							});

						} catch (final Exception e) {
							e.printStackTrace();

							handler.post(new Runnable() {
								public void run() {

									Toast.makeText(application, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

									e.printStackTrace();

									application.writeException(e);
								}
							});
						}
						
						Looper.loop();

					}
				}).start();
			}
		} catch (Exception e) {

			Toast.makeText(application, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

			e.printStackTrace();

			application.writeException(e);
		}
		
	}
	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {

	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);

	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }

	    return inSampleSize;
	}

	public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
		
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    options.inJustDecodeBounds = false;

	    return BitmapFactory.decodeResource(res, resId, options);
	}


}
