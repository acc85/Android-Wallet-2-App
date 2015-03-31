package info.blockchain.wallet.ui;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

import info.blockchain.wallet.ui.Utilities.BlockchainUtil;
import info.blockchain.wallet.ui.Utilities.DeviceUtil;
import info.blockchain.wallet.ui.Utilities.ProgressUtil;
import info.blockchain.wallet.ui.Utilities.TimeOutUtil;
import piuk.blockchain.android.Constants;
import piuk.blockchain.android.MyWallet;
import piuk.blockchain.android.RequestPasswordSuccessCallback;
import piuk.blockchain.android.WalletApplication;
import piuk.blockchain.android.SuccessCallback;
import piuk.blockchain.android.R;
import piuk.blockchain.android.ui.dialogs.RequestForgotPasswordDialog;
import piuk.blockchain.android.util.ConnectivityStatus;
import piuk.blockchain.android.util.WalletUtils;
import info.blockchain.api.ExchangeRates;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

public class PinEntryActivity extends FragmentActivity {

    String userEntered = "";

    final int PIN_LENGTH = 4;
    boolean keyPadLockedFlag = false;
    Context context = null;

    public static String NO_MORE_TRIES = "No matching key found";

    TextView titleView = null;

    TextView pinBox0 = null;
    TextView pinBox1 = null;
    TextView pinBox2 = null;
    TextView pinBox3 = null;

    TextView[] pinBoxArray = null;

    TextView statusView = null;

    Button button0 = null;
    Button button1 = null;
    Button button2 = null;
    Button button3 = null;
    Button button4 = null;
    Button button5 = null;
    Button button6 = null;
    Button button7 = null;
    Button button8 = null;
    Button button9 = null;
    Button buttonForgot = null;
    Button buttonDeleteBack = null;

    private boolean validating = true;
    private boolean creating = false;
    private String userInput = null;

    private static final String WebROOT = "https://" + Constants.BLOCKCHAIN_DOMAIN + "/pin-store";
    public static final int PBKDF2Iterations = 5000;

    public String strUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        userEntered = "";

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (!DeviceUtil.getInstance(this).isSmallScreen()) {
            setContentView(R.layout.activity_pin_entry);
        } else {
            setContentView(R.layout.activity_pin_entry_small);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String action = getIntent().getAction();
        String scheme = getIntent().getScheme();
        if (action != null && Intent.ACTION_VIEW.equals(action) && scheme.equals("bitcoin")) {
            strUri = getIntent().getData().toString();
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("N") != null && extras.getString("N").equals("1")) {
                validating = false;
                creating = true;
                ((TextView) findViewById(R.id.titleBox)).setText(R.string.create_pin);
                Toast.makeText(this, R.string.create_pin, Toast.LENGTH_LONG).show();
            } else if (extras.getString("N") != null && extras.getString("N").length() == 4) {
                validating = false;
                creating = true;
                userInput = extras.getString("N");
                ((TextView) findViewById(R.id.titleBox)).setText(R.string.confirm_pin);
                Toast.makeText(this, R.string.confirm_pin, Toast.LENGTH_LONG).show();
            } else if (extras.getString("S") != null && extras.getString("S").equals("1")) {
                validating = false;
                ((TextView) findViewById(R.id.titleBox)).setText(R.string.create_pin);
                Toast.makeText(this, R.string.create_pin, Toast.LENGTH_LONG).show();
            } else if (extras.getString("S") != null && extras.getString("S").length() == 4) {
                validating = false;
                userInput = extras.getString("S");
                ((TextView) findViewById(R.id.titleBox)).setText(R.string.confirm_pin);
                Toast.makeText(this, R.string.confirm_pin, Toast.LENGTH_LONG).show();
            } else {
                validating = true;
            }
        }

        Typeface typeface = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");

        buttonForgot = (Button) findViewById(R.id.buttonForgot);
        buttonForgot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                requestPassword();
            }
        });
        buttonForgot.setTypeface(typeface);

        buttonDeleteBack = (Button) findViewById(R.id.buttonDeleteBack);
        buttonDeleteBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (keyPadLockedFlag == true) {
                    return;
                }

                if (userEntered.length() > 0) {
                    for (int i = 0; i < pinBoxArray.length; i++)
                        pinBoxArray[i].setText("");
                    userEntered = "";
                }
            }
        });

        titleView = (TextView) findViewById(R.id.titleBox);
        titleView.setTypeface(typeface);

        pinBox0 = (TextView) findViewById(R.id.pinBox0);
        pinBox1 = (TextView) findViewById(R.id.pinBox1);
        pinBox2 = (TextView) findViewById(R.id.pinBox2);
        pinBox3 = (TextView) findViewById(R.id.pinBox3);

        pinBoxArray = new TextView[PIN_LENGTH];
        pinBoxArray[0] = pinBox0;
        pinBoxArray[1] = pinBox1;
        pinBoxArray[2] = pinBox2;
        pinBoxArray[3] = pinBox3;

        statusView = (TextView) findViewById(R.id.statusMessage);
        statusView.setTypeface(typeface);

        View.OnClickListener pinButtonHandler = new View.OnClickListener() {
            public void onClick(View v) {

                if (keyPadLockedFlag == true) {
                    return;
                }

                Button pressedButton = (Button) v;

                if (userEntered.length() < PIN_LENGTH) {
                    userEntered = userEntered + pressedButton.getText().toString().substring(0, 1);


                    // Update pin boxes
                    pinBoxArray[userEntered.length() - 1].setText("8");

                    if (userEntered.length() == PIN_LENGTH) {

                        if (validating) {
                            if (userEntered.equals("0000")) {
                                Toast.makeText(PinEntryActivity.this, R.string.zero_pin, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            validatePIN(userEntered);
                        } else {
                            if (userInput != null) {
                                if (userInput.equals("0000")) {
                                    Toast.makeText(PinEntryActivity.this, R.string.zero_pin, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(PinEntryActivity.this, PinEntryActivity.class);
                                    intent.putExtra("N", "1");
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    return;
                                }
                                if (userInput.equals(userEntered)) {

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            Looper.prepare();

                                            final WalletApplication application = (WalletApplication) getApplication();
                                            Editor edit = PreferenceManager.getDefaultSharedPreferences(PinEntryActivity.this).edit();

                                            // Save PIN
                                            try {
                                                byte[] bytes = new byte[16];
                                                SecureRandom random = new SecureRandom();
                                                random.nextBytes(bytes);
                                                final String key = new String(Hex.encode(bytes), "UTF-8");
                                                random.nextBytes(bytes);
                                                final String value = new String(Hex.encode(bytes), "UTF-8");
                                                final JSONObject response = apiStoreKey(key, value, userInput);
                                                if (response.get("success") != null) {

                                                    edit.putString("pin_kookup_key", key);
                                                    edit.putString("encrypted_password", MyWallet.encrypt(application.getRemoteWallet().getTemporyPassword(), value, PBKDF2Iterations));

                                                    if (!edit.commit()) {
                                                        throw new Exception("Error Saving Preferences");
                                                    } else {
                                                        TimeOutUtil.getInstance().updatePin();

                                                        Toast.makeText(PinEntryActivity.this, "PIN saved", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(PinEntryActivity.this, MainActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }

                                                } else {

                                                    Toast.makeText(application, response.toString(), Toast.LENGTH_LONG).show();

                                                    Toast.makeText(PinEntryActivity.this, "PIN saved", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(PinEntryActivity.this, MainActivity.class);
                                                    String navigateTo = getIntent().getStringExtra("navigateTo");
                                                    intent.putExtra("navigateTo", navigateTo);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);

                                                }
                                            } catch (Exception e) {
                                                Toast.makeText(application, e.toString(), Toast.LENGTH_LONG).show();
                                                e.printStackTrace();
                                            }
                                            Looper.loop();

                                        }
                                    }).start();

                                } else {
                                    Toast.makeText(PinEntryActivity.this, "Start over", Toast.LENGTH_SHORT).show();

                                    if (creating) {
                                        Intent intent = new Intent(PinEntryActivity.this, PinEntryActivity.class);
                                        intent.putExtra("N", "1");
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(PinEntryActivity.this, PinEntryActivity.class);
                                        intent.putExtra("S", "1");
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                }
                            } else {
                                if (creating) {
                                    Intent intent = new Intent(PinEntryActivity.this, PinEntryActivity.class);
                                    intent.putExtra("N", userEntered);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(PinEntryActivity.this, PinEntryActivity.class);
                                    intent.putExtra("S", userEntered);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }
                        }

                    }
                } else {
                    //Roll over
                    pinBoxArray[0].setText("");
                    pinBoxArray[1].setText("");
                    pinBoxArray[2].setText("");
                    pinBoxArray[3].setText("");

                    userEntered = "";

                    statusView.setText("");

                    userEntered = userEntered + pressedButton.getText().toString().substring(0, 1);
                    //Update pin boxes
                    pinBoxArray[userEntered.length() - 1].setText("8");

                    if (userEntered.equals("0000")) {
                        Toast.makeText(PinEntryActivity.this, R.string.zero_pin, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    validatePIN(userEntered);

                }
            }
        };

        button0 = (Button) findViewById(R.id.button0);
        button0.setTypeface(typeface);
        button0.setOnClickListener(pinButtonHandler);

        button1 = (Button) findViewById(R.id.button1);
        button1.setTypeface(typeface);
        button1.setOnClickListener(pinButtonHandler);

        SpannableStringBuilder cs = null;
        float sz = 0.6f;

        button2 = (Button) findViewById(R.id.button2);
        button2.setTypeface(typeface);
        button2.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("2 ABC");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button2.setText(cs);

        button3 = (Button) findViewById(R.id.button3);
        button3.setTypeface(typeface);
        button3.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("3 DEF");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button3.setText(cs);

        button4 = (Button) findViewById(R.id.button4);
        button4.setTypeface(typeface);
        button4.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("4 GHI");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button4.setText(cs);

        button5 = (Button) findViewById(R.id.button5);
        button5.setTypeface(typeface);
        button5.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("5 JKL");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button5.setText(cs);

        button6 = (Button) findViewById(R.id.button6);
        button6.setTypeface(typeface);
        button6.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("6 MNO");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button6.setText(cs);

        button7 = (Button) findViewById(R.id.button7);
        button7.setTypeface(typeface);
        button7.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("7 PQRS");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button7.setText(cs);

        button8 = (Button) findViewById(R.id.button8);
        button8.setTypeface(typeface);
        button8.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("8 TUV");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button8.setText(cs);

        button9 = (Button) findViewById(R.id.button9);
        button9.setTypeface(typeface);
        button9.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("9 WXYZ");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button9.setText(cs);

        buttonDeleteBack = (Button) findViewById(R.id.buttonDeleteBack);
        buttonDeleteBack.setTypeface(typeface);

        final int colorOff = 0xff333333;
        final int colorOn = 0xff1a87c6;

        button0.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        button0.setBackgroundColor(colorOn);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        button0.setBackgroundColor(colorOff);
                        break;
                }

                return false;
            }
        });

        button1.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        button1.setBackgroundColor(colorOn);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        button1.setBackgroundColor(colorOff);
                        break;
                }

                return false;
            }
        });

        button2.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        button2.setBackgroundColor(colorOn);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        button2.setBackgroundColor(colorOff);
                        break;
                }

                return false;
            }
        });

        button3.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        button3.setBackgroundColor(colorOn);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        button3.setBackgroundColor(colorOff);
                        break;
                }

                return false;
            }
        });

        button4.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        button4.setBackgroundColor(colorOn);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        button4.setBackgroundColor(colorOff);
                        break;
                }

                return false;
            }
        });

        button5.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        button5.setBackgroundColor(colorOn);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        button5.setBackgroundColor(colorOff);
                        break;
                }

                return false;
            }
        });

        button6.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        button6.setBackgroundColor(colorOn);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        button6.setBackgroundColor(colorOff);
                        break;
                }

                return false;
            }
        });

        button7.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        button7.setBackgroundColor(colorOn);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        button7.setBackgroundColor(colorOff);
                        break;
                }

                return false;
            }
        });

        button8.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        button8.setBackgroundColor(colorOn);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        button8.setBackgroundColor(colorOff);
                        break;
                }

                return false;
            }
        });

        button9.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        button9.setBackgroundColor(colorOn);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        button9.setBackgroundColor(colorOff);
                        break;
                }

                return false;
            }
        });

        buttonDeleteBack.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        buttonDeleteBack.setBackgroundColor(colorOn);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        buttonDeleteBack.setBackgroundColor(colorOff);
                        break;
                }

                return false;
            }
        });

        buttonForgot.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        buttonForgot.setBackgroundColor(colorOn);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        buttonForgot.setBackgroundColor(colorOff);
                        break;
                }

                return false;
            }
        });

        if (ConnectivityStatus.hasConnectivity(this)) {
            ExchangeRates fxRates = new ExchangeRates();
            DownloadFXRatesTask task = new DownloadFXRatesTask(context, fxRates);
            task.execute(new String[]{fxRates.getUrl()});

            String[] currencies = CurrencyExchange.getInstance(this).getBlockchainCurrencies();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PinEntryActivity.this);
            String strFiatCode = prefs.getString("ccurrency", "USD");
            OtherCurrencyExchange.getInstance(PinEntryActivity.this, currencies, strFiatCode);
        } else {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            final String message = getString(R.string.check_connectivity_exit);

            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_continue,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.dismiss();
                                    Intent intent = new Intent(PinEntryActivity.this, PinEntryActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            });

            builder.create().show();

        }

        final WalletApplication application = (WalletApplication) PinEntryActivity.this.getApplication();
        if (application.getGUID() == null && !creating) {
            Intent intent = new Intent(PinEntryActivity.this, SetupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
        //App not allowed to go back to Parent activity until correct pin entered.
        return;
        //super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activity_pin_entry_view, menu);
        return true;
    }

    public static void clearPrefValues(WalletApplication application) throws Exception {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(application).edit();

        editor.remove("pin_kookup_key");
        editor.remove("encrypted_password");
        editor.putBoolean("verified", false);

        if (!editor.commit()) {
            throw new Exception("Error Saving Preferences");
        }
    }

    public void validatePIN(final String PIN) {

        final WalletApplication application = (WalletApplication) PinEntryActivity.this.getApplication();

        final Handler handler = new Handler();

        final Activity activity = this;

        ProgressUtil.getInstance(PinEntryActivity.this).show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final int[] pinTries = {0};
                String pin_lookup_key = PreferenceManager.getDefaultSharedPreferences(application).getString("pin_kookup_key", null);
                String encrypted_password = PreferenceManager.getDefaultSharedPreferences(application).getString("encrypted_password", null);
                JSONObject response = null;
                String decryptionKey = null;
                try {
                    response = apiGetValue(pin_lookup_key, PIN);
                    decryptionKey = (String) response.get("success");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ProgressUtil.getInstance(PinEntryActivity.this).close();
                if (decryptionKey != null && !response.has("error")) {
                    application.setTemporyPIN(PIN);
                    application.didEncounterFatalPINServerError = false;

                    String password = decrypt(encrypted_password, decryptionKey, PIN);

                    application.checkIfWalletHasUpdatedAndFetchTransactions(password, new SuccessCallback() {
                        @Override
                        public void onSuccess() {
                            handler.post(new Runnable() {
                                public void run() {

                                    TimeOutUtil.getInstance().updatePin();

                                    Editor edit = PreferenceManager.getDefaultSharedPreferences(PinEntryActivity.this).edit();
                                    edit.putBoolean("verified", true);
                                    edit.commit();

                                    ProgressUtil.getInstance(PinEntryActivity.this).close();

                                    BlockchainUtil.getInstance(PinEntryActivity.this);

                                    Intent intent = new Intent(PinEntryActivity.this, MainActivity.class);
                                    String navigateTo = getIntent().getStringExtra("navigateTo");
                                    intent.putExtra("navigateTo", navigateTo);
                                    intent.putExtra("verified", true);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    if (strUri != null) {
                                        intent.putExtra("INTENT_URI", strUri);
                                    }
                                    startActivity(intent);
                                }
                            });
                        }

                        @Override
                        public void onFail() {
                            new Handler(getMainLooper()).post(new Runnable() {
                                public void run() {
                                    Toast.makeText(PinEntryActivity.this, R.string.toast_wallet_decryption_failed, Toast.LENGTH_LONG).show();
                                    ProgressUtil.getInstance(PinEntryActivity.this).close();
                                    pinBoxArray[0].setText("");
                                    pinBoxArray[1].setText("");
                                    pinBoxArray[2].setText("");
                                    pinBoxArray[3].setText("");
                                }
                            });
                        }
                    });
                } else if (response.has("error")) {
                    String error = "";
                    try {
                        error = response.getString("error");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    //"code" == 2 means the PIN is incorrect
                    if (response.has("code")) {
                        int codeTries = 0;
                        try {
                            codeTries = response.getInt("code");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        System.out.println("codeTries:"+codeTries);
                        if (codeTries == 1) {
                            try {
                                clearPrefValues(application);
                                throw new Exception("Fatal PIN Server Error");
                            } catch (Exception e) {
                                e.printStackTrace();
                                throwPinException(application);
                            }

                        } else {
                            //Restart in "validating" mode
                            final JSONObject finalResponse = response;
                            new Handler(getMainLooper()).post(new Runnable() {
                                public void run() {
                                    try {
                                        Toast.makeText(PinEntryActivity.this, finalResponse.getString("error"), Toast.LENGTH_SHORT).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    pinBoxArray[0].setText("");
                                    pinBoxArray[1].setText("");
                                    pinBoxArray[2].setText("");
                                    pinBoxArray[3].setText("");
                                    userEntered = "";
                                }
                            });
                        }
                    } else {
                        try {
                            throw new Exception("Unknown Error");
                        } catch (Exception e) {
                            e.printStackTrace();
                            throwPinException(application);
                        }
                    }
                }
            }
        });

    }

    public Activity getThisActivity(){
        return this;
    }

    public void throwPinException(WalletApplication application){
        application.didEncounterFatalPINServerError = true;
        new Handler(getMainLooper()).post(new Runnable() {
            public void run() {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getThisActivity());
                    builder.setCancelable(false);
                    builder.setMessage(R.string.pin_server_error_description);
                    builder.setTitle(R.string.pin_server_error);
                    builder.setPositiveButton(R.string.pin_server_error_enter_password_manually, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            requestPassword();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        final WalletApplication application = (WalletApplication) getApplication();
        application.setIsPassedPinScreen(false);
    }

    public static String postURL(String request, String urlParameters) throws Exception {
        String error = null;

        for (int ii = 0; ii < WalletUtils.DefaultRequestRetry; ++ii) {
            URL url = new URL(request);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36");

                connection.setUseCaches(false);

                connection.setConnectTimeout(WalletUtils.DefaultRequestTimeout);
                connection.setReadTimeout(WalletUtils.DefaultRequestTimeout);

                connection.connect();

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                connection.setInstanceFollowRedirects(false);

//				System.out.println("Response Code " + connection.getResponseCode() );

                //connection.getRequestProperties().get("Content-Type").equals("application/json")
                if (connection.getResponseCode() == 200)
                    return IOUtils.toString(connection.getInputStream(), "UTF-8");
                else if (connection.getResponseCode() == 500)
                    return IOUtils.toString(connection.getErrorStream(), "UTF-8");
                else
                    error = IOUtils.toString(connection.getErrorStream(), "UTF-8");

                Thread.sleep(5000);
            } finally {
                connection.disconnect();
            }
        }

        throw new Exception("Invalid Response " + error);
    }

    public static JSONObject apiGetValue(String key, String pin) throws Exception {
        StringBuilder args = new StringBuilder();

        args.append("key=" + key);
        args.append("&pin=" + pin);
        args.append("&method=get");

        String response = postURL(WebROOT, args.toString());

        if (response == null || response.length() == 0)
            throw new Exception("Invalid Server Response");

        try {
            return new JSONObject(response);
        } catch (JSONException e) {
            throw new Exception("Invalid Server Response");
        }
    }

    public static JSONObject apiStoreKey(String key, String value, String pin) throws Exception {
        StringBuilder args = new StringBuilder();

        args.append("key=" + key);
        args.append("&value=" + value);
        args.append("&pin=" + pin);
        args.append("&method=put");

        String response = postURL(WebROOT, args.toString());

        if (response == null || response.length() == 0)
            throw new Exception("Invalid Server Response");

        try {
            return new JSONObject(response);
        } catch (JSONException e) {
            throw new Exception("Invalid Server Response");
        }
    }

    public void requestPassword() {
        RequestForgotPasswordDialog.show(getSupportFragmentManager(), new RequestPasswordSuccessCallback() {
            public void onSuccess(RequestForgotPasswordDialog requestForgotPasswordDialog) {
                Toast.makeText(PinEntryActivity.this, R.string.password_correct, Toast.LENGTH_LONG).show();

                new AlertDialog.Builder(PinEntryActivity.this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.confirm_new_pin_creation)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            //                  @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent starterIntent = getIntent();
                                starterIntent.putExtra("N", "1");
                                finish();
                                startActivity(starterIntent);
                            }
                        }).show();

            }

            public void onFail(RequestForgotPasswordDialog requestForgotPasswordDialog) {
                Toast.makeText(PinEntryActivity.this, R.string.password_incorrect, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(PinEntryActivity.this, PinEntryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });
    }

    public String decrypt(String encrypted_password, String decryptionKey, String PIN) {
        String password = null;
        try {
            password = MyWallet.decrypt(encrypted_password, decryptionKey, 2000);
            Log.i("PinEntryDecrypt", "2000");

            final WalletApplication application = (WalletApplication) PinEntryActivity.this.getApplication();

            //
            // Save PIN
            //
            try {
                byte[] bytes = new byte[16];
                SecureRandom random = new SecureRandom();
                random.nextBytes(bytes);
                final String key = new String(Hex.encode(bytes), "UTF-8");
                random.nextBytes(bytes);
                final String value = new String(Hex.encode(bytes), "UTF-8");
                final JSONObject response = apiStoreKey(key, value, PIN);
                if (response.get("success") != null && password != null && password.length() >= 10) {

                    Editor edit = PreferenceManager.getDefaultSharedPreferences(PinEntryActivity.this).edit();
                    edit.putString("pin_kookup_key", key);
                    edit.putString("encrypted_password", MyWallet.encrypt(password, value, PBKDF2Iterations));
                    edit.commit();

                    Toast.makeText(PinEntryActivity.this, "PIN saved", Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(application, response.toString(), Toast.LENGTH_LONG).show();

                }
            } catch (Exception e) {
                Toast.makeText(application, e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            //
            //
            //

        } catch (Exception e1) {
            try {
                password = MyWallet.decrypt(encrypted_password, decryptionKey, PBKDF2Iterations);
                Log.i("PinEntryDecrypt", "5000");

            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        return password;
    }

}