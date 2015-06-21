package org.lukhnos.portmobile.channels.utils;

import org.lukhnos.portmobile.file.Files;
import org.lukhnos.portmobile.file.StandardOpenOption;
import org.lukhnos.portmobile.file.Path;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;

public class FileChannelUtils {
    public static FileChannel open(Path path, StandardOpenOption... options) throws IOException {
        List<StandardOpenOption> optionList = Arrays.asList(options);

        if (optionList.size() == 1 && optionList.contains(StandardOpenOption.READ)) {
            RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");
            return raf.getChannel();
        } else if (optionList.contains(StandardOpenOption.WRITE)) {
            if (Files.notExists(path) && optionList.contains(StandardOpenOption.CREATE)) {
                Files.createFile(path);
            }

            RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");
            return raf.getChannel();
        }


        throw new IOException("Unknown options: " + options);
    }

    // TODO: Compatibility test.
}
