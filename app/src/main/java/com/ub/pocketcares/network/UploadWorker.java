package com.ub.pocketcares.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.util.Calendar;

import com.ub.pocketcares.backend.BluetoothBeaconDatabaseHelper;
import com.ub.pocketcares.backend.DailyStatusDatabaseHelper;
import com.ub.pocketcares.utility.Utility;

public class UploadWorker extends Worker {
    private Context workerContext;
    private static final int CUTOFF_DATE = 14;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.workerContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.v("Worker_Test", "Starting upload...");
        PowerManager pm = (PowerManager) workerContext.getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "uploadData:wakeLock");
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        final Result[] workResult = new Result[1];
        workResult[0] = Result.success();
        SharedPreferences networkPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Runnable dataUpload = () -> {
            try {
                BluetoothBeaconDatabaseHelper beaconDatabaseHelper = new BluetoothBeaconDatabaseHelper(workerContext);
                HTTPHelper helper = new HTTPHelper();
                boolean deleteOld = Utility.fourteenDayCheck(workerContext, CUTOFF_DATE);
                Log.v("Worker_Test", "Starting upload...");
                String token = ServerHelper.generateToken(workerContext, helper);
                long lastUploadTime = networkPref.getLong("lastUploadTime", -1);
                boolean networkError = Calendar.getInstance().getTimeInMillis() - lastUploadTime >= Utility.DAY_IN_MILLS;
                uploadContact(helper, beaconDatabaseHelper.getUploadData(deleteOld, networkError), token);
                Log.v("Worker_Test", "Upload Done...");
            } catch (Exception e) {
                Log.v("Worker_Test", "Server error...");
                e.printStackTrace();
                workResult[0] = Result.retry();
            } finally {
                SharedPreferences.Editor networkPrefEdit = networkPref.edit();
                networkPrefEdit.putLong("lastUploadTime", Calendar.getInstance().getTimeInMillis());
                networkPrefEdit.apply();
            }
        };
        new Thread(dataUpload).start();
        wakeLock.release();
        return workResult[0];
    }

    private void uploadContact(HTTPHelper helper, String data, String token) throws IOException {
        if (data != null) {
            helper.postRequest(ServerHelper.CONTACT_ENDPOINT, data, token);
        }
    }

}
