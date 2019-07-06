package me.iscle.notiphone.Model;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Base64;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import static android.app.Notification.EXTRA_BIG_TEXT;
import static android.app.Notification.EXTRA_CONVERSATION_TITLE;
import static android.app.Notification.EXTRA_LARGE_ICON_BIG;
import static android.app.Notification.EXTRA_PICTURE;
import static android.app.Notification.EXTRA_SUB_TEXT;
import static android.app.Notification.EXTRA_SUMMARY_TEXT;
import static android.app.Notification.EXTRA_TEMPLATE;
import static android.app.Notification.EXTRA_TEXT;
import static android.app.Notification.EXTRA_TEXT_LINES;
import static android.app.Notification.EXTRA_TITLE;

public class PhoneNotification implements Serializable {
    private static final String TAG = "PhoneNotification";

    private final transient StatusBarNotification sbn;

    // StatusBarNotification data
    private final String groupKey;
    private final int id;
    private final String key;
    private final String packageName;
    private final boolean isClearable;
    private final boolean isGroup;
    private final boolean isOngoing;

    // Notification data
    private final NotificationAction[] actions;
    private final int color;
    //private Bundle extras;
    private final long when;
    private final String largeIcon;
    private final String smallIcon;
    private final String title;
    private final String text;
    private final String subText;
    private final String bigText;
    private final String conversationTitle;
    private final String largeIconBig;
    private final String picture;
    private final String summaryText;
    private final String[] textLines;
    private final String template;

    public PhoneNotification(Context c, StatusBarNotification sbn) {
        this.sbn = sbn;

        // StatusBarNotification data
        this.groupKey = sbn.getGroupKey();
        this.id = sbn.getId();
        this.key = sbn.getKey();
        this.packageName = sbn.getPackageName();
        this.isClearable = sbn.isClearable();
        this.isGroup = sbn.isGroup();
        this.isOngoing = sbn.isOngoing();

        // Notification data
        Notification n = sbn.getNotification();
        this.actions = new NotificationAction[NotificationCompat.getActionCount(n)];
        for (int i = 0; i < actions.length; i++) {
            Notification.Action na = n.actions[i];
            this.actions[i] = new NotificationAction(na.title.toString(), na.hashCode());
        }
        this.color = n.color;
        //this.extras = n.extras;
        Log.d(TAG, "PhoneNotification: " + n.extras.toString());
        this.when = n.when;
        this.largeIcon = n.getLargeIcon() != null ? drawableToBase64(n.getLargeIcon().loadDrawable(c)) : null;
        this.smallIcon = n.getSmallIcon() != null ? drawableToBase64(n.getSmallIcon().loadDrawable(c)) : null;

        // Notification data extras
        Bundle extras = n.extras;
        this.title = extras.get(EXTRA_TITLE) != null ? extras.get(EXTRA_TITLE).toString() : null;
        this.text = extras.get(EXTRA_TEXT) != null ? extras.get(EXTRA_TEXT).toString() : null;
        this.subText = extras.get(EXTRA_SUB_TEXT) != null ? extras.get(EXTRA_SUB_TEXT).toString() : null;
        this.bigText = extras.getString(EXTRA_BIG_TEXT);
        this.conversationTitle = extras.getString(EXTRA_CONVERSATION_TITLE);
        this.largeIconBig = extras.getString(EXTRA_LARGE_ICON_BIG);
        this.picture = extras.getString(EXTRA_PICTURE);
        this.summaryText = extras.getString(EXTRA_SUMMARY_TEXT);
        CharSequence[] tmpTextLines = extras.getCharSequenceArray(EXTRA_TEXT_LINES);
        if (tmpTextLines != null) {
            this.textLines = new String[tmpTextLines.length];
            for (int i = 0; i < this.textLines.length; i++) {
                this.textLines[i] = tmpTextLines[i].toString();
            }
        } else {
            this.textLines = null;
        }
        this.template = extras.getString(EXTRA_TEMPLATE);
    }

    private static String drawableToBase64(Drawable d) {
        Bitmap b = getBitmapFromVectorDrawable(d);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        b.compress(Bitmap.CompressFormat.WEBP, 25, baos);

        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private static Bitmap getBitmapFromVectorDrawable(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public StatusBarNotification getSbn() {
        return sbn;
    }
}
