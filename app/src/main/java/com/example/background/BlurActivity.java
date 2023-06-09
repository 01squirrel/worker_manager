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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.work.Data;
import androidx.work.WorkInfo;

import com.bumptech.glide.Glide;
import com.example.background.databinding.ActivityBlurBinding;

//您需要让用户模糊处理他们自己的图片
public class BlurActivity extends AppCompatActivity {

    private BlurViewModel mViewModel;
    private ActivityBlurBinding binding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlurBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get the ViewModesl
        mViewModel = new BlurViewModel(getApplication());

        // Setup blur image file button
        binding.goButton.setOnClickListener(view -> {
            new Thread(() -> mViewModel.applyBlur(getBlurLevel())).start();
        });
        //观察livedata以更新视图
        mViewModel.getOutputWorkInfo().observe(this,listOfWorkInfos -> {
            if (listOfWorkInfos == null || listOfWorkInfos.isEmpty()) return;
            //获取列表中的第一个 WorkInfo；只有一个标记为 TAG_OUTPUT 的 WorkInfo，因为我们的工作链是唯一的
            WorkInfo workInfo = listOfWorkInfos.get(0);
            //标记和显示工作状态
            boolean isFinished = workInfo.getState().isFinished();
            if (!isFinished) {
                showWorkInProgress();
            } else {
                showWorkFinished();
                Data output = workInfo.getOutputData();
                String uri = output.getString(Constants.KEY_IMAGE_URI);
                if (!TextUtils.isEmpty(uri)) {
                    mViewModel.setOutputUri(uri);
                    binding.seeFileButton.setVisibility(View.VISIBLE);
                }
            }
        });
        //查看模糊后的图片
        binding.seeFileButton.setOnClickListener(view -> {
            Uri currentUri = mViewModel.getOutputUri();
            if(currentUri != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW,currentUri);
                if(intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        binding.cancelButton.setOnClickListener(view -> mViewModel.cancelWork());
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private void showWorkInProgress() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.cancelButton.setVisibility(View.VISIBLE);
        binding.goButton.setVisibility(View.GONE);
        binding.seeFileButton.setVisibility(View.GONE);
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private void showWorkFinished() {
        binding.progressBar.setVisibility(View.GONE);
        binding.cancelButton.setVisibility(View.GONE);
        binding.goButton.setVisibility(View.VISIBLE);
    }

    /**
     * Get the blur level from the radio button as an integer
     * @return Integer representing the amount of times to blur the image
     */
    private int getBlurLevel() {
        switch(binding.radioBlurGroup.getCheckedRadioButtonId()) {
            case R.id.radio_blur_lv_1:
                return 1;
            case R.id.radio_blur_lv_2:
                return 2;
            case R.id.radio_blur_lv_3:
                return 3;
        }

        return 1;
    }
}