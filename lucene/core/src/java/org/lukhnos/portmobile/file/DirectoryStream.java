package org.lukhnos.portmobile.file;

import java.io.Closeable;
import java.util.Iterator;
import java.util.List;

public interface DirectoryStream<T> extends AutoCloseable, Closeable, Iterable<T> {
    static public class SimpleDirectoryStream<T> implements DirectoryStream<T> {
        List<T> paths;
        public SimpleDirectoryStream(List<T> paths) {
            this.paths = paths;
        }

        @Override
        public Iterator<T> iterator() {
            return paths.iterator();
        }

        @Override
        public void close() {
        }
    }
}
