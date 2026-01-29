package com.lx862.pwgui.gui.components.fstree;

import com.lx862.pwgui.PWGUI;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileSystemWatcher {
    private final Path path;
    private final WatchEvent.Kind<?>[] watchKinds;
    private final boolean recursive;
    private final Map<Path, WatchKey> watchKeys;

    public FileSystemWatcher(Path path, boolean recursive, WatchEvent.Kind<?> ...watchKinds) {
        this.path = path;
        this.recursive = recursive;
        this.watchKinds = watchKinds;
        this.watchKeys = new HashMap<>();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void startWatching(FileChangeCallback callback) {
        FileSystem fileSystem = FileSystems.getDefault();
        try {
            WatchService watchService = fileSystem.newWatchService();

            if(recursive) {
                Files.walkFileTree(this.path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if(dir.toFile().getName().equals(".git")) return FileVisitResult.SKIP_SUBTREE; // git directory has way too many files, we are not interested in it anyway

                    watchKeys.put(dir, dir.register(watchService, watchKinds));
                    return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                watchKeys.put(path, path.register(watchService, this.watchKinds));
            }

            try {
                monitorFileLoop(watchService, callback);
            } catch (InterruptedException ignored) {
                for(WatchKey wk : watchKeys.values()) {
                    wk.cancel();
                }
                watchService.close();
            }
        } catch (IOException e) {
            PWGUI.LOGGER.error("Failed to monitor file system!", e);
        }
    }

    private void monitorFileLoop(WatchService watchService, FileChangeCallback callback) throws InterruptedException, IOException {
        while(true) {
            WatchKey wk = watchService.take();
            if(!wk.isValid()) continue;

            Thread.sleep(40); // A bit of a hack since some files may not have finished writing by other programs.

            for (WatchEvent<?> e : wk.pollEvents())
            {
                WatchEvent.Kind<?> kind = e.kind();
                if(kind == OVERFLOW) continue;

                Path parentDirectory = (Path)wk.watchable();
                Path filePath = parentDirectory.resolve(((WatchEvent<Path>)e).context());

                if(recursive && kind == ENTRY_CREATE) {
                    PWGUI.LOGGER.debug("[FileWatcher] [+] {}", filePath);
                    if(Files.isDirectory(filePath)) {
                        PWGUI.LOGGER.debug("[FileWatcher] Watching for path {}", filePath);
                        watchKeys.put(filePath, filePath.register(watchService, watchKinds)); // Watch for our new folder
                    }
                }

                if(kind == ENTRY_DELETE) {
                    WatchKey existingWatchKey = watchKeys.get(filePath);
                    PWGUI.LOGGER.debug("[FileWatcher] [-] {}", filePath);

                    if(existingWatchKey != null) {
                        PWGUI.LOGGER.debug("[FileWatcher] Unregistering path {}", filePath);
                        existingWatchKey.cancel(); // Unregister our old entry
                        watchKeys.remove(path);
                    }
                }
                callback.onChange((Path)wk.watchable(), e);
            }
            wk.reset();
        }
    }

    @FunctionalInterface
    public interface FileChangeCallback {
        void onChange(Path parentDirectory, WatchEvent<?> watchEvent);
    }
}
