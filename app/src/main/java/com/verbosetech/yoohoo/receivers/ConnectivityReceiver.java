package com.verbosetech.whatsclone.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;


public class ConnectivityReceiver extends BroadcastReceiver {

    private static ArrayList<ConnectivityReceiverListener> connectivityReceiverListeners = new ArrayList<>();
    private static ConnectivityManager cm;

    public ConnectivityReceiver() {
        super();
    }

    public static void init(Context applicationContext){
        cm = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();

        if (connectivityReceiverListeners != null) {
            for (ConnectivityReceiverListener connectivityReceiverListener:
                    connectivityReceiverListeners) {
                connectivityReceiverListener.onNetworkConnectionChanged(isConnected);
            }
        }
    }

    public static boolean isConnected() {
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    public static void register(ConnectivityReceiverListener connectivityReceiverListener){
        connectivityReceiverListeners.add(connectivityReceiverListener);
    }

    public static void unregister(ConnectivityReceiverListener connectivityReceiverListener){
        connectivityReceiverListeners.remove(connectivityReceiverListener);
    }

    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }
}