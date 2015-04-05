package info.blockchain.wallet.ui.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;

import info.blockchain.api.ExchangeRates;
import info.blockchain.wallet.ui.CurrencyExchange;
import info.blockchain.wallet.ui.DownloadFXRatesTask;
import info.blockchain.wallet.ui.MainActivity;
import info.blockchain.wallet.ui.OtherCurrencyExchange;
import info.blockchain.wallet.ui.SetupActivity;
import info.blockchain.wallet.ui.StartActivity;
import info.blockchain.wallet.ui.Utilities.DeviceUtil;
import info.blockchain.wallet.ui.Utilities.TimeOutUtil;
import piuk.blockchain.android.Constants;
import piuk.blockchain.android.MyWallet;
import piuk.blockchain.android.R;
import piuk.blockchain.android.WalletApplication;
import piuk.blockchain.android.util.ConnectivityStatus;

/**
 * Created by Raymond on 29/03/2015.
 */
public class PinCreateFragment extends Fragment {

    String userEntered = "";
    final int PIN_LENGTH = 4;
    boolean keyPadLockedFlag = false;
    TextView titleView = null;
    TextView pinBox0 = null;
    TextView pinBox1 = null;
    TextView pinBox2 = null;
    TextView pinBox3 = null;

    private String unconfirmedPin;

    TextView[] pinBoxArray = null;

    TextView statusView = null;

    View validatePinLayout;
    View keyPadLayout;

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
    Button buttonReset = null;
    Button buttonDeleteBack = null;

    private boolean validating = true;
    private boolean creating = false;
    private String userInput = null;

    private static final String WebROOT = "https://" + Constants.BLOCKCHAIN_DOMAIN + "/pin-store";
    public static final int PBKDF2Iterations = 5000;

    public String strUri = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        userEntered = "";
        View view = inflater.inflate(R.layout.activity_pin_creating,null);
        if (DeviceUtil.getInstance(getActivity()).isSmallScreen())
            view = inflater.inflate(R.layout.activity_pin_creating_small,null);

        titleView = (TextView)view.findViewById(R.id.titleBox);
        titleView.setText(R.string.create_pin);

        validatePinLayout = view.findViewById(R.id.validatingPinLayout);
        keyPadLayout = view.findViewById(R.id.numericPad);

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf");

        buttonDeleteBack = (Button) view.findViewById(R.id.buttonDeleteBack);
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

        titleView = (TextView) view.findViewById(R.id.titleBox);
        titleView.setTypeface(typeface);

        pinBox0 = (TextView) view.findViewById(R.id.pinBox0);
        pinBox1 = (TextView) view.findViewById(R.id.pinBox1);
        pinBox2 = (TextView) view.findViewById(R.id.pinBox2);
        pinBox3 = (TextView) view.findViewById(R.id.pinBox3);

        pinBoxArray = new TextView[PIN_LENGTH];
        pinBoxArray[0] = pinBox0;
        pinBoxArray[1] = pinBox1;
        pinBoxArray[2] = pinBox2;
        pinBoxArray[3] = pinBox3;

        statusView = (TextView) view.findViewById(R.id.statusMessage);
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
                        keyPadLockedFlag = true;
                        if (userInput != null) {
                            if (userInput.equals("0000")) {
                                Toast.makeText(getActivity(), R.string.zero_pin, Toast.LENGTH_SHORT).show();
                                clearBoxes();
                                keyPadLockedFlag = false;
                                return;
                            }
                        }
                        if (unconfirmedPin != null) {
                            if (userEntered.equalsIgnoreCase(unconfirmedPin)) {
                                userInput = userEntered;
                                savePin();
                            } else {
                                Toast.makeText(getActivity(), "Confirmation incorrect, try again", Toast.LENGTH_LONG).show();
                                titleView.setText(R.string.confirm_pin);
                                clearBoxes();
                                keyPadLockedFlag = false;
                            }
                        }else {
                            unconfirmedPin = userEntered;
                            activateResetButton(true);
                            titleView.setText(R.string.confirm_pin);
                            Toast.makeText(getActivity(), R.string.confirm_pin, Toast.LENGTH_LONG).show();
                            clearBoxes();
                            keyPadLockedFlag = false;
                        }
                    }

                }
            }
        };

        button0 = (Button) view.findViewById(R.id.button0);
        button0.setTypeface(typeface);
        button0.setOnClickListener(pinButtonHandler);

        button1 = (Button) view.findViewById(R.id.button1);
        button1.setTypeface(typeface);
        button1.setOnClickListener(pinButtonHandler);

        SpannableStringBuilder cs = null;
        float sz = 0.6f;

        button2 = (Button) view.findViewById(R.id.button2);
        button2.setTypeface(typeface);
        button2.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("2 ABC");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button2.setText(cs);

        button3 = (Button) view.findViewById(R.id.button3);
        button3.setTypeface(typeface);
        button3.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("3 DEF");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button3.setText(cs);

        button4 = (Button) view.findViewById(R.id.button4);
        button4.setTypeface(typeface);
        button4.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("4 GHI");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button4.setText(cs);

        button5 = (Button) view.findViewById(R.id.button5);
        button5.setTypeface(typeface);
        button5.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("5 JKL");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button5.setText(cs);

        button6 = (Button) view.findViewById(R.id.button6);
        button6.setTypeface(typeface);
        button6.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("6 MNO");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button6.setText(cs);

        button7 = (Button) view.findViewById(R.id.button7);
        button7.setTypeface(typeface);
        button7.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("7 PQRS");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button7.setText(cs);

        button8 = (Button) view.findViewById(R.id.button8);
        button8.setTypeface(typeface);
        button8.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("8 TUV");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button8.setText(cs);

        button9 = (Button) view.findViewById(R.id.button9);
        button9.setTypeface(typeface);
        button9.setOnClickListener(pinButtonHandler);
        cs = new SpannableStringBuilder("9 WXYZ");
        cs.setSpan(new RelativeSizeSpan(sz), 2, cs.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        button9.setText(cs);

        buttonDeleteBack = (Button) view.findViewById(R.id.buttonDeleteBack);
        buttonDeleteBack.setTypeface(typeface);


        buttonReset = (Button) view.findViewById(R.id.buttonReset);

        buttonReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reCreatePinMethod();
            }
        });
        buttonReset.setTypeface(typeface);

        activateResetButton(false);

        final int colorOff = 0xff333333;
        final int colorOn = 0xff1a87c6;

        button0.setOnTouchListener(new View.OnTouchListener() {
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

        button1.setOnTouchListener(new View.OnTouchListener() {
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

        button2.setOnTouchListener(new View.OnTouchListener() {
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

        button3.setOnTouchListener(new View.OnTouchListener() {
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

        button4.setOnTouchListener(new View.OnTouchListener() {
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

        button5.setOnTouchListener(new View.OnTouchListener() {
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

        button6.setOnTouchListener(new View.OnTouchListener() {
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

        button7.setOnTouchListener(new View.OnTouchListener() {
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

        button8.setOnTouchListener(new View.OnTouchListener() {
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

        button9.setOnTouchListener(new View.OnTouchListener() {
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

        buttonDeleteBack.setOnTouchListener(new View.OnTouchListener() {
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

        if (ConnectivityStatus.hasConnectivity(getActivity())) {
            ExchangeRates fxRates = new ExchangeRates();
            DownloadFXRatesTask task = new DownloadFXRatesTask(getActivity(), fxRates);
            task.execute(new String[]{fxRates.getUrl()});

            String[] currencies = CurrencyExchange.getInstance(getActivity()).getBlockchainCurrencies();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String strFiatCode = prefs.getString("ccurrency", "USD");
            OtherCurrencyExchange.getInstance(getActivity(), currencies, strFiatCode);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            final String message = getString(R.string.check_connectivity_exit);

            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_continue,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.dismiss();
                                    pinBoxArray[0].setText("");
                                    pinBoxArray[1].setText("");
                                    pinBoxArray[2].setText("");
                                    pinBoxArray[3].setText("");
                                    userEntered = "";
                                }
                            });

            builder.create().show();

        }

        final WalletApplication application = (WalletApplication) getActivity().getApplication();
        if (application.getGUID() == null && !creating) {
            Intent intent = new Intent(getActivity(), SetupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }


        return view;
    }


    public void activateResetButton(boolean active){
        if(active) {
            buttonReset.setVisibility(View.VISIBLE);
            buttonReset.setClickable(true);
            buttonReset.setFocusable(true);
        }else{
            buttonReset.setVisibility(View.INVISIBLE);
            buttonReset.setClickable(false);
            buttonReset.setFocusable(false);
        }
    }

    public void reCreatePinMethod(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final String message = "Are you sure you want to recreate you pin?";

        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_continue,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.dismiss();
                                pinBoxArray[0].setText("");
                                pinBoxArray[1].setText("");
                                pinBoxArray[2].setText("");
                                pinBoxArray[3].setText("");
                                userEntered = "";
                                unconfirmedPin = null;
                                titleView.setText(R.string.create_pin);
                                activateResetButton(false);
                            }
                        }
                )
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int id) {
                        d.dismiss();
                    }
                });


        builder.create().show();

    }


    public void clearBoxes(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                pinBox0.setText("");
                pinBox1.setText("");
                pinBox2.setText("");
                pinBox3.setText("");
                userEntered = "";
            }
        });

    }

    public void showOrHideProgress(final boolean show){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(show) {
                    validatePinLayout.setVisibility(View.VISIBLE);
                    keyPadLayout.setVisibility(View.INVISIBLE);
                }else{
                    validatePinLayout.setVisibility(View.INVISIBLE);
                    keyPadLayout.setVisibility(View.VISIBLE);
                }
            }
        });

    }


    public void savePin(){
        showOrHideProgress(true);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final WalletApplication application = (WalletApplication) getActivity().getApplication();
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                // Save PIN
                try {
                    byte[] bytes = new byte[16];
                    SecureRandom random = new SecureRandom();
                    random.nextBytes(bytes);
                    final String key = new String(Hex.encode(bytes), "UTF-8");
                    random.nextBytes(bytes);
                    final String value = new String(Hex.encode(bytes), "UTF-8");
//                    final JSONObject response = ((PinActivity)getActivity()).apiStoreKey(key, value, userInput);
                    final JSONObject response = ((StartActivity)getActivity()).apiStoreKey(key, value, userInput);
                    if (response.get("success") != null) {

                        edit.putString("pin_kookup_key", key);
                        edit.putString("encrypted_password", MyWallet.encrypt(application.getRemoteWallet().getTemporyPassword(), value, PBKDF2Iterations));

                        if (!edit.commit()) {
                            throw new Exception("Error Saving Preferences");
                        } else {
                            TimeOutUtil.getInstance().updatePin();
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    showOrHideProgress(true);
                                    Toast.makeText(getActivity(), "PIN saved", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            });
                        }

                    } else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(application, response.toString(), Toast.LENGTH_LONG).show();
                                Toast.makeText(getActivity(), "PIN saved", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                String navigateTo = getActivity().getIntent().getStringExtra("navigateTo");
                                intent.putExtra("navigateTo", navigateTo);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                    }
                } catch (final Exception e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(application, e.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        }
       );
    }

}
