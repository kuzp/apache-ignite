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

/** */
@SuppressWarnings("unused")
final class BlasJniLoader {
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
