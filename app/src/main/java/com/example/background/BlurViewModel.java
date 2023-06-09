/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;

import com.example.background.workers.BlurWorker;
import com.example.background.workers.CleanUpWorker;
import com.example.background.workers.SavedImageWorker;

import java.util.List;

public class BlurViewModel extends ViewModel {

    private final Uri mImageUri;
    private final WorkManager workManager;
    private final LiveData<List<WorkInfo>> mSavedWorkInfo;
    //显示最终输出的uri
    private Uri mOutputUri;

    public BlurViewModel(@NonNull Application application) {
        super();
        mImageUri = getImageUri(application.getApplicationContext());
        workManager = WorkManager.getInstance(application.getApplicationContext());
        mSavedWorkInfo = workManager.getWorkInfosByTagLiveData(Constants.TAG_OUTPUT);
    }

    public LiveData<List<WorkInfo>> getOutputWorkInfo() {
        return mSavedWorkInfo;
    }

    public void setOutputUri(String mOutputImage) {
        this.mOutputUri = uriOrNull(mOutputImage);
    }

    /**
     * Create the WorkRequest chain to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    void applyBlur(int blurLevel) {
        //限制条件
        Constraints constraints = new Constraints.Builder().setRequiresStorageNotLow(true).build();
        //确保一次只运行一个工作链，现在一次只会对一张图片进行模糊处理。
        WorkContinuation continuation = workManager.beginUniqueWork(
                Constants.IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.from(CleanUpWorker.class));
        // Add WorkRequests to blur the image the number of times requested
        for(int i = 0;i < blurLevel; i++) {
            OneTimeWorkRequest.Builder BlurWorker = new OneTimeWorkRequest.Builder(BlurWorker.class);
            if(i == 0) {
                BlurWorker.setInputData(createInputDataFromUri());
            }
            continuation = continuation.then(BlurWorker.build());
        }
        OneTimeWorkRequest saveWorker = new OneTimeWorkRequest.Builder(SavedImageWorker.class)
                .setConstraints(constraints)
                .addTag(Constants.TAG_OUTPUT)
                .build();
        continuation = continuation.then(saveWorker);
        continuation.enqueue();
    }

    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }

    public Uri getOutputUri() {
        return mOutputUri;
    }

    private Uri getImageUri(Context context) {
        Resources resources = context.getResources();

        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(R.drawable.android_cupcake))
                .appendPath(resources.getResourceTypeName(R.drawable.android_cupcake))
                .appendPath(resources.getResourceEntryName(R.drawable.android_cupcake))
                .build();
    }

    /**
     * Getters
     */
    Uri getImageUri() {
        return mImageUri;
    }

    //创建数据输入对象
    private Data createInputDataFromUri() {
        Data.Builder builder = new Data.Builder();
        if(mImageUri != null) {
            builder.putString(Constants.KEY_IMAGE_URI,mImageUri.toString());
        }
        return builder.build();
    }

    /**
     * Cancel work using the work's unique name
     */
    public void cancelWork(){
        workManager.cancelUniqueWork(Constants.IMAGE_MANIPULATION_WORK_NAME);
    }

}