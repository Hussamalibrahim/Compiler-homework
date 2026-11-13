package com.compiler.service;

public class ProjectFileInfo {
    private final String fileName;
    private final String relativePath;
    private final String fileType;
    private final long fileSizeBytes;
    private final int lineCount;

    public ProjectFileInfo(String fileName, String relativePath, String fileType, long fileSizeBytes, int lineCount) {
        this.fileName = fileName;
        this.relativePath = relativePath;
        this.fileType = fileType;
        this.fileSizeBytes = fileSizeBytes;
        this.lineCount = lineCount;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getFileType() {
        return fileType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getFormattedSize() {
        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        } else if (fileSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", fileSizeBytes / 1024.0);
        } else {
            return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
        }
    }

    public int getLineCount() {
        return lineCount;
    }
}
