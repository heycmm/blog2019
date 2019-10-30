package czx.me.watch;

import java.io.IOException;
import java.nio.file.*;

/**
 * 项目名称：blog2019
 * 类 名 称：Watcher
 * 类 描 述：用于监视指定目录下的写入操作
 * 创建时间：2019/10/30 5:15 下午
 * 创 建 人：czx.me
 */
public class Watcher {
    /**
     * @param args
     * @name: main
     * @description: WatchService发送以下事件：
     * <p>
     * ENTRY_CREATE：已创建目录条目。这可以是新文件或新目录。
     * <p>
     * ENTRY_DELETE：文件或目录已被删除。当条目从观察目录中重命名时，也会生成此事件。
     * <p>
     * ENTRY_MODIFY：条目已更改。例如，是否将新数据写入文件或文件属性已更改。
     * <p>
     * OVERFLOW：表示事件可能已经丢失。
     * @return: void
     * @date: 2019/10/30 5:18 下午
     * @auther: czx.me
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

            Path path = Paths.get("/Users/apple/Downloads/ssrMac/");

            path.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.OVERFLOW);
            while (true) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> watchEvent : key.pollEvents()) {

                    WatchEvent.Kind<?> kind = watchEvent.kind();
                    Path context = (Path) watchEvent.context();

                    System.out.printf("%s:%s\n", context, kind);
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }
    }

}
