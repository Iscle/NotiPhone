package me.iscle.notiphone.model;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;

import me.iscle.notiphone.R;
import me.iscle.notiphone.Utils;

import static android.app.Notification.EXTRA_BIG_TEXT;
import static android.app.Notification.EXTRA_CHRONOMETER_COUNT_DOWN;
import static android.app.Notification.EXTRA_COMPACT_ACTIONS;
import static android.app.Notification.EXTRA_CONVERSATION_TITLE;
import static android.app.Notification.EXTRA_INFO_TEXT;
import static android.app.Notification.EXTRA_LARGE_ICON_BIG;
import static android.app.Notification.EXTRA_MEDIA_SESSION;
import static android.app.Notification.EXTRA_MESSAGES;
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
import static me.iscle.notiphone.Utils.drawableToBase64;
import static me.iscle.notiphone.Utils.getApplicationName;
import static me.iscle.notiphone.Utils.iconToBase64;

public class PhoneNotification {
    private static final String TAG = "PhoneNotification";

    private final transient StatusBarNotification sbn;

    private final String appName;

    // StatusBarNotification data
    private final String groupKey;
    private final int id;
    private final String key;
    private final String overrideGroupKey;
    private final String packageName;
    private final long postTime;
    private final String tag;
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
    private final int iconLevel;

    // Notification data extras
    private final String bigText;
    private final boolean chronometerCountDown;
    private final int[] compactActions;
    private final String conversationTitle;
    private final String infoText;
    private final String largeIconBig;
    private final Integer mediaSession;
    private final Bundle[] messages;
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
        this.id = sbn.getId();
        this.key = sbn.getKey();
        this.overrideGroupKey = sbn.getOverrideGroupKey();
        this.packageName = sbn.getPackageName();
        this.postTime = sbn.getPostTime();
        this.tag = sbn.getTag();
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
        this.iconLevel = n.iconLevel;

        // Notification data extras
        Bundle extras = n.extras;
        Log.d(TAG, "PhoneNotification: " + extras.getCharSequence(EXTRA_TITLE).getClass().getName());
        this.bigText = extras.getCharSequence(EXTRA_BIG_TEXT) == null ? null : extras.getCharSequence(EXTRA_BIG_TEXT).toString();
        this.chronometerCountDown = extras.getBoolean(EXTRA_CHRONOMETER_COUNT_DOWN);
        this.compactActions = extras.getIntArray(EXTRA_COMPACT_ACTIONS);
        this.conversationTitle = extras.getCharSequence(EXTRA_CONVERSATION_TITLE) == null ? null : extras.getCharSequence(EXTRA_CONVERSATION_TITLE).toString();
        this.infoText = extras.getCharSequence(EXTRA_INFO_TEXT) == null ? null : extras.getCharSequence(EXTRA_INFO_TEXT).toString();
        this.largeIconBig = iconToBase64(c, extras.getParcelable(EXTRA_LARGE_ICON_BIG));
        if (this.largeIconBig == null) Log.d(TAG, "PhoneNotification: largeIconBig is null!!!!!");
        MediaSession.Token mediaSessionToken = extras.getParcelable(EXTRA_MEDIA_SESSION);
        this.mediaSession = mediaSessionToken != null ? mediaSessionToken.hashCode() : null;
        //this.messages = (Bundle[]) extras.getParcelableArray(EXTRA_MESSAGES);
        this.messages = null;
        if (extras.getParcelable(EXTRA_PICTURE) == null) Log.d(TAG, "PhoneNotification: picture is null!!!!!");
        this.picture = bitmapToBase64(extras.getParcelable(EXTRA_PICTURE));
        this.progress = extras.getInt(EXTRA_PROGRESS);
        this.progressIndeterminate = extras.getBoolean(EXTRA_PROGRESS_INDETERMINATE);
        this.progressMax = extras.getInt(EXTRA_PROGRESS_MAX);
        this.showChronometer = extras.getBoolean(EXTRA_SHOW_CHRONOMETER);
        this.showWhen = extras.getBoolean(EXTRA_SHOW_WHEN);
        this.subText = extras.getCharSequence(EXTRA_SUB_TEXT) == null ? null : extras.getCharSequence(EXTRA_SUB_TEXT).toString();
        this.summaryText = extras.getCharSequence(EXTRA_SUMMARY_TEXT) == null ? null : extras.getCharSequence(EXTRA_SUMMARY_TEXT).toString();
        this.template = extras.getString(EXTRA_TEMPLATE);
        this.text = extras.getCharSequence(EXTRA_TEXT) == null ? null : extras.getCharSequence(EXTRA_TEXT).toString();
        CharSequence[] textLinesCsA = extras.getCharSequenceArray(EXTRA_TEXT_LINES);
        if (textLinesCsA != null) {
            this.textLines = new String[textLinesCsA.length];
            for (int i = 0; i < textLinesCsA.length; i++) {
                this.textLines[i] = textLinesCsA[i].toString();
            }
        } else {
            this.textLines = null;
        }
        this.title = extras.getCharSequence(EXTRA_TITLE) == null ? null : extras.getCharSequence(EXTRA_TITLE).toString();
        this.titleBig = extras.getCharSequence(EXTRA_TITLE_BIG) == null ? null : extras.getCharSequence(EXTRA_TITLE_BIG).toString();

        // Notification builder
        if (extras.getBoolean("android.contains.customView", false))
            throw new RuntimeException("This notification has a customView which is not supported right now!");

        Notification.Builder b = Notification.Builder.recoverBuilder(c, sbn.getNotification());
        if (TextUtils.isEmpty(template)) {

        } else {
            //throw new RuntimeException("This notification has a template: " + template + "!");
        }

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
        return getId(packageName, id);
    }

    public static String getId(StatusBarNotification sbn) {
        return getId(sbn.getPackageName(), sbn.getId());
    }

    public static String getId(String packageName, int id) {
        return packageName + ":" + id;
    }

    public String getAppName() {
        return appName;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public String getKey() {
        return key;
    }

    public String getOverrideGroupKey() {
        return overrideGroupKey;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getPostTime() {
        return postTime;
    }

    public String getTag() {
        return tag;
    }

    public boolean isClearable() {
        return isClearable;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public boolean isOngoing() {
        return isOngoing;
    }

    public NotificationAction[] getActions() {
        return actions;
    }

    public String getCategory() {
        return category;
    }

    public int getColor() {
        return color;
    }

    public int getFlags() {
        return flags;
    }

    public int getNumber() {
        return number;
    }

    public String getTickerText() {
        return tickerText;
    }

    public int getVisibility() {
        return visibility;
    }

    public long getWhen() {
        return when;
    }

    public String getGroup() {
        return group;
    }

    public String getLargeIcon() {
        return largeIcon;
    }

    public String getSmallIcon() {
        return smallIcon;
    }

    public String getSortKey() {
        return sortKey;
    }

    public CharSequence getBigText() {
        return bigText;
    }

    public boolean isChronometerCountDown() {
        return chronometerCountDown;
    }

    public int[] getCompactActions() {
        return compactActions;
    }

    public CharSequence getConversationTitle() {
        return conversationTitle;
    }

    public CharSequence getInfoText() {
        return infoText;
    }

    public String getLargeIconBig() {
        return largeIconBig;
    }

    public Integer getMediaSession() {
        return mediaSession;
    }

    public Bundle[] getMessages() {
        return messages;
    }

    public String getPicture() {
        return picture;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isProgressIndeterminate() {
        return progressIndeterminate;
    }

    public int getProgressMax() {
        return progressMax;
    }

    public boolean isShowChronometer() {
        return showChronometer;
    }

    public boolean isShowWhen() {
        return showWhen;
    }

    public CharSequence getSubText() {
        return subText;
    }

    public CharSequence getSummaryText() {
        return summaryText;
    }

    public CharSequence getText() {
        return text;
    }

    public CharSequence[] getTextLines() {
        return textLines;
    }

    public CharSequence getTitle() {
        return title;
    }

    public CharSequence getTitleBig() {
        return titleBig;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
