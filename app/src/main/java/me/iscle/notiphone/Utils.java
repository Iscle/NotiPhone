package me.iscle.notiphone;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class Utils {
    private static final String TAG = "Utils";

    public static boolean hasBLE(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static boolean isMiui() {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            String miuiVersionCode = (String) get.invoke(c, "ro.miui.ui.version.code");
            return miuiVersionCode != null;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return false;
        }
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

    public static String drawableToBase64(Drawable d, int maxWidth, int maxHeight) {
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

    public static String bitmapToBase64(Bitmap b) {
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
}
