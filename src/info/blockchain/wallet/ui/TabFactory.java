package info.blockchain.wallet.ui;

import android.content.Context;
import android.view.View;
import android.widget.TabHost;

/**
 * Created by Raymond on 26/03/2015.
 */
public class TabFactory implements TabHost.TabContentFactory {

    private Context context;

    public TabFactory(Context context){
        this.context = context;
    }

    @Override
    public View createTabContent(String s) {
        View view = new View(context);
        view.setMinimumHeight(0);
        view.setMinimumWidth(0);
        view.setTag(s);
        return view;
    }
}
