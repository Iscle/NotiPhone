package me.iscle.notiphone;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import me.iscle.notiphone.service.ConnectionThread;

public class SendFileThread {
    private static final int CHUNK_SIZE = 1 << 7;

    private ConnectionThread connectionThread;
    private String uuid;
    private boolean cancelled;
    private boolean finished;

    public SendFileThread(ConnectionThread connectionThread) {
        this.connectionThread = connectionThread;
        this.uuid = UUID.randomUUID().toString();
        this.cancelled = false;
    }

    public void sendFile(File file) throws IOException {
        if (isCancelled() || isFinished()) return;

        byte[] buf = new byte[CHUNK_SIZE];
        FileInputStream fileInputStream = new FileInputStream(file);

        while (fileInputStream.read(buf) != -1) {
            if (isCancelled()) {
                // Enviar trama de cancel·lació
                return;
            } else {
                // Enviar trama amb dades
            }
        }

        // Enviar checksum

        finished = true;
    }

    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isFinished() {
        return finished;
    }
}
