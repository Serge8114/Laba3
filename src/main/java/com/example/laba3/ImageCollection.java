package com.example.laba3;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

public class ImageCollection implements Aggregate {
    private File[] files;
    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    public ImageCollection(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            this.files = new File[0];
            return;
        }

        this.files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String lowerName = name.toLowerCase();
                for (String ext : IMAGE_EXTENSIONS) {
                    if (lowerName.endsWith(ext)) {
                        return true;
                    }
                }
                return false;
            }
        });

        if (files == null) {
            this.files = new File[0];
        } else {
            Arrays.sort(files, Comparator.comparing(File::getName));
        }
    }

    @Override
    public Iterator getIterator() {
        return new ImageFileIterator();
    }

    public File getFile(int index) {
        if (index >= 0 && index < files.length) {
            return files[index];
        }
        return null;
    }

    public int size() {
        return files.length;
    }

    public boolean isEmpty() {
        return files.length == 0;
    }

    class ImageFileIterator implements Iterator {
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return files.length > 0;
        }

        @Override
        public Object next() {
            if (files.length == 0) {
                throw new NoSuchElementException();
            }
            File file = files[currentIndex];
            currentIndex = (currentIndex + 1) % files.length;
            return file;
        }

        @Override
        public Object preview() {
            if (files.length == 0) {
                throw new NoSuchElementException();
            }
            int prevIndex = (currentIndex - 1 + files.length) % files.length;
            return files[prevIndex];
        }

        @Override
        public boolean hasPreview() {
            return files.length > 0;
        }

        public boolean hasPrevious() {
            return files.length > 0;
        }

        public Object previous() {
            if (files.length == 0) {
                throw new NoSuchElementException();
            }
            currentIndex = (currentIndex - 1 + files.length) % files.length;
            return files[currentIndex];
        }

        public void reset() {
            currentIndex = 0;
        }

        public int getCurrentIndex() {
            return currentIndex;
        }

        public File getCurrentFile() {
            if (files.length == 0) {
                return null;
            }
            return files[currentIndex];
        }

        public void goToFirst() {
            if (files.length > 0) {
                currentIndex = 0;
            }
        }

        public void goToLast() {
            if (files.length > 0) {
                currentIndex = files.length - 1;
            }
        }
    }
}
