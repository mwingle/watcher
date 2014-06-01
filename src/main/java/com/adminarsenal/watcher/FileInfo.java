package com.adminarsenal.watcher;

public class FileInfo {

    public FileInfo(String fileName, long lastModified, long lineCount) {
        this.fileName = fileName;
        this.lastModified = lastModified;
        this.lineCount = lineCount;
    }

    String fileName;
    long lastModified;
    long lineCount;

}
