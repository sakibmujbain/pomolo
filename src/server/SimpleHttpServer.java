package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class SimpleHttpServer {
    public static void main(String[] args){
        try {
            // Create an HttpServer instance
            HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
            server.createContext("/", new MyHandler());



            // Serve MP3
            server.createContext("/music", exchange -> {
                File file = new File("/home/shromi/Downloads/Save_your_tears.mp3");
                exchange.getResponseHeaders().add("Content-Type", "audio/mpeg");
                exchange.sendResponseHeaders(200, file.length());
                try (OutputStream os = exchange.getResponseBody();
                     FileInputStream fis = new FileInputStream(file)) {
                    fis.transferTo(os);
                }
            });
            WebSocketLauncher.startServer();

            server.setExecutor(null);
            server.start();
            System.out.println("HTTP server at http://<host-ip>:8081");

            Scanner sc = new Scanner(System.in);
            while(true) {
                System.out.println("Enter code:");
                System.out.flush();
                int x = Integer.parseInt(sc.nextLine().trim());
                if(x==1){
                    SyncWebSocketServer.broadcast("PLAY");
                    System.out.println("PLAY");
                }else if(x==2){
                    SyncWebSocketServer.broadcast("PAUSE");
                }else{
                    SyncWebSocketServer.broadcast("PLAY");
                }
            }

        } catch (IOException e){
            System.out.println("Error starting the server: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            String response = """
                <!DOCTYPE html>
                <html>
                <body>
                  <h2>Shared Music Player</h2>
                  <audio id='player' controls></audio>
                  <script>
                    const audio = document.getElementById('player');
                    audio.src = '/music';

                    const ws = new WebSocket('ws://' + location.hostname + ':8080/sync');
                    console.log('ws://' + location.hostname + ':8080/sync');
                    ws.onmessage = e => {
                        if (e.data === 'PLAY') audio.play();
                        else if (e.data === 'PAUSE') audio.pause();
                        else if (e.data.startsWith('SEEK:')) audio.currentTime = parseFloat(e.data.split(':')[1]);
                    };
                  </script>
                </body>
                </html>
            """;
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
