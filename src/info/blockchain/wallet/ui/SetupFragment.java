package info.blockchain.wallet.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import info.blockchain.api.ExchangeRates;
import info.blockchain.wallet.ui.Utilities.DeviceUtil;
import info.blockchain.wallet.ui.Utilities.TypefaceUtil;
import piuk.blockchain.android.R;
import piuk.blockchain.android.util.ConnectivityStatus;

/**
 * Created by Raymond on 31/03/2015.
 */
public class SetupFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.setup,null);
        if(DeviceUtil.getInstance(getActivity()).isSmallScreen()) {
            view = inflater.inflate(R.layout.setup_small,null);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("virgin", true);
        edit.commit();

        Button imgCreate = ((Button)view.findViewById(R.id.create));
        imgCreate.setTypeface(TypefaceUtil.getInstance(getActivity()).getGravityBoldTypeface());
        imgCreate.setTextColor(0xFF1B8AC7);
        imgCreate.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PinCreateActivity.class);

                startActivity(intent);
            }
        });

        Button imgPair = ((Button)view.findViewById(R.id.pair));
        imgPair.setTypeface(TypefaceUtil.getInstance(getActivity()).getGravityLightTypeface());
        imgPair.setTextColor(0xFF808080);
        imgPair.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PairingHelp.class);
                startActivity(intent);
            }
        });

        if(ConnectivityStatus.hasConnectivity(getActivity())) {
            ExchangeRates fxRates = new ExchangeRates();
            DownloadFXRatesTask task = new DownloadFXRatesTask(getActivity(), fxRates);
            task.execute(new String[] { fxRates.getUrl() });
        }

        return view;
    }


}
