package com.tj.partupload;

public class PartUploadParam {

    private String fileMd5;

    private Long pos;

    private Short totalPart;

    private Short currentPart;

    private Long fileSize;

    private String originalFileName;

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Long getPos() {
        return pos;
    }

    public void setPos(Long pos) {
        this.pos = pos;
    }

    public Short getTotalPart() {
        return totalPart;
    }

    public void setTotalPart(Short totalPart) {
        this.totalPart = totalPart;
    }

    public Short getCurrentPart() {
        return currentPart;
    }

    public void setCurrentPart(Short currentPart) {
        this.currentPart = currentPart;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
}
