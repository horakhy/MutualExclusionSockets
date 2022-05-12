package MutualExclusion;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.UUID;

import MutualExclusion.constants.StatusType;

public class ProcessImp extends Thread {
  private final String IP = "230.0.0.0";
  private final int PORT = 4446;
  private final String MENSAGEM_ENTRADA = "ENTRADA";
  private final String MENSAGEM_SOLICITACAO_SC = "SOLICITA SC";
  private final String MENSAGEM_AUTORIZACAO_ACESSO_SC = "AUTORIZACAO ACESSO SC";
  private final String MENSAGEM_LIBERACAO_SC = "SC LIBERADA";

  private long id = (UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE) % 1000000;;
  protected MulticastSocket socket = null;
  protected byte[] buf = new byte[256];
  private ArrayList<ProcessImp> processosNaFilaDeEspera;
  private ArrayList<String> processosQueJaAvisaramEntrada;
  private boolean solicitouAcessoSC = false;

  private int numeroProcessosConectados = 0;
  private int numeroProcessosQueAutorizaramAcessoSC = 0;
  private StatusType status;

  public ProcessImp() {
    this.id = (UUID.randomUUID().getLeastSignificantBits() & Long.MAX_VALUE) % 10000;
    this.processosNaFilaDeEspera = new ArrayList<ProcessImp>();
    this.processosQueJaAvisaramEntrada = new ArrayList<String>();
    System.out.println("Processo " + this.id + " criado");
    this.status = StatusType.RELEASE;
  }

  private void notificarSolicitacaoOutrosProcessos() throws IOException {
    this.solicitouAcessoSC = true;
    // make datagram packet
    byte[] message = (MENSAGEM_SOLICITACAO_SC + " " + this.id).getBytes();
    DatagramPacket packet = new DatagramPacket(message, message.length,
        InetAddress.getByName(IP), PORT);
    // send packet
    this.socket.send(packet);
  }

  private void notificarEntrada(long id) throws IOException {

    // make datagram packet
    byte[] message = (MENSAGEM_ENTRADA + " " + this.id).getBytes();
    DatagramPacket packet = new DatagramPacket(message, message.length,
        InetAddress.getByName(IP), PORT);
    // send packet
    this.socket.send(packet);

  }

  public void registrarInteresse(ProcessImp process) throws IOException {
    System.out.println("Registrando interesse...");
    this.status = StatusType.WANTED;

    notificarSolicitacaoOutrosProcessos();
  }

  public void notificarAutorizacaoAcessoSC() throws IOException {
    byte[] message = (MENSAGEM_AUTORIZACAO_ACESSO_SC + ' ' + this.id).getBytes();
    DatagramPacket packet = new DatagramPacket(message, message.length,
        InetAddress.getByName(IP), PORT);
    // send packet
    this.socket.send(packet);
  }

  public void notificarLiberacaoSC() throws IOException {
    byte[] message = (MENSAGEM_LIBERACAO_SC + ' ' + this.id).getBytes();
    DatagramPacket packet = new DatagramPacket(message, message.length,
        InetAddress.getByName(IP), PORT);
    // send packet
    this.socket.send(packet);
  }

  public void liberarSC() throws IOException {
    System.out.println("Liberando SC...");
    this.status = StatusType.RELEASE;

    notificarLiberacaoSC();
  }

  private void atualizarParesConectados(String received) throws IOException {
    // Verifica se não é o processo enviando para si mesmo
    // Ou se já não recebeu a mensagem de entrada desse processo antes
    if (received.contains("" + this.id) || processosQueJaAvisaramEntrada.contains(received))
      return;

    processosQueJaAvisaramEntrada.add(received);
    processosQueJaAvisaramEntrada.forEach(processo -> {
      System.out.println("Processos na fila: " + processo);
    });
    notificarEntrada(this.id);
    numeroProcessosConectados++;
    System.out.print("Numero de processos conectados: " + numeroProcessosConectados);
    System.out.println();
  }

  public void run() {
    try {
      this.socket = new MulticastSocket(4446);

      InetAddress group = InetAddress.getByName("230.0.0.0");
      socket.joinGroup(group);
      System.out.println("Processo " + this.id + " juntou-se ao grupo");
      this.numeroProcessosConectados = 1;
      notificarEntrada(this.id);
    } catch (IOException e) {
      e.printStackTrace();
    }
    while (true) {
      DatagramPacket packet = new DatagramPacket(buf, buf.length);
      try {
        socket.receive(packet);
        String received = new String(
            packet.getData(), 0, packet.getLength());

        if (!received.contains("" + this.id)) {
          System.out.println("Processo " + this.id + " recebeu: " + received);
          System.out.println();
        }
          // Verifica se é uma mensagem de entrada
          if (received.contains(MENSAGEM_ENTRADA)) {
            atualizarParesConectados(received);
          }

          if (received.contains(MENSAGEM_SOLICITACAO_SC)) {
            if (this.status == StatusType.HELD) {
              return;
            }
            notificarAutorizacaoAcessoSC();
          }

          if (received.equals(MENSAGEM_AUTORIZACAO_ACESSO_SC) && this.status == StatusType.WANTED
              && this.solicitouAcessoSC) {
            System.out.println("ENTREI AWUIII");
            numeroProcessosQueAutorizaramAcessoSC++;
            if (numeroProcessosQueAutorizaramAcessoSC <= numeroProcessosConectados - 1) {
              this.status = StatusType.HELD;
              System.out.println("ESTOU COM A SC");
            }
          }
        

        // if(received.equals(MENSAGEM_ENTRADA + " " + this.id)) return;
        // notificarEntrada(this.id);
        // numeroProcessosConectados++;
        // if (received.contains(MENSAGEM_ENTRADA)) {
        // if(received.replace(MENSAGEM_ENTRADA,
        // "").trim().equals(String.valueOf(this.id))){
        // System.out.println(received.replace(MENSAGEM_ENTRADA, "").trim());
        // System.out.println("" + this.id);
        // System.out.println("ENTREI AQUI");
        // return;
        // }
        // notificarEntrada(this.id);
        // numeroProcessosConectados++;
        // }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
