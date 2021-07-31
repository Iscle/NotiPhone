package me.iscle.notiphone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class Utils {
    private static final String TAG = "Utils";

    public static boolean hasBLE(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static String[] csArrayToString(CharSequence[] csArray) {
        final String[] result = new String[csArray.length];

        for (int i = 0; i < csArray.length; i++) {
            result[i] = csArray[i].toString();
        }

        return result;
    }

    public static void writeLength(OutputStream os, int length) throws IOException {
        os.write(length >> 24);
        os.write(length >> 16);
        os.write(length >> 8);
        os.write(length);
    }

    public static int readLength(InputStream is) throws IOException {
        int length = 0;

        length |= (is.read() << 24);
        length |= (is.read() << 16);
        length |= (is.read() << 8);
        length |= is.read();

        return length;
    }

    public static String readString(InputStream is, int length) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int currentLength = 0;

        while (currentLength != length) {
            int bufLen = is.read(buffer, 0, Math.min(buffer.length, length - currentLength));
            currentLength += bufLen;
            baos.write(buffer, 0, bufLen);
        }

        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    @Nullable
    public static String drawableToBase64(@Nullable Drawable d, int maxWidth, int maxHeight) {
        if (d == null)
            return null;
        return bitmapToBase64(resizeMaxBitmap(getBitmapFromVectorDrawable(d), maxWidth, maxHeight));
    }

    public static Bitmap resizeMaxBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        if (width < maxWidth) {
            maxWidth = width;
        }

        if (height < maxHeight) {
            maxHeight = height;
        }

        final float ratioBitmap = (float) width / (float) height;
        final float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;
        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }

    public static String iconToBase64(Context c, @Nullable Icon i) {
        if (i == null)
            return null;

        return bitmapToBase64(drawableToBitmap(i.loadDrawable(c)));
    }

    public static Bitmap drawableToBitmap(Drawable d) {
        if (d instanceof BitmapDrawable)
            return ((BitmapDrawable) d).getBitmap();

        if (d.getIntrinsicHeight() <= 0 || d.getIntrinsicHeight() <= 0)
            return null;

        Bitmap b = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, c.getWidth(), c.getHeight());
        d.draw(c);
        return b;
    }

    public static String bitmapToBase64(@Nullable Bitmap b) {
        if (b == null)
            return null;

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        b.compress(Bitmap.CompressFormat.WEBP, 25, baos);

        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    public static String getApplicationName(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        try {
            final ApplicationInfo ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

            if (ai.labelRes == 0) {
                return ai.nonLocalizedLabel.toString();
            } else {
                final Resources appRes = packageManager.getResourcesForApplication(packageName);
                return appRes.getString(ai.labelRes);
            }
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static Drawable getDrawableFromPackage(Context context, int idRes, String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        try {
            final Resources appRes = packageManager.getResourcesForApplication(packageName);
            return appRes.getDrawable(idRes, null);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            return null;
        }
    }

    public static Bitmap getBitmapFromVectorDrawable(Drawable drawable) {
        final Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @SuppressLint("ResourceType")
    @Nullable
    public static Drawable getNotificationIconDrawable(Context context, String packageName, @Nullable Icon icon) {
        if (icon == null)
            return null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (icon.getType() != Icon.TYPE_RESOURCE || packageName.equals(icon.getResPackage()) || "com.android.systemui".equals(packageName)) {
                    return icon.loadDrawable(context);
                }

                String resPackage = icon.getResPackage();
                // figure out where to load resources from
                if (TextUtils.isEmpty(resPackage)) {
                    // if none is specified, try the given context
                    resPackage = packageName;
                }
                Resources resources;
                if ("android".equals(resPackage)) {
                    resources = Resources.getSystem();
                } else {
                    final PackageManager pm = context.getPackageManager();
                    try {
                        ApplicationInfo ai = pm.getApplicationInfo(
                                resPackage, PackageManager.MATCH_UNINSTALLED_PACKAGES);
                        if (ai != null) {
                            resources = pm.getResourcesForApplication(ai);
                        } else {
                            Log.e(TAG, "getNotificationIconDrawable: failed to get application info for package " + resPackage);
                            return null;
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, "getNotificationIconDrawable: Unable to get package resources", e);
                        return null;
                    }
                }
                return ResourcesCompat.getDrawable(resources, icon.getResId(), context.getTheme());
            } else { // Let's use reflection :)
                try {
                    Field mTypeField = Icon.class.getDeclaredField("mType");
                    mTypeField.setAccessible(true);
                    Field mString1Field = Icon.class.getDeclaredField("mString1");
                    mString1Field.setAccessible(true);
                    String resPackage = (String) mString1Field.get(icon);
                    if (mTypeField.getInt(icon) != Icon.TYPE_RESOURCE || packageName.equals(resPackage)) {
                        return icon.loadDrawable(context);
                    }

                    // figure out where to load resources from
                    if (TextUtils.isEmpty(resPackage)) {
                        // if none is specified, try the given context
                        resPackage = packageName;
                    }
                    Resources resources;
                    if ("android".equals(resPackage)) {
                        resources = Resources.getSystem();
                    } else {
                        final PackageManager pm = context.getPackageManager();
                        try {
                            ApplicationInfo ai = pm.getApplicationInfo(
                                    resPackage, PackageManager.MATCH_UNINSTALLED_PACKAGES);
                            if (ai != null) {
                                resources = pm.getResourcesForApplication(ai);
                            } else {
                                Log.e(TAG, "getNotificationIconDrawable: failed to get application info for package " + resPackage);
                                return null;
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            Log.e(TAG, "getNotificationIconDrawable: Unable to get package resources", e);
                            return null;
                        }
                    }
                    Field mInt1Field = Icon.class.getDeclaredField("mInt1");
                    mInt1Field.setAccessible(true);
                    return ResourcesCompat.getDrawable(resources, mInt1Field.getInt(icon), context.getTheme());
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    Log.e(TAG, "getNotificationIconDrawable: reflection failed!", e);
                    return null;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getNotificationIconDrawable: FIX ME!", e);
            return null;
        }
    }

    public static String toString(CharSequence cs) {
        if (cs == null)
            return null;

        return cs.toString();
    }

    public static void closeCloseable(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            // Ignored
        }
    }
}
