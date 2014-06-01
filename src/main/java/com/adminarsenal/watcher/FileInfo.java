package com.adminarsenal.watcher;

class FileInfo {

    public FileInfo(String fileName, long lastModified, long lineCount) {
        this.fileName = fileName;
        this.lastModified = lastModified;
        this.lineCount = lineCount;
    }

    final String fileName;
    final long lastModified;
    final long lineCount;

}
