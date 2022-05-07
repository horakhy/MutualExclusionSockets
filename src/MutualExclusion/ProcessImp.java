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

  private long id = (UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE) % 1000000;;
  protected MulticastSocket socket = null;
  protected byte[] buf = new byte[256];
  private ArrayList<ProcessImp> processosNaFila;
  private ArrayList<Long> processosQueJaAvisaramEntrada;

  private int numeroProcessosConectados = 0;
  private StatusType status;

  public ProcessImp() {
    this.id = (UUID.randomUUID().getLeastSignificantBits() & Long.MAX_VALUE) % 10000;
    this.processosNaFila = new ArrayList<ProcessImp>();
    this.processosQueJaAvisaramEntrada = new ArrayList<Long>();
    System.out.println("Processo " + this.id + " criado");
    this.status = StatusType.RELEASE;
  }

  private void notificarOutrosProcessos() throws IOException {

    // make datagram packet
    byte[] message = ("Multicasting...").getBytes();
    DatagramPacket packet = new DatagramPacket(message, message.length,
        InetAddress.getByName(IP), PORT);
    // send packet
    this.socket.send(packet);

  }

  private void notificarEntrada(long id) throws IOException {

    // make datagram packet
    byte[] message = ("" + this.id).getBytes();
    DatagramPacket packet = new DatagramPacket(message, message.length,
        InetAddress.getByName(IP), PORT);
    // send packet
    this.socket.send(packet);

  }

  public void registrarInteresse(ProcessImp process) throws IOException {
    System.out.println("Registrando interesse...");
    this.status = StatusType.WANTED;
    
    notificarOutrosProcessos();
  }

  private void atualizarParesConectados(String received) throws IOException{
    if(received.equals("" + this.id) || processosQueJaAvisaramEntrada.contains(Long.parseLong(received))) return;
    
    processosQueJaAvisaramEntrada.add(Long.parseLong(received));
    notificarEntrada(this.id);
    numeroProcessosConectados++;
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
        System.out.println("Processo " + this.id + " recebeu: " + received);
        System.out.println("Numero de processos conectados: " + numeroProcessosConectados);
        System.out.println();
        atualizarParesConectados(received);
        // if(received.equals(MENSAGEM_ENTRADA + " " + this.id)) return;
        //   notificarEntrada(this.id);
        //   numeroProcessosConectados++;
        // if (received.contains(MENSAGEM_ENTRADA)) {
        //   if(received.replace(MENSAGEM_ENTRADA, "").trim().equals(String.valueOf(this.id))){
        //     System.out.println(received.replace(MENSAGEM_ENTRADA, "").trim());
        //     System.out.println("" + this.id);
        //     System.out.println("ENTREI AQUI");
        //     return;
        //   }
        //   notificarEntrada(this.id);
        //   numeroProcessosConectados++;
        // }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
