import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class SMTPServer {

  private final int port;
  

  public SMTPServer() {
      this.port = 2525;
  }

  public SMTPServer(int port) {
      this.port = port;
  }

  public void start() throws IOException {

      try (ServerSocket server = new ServerSocket(this.port, 2048, InetAddress.getByName("127.0.0.1"))) {
          System.out.println("Servidor iniciado na porta " + this.port);

          while (true) {
              new Thread(new ClientProcessor(server.accept())).start();
          }
      } 
  }

}

