package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.background.Constants;

/**
 * *用于对图片进行模糊处理
 */
public class BlurWorker extends Worker {
    private static final String TAG = BlurWorker.class.getSimpleName();
    public BlurWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        WorkerUtils.makeStatusNotification("blur image starts",applicationContext);
        WorkerUtils.sleep();
        String inputData = getInputData().getString(Constants.KEY_IMAGE_URI);
        try {
           // Bitmap bitmap = BitmapFactory.decodeResource(applicationContext.getResources(), R.drawable.test);
           if(TextUtils.isEmpty(inputData)) {
               Log.e(TAG, "Invalid input uri");
               throw new IllegalArgumentException("Invalid input uri");
           }
            ContentResolver resolver = applicationContext.getContentResolver();
            Bitmap bitmap = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(inputData)));
            Bitmap outBitmap = WorkerUtils.blurBitmap(bitmap, applicationContext);
            Uri tempUri = WorkerUtils.writeBitmapToFile(applicationContext,outBitmap);
            WorkerUtils.makeStatusNotification("output is " + tempUri.toString(),applicationContext);
            Data outputData = new Data.Builder().putString(Constants.KEY_IMAGE_URI,tempUri.toString()).build();
            return Result.success(outputData);
        } catch (Throwable throwable) {
            Log.e(TAG, "Error applying blur", throwable);
            return Result.failure();
        }
    }
}
