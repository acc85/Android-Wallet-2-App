package info.blockchain.wallet.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

import piuk.blockchain.android.Constants;
import piuk.blockchain.android.MyWallet;
import piuk.blockchain.android.R;
import piuk.blockchain.android.RequestPasswordSuccessCallback;
import piuk.blockchain.android.WalletApplication;
import piuk.blockchain.android.ui.dialogs.RequestForgotPasswordDialog;
import piuk.blockchain.android.util.WalletUtils;

/**
 * Created by Raymond on 31/03/2015.
 */
public class StartActivity extends FragmentActivity {

    String userEntered = "";
    Context context = null;

    public static String PIN_CREATE_FRAGMENT_NAME = "PINCREATEFRAGMENT";
    public static String PIN_ENTRY_FRAGMENT_NAME = "PINENTRYFRAGMENT";
    public static String SETUP_FRAGMENT = "SETUPFRAGMENT";

    private boolean creating = false;
    private boolean isStartUp;
    private static final String WebROOT = "https://" + Constants.BLOCKCHAIN_DOMAIN + "/pin-store";
    public static final int PBKDF2Iterations = 5000;

    public String strUri = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        userEntered = "";

        final WalletApplication application = (WalletApplication) this.getApplication();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.pin_activity_layout);

        Bundle extras = getIntent().getExtras();
        if (application.getGUID() == null && !creating) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.startFragmentContainer,new SetupFragment(),SETUP_FRAGMENT)
                    .commit();
            isStartUp = true;
        }else {
            if (extras != null) {
                if (extras.getString("S") != null && extras.getString("S").equals("1")) {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.startFragmentContainer, new PinCreateFragment(), PIN_CREATE_FRAGMENT_NAME)
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.startFragmentContainer, new PinEntryFragment(), PIN_ENTRY_FRAGMENT_NAME)
                            .commit();
                }
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.startFragmentContainer, new PinEntryFragment(), PIN_ENTRY_FRAGMENT_NAME)
                        .commit();
            }
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String action = getIntent().getAction();
        String scheme = getIntent().getScheme();

        if (action != null && Intent.ACTION_VIEW.equals(action) && scheme.equals("bitcoin")) {
            strUri = getIntent().getData().toString();
        }

    }

    @Override
    public void onBackPressed() {
        if(isStartUp)
            super.onBackPressed();
        else
            return;
        //App not allowed to go back to Parent activity until correct pin entered.

        //super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activity_pin_entry_view, menu);
        return true;
    }

    public static void clearPrefValues(WalletApplication application) throws Exception {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(application).edit();

        editor.remove("pin_kookup_key");
        editor.remove("encrypted_password");
        editor.putBoolean("verified", false);

        if (!editor.commit()) {
            throw new Exception("Error Saving Preferences");
        }
    }

    public void requestPassword() {
        RequestForgotPasswordDialog.show(StartActivity.this.getSupportFragmentManager(), new RequestPasswordSuccessCallback() {
            @Override
            public void onSuccess(RequestForgotPasswordDialog requestForgotPasswordDialog) {
                Toast.makeText(StartActivity.this, R.string.password_correct, Toast.LENGTH_LONG).show();
                new AlertDialog.Builder(StartActivity.this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.confirm_new_pin_creation)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.startFragmentContainer, new PinCreateFragment(), PIN_CREATE_FRAGMENT_NAME)
                                        .commit();
                            }
                        }).show();

            }

            @Override
            public void onFail(RequestForgotPasswordDialog requestForgotPasswordDialog) {
                Toast.makeText(StartActivity.this, R.string.password_incorrect, Toast.LENGTH_LONG).show();
                requestForgotPasswordDialog.showProgress(false);
            }

        });
    }

    public void showOrHideKeyboard(boolean show, View view){
        if(show) {
            try {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInputFromInputMethod(view.getWindowToken(), InputMethodManager.SHOW_IMPLICIT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            try {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showOrHideKeyboard(boolean show){
        if(show){
            try {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInputFromInputMethod(getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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


    public String decrypt(String encrypted_password, String decryptionKey, String PIN) {
        String password = null;
        try {
            password = MyWallet.decrypt(encrypted_password, decryptionKey, 2000);
            Log.i("PinEntryDecrypt", "2000");

            final WalletApplication application = (WalletApplication) this.getApplication();

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

                    SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    edit.putString("pin_kookup_key", key);
                    edit.putString("encrypted_password", MyWallet.encrypt(password, value, PBKDF2Iterations));
                    edit.commit();

                    Toast.makeText(this, "PIN saved", Toast.LENGTH_SHORT).show();
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
