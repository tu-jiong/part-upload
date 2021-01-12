package com.tj.partupload;

public class PartMeta {

    private short partNumber;

    private long pos;

    private long length;

    private boolean isPartCompleted;

    public short getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(short partNumber) {
        this.partNumber = partNumber;
    }

    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public boolean isPartCompleted() {
        return isPartCompleted;
    }

    public void setPartCompleted(boolean partCompleted) {
        isPartCompleted = partCompleted;
    }
}
