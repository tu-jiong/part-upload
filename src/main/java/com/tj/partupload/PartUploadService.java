package com.tj.partupload;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.util.Objects;

public class PartUploadService {

    private static final Logger logger = LoggerFactory.getLogger(PartUploadService.class);

    public PartUploadResult uploadPart(PartUploadParam param, InputStream inputStream) {
        File tmpFile = null;
        RandomAccessFile raf = null;
        PartMetaFile partMetaFile = null;
        FileInputStream tmpInputStream = null;
        FileInputStream tmpMd5 = null;
        long actuallyWriteLength = 0;
        boolean success = false;
        try {
            File tmpDir = FileUtils.getTempDirectory();
            tmpFile = new File(tmpDir, param.getFileMd5());
            if (!tmpFile.isFile() || !tmpFile.exists()) {
                tmpFile.createNewFile();
            }
            partMetaFile = new PartMetaFile(tmpDir + File.separator + param.getFileMd5() + ".part", param.getTotalPart());
            logger.info("part upload tmp path {}", tmpFile.getAbsolutePath());
            raf = new RandomAccessFile(tmpFile, "rw");
            raf.setLength(param.getFileSize());
            PartMeta partMeta = partMetaFile.readPartMeta(param.getCurrentPart());
            long writtenLength = partMeta.getLength();
            long skip = inputStream.skip(writtenLength);
            actuallyWriteLength = skip;
            long seekPos = param.getPos() + skip;
            raf.seek(seekPos);
            byte[] bytes = new byte[1024 * 1024];
            int len;
            while ((len = inputStream.read(bytes)) > 0) {
                raf.write(bytes, 0, len);
                actuallyWriteLength += len;
            }
            partMetaFile.writePartMeta(param.getCurrentPart(), param.getPos(), actuallyWriteLength, true);
            if (partMetaFile.isAllPartCompleted()) {
                tmpMd5 = new FileInputStream(tmpFile);
                String sMd5 = DigestUtils.md5DigestAsHex(tmpMd5);
                if (!Objects.equals(sMd5, param.getFileMd5())) {
                    throw new RuntimeException("file md5 not match");
                }
                logger.info("[分片上传] 上传完毕");
                success = true;
                return new PartUploadResult(true, true);
            }
            return new PartUploadResult(true, false);
        } catch (Exception e) {
            logger.error("上传失败", e);
            if (Objects.nonNull(partMetaFile)) {
                try {
                    partMetaFile.writePartMeta(param.getCurrentPart(), param.getPos(), actuallyWriteLength, false);
                } catch (IOException ie) {
                    //ignore
                }
            }
            return new PartUploadResult(false, false);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(raf);
            IOUtils.closeQuietly(tmpInputStream);
            IOUtils.closeQuietly(tmpMd5);
            IOUtils.closeQuietly(partMetaFile);
            if (success) {
                FileUtils.deleteQuietly(tmpFile);
                partMetaFile.deleteQuietly();
            }
        }
    }
}
