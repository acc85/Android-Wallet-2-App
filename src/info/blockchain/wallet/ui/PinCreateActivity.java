package info.blockchain.wallet.ui;


import android.os.Bundle;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import info.blockchain.wallet.ui.Utilities.DeviceUtil;
import info.blockchain.wallet.ui.Utilities.TypefaceUtil;
import piuk.blockchain.android.R;

public class PinCreateActivity extends ActionBarActivity {

	private TextView tvHeader = null;

	private String pin;
	private String confirmPin;

    public static final int PBKDF2Iterations = 5000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
//	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    if(!DeviceUtil.getInstance(this).isSmallScreen()) {
			setContentView(R.layout.activity_pin_create2);
	    }
	    else {
			setContentView(R.layout.activity_pin_create_small);
	    }

		Toolbar toolbar = (Toolbar)findViewById(R.id.createPinToolbar);
		setSupportActionBar(toolbar);
		setTitle("");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		tvHeader = (TextView)findViewById(R.id.header);
		tvHeader.setTypeface(TypefaceUtil.getInstance(this).getGravityLightTypeface());
//		tvHeader.setText(R.string.create_new_wallet);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.pinCreateFragmentContainer,new PinCreateNewWalletFragment(), PinCreateNewWalletFragment.PIN_CREATE_NEW_FRAGMENT)
				.commit();

	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case android.R.id.home:
				onBackPressed();
				break;

		}
		return true;
	}

	public String getConfirmPin() {
		return confirmPin;
	}

	public void setConfirmPin(String confirmPin) {
		this.confirmPin = confirmPin;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public void switchToConfirmPin(String pin){
		this.pin = pin;
		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_alpha_in_from_right_to_left,R.anim.slide_alpha_out_from_right_to_left,R.anim.slide_alpha_in_from_left_to_right,R.anim.slide_alpha_out_from_left_to_right)
				.replace(R.id.pinCreateFragmentContainer,new PinConfirmNewWalletFragment(), PinConfirmNewWalletFragment.PIN_CONFIRM_NEW_FRAGMENT)
				.addToBackStack(PinConfirmNewWalletFragment.PIN_CONFIRM_NEW_FRAGMENT)
				.commit();
	}

	public void backToCreatePin(){
		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_alpha_in_from_left_to_right,R.anim.slide_alpha_out_from_left_to_right)
				.replace(R.id.pinCreateFragmentContainer, new PinCreateNewWalletFragment(), PinCreateNewWalletFragment.PIN_CREATE_NEW_FRAGMENT)
				.commit();
	}
}
