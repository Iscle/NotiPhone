package me.iscle.notiphone.model;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;

import me.iscle.notiphone.Utils;

import static android.app.Notification.EXTRA_BIG_TEXT;
import static android.app.Notification.EXTRA_CHRONOMETER_COUNT_DOWN;
import static android.app.Notification.EXTRA_COMPACT_ACTIONS;
import static android.app.Notification.EXTRA_CONVERSATION_TITLE;
import static android.app.Notification.EXTRA_INFO_TEXT;
import static android.app.Notification.EXTRA_LARGE_ICON_BIG;
import static android.app.Notification.EXTRA_MEDIA_SESSION;
import static android.app.Notification.EXTRA_PEOPLE;
import static android.app.Notification.EXTRA_PICTURE;
import static android.app.Notification.EXTRA_PROGRESS;
import static android.app.Notification.EXTRA_PROGRESS_INDETERMINATE;
import static android.app.Notification.EXTRA_PROGRESS_MAX;
import static android.app.Notification.EXTRA_SHOW_CHRONOMETER;
import static android.app.Notification.EXTRA_SHOW_WHEN;
import static android.app.Notification.EXTRA_SUB_TEXT;
import static android.app.Notification.EXTRA_SUMMARY_TEXT;
import static android.app.Notification.EXTRA_TEMPLATE;
import static android.app.Notification.EXTRA_TEXT;
import static android.app.Notification.EXTRA_TEXT_LINES;
import static android.app.Notification.EXTRA_TITLE;
import static android.app.Notification.EXTRA_TITLE_BIG;
import static me.iscle.notiphone.Utils.bitmapToBase64;
import static me.iscle.notiphone.Utils.csArrayToString;
import static me.iscle.notiphone.Utils.drawableToBase64;
import static me.iscle.notiphone.Utils.getApplicationName;

public class PhoneNotification implements Serializable {
    private static final String TAG = "PhoneNotification";

    private final transient StatusBarNotification sbn;

    private final String appName;

    // StatusBarNotification data
    private final String groupKey;
    private final String id;
    private final String key;
    private final String opPkg;
    private final String overrideGroupKey;
    private final String packageName;
    private final long postTime;
    private final String tag;
    private final int uid;
    private final boolean isClearable;
    private final boolean isGroup;
    private final boolean isOngoing;

    // Notification data
    private final NotificationAction[] actions;
    private final String category;
    private final int color;
    private final int flags;
    private final int number;
    private final String tickerText;
    private final int visibility;
    private final long when;
    private final String group;
    private final String largeIcon;
    private final String smallIcon;
    private final String sortKey;

    // Notification data extras
    private final String bigText;
    private final boolean chronometerCountDown;
    private final int[] compactActions;
    private final String conversationTitle;
    private final String infoText;
    private final String largeIconBig;
    private final Integer mediaSession;
    //private final Bundle[] messages;
    private final String[] people;
    private final String picture;
    private final int progress;
    private final boolean progressIndeterminate;
    private final int progressMax;
    private final boolean showChronometer;
    private final boolean showWhen;
    private final String subText;
    private final String summaryText;
    private final String template;
    private final String text;
    private final String[] textLines;
    private final String title;
    private final String titleBig;

    public PhoneNotification(Context c, StatusBarNotification sbn) {
        final long startTime = System.currentTimeMillis();

        this.sbn = sbn;

        this.appName = getApplicationName(c, sbn.getPackageName());

       // StatusBarNotification data
        this.groupKey = sbn.getGroupKey();
        this.id = sbn.getPackageName() + ":" + sbn.getId();
        this.key = sbn.getKey();
        this.opPkg = sbn.getOpPkg();
        this.overrideGroupKey = sbn.getOverrideGroupKey();
        this.packageName = sbn.getPackageName();
        this.postTime = sbn.getPostTime();
        this.tag = sbn.getTag();
        this.uid = sbn.getUid();
        this.isClearable = sbn.isClearable();
        this.isGroup = sbn.isGroup();
        this.isOngoing = sbn.isOngoing();

        // Notification data
        Notification n = sbn.getNotification();
        if (n.actions == null) {
            this.actions = null;
        } else {
            this.actions = new NotificationAction[n.actions.length];
            Field resIdField;
            try {
                resIdField = Icon.class.getDeclaredField("mInt1");
                resIdField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                resIdField = null;
            }

            for (int i = 0; i < actions.length; i++) {
                Notification.Action na = n.actions[i];
                Drawable icon;
                if (resIdField != null && na.getIcon() != null) {
                    try {
                        icon = Utils.getDrawableFromPackage(c, resIdField.getInt(na.getIcon()), sbn.getPackageName());
                    } catch (IllegalAccessException e) {
                        icon = null;
                    }
                } else {
                    icon = null;
                }
                this.actions[i] = new NotificationAction(na.title.toString(), icon, na.hashCode());
            }
        }
        this.category = n.category;
        this.color = n.color;
        this.flags = n.flags;
        this.number = n.number;
        this.tickerText = n.tickerText != null ? n.tickerText.toString() : null;
        this.visibility = n.visibility;
        this.when = n.when;
        this.group = n.getGroup();
        this.largeIcon = n.getLargeIcon() != null ? drawableToBase64(n.getLargeIcon().loadDrawable(c), 400, 400) : null;
        this.smallIcon = n.getSmallIcon() != null ? drawableToBase64(n.getSmallIcon().loadDrawable(c), 400, 400) : null;
        this.sortKey = n.getSortKey();

        // Notification data extras
        Bundle extras = n.extras;
        CharSequence bigTextCs = extras.getCharSequence(EXTRA_BIG_TEXT);
        this.bigText = bigTextCs != null ? bigTextCs.toString() : null;
        this.chronometerCountDown = extras.getBoolean(EXTRA_CHRONOMETER_COUNT_DOWN);
        this.compactActions = extras.getIntArray(EXTRA_COMPACT_ACTIONS);
        CharSequence conversationTitleCs = extras.getCharSequence(EXTRA_CONVERSATION_TITLE);
        this.conversationTitle = conversationTitleCs != null ? conversationTitleCs.toString() : null;
        CharSequence infoTextCs = extras.getCharSequence(EXTRA_INFO_TEXT);
        this.infoText = infoTextCs != null ? infoTextCs.toString() : null;
        Bitmap largeIconBigBmp = extras.getParcelable(EXTRA_LARGE_ICON_BIG);
        this.largeIconBig = largeIconBigBmp != null ? bitmapToBase64(largeIconBigBmp) : null;
        MediaSession.Token mediaSessionToken = extras.getParcelable(EXTRA_MEDIA_SESSION);
        this.mediaSession = mediaSessionToken != null ? extras.getParcelable(EXTRA_MEDIA_SESSION).hashCode() : null;
        //this.messages = (Bundle[]) extras.getParcelableArray(EXTRA_MESSAGES); // TODO: check
        this.people = extras.getStringArray(EXTRA_PEOPLE);
        Bitmap pictureBmp = extras.getParcelable(EXTRA_PICTURE);
        this.picture = pictureBmp != null ? bitmapToBase64(pictureBmp) : null;
        this.progress = extras.getInt(EXTRA_PROGRESS);
        this.progressIndeterminate = extras.getBoolean(EXTRA_PROGRESS_INDETERMINATE);
        this.progressMax = extras.getInt(EXTRA_PROGRESS_MAX);
        this.showChronometer = extras.getBoolean(EXTRA_SHOW_CHRONOMETER);
        this.showWhen = extras.getBoolean(EXTRA_SHOW_WHEN);
        CharSequence subTextCs = extras.getCharSequence(EXTRA_SUB_TEXT);
        this.subText = subTextCs != null ? subTextCs.toString() : null;
        CharSequence summaryTextCs = extras.getCharSequence(EXTRA_SUMMARY_TEXT);
        this.summaryText = summaryTextCs != null ? summaryTextCs.toString() : null;
        this.template = extras.getString(EXTRA_TEMPLATE);
        CharSequence textCs = extras.getCharSequence(EXTRA_TEXT);
        this.text = textCs != null ? textCs.toString() : null;
        CharSequence[] textLinesCs = extras.getCharSequenceArray(EXTRA_TEXT_LINES);
        this.textLines = textLinesCs != null ? csArrayToString(textLinesCs) : null;
        CharSequence titleCs = extras.getCharSequence(EXTRA_TITLE);
        this.title = titleCs != null ? titleCs.toString() : null;
        CharSequence titleBigCs = extras.getCharSequence(EXTRA_TITLE_BIG);
        this.titleBig = titleBigCs != null ? titleBigCs.toString() : null;

        final long endTime = System.currentTimeMillis();
        Log.d(TAG, "Notification for " + appName + " took " + (endTime - startTime) + "ms");

        Log.d(TAG, toString());
    }

    public StatusBarNotification getSbn() {
        return sbn;
    }

    public String getTemplate() {
        return template;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "PhoneNotification{" +
                "sbn=" + sbn +
                ", appName='" + appName + '\'' +
                ", groupKey='" + groupKey + '\'' +
                ", id=" + id +
                ", key='" + key + '\'' +
                ", opPkg='" + opPkg + '\'' +
                ", overrideGroupKey='" + overrideGroupKey + '\'' +
                ", packageName='" + packageName + '\'' +
                ", postTime=" + postTime +
                ", tag='" + tag + '\'' +
                ", uid=" + uid +
                ", isClearable=" + isClearable +
                ", isGroup=" + isGroup +
                ", isOngoing=" + isOngoing +
                ", actions=" + Arrays.toString(actions) +
                ", category='" + category + '\'' +
                ", color=" + color +
                ", flags=" + flags +
                ", number=" + number +
                ", tickerText='" + tickerText + '\'' +
                ", visibility=" + visibility +
                ", when=" + when +
                ", group='" + group + '\'' +
                ", sortKey='" + sortKey + '\'' +
                ", bigText='" + bigText + '\'' +
                ", chronometerCountDown=" + chronometerCountDown +
                ", compactActions=" + Arrays.toString(compactActions) +
                ", conversationTitle='" + conversationTitle + '\'' +
                ", infoText='" + infoText + '\'' +
                ", largeIconBig='" + largeIconBig + '\'' +
                ", mediaSession=" + mediaSession +
                ", people=" + Arrays.toString(people) +
                ", picture='" + picture + '\'' +
                ", progress=" + progress +
                ", progressIndeterminate=" + progressIndeterminate +
                ", progressMax=" + progressMax +
                ", showChronometer=" + showChronometer +
                ", showWhen=" + showWhen +
                ", subText='" + subText + '\'' +
                ", summaryText='" + summaryText + '\'' +
                ", template='" + template + '\'' +
                ", text='" + text + '\'' +
                ", textLines=" + Arrays.toString(textLines) +
                ", title='" + title + '\'' +
                ", titleBig='" + titleBig + '\'' +
                '}';
    }
}
