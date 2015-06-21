package org.lukhnos.portmobile.file;

import java.io.File;
import java.io.IOException;

public class Path {
    File file;

    Path(String path) {
        file = new File(path);
    }

    Path(File file) {
        this.file = file;
    }

    public File toFile() {
        return file;
    }

    public Path toRealPath() throws IOException {
        // We assume case sensitivity and other representations don't matter.
        return this;
    }

    public Path getFileName() {
        return new Path(file.getName());
    }

    public String toString() {
        return file.toString();
    }

    public Path resolve(String other) {
        if (other.isEmpty()) {
            return this;
        }

        File otherFile = new File(other);
        if (otherFile.isAbsolute()) {
            return new Path(otherFile);
        }

        return new Path(new File(this.file, other));
    }

    public boolean isAbsolute() {
        return file.isAbsolute();
    }

    public Path toAbsolutePath() {
        return new Path(file.getAbsoluteFile());
    }

    public static void main(String args[]) {
        // TODO: Implement and move tests.
        System.out.println("oldcoffee");

        Path p = Paths.get("/foo/bar/blah.txt");
        System.out.println(p.toString());
        System.out.println(p.getFileName().toString());

        System.out.println(Paths.get("").toString());
        System.out.println(Paths.get("/foo").resolve(""));
        System.out.println(Paths.get("/foo").resolve("/bar"));
        System.out.println(Paths.get("/foo").resolve("bar.txt"));
    }

    public Path getParent() {
        return new Path(file.getParent());
    }
}
