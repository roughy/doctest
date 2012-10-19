package com.devbliss.doctest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.devbliss.doctest.items.LinkDocItem;
import com.devbliss.doctest.renderer.ReportRenderer;

/**
 * Defines some general methods to write a file and defines its name.
 * 
 * Each {@link ReportRenderer} must use this class to write the file and build its correct
 * name.
 * 
 * @author bmary
 * 
 */
public class FileHelper {

    /**
     * By convention we are using the maven project structure.
     * Therefore doctest will be written into ./target/doctests/.
     */
    public static final String OUTPUT_DIRECTORY = new File("").getAbsolutePath()
            + "/target/site/doctests/";

    /**
     * This writes out the file and retries if some other task has
     * locked the file.
     * 
     * This could cause a StackOverflowException, but I cannot
     * think of any real case where this happens...
     * 
     * @param nameOfFile
     * @throws InvalidReportException
     */
    public void writeFile(String nameCompletePath, String finalDoc) {
        // make sure the directory exists
        new File(nameCompletePath).getParentFile().mkdirs();
        writeOutFile(nameCompletePath, finalDoc);
    }

    private void writeOutFile(String nameOfFile, String content) {
        Writer fw = null;
        if (content != null) {
            try {
                fw = new FileWriter(nameOfFile);
                fw.write(content);
            } catch (IOException e) {
                try {
                    Thread.sleep(200);
                    writeOutFile(nameOfFile, content);
                } catch (InterruptedException err2) {
                    writeOutFile(nameOfFile, content);
                }
            } finally {
                closeFileWriter(fw);
            }
        }
    }

    private void closeFileWriter(Writer fw) {
        if (fw != null) {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getCompleteFileName(String name, String extension) {
        return OUTPUT_DIRECTORY + name + extension;
    }

    public String readFile(File fileToUpload) throws IOException {
        FileInputStream stream = new FileInputStream(fileToUpload);
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            String body = Charset.forName("UTF-8").decode(bb).toString();
            return body.replaceAll("\n", "<br/>");
        } finally {
            stream.close();
        }
    }

    public List<LinkDocItem> getListOfFile(String fileName) {
        File[] files = fetchFilesInDirectory(fileName);
        List<LinkDocItem> list = new ArrayList<LinkDocItem>();
        // fetch neither the file itself nor the hidden files
        for (File file : files) {
            if (!file.getPath().equals(fileName)) {
                if (!file.isHidden()) {
                    list.add(new LinkDocItem(file.getName(), file.getName()));
                }
            }
        }
        return list;
    }

    /**
     * Fetch all the files of the doctests directory. Each file corresponds to a test case.
     * 
     * @param indexFileWithCompletePath
     * @return
     */
    private File[] fetchFilesInDirectory(String indexFileWithCompletePath) {
        return new File(indexFileWithCompletePath).getParentFile().listFiles();
    }

    /**
     * Returns true if the name of the file already exists its directory
     * 
     * @param fileName
     * @return
     */
    private boolean isFileNameAlreadyTaken(String fileName) {
        String completeName = OUTPUT_DIRECTORY + fileName;
        File[] files = fetchFilesInDirectory(completeName);

        if (files != null) {
            for (File file : files) {
                if (completeName.equals(file.getPath())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void validateFileName(String fileName) throws AssertionError {
        if (fileName == null) {
            throw new AssertionError("The file name can not be null.");
        }

        if (isFileNameAlreadyTaken(fileName)) {
            throw new AssertionError("The file name " + fileName
                    + " already exists. Please choose a new one!");
        }
    }
}