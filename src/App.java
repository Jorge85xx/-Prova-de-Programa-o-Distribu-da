import java.io.IOException;

public class App {
    public static void main(String ... args) {

        try {

            SMTPServer server = new SMTPServer(2525);

            server.start();

        } catch (IOException e) {

            System.out.println("Erro ao iniciar o servidor, contate o administrador do sistema: " + e.getMessage());

        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage() + ". Contate o administrador do sistema.");
        }
    }
}