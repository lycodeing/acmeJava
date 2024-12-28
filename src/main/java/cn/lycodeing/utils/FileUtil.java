package cn.lycodeing.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author lycodeing
 */
public class FileUtil {

    /**
     * 读取文件内容到字符串
     * @param filePath 文件路径
     * @return 文件内容的字符串表示
     * @throws IOException 如果发生I/O错误
     */
    public static String readFileToString(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return new String(Files.readAllBytes(path));
    }

    /**
     * 将字符串写入文件
     * @param filePath 文件路径
     * @param content 要写入的内容
     * @throws IOException 如果发生I/O错误
     */
    public static void writeStringToFile(String filePath, String content) throws IOException {
        Files.write(Paths.get(filePath), content.getBytes());
    }

    /**
     * 读取文件内容到字节数组
     * @param filePath 文件路径
     * @return 文件内容的字节数组
     * @throws IOException 如果发生I/O错误
     */
    public static byte[] readFileToByteArray(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    /**
     * 将字节数组写入文件
     * @param filePath 文件路径
     * @param bytes 要写入的字节数组
     * @throws IOException 如果发生I/O错误
     */
    public static void writeByteArrayToFile(String filePath, byte[] bytes) throws IOException {
        Files.write(Paths.get(filePath), bytes);
    }
}
