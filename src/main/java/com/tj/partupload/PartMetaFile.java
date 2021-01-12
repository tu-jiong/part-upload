package com.tj.partupload;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Objects;

public class PartMetaFile implements Closeable {

    private static final int META_LENGTH = 19;
    private static final int PART_NUMBER_LENGTH = 2;
    private static final Object lock = new Object();
    private final File file;
    private final RandomAccessFile raf;

    public PartMetaFile(String path, short partNumber) throws IOException {
        if (StringUtils.isEmpty(path)) {
            throw new RuntimeException("path must not null");
        }
        file = new File(path);
        if (!file.isFile() || !file.exists()) {
            boolean res = file.createNewFile();
            if (res) {
                raf = new RandomAccessFile(file, "rw");
                init(partNumber);
                return;
            }
        }
        raf = new RandomAccessFile(file, "rw");
    }

    private void init(short totalPart) throws IOException {
        if (totalPart < 0) {
            throw new RuntimeException("invalid partNumber : " + totalPart);
        }
        synchronized (lock) {
            FileChannel channel = raf.getChannel();
            FileLock lock = null;
            try {
                for (; ; ) {
                    try {
                        lock = channel.lock();
                        break;
                    } catch (IOException | OverlappingFileLockException e) {
                        //ignore
                    }
                }
                raf.seek(0);
                raf.writeShort(totalPart);
                for (int i = 1; i <= totalPart; i++) {
                    raf.writeShort(i);
                    raf.writeLong(0);
                    raf.writeLong(0);
                    raf.writeByte(0);
                }
            } finally {
                if (Objects.nonNull(lock)) {
                    lock.release();
                }
            }
        }
    }

    public void writePartMeta(short currentPart, long pos, long length, boolean isPartCompleted) throws IOException {
        synchronized (lock) {
            FileChannel channel = raf.getChannel();
            FileLock lock = null;
            try {
                for (; ; ) {
                    try {
                        lock = channel.lock();
                        break;
                    } catch (IOException | OverlappingFileLockException e) {
                        //ignore
                    }
                }
                raf.seek(0);
                short totalPart = raf.readShort();
                if (currentPart > totalPart) {
                    throw new RuntimeException("invalid partNumber : " + currentPart + " , total : " + totalPart);
                }
                int skipBytes = PART_NUMBER_LENGTH + META_LENGTH * (currentPart - 1);
                raf.seek(skipBytes + PART_NUMBER_LENGTH);
                raf.writeLong(pos);
                raf.writeLong(length);
                if (isPartCompleted) {
                    raf.writeByte(1);
                } else {
                    raf.writeByte(0);
                }
            } finally {
                if (Objects.nonNull(lock)) {
                    lock.release();
                }
            }
        }
    }

    public PartMeta readPartMeta(short currentPart) throws IOException {
        synchronized (lock) {
            FileChannel channel = raf.getChannel();
            FileLock lock = null;
            try {
                for (; ; ) {
                    try {
                        lock = channel.lock();
                        break;
                    } catch (IOException | OverlappingFileLockException e) {
                        //ignore
                    }
                }
                raf.seek(0);
                short totalPart = raf.readShort();
                if (currentPart > totalPart) {
                    throw new RuntimeException("invalid partNumber : " + currentPart + " , total : " + totalPart);
                }
                int skipBytes = PART_NUMBER_LENGTH + META_LENGTH * (currentPart - 1);
                raf.seek(skipBytes);
                PartMeta partMeta = new PartMeta();
                partMeta.setPartNumber(raf.readShort());
                partMeta.setPos(raf.readLong());
                partMeta.setLength(raf.readLong());
                partMeta.setPartCompleted(raf.readByte() == 1);
                return partMeta;
            } finally {
                if (Objects.nonNull(lock)) {
                    lock.release();
                }
            }
        }
    }

    public short readTotalPart() throws IOException {
        synchronized (lock) {
            FileChannel channel = raf.getChannel();
            FileLock lock = null;
            try {
                for (; ; ) {
                    try {
                        lock = channel.lock();
                        break;
                    } catch (IOException | OverlappingFileLockException e) {
                        //ignore
                    }
                }
                raf.seek(0);
                return raf.readShort();
            } finally {
                if (Objects.nonNull(lock)) {
                    lock.release();
                }
            }
        }
    }

    public boolean isAllPartCompleted() {
        synchronized (lock) {
            FileChannel channel = raf.getChannel();
            FileLock lock = null;
            try {
                for (; ; ) {
                    try {
                        lock = channel.lock();
                        break;
                    } catch (IOException | OverlappingFileLockException e) {
                        //ignore
                    }
                }
                raf.seek(0);
                short totalPart = raf.readShort();
                for (int i = 1; i <= totalPart; i++) {
                    raf.seek(PART_NUMBER_LENGTH + META_LENGTH * (i - 1) + META_LENGTH - 1);
                    byte res = raf.readByte();
                    if (res == 0) {
                        return false;
                    }
                }
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (Objects.nonNull(lock)) {
                    try {
                        lock.release();
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
        }
    }

    public void deleteQuietly() {
        FileUtils.deleteQuietly(file);
    }

    @Override
    public void close() {
        synchronized (lock) {
            FileChannel channel = raf.getChannel();
            FileLock lock = null;
            try {
                for (; ; ) {
                    try {
                        lock = channel.lock();
                        break;
                    } catch (IOException | OverlappingFileLockException e) {
                        //ignore
                    }
                }
                raf.close();
            } catch (IOException e) {
                //ignore
            } finally {
                if (Objects.nonNull(lock)) {
                    try {
                        lock.release();
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
        }
    }
}
