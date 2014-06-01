package com.adminarsenal.watcher;

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.*;

/**
 * You may use C#, VB, or Java to implement the project. Zip up your implementation sources into a single file.
 * In this exercise you will create a command-line program to watch for text files to be created or modified in a directory and then output information about them.
 * <p/>
 * - The program takes 2 arguments, the directory to watch and a file pattern, example: program.exe "c:file folder" *.txt
 * - The path may be an absolute path, relative to the current directory, or UNC.
 * - Use the modified date of the file as a trigger that the file has changed.
 * - Check for changes every 10 seconds.
 * - When a file is created output a line to the console with its name and how many lines are in it.
 * - When a file is modified output a line with its name and the change in number of lines (use a + or - to indicate more or less).
 * - When a file is deleted output a line with its name.
 * - Files will be ASCII or UTF-8 and will use Windows line separators (CR LF).
 * - Multiple files may be changed at the same time, can be up to 2 GB in size, and may be locked for several seconds at a time.
 * - Use multiple threads so that the program doesn't block on a single large or locked file.
 * - Program will be run on Windows 7 and file names are case insensitive.
 */
public class FileWatcher {

    public static int TIME_TO_WAIT_MILLIS = 1000 * 10;

    static Map<String, FileInfo> fileList = Collections.synchronizedMap(new HashMap<String, FileInfo>());

    static File directory;
    static String pattern;
    static FilenameFilter filenameFilter;

    public static void main(String[] args) {
        if (args.length == 0 || args.length > 2) {
            System.out.println("args = " + Arrays.toString(args));
            System.out.println("FileWatcher - this program keeps an eye out for changes to the files in the specified directory");
            System.out.println("Usage:  FileWatcher \"directory\" [\"extension\"]");
            System.out.println("*use quotes to avoid globbing*");
            System.exit(-1);
        }

        String path = args[0];

        path = path.replaceAll("\\\\", "/");

/*
        if (path.indexOf("////") >= 0) {
            path.replaceAll("\\\\", "//");
            if (!path.toUpperCase().startsWith("FILE:")) {
                path = "file:" + path;
            }

            System.out.println("directoryURI = " + path);
            try {
                directory = Paths.get(new URL(path).toURI()).toFile();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
*/
        System.out.println("directoryURI = " + path);
        directory = new File(path);

/*
        }
*/

        if (!directory.exists()) {
            System.out.println("Directory " + directory + " does not exist.");
            System.exit(1);
        }

        if (!directory.isDirectory()) {
            System.out.println(directory + " is not a directory.");
            System.exit(1);

        }

        if (args.length > 1 && args[1] != null) {
            pattern = args[1].replace("*", ""); //need to see if this is necessary, doesn't hurt to have it
        } else {
            pattern = "";
        }

        filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.toUpperCase().endsWith(pattern.toUpperCase())) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        boolean running = true;

        while (running) {
            File[] newList = directory.listFiles(filenameFilter);

            processDeletedFiles(newList);

            processNewOrModifiedFiles(newList);

            try {
                Thread.sleep(TIME_TO_WAIT_MILLIS);
            } catch (InterruptedException e) {
                running = false;
            }
        }

    }

    private static void processNewOrModifiedFiles(File[] newList) {
        for (int i = 0; i < newList.length; i++) {
            File newFile = newList[i];
            synchronized (fileList) {
                FileInfo cachedFileInfo = fileList.get(newFile.getName());

                //only start a thread if the file has been added or modified
                if (newFile.isFile()) {
                    if (cachedFileInfo == null || newFile.lastModified() != cachedFileInfo.lastModified) {
                        new FileProcessor(newFile, fileList).start();
                    }
                }
            }
        }
    }

    private static void processDeletedFiles(File[] newList) {
        synchronized (fileList) {
            for (Iterator<String> iterator = fileList.keySet().iterator(); iterator.hasNext(); ) {
                String cachedFilename = iterator.next();
                boolean found = false;
                for (int i = 0; i < newList.length; i++) {
                    File newFile = newList[i];
                    if (cachedFilename.equals(newFile.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println(MessageFormat.format("File {0} has been deleted.", cachedFilename));
                    iterator.remove();
                }
            }
        }
    }
}
