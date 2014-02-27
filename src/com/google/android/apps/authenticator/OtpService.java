
package com.google.android.apps.authenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.apps.authenticator.AccountDb.OtpType;
import com.google.android.apps.authenticator.testability.DependencyInjector;

public class OtpService extends Service {

    private static final String TAG = OtpService.class.getSimpleName();

    private AccountDb mAccountDb;
    private OtpSource mOtpProvider;

    public OtpService() {
    }

    @Override
    public void onCreate() {
        mAccountDb = DependencyInjector.getAccountDb();
        mOtpProvider = DependencyInjector.getOtpProvider();
    }

    private synchronized String getOtp(String accountName) throws OtpSourceException {
        if (!mAccountDb.nameExists(accountName)) {
            Log.d(TAG, "Account not found: " + accountName); 
            return null;
        }
        
        OtpType type = mAccountDb.getType(accountName);
        Log.d(TAG, String.format("Getting %s code for %s", type, accountName));

        return mOtpProvider.getNextCode(accountName);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final IRemoteOtpSource.Stub binder = new IRemoteOtpSource.Stub() {

        @Override
        public String getNextCode(String accountName) throws RemoteException {
            try {
                return getOtp(accountName);
            } catch (OtpSourceException e) {
                throw new RemoteException(e.getMessage());
            }
        }
    };
}
