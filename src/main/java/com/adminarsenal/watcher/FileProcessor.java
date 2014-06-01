package com.adminarsenal.watcher;

import java.io.*;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Scanner;

public class FileProcessor extends Thread {

    File file;

    Map<String, FileInfo> fileList;

    public FileProcessor(File f, Map<String, FileInfo> fl) {
        file = f;
        fileList = fl;
    }

    @Override
    public void run() {

        boolean processed = false;
        long lineCount;


        while (!processed) {

            try {

                if (file != null && file.exists()) {
                    lineCount = 0;
                    try {
                        if (file.canRead()) {
                            BufferedReader br = new BufferedReader(new FileReader(file));
                            try {
                                while (br.ready()) {
                                    br.readLine();
                                    lineCount++;
                                }
                                br.close();
                            } catch (IOException e) {
                                System.out.println(MessageFormat.format("Error reading file {0}", file.getName()));
                                e.printStackTrace();
                            }
                            synchronized (fileList) {

                                FileInfo cachedFile = fileList.get(file.getName());

                                FileInfo newFile = new FileInfo(file.getName(), file.lastModified(), lineCount);

                                if (cachedFile == null) {
                                    System.out.println(MessageFormat.format("New File {0} has {1,number,#} lines", file.getName(), lineCount));
                                } else if (cachedFile.lastModified != newFile.lastModified) {
                                    System.out.println(MessageFormat.format("File {0} was modified, and now has {1,number,#} lines [ {2,number,+#;-#} line(s) ]", file.getName(), lineCount, newFile.lineCount - cachedFile.lineCount));
                                }


                                fileList.put(newFile.fileName, newFile);

                            }

                            processed = true;

                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("Error while processing file: ");
                        e.printStackTrace();
                        processed = true;
                    }
                } else {
                    processed = false;
                    if (file == null) {
                        System.out.println("Error while processing file: File was null");
                    } else {
                        System.out.println(MessageFormat.format("Error while processing file {0}: File not found", file.getName()));
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
