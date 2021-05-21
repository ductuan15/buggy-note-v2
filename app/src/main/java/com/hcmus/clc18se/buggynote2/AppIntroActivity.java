package com.hcmus.clc18se.buggynote2;

import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

public class AppIntroActivity extends IntroActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setButtonBackVisible(false);
        setButtonNextVisible(false);
        setButtonCtaVisible(true);
        setButtonCtaTintMode(BUTTON_CTA_TINT_MODE_BACKGROUND);

        TypefaceSpan labelSpan = new TypefaceSpan(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                ? "sans-serif-medium" : "sans serif");

        SpannableString label = SpannableString.valueOf(getString(R.string.intro_title));
        label.setSpan(labelSpan, 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        setButtonCtaLabel(label);

        setPageScrollInterpolator(android.R.interpolator.fast_out_slow_in);

        addSlide(new SimpleSlide.Builder()
                .title(R.string.title_intro1)
                .description(R.string.description_intro1)
                .image(R.drawable.app_intro1)
                .background(R.color.note_light_white)
                .backgroundDark(R.color.note_dark_white)
                .layout(R.layout.intro_slide)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.title_intro2)
                .description(R.string.description_intro2)
                .image(R.drawable.app_intro2)
                .background(R.color.indigo_500)
                .backgroundDark(R.color.indigo_700)
                .layout(R.layout.intro_slide)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.title_intro3)
                .description("Anytime, Anywhere")
                .image(R.drawable.app_intro3)
                .background(R.color.green_500)
                .backgroundDark(R.color.green_700)
                .layout(R.layout.intro_slide)
                .build());
    }
}
