package info.blockchain.wallet.ui;

import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
//import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.view.View.OnClickListener;
import android.graphics.Rect;
import android.widget.Toast;

import info.blockchain.wallet.ui.Utilities.TypefaceUtil;
import piuk.blockchain.android.R;
import piuk.blockchain.android.SuccessCallback;
import piuk.blockchain.android.WalletApplication;

public class SecureWallet extends Activity {
	
    private static int EDIT_PASSWORD    = 1;
    private static int EDIT_PASSWORD2   = 2;
    private static int EDIT_EMAIL 		= 3;

	private Pattern emailPattern = Patterns.EMAIL_ADDRESS;

	private TextView tvHeader = null;
	private TextView tvFooter = null;
	private TextView tvWarning1 = null;
	private TextView tvWarning2 = null;
	private TextView tvSwitchTitle1 = null;
	private TextView tvSwitchText1 = null;
	private TextView tvSwitchTitle2 = null;
	private TextView tvSwitchText2 = null;
	
	private String strPIN = null;
	
	private String strPw1 = null;
	
	private SharedPreferences prefs = null;
	private Editor edit = null;

	private boolean emailBackups = false;
	private boolean pwSecured = false;
	private boolean creating = true;

	private LinearLayout pwLayout = null;
	private LinearLayout emLayout = null;
	
    private TextView tvDismiss = null;
    private ToggleButton tgPassword = null;
    private ToggleButton tgEmail = null;

	private Handler handler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_secure2);

	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    creating = prefs.getBoolean("Creating1", true);
		edit = prefs.edit();
		edit.putBoolean("Creating1", false);
		edit.commit();

	    pwSecured = prefs.getBoolean("PWSecured", false);
	    emailBackups = prefs.getBoolean("EmailBackups", false);
	    
    	pwLayout = (LinearLayout)findViewById(R.id.switch1);
    	emLayout = (LinearLayout)findViewById(R.id.switch2);

		tvHeader = (TextView)findViewById(R.id.header);
		tvHeader.setTypeface(TypefaceUtil.getInstance(this).getGravityLightTypeface());
		if(creating) {
			tvHeader.setText(R.string.create_new_wallet);
		}
		else {
			tvHeader.setText(R.string.secure_your_wallet);
		}

		tvFooter = (TextView)findViewById(R.id.footer);
		if(creating) {
			tvFooter.setText(R.string.enabling_features);
		}
		else {
			tvFooter.setText(R.string.enabling_features2);
		}

		tvWarning1 = (TextView)findViewById(R.id.warning1);
		if(creating) {
			tvWarning1.setText(R.string.your_wallet_ready);
		}
		else {
			tvWarning1.setVisibility(View.GONE);
		}

		tvWarning2 = (TextView)findViewById(R.id.warning2);
		if(creating) {
			tvWarning2.setTextColor(0xFF039BD3);
			tvWarning2.setText(R.string.enabling_features3);
		}
		else {
			tvWarning2.setTextColor(0xFFd65858);
			tvWarning2.setText(R.string.enabling_features4);
		}

	    if(pwSecured) {
	    	LinearLayout pwLayout = (LinearLayout)findViewById(R.id.switch1);
	    	pwLayout.setVisibility(View.GONE);
	    }
	    else {
			tvSwitchTitle1 = (TextView)findViewById(R.id.switch_title1);
			tvSwitchTitle1.setTypeface(TypefaceUtil.getInstance(this).getGravityBoldTypeface());
			tvSwitchTitle1.setText(R.string.set_memorable_password);

			tvSwitchText1 = (TextView)findViewById(R.id.switch_text1);
			tvSwitchText1.setText(R.string.set_memorable_password2);
	    }

	    if(emailBackups) {
	    	LinearLayout emLayout = (LinearLayout)findViewById(R.id.switch2);
	    	emLayout.setVisibility(View.GONE);
	    }
	    else {
			tvSwitchTitle2 = (TextView)findViewById(R.id.switch_title2);
			tvSwitchTitle2.setTypeface(TypefaceUtil.getInstance(this).getGravityBoldTypeface());
			tvSwitchTitle2.setText(R.string.auto_email_backups);

			tvSwitchText2 = (TextView)findViewById(R.id.switch_text2);
			tvSwitchText2.setText(R.string.auto_email_backups2);
	    }

        tvDismiss = (TextView)findViewById(R.id.dismiss);
        tvDismiss.setOnTouchListener(new OnTouchListener() {
        	private Rect rect = null;
        	
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	switch(event.getAction()) {
            		case MotionEvent.ACTION_DOWN:
            			rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            			return true;
            		case MotionEvent.ACTION_UP:
            			if(rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                			Intent intent = new Intent(SecureWallet.this, MainActivity.class);
                			intent.putExtra("dismissed", true);
                			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                			startActivity(intent);
            			}
            			return false;
            		default:
            			return false;
            	}
            }
        });

        tgPassword = (ToggleButton) findViewById(R.id.toggle1);
        tgPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                 if (tgPassword.isChecked()) {
             	 	Intent intent = new Intent(SecureWallet.this, EditSetting.class);
             	 	intent.putExtra("prompt", "Password");
             	 	intent.putExtra("existing", "Password");
             	 	startActivityForResult(intent, EDIT_PASSWORD);            	
                    }
                 else {
//                        Toast.makeText(SecureWallet.this, "PW Off", Toast.LENGTH_SHORT).show();
                 }
            }
        });

        tgEmail = (ToggleButton) findViewById(R.id.toggle2);
        tgEmail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                 if (tgEmail.isChecked()) {
                	 	Intent intent = new Intent(SecureWallet.this, EditSetting.class);
                	 	intent.putExtra("prompt", "E-mail address");
                	 	intent.putExtra("existing", "E-mail address");
                	 	startActivityForResult(intent, EDIT_EMAIL);            	
                    }
                 else {
//                        Toast.makeText(SecureWallet.this, "EM Off", Toast.LENGTH_SHORT).show();
                 }
            }
        });
        
		CurrencyExchange.getInstance(this).localUpdate();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode == Activity.RESULT_OK && requestCode == EDIT_PASSWORD)	{

			String pw1 = data.getAction();
			
			if(pw1 == null || pw1.length() < 10 || pw1.length() > 255) {
				Toast.makeText(SecureWallet.this, R.string.new_account_password_length_error, Toast.LENGTH_LONG).show();
		    	tgPassword.setChecked(false);
				return;
			}
			else {
				strPw1 = pw1;
				
         	 	Intent intent = new Intent(SecureWallet.this, EditSetting.class);
         	 	intent.putExtra("prompt", "Confirm password");
         	 	intent.putExtra("existing", "Confirm password");
         	 	startActivityForResult(intent, EDIT_PASSWORD2);            	
			}

        }
		else if(resultCode == Activity.RESULT_OK && requestCode == EDIT_PASSWORD2)	{

			String pw2 = data.getAction();
			
			if(pw2 == null || pw2.length() < 10 || pw2.length() > 255 || !strPw1.equals(pw2)) {
				Toast.makeText(SecureWallet.this, R.string.new_account_password_mismatch_error, Toast.LENGTH_LONG).show();
		    	tgPassword.setChecked(false);
			}
			else {
//	    		Log.d("SecureWallet", "SecureWallet setTemporyPassword");
	    		
				handler = new Handler();

				final WalletApplication application = (WalletApplication)this.getApplication();
//				final MyRemoteWallet remoteWallet = application.getRemoteWallet();

				application.getRemoteWallet().setTemporyPassword(strPw1);
//	    		Log.d("SecureWallet", "SecureWallet setTemporyPassword: " + remoteWallet.getTemporyPassword());
   				application.localSaveWallet();

				application.saveWallet( new SuccessCallback() {
					@Override
					public void onSuccess() {		    		
//			    		Log.d("SecureWallet", "SecureWallet setTemporyPassword saveWallet onSuccess");
			    		
			    		edit.putBoolean("PWSecured", true);
			    		edit.commit();
			    		
						String pinCode = application.getTemporyPIN();
		   				application.apiStoreKey(pinCode, new SuccessCallback() {

							@Override
							public void onSuccess() {
//					            Log.d("apiStoreKey", "apiStoreKey apiStoreKey onSuccess");
							}

							@Override
							public void onFail() {
//					            Log.d("apiStoreKey", "apiStoreKey apiStoreKey onFail");				
							}
		   				});

						new Thread(new Runnable() {
							@Override
							public void run() {
								try {							
									handler.post(new Runnable() {
										@Override
										public void run() {
									    	pwLayout.setVisibility(View.GONE);
										}
									});
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}).start();
						
					    emailBackups = prefs.getBoolean("EmailBackups", false);
					    if(emailBackups) {
				        	Intent intent = new Intent(SecureWallet.this, MainActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				    		startActivity(intent);
					    }

					}
					
					@Override
					public void onFail() {
//			    		Log.d("SecureWallet", "SecureWallet setTemporyPassword saveWallet onFail");	
					}
				});

			}

        }
		else if(resultCode == Activity.RESULT_OK && requestCode == EDIT_EMAIL) {
			
			String em = data.getAction();
			
			if(em.length() > 0 && !emailPattern.matcher(em).matches()) {
				Toast.makeText(SecureWallet.this, R.string.new_account_password_invalid_email, Toast.LENGTH_LONG).show();
		    	tgEmail.setChecked(false);
			}
			else {
				handler = new Handler();
				
				final WalletApplication application = (WalletApplication)this.getApplication();

				application.updateEmail(em, new SuccessCallback() {
					@Override
					public void onSuccess() {
//			    		Log.d("SecureWallet", "SecureWallet updateEmail onSuccess");
			    		
						application.updateNotificationsType(true, false, new SuccessCallback() {
							@Override
							public void onSuccess() {
//					    		Log.d("SecureWallet", "SecureWallet updateNotificationsType onSuccess");
					    		
					    		edit.putBoolean("EmailBackups", true);
					    		edit.commit();

								new Thread(new Runnable() {
									@Override
									public void run() {
										try {							
											handler.post(new Runnable() {
												@Override
												public void run() {
											    	emLayout.setVisibility(View.GONE);
												}
											});
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}).start();
								
							    pwSecured = prefs.getBoolean("PWSecured", false);
							    if(pwSecured) {
						        	Intent intent = new Intent(SecureWallet.this, MainActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
						    		startActivity(intent);
							    }

							}
							
							@Override
							public void onFail() {
//					    		Log.d("SecureWallet", "SecureWallet updateNotificationsType fail");	
							}
						});

					}
					
					@Override
					public void onFail() {
//			    		Log.d("SecureWallet", "SecureWallet updateEmail fail");	
					}
				});

			}

		}
		else {
		    pwSecured = prefs.getBoolean("PWSecured", false);
		    emailBackups = prefs.getBoolean("EmailBackups", false);
		    
		    if(pwSecured) {
		    	pwLayout.setVisibility(View.GONE);
		    }
		    else {
		    	tgPassword.setChecked(false);
		    }
		    
		    if(emailBackups) {
		    	emLayout.setVisibility(View.GONE);
		    }
		    else {
		    	tgEmail.setChecked(false);
		    }

		}
		
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        else	{
        	;
        }

        return false;
    }

}
