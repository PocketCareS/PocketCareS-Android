/*
 * Copyright 2020 University at Buffalo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ub.pocketcares.introduction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.appintro.AppIntro2;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.model.SliderPage;


import java.util.Objects;

import pub.devrel.easypermissions.AppSettingsDialog;

import com.ub.pocketcares.R;
import com.ub.pocketcares.home.MainActivity;
import com.ub.pocketcares.utility.FirstTimeChecker;
import com.ub.pocketcares.utility.PreferenceTags;

@SuppressLint("Registered")
public class AppIntroActivity extends AppIntro2 {
    AlertDialog alertMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addWelcomeSlide();
        addDisclaimerSlide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 3, true);
        } else {
            askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 3, true);
        }
        addLocationPermissionsSlide();
        addBluetoothNameGeneratorSlide();
        addLastPage();
        setWizardMode(true);
        setIndicatorColor(Color.BLUE, Color.BLACK);
        setSystemBackButtonLocked(true);
        setNextPageSwipeLock(true);
        setSwipeLock(true);
    }

    public void addWelcomeSlide() {
//        Slide 1
        addSlide(new IntroductionFragment());
    }

    public void addDisclaimerSlide() {
//        Slide 2
        addSlide(new DisclaimerFragment());
    }

    public void addLocationPermissionsSlide() {
        //slide 4
        SliderPage sliderPage = new SliderPage();
        sliderPage.setTitle("Location Permission Request");
        sliderPage.setTitleColor(Color.BLACK);
        sliderPage.setDescription("PocketCare S requires GPS to be always enabled, but your location information is not stored anywhere.");
        sliderPage.setDescriptionColor(R.color.black0);
        sliderPage.setImageDrawable(R.drawable.map);
        sliderPage.setBackgroundColor(Color.WHITE);
        addSlide(AppIntroFragment.newInstance(sliderPage));
    }

    public void addBluetoothNameGeneratorSlide() {
//        Slide 5
        addSlide(new BluetoothOnFragment());
    }

    public void addLastPage() {
        SliderPage sliderPage = new SliderPage();
        sliderPage.setDescription("Thank You! You are all set to use this application.");
        sliderPage.setDescriptionColor(R.color.black);
        sliderPage.setImageDrawable(R.drawable.updated_logo);
        sliderPage.setBackgroundColor(Color.WHITE);
        addSlide(AppIntroFragment.newInstance(sliderPage));
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        startActivity(new Intent(this, MainActivity.class));
        finish();
        firstTimeCheck();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

    @Override
    protected void onUserDeniedPermission(@NonNull String permissionName) {
//        User pressed "Deny" on the permission dialog
        super.onUserDeniedPermission(permissionName);
        if (Manifest.permission.ACCESS_BACKGROUND_LOCATION.equals(permissionName)) {
            Toast.makeText(this, "Please grant Background Location permission for this app to scan in background.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please grant Location permission for this app to work.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onUserDisabledPermission(@NonNull String permissionName) {
//      User pressed "Deny" + "Don't ask again" on the permission dialog
        super.onUserDisabledPermission(permissionName);
        if (alertMessage == null) {
            createPermissionDialog(permissionName);
        }
    }

    private void createPermissionDialog(String permissionName) {
        String message;
        if (Manifest.permission.ACCESS_BACKGROUND_LOCATION.equals(permissionName)) {
            message = "In order to scan for encounters in background, " + getString(R.string.app_name) +
                    " needs to have Background location permission. Please grant the permission. Would you like to go to the settings to grant permission?";
        } else {
            message = getString(R.string.app_name) + " will not work without location. Your information is not stored anywhere. " +
                    "Please grant the permission. Would you like to go to the settings to grant permission?";
        }
        alertMessage = new AlertDialog.Builder(this, R.style.DisclaimerDialogTheme)
                .setTitle("Warning!")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> {
                    alertMessage.dismiss();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    alertMessage = null;
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    alertMessage = null;
                }).show();
        Objects.requireNonNull(alertMessage.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void firstTimeCheck() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean(PreferenceTags.STATUS_ACCEPTENCE, true);
        ed.apply();
        FirstTimeChecker.setBooleanPreferenceValue(this, "isFirstTimeExecution", true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("Perms", "Request Code: " + requestCode + ", Result Code: " + resultCode);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            alertMessage.dismiss();
        }
    }
}
