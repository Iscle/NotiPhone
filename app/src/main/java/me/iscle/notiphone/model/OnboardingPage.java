package me.iscle.notiphone.model;

import android.view.View;

import androidx.annotation.DrawableRes;

public class OnboardingPage {
    @DrawableRes private final int drawable;
    private final String title;
    private final String description;
    private final Button button;

    public OnboardingPage(@DrawableRes int drawable, String title, String description) {
        this(drawable, title, description, null, null);
    }

    public OnboardingPage(@DrawableRes int drawable, String title, String description, String buttonText, View.OnClickListener clickListener) {
        this.drawable = drawable;
        this.title = title;
        this.description = description;
        if (buttonText != null && clickListener != null) {
            this.button = new Button(buttonText, clickListener);
        } else {
            this.button = null;
        }
    }

    @DrawableRes
    public int getDrawable() {
        return drawable;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Button getButton() {
        return button;
    }

    public static class Button {
        private final String text;
        private final View.OnClickListener clickListener;

        public Button(String text, View.OnClickListener clickListener) {
            this.text = text;
            this.clickListener = clickListener;
        }

        public String getText() {
            return text;
        }

        public View.OnClickListener getOnClickListener() {
            return clickListener;
        }
    }
}
