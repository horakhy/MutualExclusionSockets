package MutualExclusion;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.UUID;

import MutualExclusion.constants.StatusType;

public class ProcessImp extends Thread {
  private final String IP = "230.0.0.0";
  private final int PORT = 4446;
  private final String MENSAGEM_SOLICITACAO_SC = "SOLICITA SC";
  private final String MENSAGEM_AUTORIZACAO_ACESSO_SC = "AUTORIZACAO ACESSO SC";
  private final String MENSAGEM_LIBERACAO_SC = "SC LIBERADA";

  private long id = (UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE) % 1000000;;
  protected MulticastSocket socketMulticast = null;
  protected DatagramSocket socketUnicast = null;

  protected byte[] buf = new byte[1000];
  private ArrayList<ProcessImp> processosNaFilaDeEspera;
  // private boolean solicitouAcessoSC = false;

  private int numeroProcessosQueAutorizaramAcessoSC = 0;
  private StatusType status;

  public ProcessImp() {
    this.id = (UUID.randomUUID().getLeastSignificantBits() & Long.MAX_VALUE) % 10000;
    this.processosNaFilaDeEspera = new ArrayList<ProcessImp>();
    System.out.println("Processo " + this.id + " criado");
    this.status = StatusType.RELEASE;
  }

  private void notificarSolicitacaoOutrosProcessos() throws IOException {
    // make datagram packet
    byte[] message = (MENSAGEM_SOLICITACAO_SC + " " + this.id).getBytes();
    DatagramPacket packet = new DatagramPacket(message, message.length,
        InetAddress.getByName(IP), PORT);
    // send packet
    this.socketMulticast.send(packet);
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
    this.socketMulticast.send(packet);
  }

  public void notificarLiberacaoSC() throws IOException {
    byte[] message = (MENSAGEM_LIBERACAO_SC + ' ' + this.id).getBytes();
    DatagramPacket packet = new DatagramPacket(message, message.length,
        InetAddress.getByName(IP), PORT);
    // send packet
    this.socketMulticast.send(packet);
  }

  public void liberarSC() throws IOException {
    System.out.println("Liberando SC...");
    this.status = StatusType.RELEASE;

    notificarLiberacaoSC();
  }

  private void handleUnicastSocket() {
    try {
      this.socketUnicast = new DatagramSocket(4446);
      byte[] buf = new byte[1000];

      while (true) {
        DatagramPacket received = new DatagramPacket(buf, buf.length);
        this.socketUnicast.receive(received);
        String messageReceived = new String(received.getData());

        if (!messageReceived.contains("" + this.id)) {
          System.out.println("Processo " + this.id + " recebeu msg unicast: " + messageReceived);
          System.out.println();
        }

        if(messageReceived.contains(MENSAGEM_LIBERACAO_SC)) {
          this.numeroProcessosQueAutorizaramAcessoSC++;
        }
        
        if (messageReceived.contains(MENSAGEM_AUTORIZACAO_ACESSO_SC) && this.status == StatusType.WANTED) {
          this.numeroProcessosQueAutorizaramAcessoSC++;
          if (numeroProcessosQueAutorizaramAcessoSC >= 2) {
            this.status = StatusType.HELD;
            System.out.println("ESTOU COM A SC");
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void handleMulticastSocket() {
    try {
      this.socketMulticast = new MulticastSocket(4446);

      InetAddress group = InetAddress.getByName("230.0.0.0");
      socketMulticast.joinGroup(group);
      System.out.println("Processo " + this.id + " juntou-se ao grupo");
    } catch (IOException e) {
      e.printStackTrace();
    }
    while (true) {
      // Recebe pacote de outros processos
      DatagramPacket packet = new DatagramPacket(buf, buf.length);
      try {
        socketMulticast.receive(packet);
        DatagramPacket received = new DatagramPacket(
            packet.getData(), 0, packet.getLength());

        String messageReceived = new String(received.getData());

        if (messageReceived.contains("" + this.id)) {
          System.out.println("Processo " + this.id + " recebeu: " + messageReceived);
          System.out.println();
        }

        if (messageReceived.contains(MENSAGEM_SOLICITACAO_SC)) {
          if (this.status == StatusType.HELD) {
            return;
          }
          notificarAutorizacaoAcessoSC();
        }

        if ((messageReceived.contains(MENSAGEM_AUTORIZACAO_ACESSO_SC) || messageReceived.contains(MENSAGEM_LIBERACAO_SC)) && this.status == StatusType.WANTED) {
          this.numeroProcessosQueAutorizaramAcessoSC++;
          if (numeroProcessosQueAutorizaramAcessoSC >= 2) {
            this.status = StatusType.HELD;
            System.out.println("ESTOU COM A SC");
          }
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  public void run() {
    handleMulticastSocket();
    // handleUnicastSocket();
  }
}
