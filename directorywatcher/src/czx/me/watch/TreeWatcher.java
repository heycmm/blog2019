package czx.me.watch;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * 项目名称：blog2019
 * 类 名 称：TreeWatcher
 * 类 描 述：多级目录检测
 * 创建时间：2019/10/30 5:21 下午
 * 创 建 人：czx.me
 */
public class TreeWatcher {

    private static Map<WatchKey, Path> watchKeyToPathMap = new HashMap<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            watch(watchService, Paths.get("/Users/apple/Downloads/ssrMac/"));
        }
    }
    /**
     * @name: watch
     * @description: 监听目录和子目录下的全部文件操作
     * @param watchService
     * @param start
     * @return: void
     * @date: 2019/10/30 5:46 下午
     * @auther: czx.me
     *
     */
    /**
     * 该程序首先使用newWatchService()获取WatchService的实例。
     * 然后，它获取对我们想要观察的目录的引用，在观察器中注册路径，并指定它感兴趣的事件。
     * 您可以通过为每个目录调用register()来同时查看多个目录。
     * <p>
     * 在无限循环中，应用程序使用take()方法请求一个WatchKey。
     * 此方法阻止调用线程，直到有一个WatchKey可用。
     * 或者，应用程序可以调用poll()，该轮询不会阻塞调用线程，并且要么立即返回WatchKey，要么在没有可用事件的情况下返回null。
     * <p>
     * 观察键(WatchKey)只是一个指示已经接收到一个或多个事件的信号。
     * 要访问已经到达的事件，程序必须调用键上的轮询事件()，该键返回所有接收到的监视事件的列表。应用程序现在可以用类型()检查事件的类型(ENTRY_CREATE、ENTRY_DELETE、ENTRY_MODIFY、OVERFLOW)。context()返回触发事件的路径对象。
     * <p>
     * 处理完事件后，使用reset()重置观察键非常重要。
     * 如果忘记了这一点，那么密钥就不会再收到任何事件！如果WatchKey不再有效，reset()返回true或false。
     * <p>
     * 现在，在受监控的目录中执行文件操作时，可以观察到程序的输出。
     * 当我们创建一个文本文件e:/watcher/text . txt时，会发出ENTRY_CREATE事件。
     * 如果我们在编辑器中打开文本文件并进行更改，将会发送ENTRY_MODIFY事件。
     * 如果我们重命名文件，将发送ENTRY_DELETE和ENTRY_CREATE事件。
     */
    private static void watch(WatchService watchService, Path start) throws IOException, InterruptedException {
        registerTree(watchService, start);

        while (true) {
            WatchKey key = watchService.take();//此方法将阻塞调用线程，直到WatchKey可用为止
            //事件处理
            for (WatchEvent<?> watchEvent : key.pollEvents()) {
                WatchEvent.Kind<?> kind = watchEvent.kind();
                Path eventPath = (Path) watchEvent.context();

                Path directory = watchKeyToPathMap.get(key);
                // Path directory = (Path) key.watchable(); //存在重命名问题

                Path child = directory.resolve(eventPath);//获取操作文件真实路径

                if (kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(child)) {
                    registerTree(watchService, child);
                }

                System.out.printf("%s:%s\n", child, kind);
            }

            boolean valid = key.reset();//重启该线程，因为处理文件可能是一个耗时的过程，因此调用 pool() 时需要阻塞监控器线程
            if (!valid) {
                watchKeyToPathMap.remove(key);
                if (watchKeyToPathMap.isEmpty()) {
                    break;
                }
            }
        }

    }

    /**
     * @param watchService
     * @param start
     * @name: registerTree
     * @description: 遍历目录树封装到watchKeyToPathMap(key, value)
     * @return: void
     * @date: 2019/10/30 5:24 下午
     * @auther: czx.me
     */
    private static void registerTree(WatchService watchService, Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
                watchKeyToPathMap.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
