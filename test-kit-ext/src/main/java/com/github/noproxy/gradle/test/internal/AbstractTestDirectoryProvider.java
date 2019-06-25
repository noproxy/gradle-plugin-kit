/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.noproxy.gradle.test.internal;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Pattern;


/**
 * A JUnit rule which provides a unique temporary folder for the test.
 */
public abstract class AbstractTestDirectoryProvider implements TestRule {
    private static final Random RANDOM = new Random();
    private static final int ALL_DIGITS_AND_LETTERS_RADIX = 36;
    private static final int MAX_RANDOM_PART_VALUE = Integer.valueOf("zzzzz", ALL_DIGITS_AND_LETTERS_RADIX);
    private static final Pattern WINDOWS_RESERVED_NAMES = Pattern.compile("(con)|(prn)|(aux)|(nul)|(com\\d)|(lpt\\d)", Pattern.CASE_INSENSITIVE);
    protected File root;
    private File dir;
    private String prefix;
    private boolean cleanup = true;
    private boolean suppressCleanupErrors;

    private static File join(File file, Object... path) {
        File current = file.getAbsoluteFile();
        for (Object p : path) {
            current = new File(current, p.toString());
        }
        try {
            return current.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not canonicalise '%s'.", current), e);
        }
    }

    static long toMillis(double seconds) {
        return (long) (seconds * 1000);
    }

    //simplistic polling assertion. attempts asserting every x millis up to some max timeout
    static void poll(Action<Void> assertion) {
        long start = System.nanoTime() / 1000000L;
        long expiry = start + toMillis(10); // convert to ms
        long sleepTime = 100;
        while (true) {
            try {
                assertion.execute(null);
                return;
            } catch (Throwable t) {
                if (System.nanoTime() / 1000000L > expiry) {
                    throw t;
                }
                sleepTime = Math.min(250, (long) (sleepTime * 1.2));
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public File getRoot() {
        return root;
    }

    public void suppressCleanup() {
        cleanup = false;
    }

    public void suppressCleanupErrors() {
        suppressCleanupErrors = true;
    }

    public boolean isCleanup() {
        return cleanup;
    }

    public void cleanup() {
        if (cleanup && dir != null && dir.exists()) {
            poll(aVoid -> {
                try {
                    FileUtils.forceDelete(dir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public Statement apply(final Statement base, Description description) {
        init(description.getMethodName(), description.getTestClass().getSimpleName());

        return new TestDirectoryCleaningStatement(base, description);
    }

    protected void init(String methodName, String className) {
        if (methodName == null) {
            // must be a @ClassRule; use the rule's class name instead
            methodName = getClass().getSimpleName();
        }
        if (prefix == null) {
            String safeMethodName = methodName.replaceAll("[^\\w]", "_");
            if (safeMethodName.length() > 30) {
                safeMethodName = safeMethodName.substring(0, 19) + "..." + safeMethodName.substring(safeMethodName.length() - 9);
            }
            prefix = String.format("%s/%s", className, safeMethodName);
        }
    }

    public File getTestDirectory() {
        if (dir == null) {
            dir = createUniqueTestDirectory();
        }
        return dir;
    }

    private File createUniqueTestDirectory() {
        while (true) {
            // Use a random prefix to avoid reusing test directories
            String randomPrefix = Integer.toString(RANDOM.nextInt(MAX_RANDOM_PART_VALUE), ALL_DIGITS_AND_LETTERS_RADIX);
            if (WINDOWS_RESERVED_NAMES.matcher(randomPrefix).matches()) {
                continue;
            }
            File result = join(root, getPrefix(), randomPrefix);
            if (result.mkdirs()) {
                return result;
            }
        }
    }

    private String getPrefix() {
        if (prefix == null) {
            // This can happen if this is used in a constructor or a @Before method. It also happens when using
            // @RunWith(SomeRunner) when the runner does not support rules.
            prefix = "unknown-test-class";
        }
        return prefix;
    }

    public File file(Object... path) {
        return join(getTestDirectory(), path);
    }

    private class TestDirectoryCleaningStatement extends Statement {
        private final Statement base;
        private final Description description;

        TestDirectoryCleaningStatement(Statement base, Description description) {
            this.base = base;
            this.description = description;
        }

        @Override
        public void evaluate() throws Throwable {
            // implicitly don't clean up if this throws
            base.evaluate();

            try {
                cleanup();
            } catch (Exception e) {
                if (suppressCleanupErrors()) {
                    System.err.println(cleanupErrorMessage());
                    e.printStackTrace(System.err);
                } else {
                    throw new GradleException(cleanupErrorMessage(), e);
                }
            }
        }

        private boolean suppressCleanupErrors() {
            return suppressCleanupErrors;
        }

        private Class<?> testClass() {
            return description.getTestClass();
        }

        private String cleanupErrorMessage() {
            return "Couldn't delete test dir for `" + displayName() + "` (test is holding files open). "
                    + "In order to find out which files are held open you may find http://file-leak-detector.kohsuke.org/ useful.";
        }

        private String displayName() {
            return description.getDisplayName();
        }
    }
}
