package eu.leps.eIDASbrowser;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReadCryptoFragment extends Fragment {

    public ReadCryptoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(eu.leps.eIDASbrowser.R.layout.fragment_read_crypto, container, false);
    }
}
