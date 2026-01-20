package main;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

public class Server {
    private static final int PORT = 22222;
    private static final Logger logger = Logger.getLogger("srv");

    // キューは静的に保持（共有リソース）
    private static final BlockingQueue<JSONObject> commandQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        // 【修正点1】実行スレッドはサーバー起動と同時に1つだけ走らせておく
        startExecuterThread();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.log(Level.INFO, "the server is working at port " + PORT);

            while (true) {
                // 接続待ち
                Socket clientSocket = serverSocket.accept();
                logger.log(Level.INFO, "a connection is started with " + clientSocket.getInetAddress());

                // クライアント対応開始（ここで切断されるまでブロックします）
                // ※一度に1台のPCしか接続させない仕様ならこれでOKです
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "failed to start the server", e);
        }
    }

    private static void startExecuterThread() {
        CommandExecuterThread task = new CommandExecuterThread(commandQueue, Logger.getLogger("srv.cmdexec"));
        Thread thread = new Thread(task);
        thread.start();
    }

    // クライアントからのデータ受信処理
    private static void handleClient(Socket socket) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            while (true) {
                // ヘッダー(4バイト)読み込み
                int length = in.readInt();
                if (length <= 0)
                    break;

                // ボディ読み込み
                byte[] buffer = new byte[length];
                in.readFully(buffer);

                // 文字列 -> JSON変換 -> キューへ投入
                String jsonStr = new String(buffer, StandardCharsets.UTF_8);
                JSONObject cmd = new JSONObject(jsonStr);
                commandQueue.put(cmd);
            }
        } catch (EOFException e) {
            logger.log(Level.INFO, "the client disconnected cleanly");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "communication error", e);
        } finally {
            try {
                socket.close(); // 念のため閉じる（try-with-resourcesで閉じられるが明示的でも良い）
            } catch (IOException e) {
                // ignore
            }
        }
    }
}