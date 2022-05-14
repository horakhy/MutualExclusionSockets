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
        // ProcessImpUnicast process_2 = new ProcessImpUnicast();
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