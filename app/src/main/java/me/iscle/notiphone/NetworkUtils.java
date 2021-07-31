package me.iscle.notiphone;

import android.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NetworkUtils {
    public static void read(InputStream is, byte[] data) throws IOException {
        int received = 0;
        do {
            int ret = is.read(data, received, data.length - received);
            if (ret != -1) received += ret;
        } while (received != data.length); // TODO: Implement a timeout?
    }

    public static int readInt(InputStream is) throws IOException {
        int[] buf = new int[4];
        int received = 0;

        do {
            int ret = is.read();
            if (ret != -1) buf[received++] = ret;
        } while (received != buf.length); // TODO: Implement a timeout?

        return buf[0] << 24 | buf[1] << 16 | buf[2] << 8 | buf[3];
    }

    public static void writeInt(OutputStream os, int data) throws IOException {
        os.write(data >>> 24);
        os.write(data >>> 16);
        os.write(data >>> 8);
        os.write(data);
    }

    public static byte[] concatenateBytes(@Nullable byte[] prev, @Nullable byte[] next) {
        if (prev == null && next == null) return new byte[0];
        else if (prev == null) return next;
        else if (next == null) return prev;
        byte[] data = new byte[prev.length + next.length];
        System.arraycopy(prev, 0, data, 0, prev.length);
        System.arraycopy(next, 0, data, prev.length, next.length);
        return data;
    }
}
