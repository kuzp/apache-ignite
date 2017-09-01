/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.ml.math;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static java.io.File.createTempFile;
import static java.nio.channels.Channels.newChannel;
import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/** */
@SuppressWarnings("unused")
final class BlasJniLoader {
    /** */
    private static final Logger log = Logger.getLogger(BlasJniLoader.class.getName());
    /** */
    private static final String JNI_EXTRACT_DIR_PROP = "org.apache.ignite.ml.math.jni.dir";
    /** */
    private static final Set<String> loaded = new HashSet<>();

    /**
     * Attempts to load a native library from the {@code java.library.path}
     * and (if that fails) will extract the named file from the classpath
     * into a pre-defined directory ({@value #JNI_EXTRACT_DIR_PROP}, or - if not
     * defined - a temporary directory is created) and load from there.
     * <p/>
     * Will stop on the first successful load of a native library.
     *
     * @param paths alternative relative path of the native library
     *              on either the library path or classpath.
     * @throws ExceptionInInitializerError if the input parameters are invalid or
     *                                     all paths failed to load (making this
     *                                     safe to use in static code blocks).
     */
    public synchronized static void load(String... paths) {
        if (paths == null || paths.length == 0)
            throw new ExceptionInInitializerError("invalid parameters");

        for (String path : paths) {
            String key = new File(path).getName();
            if (loaded.contains(key)) {
                log.info("already loaded " + path);
                return;
            }
        }

        String[] javaLibPath = System.getProperty("java.library.path").split(File.pathSeparator);
        for (String path : paths) {
            log.config("JNI LIB = " + path);
            for (String libPath : javaLibPath) {
                File file = new File(libPath, path).getAbsoluteFile();
                log.finest("checking " + file);
                if (file.exists() && file.isFile() && liberalLoad(file, path))
                    return;
            }
            File extracted = extract(path);
            if (extracted != null && liberalLoad(extracted, path))
                return;
        }

        throw new ExceptionInInitializerError("unable to load from " + Arrays.toString(paths));
    }

    /** return true if the file was loaded, otherwise false.
     * side effect: files in the tmpdir that fail to load will be deleted
     */
    private static boolean liberalLoad(File file, String name) {
        try {
            log.finest("attempting to load " + file);
            System.load(file.getAbsolutePath());
            log.info("successfully loaded " + file);
            loaded.add(name);
            return true;
        } catch (UnsatisfiedLinkError e) {
            log.log(FINE, "skipping load of " + file, e);
            String tmpdir = System.getProperty("java.io.tmpdir");
            if (tmpdir != null && tmpdir.trim().length() > 2 && file.getAbsolutePath().startsWith(tmpdir)) {
                log.log(FINE, "deleting " + file);
                try {
                    boolean res = file.delete();
                    log.finest("deleting result: " + res);
                } catch (Exception e2) {
                    log.info("failed to delete " + file);
                }
            }
            return false;
        } catch (SecurityException e) {
            log.log(INFO, "skipping load of " + file, e);
            return false;
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /** */
    private static File extract(String path) {
        try {
            long start = System.currentTimeMillis();
            URL url = BlasJniLoader.class.getResource("/" + path);
            if (url == null) return null;

            log.fine("attempting to extract " + url);

            try (InputStream in = BlasJniLoader.class.getResourceAsStream("/" + path)) {
                File file = file(path);
                deleteOnExit(file);

                log.config("extracting " + url + " to " + file.getAbsoluteFile());

                ReadableByteChannel src = newChannel(in);
                    try (FileChannel dest = new FileOutputStream(file).getChannel()) {
                    dest.transferFrom(src, 0, Long.MAX_VALUE);

                    long end = System.currentTimeMillis();
                    log.fine("extracted " + path + " in " + (end - start) + " millis");

                    return file;
                }
            }
        } catch (Throwable e) {
            if (e instanceof SecurityException || e instanceof IOException) {
                log.log(CONFIG, "skipping extraction of " + path, e);
                return null;
            } else throw new ExceptionInInitializerError(e);
        }
    }

    /** */
    private static File file(String path) throws IOException {
        String name = new File(path).getName();

        String dir = System.getProperty(JNI_EXTRACT_DIR_PROP);
        if (dir == null)
            return createTempFile("jniloader", name);

        File file = new File(dir, name);
        if (file.exists() && !file.isFile())
            throw new IllegalArgumentException(file.getAbsolutePath() + " is not a file.");
        if (!file.exists()) {
            boolean res = file.createNewFile();
            log.finest("file creation result: " + res);
        }
        return file;
    }

    /**
     * Sadly, a wrapper for the File method of this name. Swallows security exceptions, which
     * can erroneously appear (on OS X at least) despite the policy file saying otherwise and which
     * are probably not fatal at any rate.
     *
     * @param file File to be deleted
     * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6997203">Java Bug 6997203</a>
     */
    @SuppressWarnings("CallToThreadYield")
    private static void deleteOnExit(File file) {
        try {
            file.deleteOnExit();
        }
        catch (Exception e1) {
            log.log(INFO, file.getAbsolutePath() + " delete denied, retrying - might be Java bug #6997203.");
            try {
                System.gc();
                Thread.yield();
                file.deleteOnExit();
            }
            catch (Exception e2) {
                log.log(WARNING, file.getAbsolutePath() + " delete denied a second time.", e2);
            }
        }
    }

    /** */
    private BlasJniLoader() {
    }


    /**
     * Based on https://github.com/adamheinrich/native-utils/blob/master/src/main/java/cz/adamh/utils/NativeUtils.java
     * Loads library from current JAR archive
     *
     * The file from JAR is copied into system temporary directory and then loaded. The temporary file is deleted after exiting.
     * Method uses String as filename because the pathname is "abstract", not system-dependent.
     *
     * @param path The path of file inside JAR as absolute path (beginning with '/'), e.g. /package/File.ext
     * @throws IOException If temporary file creation or read/write operation fails
     * @throws IllegalArgumentException If source file (param path) does not exist
     * @throws IllegalArgumentException If the path is not absolute or if the filename is shorter than three characters (restriction of {@see File#createTempFile(java.lang.String, java.lang.String)}).
     */
    static void loadLibraryFromJar(String path) throws IOException {
        if (!path.startsWith("/"))
            throw new IllegalArgumentException("The path has to be absolute (start with '/').");

        // Obtain filename from path
        String[] parts = path.split("/");
        String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

        // Split filename to prefix and suffix (extension)
        String prefix = "";
        String suffix = null;
        if (filename != null) {
            parts = filename.split("\\.", 2);
            prefix = parts[0];
            suffix = (parts.length > 1) ? "."+parts[parts.length - 1] : null; // Thanks, davs! :-)
        }

        // Check if the filename is okay
        if (filename == null || prefix.length() < 3)
            throw new IllegalArgumentException("The filename has to be at least 3 characters long.");

        // Prepare temporary file
        File temp = File.createTempFile(prefix, suffix);
        temp.deleteOnExit();

        if (!temp.exists())
            throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");

        // Prepare buffer for data copying
        byte[] buf = new byte[1024];
        int readBytes;

        // Open and check input stream
        try (InputStream is = BlasJniLoader.class.getResourceAsStream(path)) {
            if (is == null)
                throw new FileNotFoundException("File " + path + " was not found inside JAR.");

            // Open output stream and copy data between source file in JAR and the temporary file
            try (OutputStream os = new FileOutputStream(temp)) {
                while ((readBytes = is.read(buf)) != -1)
                    os.write(buf, 0, readBytes);
            }
        }

        // Finally, load the library
        System.load(temp.getAbsolutePath());
    }

}
