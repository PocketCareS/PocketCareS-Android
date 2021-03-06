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

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ub.pocketcares.R;
import com.ub.pocketcares.utility.Utility;

public class IntroductionFragment extends Fragment {
    public static final String FAQ_URL = "https://engineering.buffalo.edu/computer-science-engineering/pocketcares.html";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_introduction, container, false);

        TextView disclaimer = v.findViewById(R.id.textView7);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Utility.launchInAppLink(getResources().getColor(R.color.ub), getContext(), FAQ_URL);
            }
        };
        String infoString = getString(R.string.introduction_activity);
        disclaimer.setText(Utility.getSpannableHyperlink(infoString, clickableSpan, infoString.indexOf("FAQ"), infoString.length()));
        disclaimer.setMovementMethod(LinkMovementMethod.getInstance());
        return v;
    }

}

