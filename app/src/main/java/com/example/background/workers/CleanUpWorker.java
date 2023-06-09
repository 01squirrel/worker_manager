package com.example.background.workers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.background.Constants;
import java.io.File;

//清楚临时文件夹里的照片
public class CleanUpWorker extends Worker {

private static final String TAG = CleanUpWorker.class.getSimpleName();
    public CleanUpWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        WorkerUtils.makeStatusNotification("clean up work starts",context);
        WorkerUtils.sleep();
        try{
            File tempDir = new File(context.getFilesDir(), Constants.OUTPUT_PATH);
            if(tempDir.exists()) {
                File[] files = tempDir.listFiles();
                if(files != null && files.length > 0) {
                    for(File image : files) {
                        String name = image.getName();
                        if(!TextUtils.isEmpty(name) && name.endsWith(".png")) {
                            boolean deleted = image.delete();
                            Log.i(TAG, String.format("Deleted %s - %s",
                                    name, deleted));
                        }
                    }
                }
            }
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up", e);
            return Result.failure();
        }
    }
}
