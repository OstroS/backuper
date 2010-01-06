/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;


import java.io.*;
import java.net.*;
import java.util.*;
import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
/**
 * Klasa wysyłająca pojednyczny plik na wskazane gniazdko
 * Podajemy tylko gniazdko w konstrukotrze oraz nazwę pliku do wysłania w odpowiedniej metodzie
 * Teoretycznie klasa jest wielorazowego użytku, wymaga jednak testów
 * @author Ostros
 */
public class SendFile {

    private Socket socket;
    private OutputStream os;
    private InputStream is;
    private static final int WIELKOSC_PROBKI = backuper.Conf.WIELKOSC_PROBKI;

    /**
     * Konstruktor klasy do wysyłania plików
     * @param s Gniazdko do komunikacji z klientem przyjmującym plik
     */
    @Deprecated
    public SendFile(Socket s) {
        this.socket = s;

        try {
            os = socket.getOutputStream();
            is = socket.getInputStream();
        }
        catch(IOException ex) {

        }
    }
    public SendFile(InputStream in, OutputStream out) {

            os = out;
            is = in;


    }
    /**
     * Metoda wywoływana w celu przesłania pliku na zdefiniowane wcześniej ciastko
     * Po stronie odbiorczej należy
     * @param plik po stronie serwera
     * @param path ścieżka po stronie klienta
     * @return
     * @throws IOException
     */
    public int sendFile(File plik, String path) throws IOException {

        File myFile = plik;
        Date date = new Date();
        System.out.println(myFile.setLastModified(date.getTime()+2000));
       

        /**
         * Stworzenie strumienia wyjściowego, zeby klientowi wyslac niezbędne dane o pliku
         */
        // Długość ścieżki
        os.write(path.getBytes().length);

        // Ścieżka
        os.write(path.getBytes());

        // Długość pliku
        String dlugosc = Long.toString(myFile.length());
        byte aaa[] = dlugosc.getBytes();
        os.write(aaa.length);
        os.write(aaa);

        // Ilosc probek po WIELKOSC_PROBKI
        // Nie wysylam dopelnienia, klient sam sobie je wyliczy
        // Zakladam ze w ciele Z liczyc umie komputer :)
        int ile = (int)myFile.length() / WIELKOSC_PROBKI;
        String ilee = Integer.toString(ile);
        byte[] ile_b = ilee.getBytes();
        os.write(ile_b.length);
        os.write(ile_b);

        os.flush();


        // wysylanie pliku
        byte[] data = new byte[WIELKOSC_PROBKI];
        FileInputStream fis = new FileInputStream(myFile);
        for(int i = 0; i < ile; i++) {
            fis.read(data, 0 ,WIELKOSC_PROBKI);
            os.write(data, 0 , WIELKOSC_PROBKI);
         //   fos.write(data, 0 , WIELKOSC_PROBKI);
            os.flush();

          //  System.out.write(data);
        }
        int dopelnienie = (int)myFile.length() - ile *WIELKOSC_PROBKI;
        byte[] tmp = new byte[dopelnienie];
       // System.out.println(tmp.length);
        fis.read(tmp, 0, tmp.length);
        os.write(tmp, 0 ,tmp.length);
       // fos.write(tmp, 0, tmp.length);
        os.flush();
        byte[] tren = new byte[4];
        System.out.println(is.read(tren, 0, 4));
     /*    try {
            FileChannel fileChannel = fis.getChannel();
            FileLock lock = fileChannel.lock();
            lock.release();
        }
        catch(Exception ex) {ex.printStackTrace();}*/
       
        fis.close();
        String tr3n = new String(tren);
        if(tr3n.equals("TrEn")) System.out.println("Plik wysłany poprawnie");
       return 1;
    }

    public int sendFile(String path) throws IOException {
        File plik = new File(path);
        this.sendFile(plik, path);
        return 1;
    }


}
