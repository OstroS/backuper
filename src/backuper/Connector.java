/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package backuper;

import java.net.Socket;
import java.io.*;
import javax.swing.JProgressBar;

/**
 * Klasa obsługująca połączenie z serwerem
 * TODO: przesyłanie plików, autoryzacja
 * @author Ostros
 */
public class Connector implements Runnable {

    Socket socket;
    String remoteHost;
    int portNo;
    String name;
    char[] password;
    InputStream is;
    OutputStream os;

    /**
     * Konstrukotr zapisujący dane do połączenia.
     * Połączenie jeszcze nie jest tworzone!
     * @param remoteHost Adres zdalnego hosta
     * @param portNo Numer portu zdalnego hosta
     */
    public Connector(String name, char[] password, String remoteHost, int portNo) {
        this.remoteHost = remoteHost;
        remoteHost = "localhost";
        this.portNo = portNo;
        portNo = 8000;
        this.name = name;
        this.password = password;

    }

    /**
     * Metoda obsługująca połączenie z serwerem
     * TODO: autoryzacja!
     * @return Zwraca ciastko na którym nawiązane jest połączenie
     */
    public boolean getConnection() throws ConnectionException {

        try {
            print("Łączenie z serwerem(" + name + "@" + remoteHost + ":" + portNo + ")...");
            try {
                /*
                socket = new Socket(remoteHost, portNo);
                this.is = socket.getInputStream();
                this.os = socket.getOutputStream();
                byte[] hello = new byte[3];
                is.read(hello, 0, 3);
                 */
                socket = new Socket(remoteHost, portNo);
                this.is = socket.getInputStream();
                this.os = socket.getOutputStream();
                byte[] hello = new byte[3];

                is.read(hello, 0, 3);
                System.out.write(hello);

                /**
                 * Wysłanie nazwy użytkownika
                 */
                byte[] username = name.getBytes();
                os.write(username.length);
                os.write(username);

                /**
                 * Wysłanie hasła
                 */
                String t = new String(password);
                byte[] pass = t.getBytes();
                os.write(pass.length);
                os.write(pass);

                /**
                 * Sprawdzenie odpowiedzi z serwera
                 */
                is.read(hello, 0, 3);
                String check = new String(hello);

                if (check.equals("oki")) {
                    ReceiveFile rf = new ReceiveFile(is, os);
                    rf.receiveList();

                    return true;
                } else if (check.equals("err")) {
                    System.out.println("Auth error!");
                    throw new ConnectionException("Auth error");
                    // return 0;
                }

            } catch (Exception ex) {
            } finally {
                //socket.close();
            }
        } catch (Exception ex) {
        }

        return false;

    }

    class ConnectionException extends Exception {

        public ConnectionException(String msg) {
            super(msg);
        }
    }

    /**
     * Wątek połączenia
     */
    public void run() {
        // niepotrzebne w sumie, ale zostawiam na przyszłość
    }

    /**
     * Wyświetlenie komunikatu informacyjnego
     * Metoda zmodyfikowana aby pobierać tylko istotne pliki do backupu,
     * a nie całość danych jaką podał user
     * @param text Tresć komunikatu
     */
    public int doBackup(FileContainer lista) {

        byte[] hello = new byte[3];
        // info DoBAckup


        try {/*
            socket = new Socket(remoteHost, portNo);
            this.is = socket.getInputStream();
            this.os = socket.getOutputStream();

            is.read(hello, 0, 3);
            System.out.write(hello);
             */
            hello = "dba".getBytes();


            os.write(hello);
            /**
             * Trzeba wysłać ile plików chcemy przesylac i nadawac w petli z jakims potwierdzeniem...
             */
            int ile = lista.getBackupSize();
            byte[] ile_b = Integer.toString(ile).getBytes();
            System.out.println(ile_b.length);
            System.out.write(ile_b);
            os.write(ile_b.length);
            os.write(ile_b);
            for (int i = 0; i < ile; i++) {
                SendFile wyslij = new SendFile(is, os);
                File tmp = new File(lista.getBackup(i));
                wyslij.sendFile(tmp);

            }

            is.read(hello, 0, 3);
            String koniec = new String(hello);
            if (koniec.equals("end")) {
                return 1;
            } else {
                return 0;
            }


        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }

    }

    public int przywroc() {
        byte[] hello = new byte[3];
        // info PRZywroc


        try {/*
            socket = new Socket(remoteHost, portNo);
            this.is = socket.getInputStream();
            this.os = socket.getOutputStream();

            is.read(hello, 0, 3);
            System.out.write(hello);
             */
            hello = "prz".getBytes();
            os.write(hello);
            System.out.println("Przywracanie plików");
            int ile_byte = is.read();
            System.out.println(ile_byte);
            byte[] ile_b = new byte[ile_byte];
            is.read(ile_b, 0, ile_b.length);

            String tmp = new String(ile_b);
            int ile = new Integer(tmp);
            System.out.println("Plików do odberania: " + ile);
            for (int i = 0; i < ile; i++) {
                ReceiveFile odbierz = new ReceiveFile(is, os);
                odbierz.receiveFile();
                //lista.add(odbierz.receiveFile());
            }
            hello = "end".getBytes();
            os.write(hello, 0, 3);

            os.write(hello);


        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }

        return 1;
    }

    /**
     * Przywrocenie danego pliku z serwera
     * @param plik
     */
    public void getFile(File plik) {
        try {
            byte[] hello = new byte[3];
            hello = "get".getBytes();
            os.write(hello);

            // Długość ścieżki
            os.write(plik.getPath().getBytes().length);

            // Ścieżka
            os.write(plik.getPath().getBytes());

            ReceiveFile odbierz = new ReceiveFile(is, os);
            odbierz.receiveFile();
            //lista.add(odbierz.receiveFile());

            hello = "end".getBytes();
            os.write(hello, 0, 3);

            os.write(hello);
        } catch (IOException ex) {
        }

    }

    public void receiveList() {
        try {
            byte[] hello = new byte[3];
            hello = "sli".getBytes();
            os.write(hello);
            System.out.println("SendList");
            ReceiveFile rf = new ReceiveFile(is, os);
            rf.receiveList();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void print(String text) {
        System.out.println("Connector: " + text);
    }

    public void disconnect() {
        try {
            byte[] kon = new byte[3];
            kon = "kon".getBytes();
            os.write(kon, 0, 3);
            os.close();
            is.close();
            socket.close();
        } catch (Exception ex) {
        }
        this.print("Rozłączony");

    }

    public void delFileFromServer(File path) {
        try {
            byte[] kon = new byte[3];
            kon = "del".getBytes();
            os.write(kon, 0, 3);



            os.write(path.getPath().getBytes().length);

            // Ścieżka
            os.write(path.getPath().getBytes());
        } catch (IOException ex) {
        }
    }
}
