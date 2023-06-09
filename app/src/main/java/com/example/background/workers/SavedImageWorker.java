package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.background.Constants;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//保存图片到设备
public class SavedImageWorker extends Worker {

    public SavedImageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    private static final String TAG = SavedImageWorker.class.getSimpleName();
    private static final String TITLE = "Blurred Image";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.CHINA);
    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        WorkerUtils.makeStatusNotification("Saved Image start",context);
        ContentResolver resolver = context.getContentResolver();
        try {
            String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);
            Bitmap image = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)));
            String outputUri = MediaStore.Images.Media.insertImage(resolver,image, TITLE,FORMAT.format(new Date()));
            if(TextUtils.isEmpty(outputUri)) {
                Log.e(TAG, "Writing to MediaStore failed");
                return Result.failure();
            }
            Data outputData = new Data.Builder().putString(Constants.KEY_IMAGE_URI,outputUri).build();
            return Result.success(outputData);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to save image to Gallery", e);
            return Worker.Result.failure();
        }
    }
}
