package com.tj.partupload;

public class PartUploadResult {

    private Boolean partCompleted;

    private Boolean totalCompleted;

    public PartUploadResult(Boolean partCompleted, Boolean totalCompleted) {
        this.partCompleted = partCompleted;
        this.totalCompleted = totalCompleted;
    }

    public Boolean getPartCompleted() {
        return partCompleted;
    }

    public Boolean getTotalCompleted() {
        return totalCompleted;
    }
}
