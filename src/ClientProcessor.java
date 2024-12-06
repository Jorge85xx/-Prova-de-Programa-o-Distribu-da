import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ClientProcessor implements Runnable {

    private final Socket socket; 
    private static final String server_name = "IFSPServer"; 

    public ClientProcessor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()))
        ) {
            // Envia a mensagem inicial de boas-vindas
            out.println("220 " + this.server_name + " Simple Mail Transfer Service Ready");

            String line; // Variável para armazenar as mensagens recebidas do cliente
            boolean isDataMode = false; // Indica se o servidor está no modo de recebimento do corpo da mensagem
            StringBuilder corpoEmail = new StringBuilder(); // Acumula o corpo do e-mail recebido

            while ((line = in.readLine()) != null) {
                System.out.println("Recebido: " + line); 

                // Se estiver no modo DATA, processa o corpo do e-mail
                if (isDataMode) {
                    if (line.equals(".")) { // Linha contendo apenas um ponto finaliza o corpo do e-mail
                        out.println("250 Message accepted for delivery");
                        corpoEmail.setLength(0); 
                        isDataMode = false; 
                    } else {
                        corpoEmail.append(line).append("\n"); 
                    }
                    continue; 
                }

                // Comando MAIL FROM
                if (line.startsWith("MAIL FROM:")) {
                    String email = line.substring(10).trim(); // Extrai o endereço de e-mail
                    if (emailEhValido(email)) {
                        out.println("250 Sender " + email + " OK"); // Resposta para e-mail válido
                    } else {
                        out.println("500 invalid format!"); // Resposta para e-mail inválido
                    }

                // Comando RCPT TO
                } else if (line.startsWith("RCPT TO:")) {
                    if (line.length() <= 8) { 
                        out.println("500 incomplete command.");
                        continue;
                    }
                    String email = line.substring(8).trim(); // Extrai o endereço do destinatário
                    if (emailEhValido(email)) {
                        out.println("250 Recipient " + email + " OK"); // Resposta para destinatário válido
                    } else {
                        out.println("500 invalid format!"); // Resposta para destinatário inválido
                    }

                // Comando DATA
                } else if (line.equals("DATA")) {
                    out.println("354 End data with <CR><LF>.<CR><LF>");
                    out.println(" "); 
                    isDataMode = true; 

                // Comando QUIT
                } else if (line.equals("QUIT")) {
                    out.println("221 " + server_name + " Service closing transmission channel");
                    break; 

                } else {
                    out.println("500 Syntax error, command unrecognized.");
                }
            }
        } catch (IOException e) {
            System.err.println("Comunicação com o cliente falhou: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Falha ao fechar o socket: " + e.getMessage());
            }
        }
    }

    // Valida se o e-mail está no formato básico "usuario@dominio.com"
    private boolean emailEhValido(String email) {
        if (email == null || !email.contains("@")) {
            return false; 
        }

        String[] partes = email.split("@"); 
        if (partes.length != 2) { 
            return false;
        }

        String usuario = partes[0];
        String dominio = partes[1];

        // Verifica se o usuário e o domínio não estão vazios e o domínio contém "."
        return !usuario.isEmpty() && !dominio.isEmpty() && dominio.contains(".");
    }
}
