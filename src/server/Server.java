/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.io.File;

/**
 * Klasa przyjmujÄ�ca polecenia pisane serwerowi z palca
 * @author Ostros
 */
public class Server {

    public static void main(String args[]) {
      
        Server server = new Server(8000);
    }
    private int port;
    public Server(int port) {
        this.port = port;
        System.out.println("Serwer uruchomiony na porcie " + port);
        Runnable r = new SrvWait(port);
        Thread t = new Thread(r);
        t.start();
        System.out.println("Wpisz STOP to stop");

        BufferedReader keyInput = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String line = keyInput.readLine();
                if (line.trim().equals("STOP")) {
                    System.exit(0);
                }
                else if(line.trim().equals("add")) {
                    System.out.println("Dodawanie użytkownika");
                }
            } catch (Exception ex) {
            }
        }



    }
}

/**
 * Klasa obsĹ�ugujÄ�ca gĹ�Ăłwny wÄ�tek serwera przyjmujÄ�cy zgĹ�oszenia
 * Nowi klienci sÄ� delegowani do innego obietku (tworzony jest dla nich nowy wÄ�tek)
 * @author Ostros
 */
class SrvWait implements Runnable {
    int port;
    public SrvWait(int port) {
        this.port = port;
    }

    public void run() {
        try {
            /**
             * Tworzenie gniazdka na porcie 8000;
             */
            ServerSocket s = new ServerSocket(port);
            int i = 0;

            /**
             * Przyjmowanie klientĂłw i tworzenie dla nich nowych wÄ�tkĂłw
             */
            while (true) {
                Socket incoming = s.accept();
                System.out.println("Klient#" + i);
                Runnable r = new SrvThread(incoming);
                Thread t = new Thread(r);
                t.start();
                i++;
            }


        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

/**
 * Klasa stanowiÄ�ca wÄ�tek serwera dla jednego uĹźytkownika
 * @author Ostros
 */
class SrvThread implements Runnable {

    Socket sock;
    InputStream is;
    OutputStream os;

    public SrvThread(Socket s) {
        sock = s;
    }

    public void run() {
        try {
            try {
                this.is = sock.getInputStream();
                this.os = sock.getOutputStream();

                /*
                 * Przywitanie z klientem
                 * send HELLO!
                 */
                byte[] hello = "HEL".getBytes();
                os.write(hello, 0, 3);
                /**
                 * Odczyt nazwy użytkownika
                 */
                int n_l = is.read();
                byte[] name = new byte[n_l];
                is.read(name, 0, n_l);
                String username = new String(name);


                int p_l = is.read();
                byte[] pass = new byte[p_l];
                is.read(pass, 0, p_l);
                String password = new String(pass);

                /**
                 * Sprawdzenie czy dane konto uzytkownika w ogole istnieje
                 * i czy jego haslo jest prawidlowe
                 */
                boolean czekaj;
                FileContainer lista;
                SendFile send;
                if(FileContainer.checkUser(username, password)) {
                    System.out.println("Uzytkownik " + username + " zalogowany");
                    lista = new FileContainer(username, password);
                    hello = "oki".getBytes();
                    os.write(hello, 0, 3);
                    /**
                     * Wysłanie listy zbackupowanych plikow
                     */
                    send = new SendFile(is, os);
                    send.sendFile(lista.getListLocation());
                    // String path = new String("\\Server_backup\\" + username + "\\" + password + "\\");
                    // System.out.println("path: " + System.getProperty("user.dir") + path);
                   czekaj = true;
                }
                else {
                   czekaj = false;
                   lista = null;
                   hello = "err".getBytes();
                   os.write(hello, 0, 3);
                }
                
                while (czekaj) {
                    //lista.saveList();
                    //hello = null;
                    is.read(hello, 0, 3);
                    System.out.write(hello);
                    String choose = new String(hello);

                    if (choose.equals("dba")) {
                        /**
                         * Opcje do wyboru co ma zrobić serwer:
                         * dba - wykonaj backup, przeslanie plikow od klienta do serwera
                         */
                        System.out.println("Do backup");
                        int ile_byte = is.read();
                        System.out.println(ile_byte);
                        byte[] ile_b = new byte[ile_byte];
                        is.read(ile_b, 0, ile_b.length);

                        String tmp = new String(ile_b);
                        int ile = new Integer(tmp);
                        System.out.println("Plików do odberania: " + ile);
                        for (int i = 0; i < ile; i++) {
                            ReceiveBackFile odbierz = new ReceiveBackFile(is, os, username, password);
                            lista.add(odbierz.receiveFile());
                           
                        }
                       
                        hello = "end".getBytes();
                        os.write(hello, 0, 3);
                       
                    } /**
                     * prz - przywrocenie wszystkich plikow
                     */
                    else if (choose.equals("prz")) {
                        System.out.println("Przywrócenie wszystkich plkików");
                        // lista.getContainer();
                        
                        int ile = lista.getSize();
                        byte[] ile_b = Integer.toString(ile).getBytes();
                        System.out.println(ile_b.length);
                        System.out.write(ile_b);
                        os.write(ile_b.length);
                        os.write(ile_b);
                        for (int i = 0; i < ile; i++) {
                            SendFile wyslij = new SendFile(is, os);

                            wyslij.sendFile(lista.getFileAtSrv(i), lista.getLocalPath(i));
                        }
                        lista.saveList();
                        is.read(hello, 0, 3);
                        String koniec = new String(hello);


                    }
                    /**
                     * Wyslij aktualna liste plikow
                     */
                    else if(choose.equals("sli")) {
                        lista.saveList();
                        send = new SendFile(is, os);
                        send.sendFile(lista.getListLocation());

                    }

                    else if(choose.equals("del")) {
                        // Dlugosc sciezki
                        int wielkosc;
                        do {
                            wielkosc = is.read();
                        } while (wielkosc == -1);


                        System.out.println(wielkosc);

                        // Sciezka
                        byte[] sciezka_t = new byte[wielkosc];
                        is.read(sciezka_t, 0, sciezka_t.length);
                        String sciezka = new String(sciezka_t);
                        System.out.println(sciezka);

                        lista.delFileFromList(sciezka);
                    }
                    /**
                     * Przywrocenie danego pliku z serwera na dysk lokalny
                     */
                    else if(choose.equals("get")) {
                        // Dlugosc sciezki
                        int wielkosc;
                        do {
                            wielkosc = is.read();
                        } while (wielkosc == -1);


                        System.out.println(wielkosc);

                        // Sciezka
                        byte[] sciezka_t = new byte[wielkosc];
                        is.read(sciezka_t, 0, sciezka_t.length);
                        String sciezka = new String(sciezka_t);
                        System.out.println(sciezka);

                        File local = new File(sciezka);
                        File remote = lista.makePath(local);
                        SendFile wyslij = new SendFile(is, os);

                            wyslij.sendFile(remote, sciezka);
                             is.read(hello, 0, 3);
                        String koniec = new String(hello);

                    }
                    else if(choose.equals("kon")) {
                        System.out.println("Klient rozłączony");
                        czekaj = false;

                    }




                }
                /*
                SendFile send = new SendFile(sock);
                send.sendFile("plik.jpg");
                send.sendFile("edituser.php");
                 */
            } catch (Exception ex) {
                //ex.printStackTrace();
            } finally {
                is.close();
                os.close();
                sock.close();
                System.out.println("Wątek zakończony");
                //System.exit(0);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
