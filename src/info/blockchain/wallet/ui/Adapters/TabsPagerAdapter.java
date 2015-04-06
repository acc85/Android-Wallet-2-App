package info.blockchain.wallet.ui.Adapters;
 
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragmentList;

    public TabsPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragmentList = fragments;
    }


 
    @Override
    public Fragment getItem(int index) {
        return fragmentList.get(index);
//        switch (index) {
//        	case 0:
//        		return new SendFragment();
//        	case 1:
//        		return new BalanceFragment();
//        	case 2:
//        		return new ReceiveFragment();
//        }
 
//        return null;
    }

    public Fragment getFragment(int i){
        return fragmentList.get(i);
    }
 
    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
        	case 0:
        		return "Send";
        	case 1:
        		return "Balance";
        	case 2:
        		return "Recieve" ;
        }
        return super.getPageTitle(position);
    }
}
