package MutualExclusion;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.UUID;

public class Process extends Thread {
    private long id = (UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE) % 1000000;
    protected MulticastSocket socket = null;
    protected byte[] buf = new byte[256];

   
    public static void main(String[] args) throws Exception {
        ProcessImp process = new ProcessImp();
        Thread t1 = new Thread(process);      
        t1.start();    

        System.out.println("Selecione uma opção abaixo:");
        System.out.println();
        System.out.println("+---+------------------------+");
        System.out.println("+ 1 +    Acessar recurso     +");
        System.out.println("+---+------------------------+");
        System.out.println("+ 2 +    Liberar recurso     +");
        System.out.println("+---+------------------------+");
        System.out.println("+ 3 +         Sair           +");
        System.out.println("+---+------------------------+");
        System.out.println();

        Scanner scanner = new Scanner(System.in);
        // try
        //     {
                // InetAddress group = InetAddress.getByName(args[0]);
                // int port = Integer.parseInt(args[1]);
                // Scanner sc = new Scanner(System.in);
                // System.out.print("Enter your name: ");
                // name = sc.nextLine();
                // MulticastSocket socket = new MulticastSocket(port);
              
                // // Since we are deploying
                // socket.setTimeToLive(0);
                // //this on localhost only (For a subnet set it as 1)
                  
                // socket.joinGroup(group);
                // Thread t = new Thread(new
                // ProcessImp(socket,group,port));
              
                // // Spawn a thread for reading messages
                // t.start(); 
                  
                // sent to the current group
            //     System.out.println("Start typing messages...\n");
            //     while(true)
            //     {
            //         String message;
            //         message = sc.nextLine();
            //         if(message.equalsIgnoreCase(GroupChat.TERMINATE))
            //         {
            //             socket.leaveGroup(group);
            //             socket.close();
            //             break;
            //         }
            //         message = name + ": " + message;
            //         byte[] buffer = message.getBytes();
            //         DatagramPacket datagram = new
            //         DatagramPacket(buffer,buffer.length,group,port);
            //         socket.send(datagram);
            //     }
            // }
            // catch(SocketException se)
            // {
            //     System.out.println("Error creating socket");
            //     se.printStackTrace();
            // }
            // catch(IOException ie)
            // {
            //     System.out.println("Error reading/writing from/to socket");
            //     ie.printStackTrace();
            // }
        while (true) {

            System.out.println();
            int opt = scanner.nextInt();

            switch (opt) {
                case 1:
                    System.out.println("Acessando recurso...");
                    process.registrarInteresse(process);
                    System.out.println();
                    break;
                case 2:
                    System.out.println("Liberando o recurso...");
                    System.out.println();
                    break;
                case 3:
                    System.out.println("Adios.........");
                    System.out.println();
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Opção inválida");
                    System.out.println();
                    break;
            }
        }
    }
}