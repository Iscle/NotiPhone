package me.iscle.notiphone.model;

import android.app.Notification;
import android.content.Context;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

import me.iscle.notiphone.Utils;

import static android.app.Notification.EXTRA_BIG_TEXT;
import static android.app.Notification.EXTRA_CHRONOMETER_COUNT_DOWN;
import static android.app.Notification.EXTRA_COMPACT_ACTIONS;
import static android.app.Notification.EXTRA_CONVERSATION_TITLE;
import static android.app.Notification.EXTRA_INFO_TEXT;
import static android.app.Notification.EXTRA_LARGE_ICON_BIG;
import static android.app.Notification.EXTRA_MEDIA_SESSION;
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

public class PhoneNotification {
    private static final String TAG = "PhoneNotification";

    private static final int MAX_HEIGHT = 1024;
    private static final int MAX_WIDTH = 1024;

    public static final String EXTRA_WEARABLE_EXTENSIONS = "android.wearable.EXTENSIONS";

    private final transient StatusBarNotification sbn;

    private final String appName;

    // StatusBarNotification data
    private final String groupKey;
    private final int id;
    private final String key;
    private final String opPkg;
    private final String overrideGroupKey;
    private final String packageName;
    private final long postTime;
    private final String tag;
    private final boolean isAppGroup;
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

    public PhoneNotification(Context context, StatusBarNotification sbn) {
        final long startTime = System.currentTimeMillis();

        this.sbn = sbn;

        this.appName = Utils.getApplicationName(context, sbn.getPackageName());

       // StatusBarNotification data
        this.groupKey = sbn.getGroupKey();
        this.id = sbn.getId();
        this.key = sbn.getKey();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.opPkg = sbn.getOpPkg();
        } else {
            this.opPkg = null;
        }
        this.overrideGroupKey = sbn.getOverrideGroupKey();
        this.packageName = sbn.getPackageName();
        this.postTime = sbn.getPostTime();
        this.tag = sbn.getTag();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.isAppGroup = sbn.isAppGroup();
        } else {
            this.isAppGroup = false;
        }
        this.isClearable = sbn.isClearable();
        this.isGroup = sbn.isGroup();
        this.isOngoing = sbn.isOngoing();

        // Notification data
        Notification n = sbn.getNotification();
        if (n.actions == null) {
            this.actions = null;
        } else {
            this.actions = new NotificationAction[n.actions.length];

            for (int i = 0; i < actions.length; i++) {
                Notification.Action na = n.actions[i];
                String icon = Utils.drawableToBase64(Utils.getNotificationIconDrawable(context, sbn.getPackageName(), na.getIcon()), MAX_WIDTH, MAX_HEIGHT);
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
        this.largeIcon = Utils.drawableToBase64(Utils.getNotificationIconDrawable(context, sbn.getPackageName(), n.getLargeIcon()), MAX_WIDTH, MAX_HEIGHT);
        this.smallIcon = Utils.drawableToBase64(Utils.getNotificationIconDrawable(context, sbn.getPackageName(), n.getSmallIcon()), MAX_WIDTH, MAX_HEIGHT);
        this.sortKey = n.getSortKey();
        this.iconLevel = n.iconLevel;

        // Notification data extras
        Bundle extras = n.extras;
        Log.d(TAG, "PhoneNotification: extras = " + extras.toString());
        this.bigText = Utils.toString(extras.getCharSequence(EXTRA_BIG_TEXT));
        this.chronometerCountDown = extras.getBoolean(EXTRA_CHRONOMETER_COUNT_DOWN);
        this.compactActions = extras.getIntArray(EXTRA_COMPACT_ACTIONS);
        this.conversationTitle = Utils.toString(extras.getCharSequence(EXTRA_CONVERSATION_TITLE));
        this.infoText = Utils.toString(extras.getCharSequence(EXTRA_INFO_TEXT));
        this.largeIconBig = Utils.iconToBase64(context, extras.getParcelable(EXTRA_LARGE_ICON_BIG));
        MediaSession.Token mediaSessionToken = extras.getParcelable(EXTRA_MEDIA_SESSION);
        this.mediaSession = mediaSessionToken != null ? mediaSessionToken.hashCode() : null;
        //this.messages = (Bundle[]) extras.getParcelableArray(EXTRA_MESSAGES);
        this.messages = null;
        this.picture = Utils.bitmapToBase64(extras.getParcelable(EXTRA_PICTURE));
        this.progress = extras.getInt(EXTRA_PROGRESS);
        this.progressIndeterminate = extras.getBoolean(EXTRA_PROGRESS_INDETERMINATE);
        this.progressMax = extras.getInt(EXTRA_PROGRESS_MAX);
        this.showChronometer = extras.getBoolean(EXTRA_SHOW_CHRONOMETER);
        this.showWhen = extras.getBoolean(EXTRA_SHOW_WHEN);
        this.subText = Utils.toString(extras.getCharSequence(EXTRA_SUB_TEXT));
        this.summaryText = Utils.toString(extras.getCharSequence(EXTRA_SUMMARY_TEXT));
        this.template = extras.getString(EXTRA_TEMPLATE);
        this.text = Utils.toString(extras.getCharSequence(EXTRA_TEXT));
        CharSequence[] textLinesCSA = extras.getCharSequenceArray(EXTRA_TEXT_LINES);
        if (textLinesCSA != null) {
            this.textLines = new String[textLinesCSA.length];
            for (int i = 0; i < textLinesCSA.length; i++) {
                this.textLines[i] = textLinesCSA[i].toString();
            }
        } else {
            this.textLines = null;
        }
        this.title = Utils.toString(extras.getCharSequence(EXTRA_TITLE));
        this.titleBig = Utils.toString(extras.getCharSequence(EXTRA_TITLE_BIG));

        // Notification builder
        if (extras.getBoolean("android.contains.customView", false))
            throw new RuntimeException("This notification has a customView which is not supported right now");

        Notification.Builder b = Notification.Builder.recoverBuilder(context, sbn.getNotification());
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
        if (sbn == null) {
            Log.e(TAG, "getId: sbn is null!?");
            return "";
        }
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
                ", isAppGroup=" + isAppGroup +
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
                ", largeIcon='" + (largeIcon != null) + '\'' +
                ", smallIcon='" + (smallIcon != null) + '\'' +
                ", sortKey='" + sortKey + '\'' +
                ", iconLevel=" + iconLevel +
                ", bigText='" + bigText + '\'' +
                ", chronometerCountDown=" + chronometerCountDown +
                ", compactActions=" + Arrays.toString(compactActions) +
                ", conversationTitle='" + conversationTitle + '\'' +
                ", infoText='" + infoText + '\'' +
                ", largeIconBig='" + (largeIconBig != null) + '\'' +
                ", mediaSession=" + mediaSession +
                ", messages=" + Arrays.toString(messages) +
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
