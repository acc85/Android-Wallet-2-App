package info.blockchain.wallet.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;

import info.blockchain.wallet.ui.Utilities.TimeOutUtil;
import info.blockchain.wallet.ui.Utilities.WalletUtil;
import piuk.blockchain.android.Constants;
import piuk.blockchain.android.EventListeners;
import piuk.blockchain.android.MyWallet;
import piuk.blockchain.android.R;
import piuk.blockchain.android.SuccessCallback;
import piuk.blockchain.android.WalletApplication;

/**
 * Created by Raymond on 03/04/2015.
 */
public class PinConfirmNewWalletFragment extends Fragment{

    private EditText pin1,pin2,pin3,pin4;
    private boolean deleteActive;
    private CheckBox cbAccept;
    public static String PIN_CONFIRM_NEW_FRAGMENT = "pin_confirm_new_fragment";
    public static final int PBKDF2Iterations = 5000;
    private TextView tvTOS;
    private WalletApplication application;
    private String guid, sharedKey,password, key, value;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.pin_confirm_fragment_layout,null);

        pin1 = (EditText)view.findViewById(R.id.pin1);
        pin2 = (EditText)view.findViewById(R.id.pin2);
        pin3 = (EditText)view.findViewById(R.id.pin3);
        pin4 = (EditText)view.findViewById(R.id.pin4);


        tvTOS = (TextView)view.findViewById(R.id.tos_text);
        Linkify.addLinks(tvTOS, Linkify.WEB_URLS);

        cbAccept = (CheckBox) view.findViewById(R.id.tos_accept);
        cbAccept.setEnabled(false);

        pin1.requestFocus();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);


        pin1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!pin2.getText().toString().isEmpty()) {
                    return true;
                } else
                    deleteActive = false;
                return false;
            }
        });

        pin2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!pin3.getText().toString().isEmpty()) {
                    return true;
                }else
                    deleteActive = false;
                return false;
            }
        });

        pin3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!pin4.getText().toString().isEmpty()) {
                    return true;
                }else
                    deleteActive = false;
                return false;
            }
        });

        pin4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                deleteActive = false;
                return false;
            }
        });


        pin2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                EditText eView = (EditText) view;
                if (deleteActive) {
                    deleteActive = false;
                    return true;
                } else if (i == 67) {
                    if (eView.getText().length() < 1) {
                        deleteActive = true;
                        pin1.getText().clear();
                        pin1.requestFocus();
                        return true;
                    } else {
                        pin2.getText().clear();
                        pin1.requestFocus();
                        deleteActive = true;
                        return true;
                    }
                }
                return false;
            }
        });


        pin3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                EditText eView = (EditText) view;
                if (deleteActive) {
                    deleteActive = false;
                    return true;
                } else if (i == 67) {
                    if (eView.getText().length() < 1) {
                        deleteActive = true;
                        pin2.getText().clear();
                        pin2.requestFocus();
                        return true;
                    } else {
                        pin3.getText().clear();
                        pin2.requestFocus();
                        deleteActive = true;
                        return true;
                    }
                }
                return false;
            }
        });

        pin4.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                EditText eView = (EditText) view;
                if (deleteActive) {
                    deleteActive = false;
                    return true;
                } else if (i == 67) {
                    if (eView.getText().length() < 1) {
                        deleteActive = true;
                        pin3.getText().clear();
                        pin3.requestFocus();
                        return true;
                    } else {
                        pin4.getText().clear();
                        pin3.requestFocus();
                        deleteActive = true;
                        return true;
                    }
                }
                return false;
            }

        });


        pin1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    pin2.requestFocus();
                }
                checkFieldsFilled();
            }
        });


        pin2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    pin3.requestFocus();
                }
                checkFieldsFilled();
            }
        });

        pin3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    pin4.requestFocus();
                }
                checkFieldsFilled();
            }
        });

        pin4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkFieldsFilled();
            }
        });


        cbAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbAccept.isChecked()) {
                    ((PinCreateActivity) getActivity()).setConfirmPin(getPinString());
                    final String p1 = ((PinCreateActivity) getActivity()).getPin();
                    final String p2 = ((PinCreateActivity) getActivity()).getConfirmPin();
                    if (p1.equals("0000") || p2.equals("0000")) {
                        resetPinDisplay();
                        Toast.makeText(getActivity(), R.string.zero_pin, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (p1 != null && p2 != null && p1.length() == 4 && p2.length() == 4 && p1.equals(p2)) {

                        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.creating_account), true);
                        progressDialog.show();

                        application = WalletUtil.getInstance(getActivity()).getWalletApplication();
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    createNewWallet();
                                    EventListeners.invokeWalletDidChange();
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                progressDialog.dismiss();
                                                Toast.makeText(getActivity(), R.string.new_account_success, Toast.LENGTH_SHORT).show();
                                                final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext()).edit();
                                                edit.putString("guid", guid);
                                                edit.putString("sharedKey", sharedKey);
                                                if (edit.commit()) {
                                                    AsyncTask.execute(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                final JSONObject response = generateAndStoreSecureKey();
                                                                if (response.get("success") != null) {
                                                                    edit.putString("pin_kookup_key", key);
                                                                    edit.putString("encrypted_password", MyWallet.encrypt(application.getRemoteWallet().getTemporyPassword(), value, PBKDF2Iterations));
                                                                    if (!edit.commit()) {
                                                                        throw new Exception("Error Saving Preferences");
                                                                    } else {
                                                                        StorePinAndStartActivity();
                                                                    }
                                                                } else {
                                                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Toast.makeText(application, response.toString(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });

                                                                }
                                                            } catch (final Exception e) {
                                                                e.printStackTrace();
                                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Toast.makeText(application, e.getStackTrace().toString(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });

                                                            }
                                                            checkWalletUpdated();
                                                        }
                                                    });
                                                } else {
                                                    throw new Exception("Error saving preferences");
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                application.clearWallet();
                                                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        });

        return view;
    }

    public void checkWalletUpdated(){
        application.checkIfWalletHasUpdated(password, guid, sharedKey, true, new SuccessCallback() {
            @Override
            public void onSuccess() {
                try {
                    final String regId = GCMRegistrar.getRegistrationId(getActivity());

                    if (regId == null || regId.equals("")) {
                        GCMRegistrar.register(getActivity(), Constants.SENDER_ID);
                    } else {
                        application.registerForNotificationsIfNeeded(regId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail() {
                Toast.makeText(application, R.string.toast_error_syncing_wallet, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void StorePinAndStartActivity(){
        TimeOutUtil.getInstance().updatePin();
        application.setTemporyPIN(((PinCreateActivity) getActivity()).getPin());
        Intent intent = new Intent(getActivity(), SecureWallet.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public org.json.JSONObject generateAndStoreSecureKey() throws Exception {
        byte[] bytes = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        key = new String(Hex.encode(bytes), "UTF-8");
        random.nextBytes(bytes);
        value = new String(Hex.encode(bytes), "UTF-8");
        return piuk.blockchain.android.ui.PinEntryActivity.apiStoreKey(key, value, ((PinCreateActivity) getActivity()).getPin());
    }

    public void createNewWallet() throws Exception {
        try {
            application.generateNewWallet();
        } catch (Exception e1) {
            throw new Exception("Error Generating Wallet");
        }

        guid = application.getRemoteWallet().getGUID();
        sharedKey = application.getRemoteWallet().getSharedKey();
        String pinCode = ((PinCreateActivity)getActivity()).getPin();
        password = pinCode + pinCode + pinCode;
        application.getRemoteWallet().setTemporyPassword(password);
        if (!application.getRemoteWallet().remoteSave("")) {
            throw new Exception("Unknown Error Inserting wallet");
        }
    }


    public String getPinString(){
        return pin1.getText().toString()+pin2.getText().toString()+pin3.getText().toString()+pin4.getText().toString();
    }

    public void checkFieldsFilled(){
        if(pin1.getText().toString().isEmpty() ||
                pin2.getText().toString().isEmpty() ||
                pin3.getText().toString().isEmpty() ||
                pin4.getText().toString().isEmpty()){
            cbAccept.setEnabled(false);
        }else{
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            cbAccept.setEnabled(true);
        }
    }

    public void resetPinDisplay(){
        pin1.getText().clear();
        pin2.getText().clear();
        pin3.getText().clear();
        pin4.getText().clear();
        pin1.requestFocus();
    }
}


