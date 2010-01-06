/*

 */
package backuper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Klasa do odbierania pliku
 * Po nawiązaniu połączenia trzeba stworzyć jej obiekt przekazując gniazdko
 *
 * @author Ostros
 */
public class ReceiveFile {

    Socket socket;
    InputStream is;
    OutputStream os;
    private static final int WIELKOSC_PROBKI = Conf.WIELKOSC_PROBKI;

    @Deprecated
    public ReceiveFile(Socket sock) throws IOException {
        this.socket = sock;
        this.is = socket.getInputStream();
        this.os = socket.getOutputStream();

    }

    public ReceiveFile(InputStream in, OutputStream out) {
        is = in;
        os = out;
    }

    public void reinit() throws IOException {
        this.is = socket.getInputStream();
        this.os = socket.getOutputStream();

    }

    public File receiveFile() throws IOException {
        long start = System.currentTimeMillis();
        long current = 0;
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

        // Dlugosc dlugosci
        int dlugosc_t = is.read();

        // Dlugosc pliku

        byte[] dlugosc_te = new byte[dlugosc_t];
        is.read(dlugosc_te, 0, dlugosc_te.length);
        String dlugosc = new String(dlugosc_te);
        System.out.println(dlugosc);
        int dlugosc_pliku = new Integer(dlugosc);

        // Dlugosc probki
        int dlugosc_ile_probek = is.read();
        byte[] ile_probek = new byte[dlugosc_ile_probek];
        is.read(ile_probek, 0, ile_probek.length);
        String probek = new String(ile_probek);
        System.out.println(probek);
        int ile = new Integer(probek);

        /**
         * Tu jest definiowana sciezka zapisu pliku
         */
        File myFile = new File(sciezka);
        myFile = this.makePath(myFile);
        FileOutputStream fos = new FileOutputStream(myFile);

        byte[] c = new byte[WIELKOSC_PROBKI];


        for (int i = 0; i < ile; i++) {
            int size = is.read(c, 0, WIELKOSC_PROBKI);
            fos.write(c, 0, size);
            fos.flush();
            current += size;
        }

        int dopelnienie = dlugosc_pliku - WIELKOSC_PROBKI * ile;

        byte[] tmp = new byte[dopelnienie];
        int size = is.read(tmp, 0, dopelnienie);
        // System.out.write(tmp);
        fos.write(tmp, 0, size);



        os.write("TrEn".getBytes());

        FileChannel fileChannel = fos.getChannel();
        FileLock lock = fileChannel.lock();
        lock.release();


        fos.flush();
        fos.close();




        System.out.println(myFile.hashCode());
        long stop = System.currentTimeMillis();
//        double przepl = dlugosc_pliku/(stop-start);
//      System.out.println(przepl);


        //is.close();
        return myFile;
    }

    public File makePath(File plik) {
        String abspath = plik.getAbsolutePath();
        String name = plik.getName();
        String path = abspath.substring(0, abspath.length() - name.length());

        File tmp = new File(path);
        tmp.mkdirs();
        tmp = new File(abspath);
        try {
            tmp.createNewFile();
        } catch (Exception ex) {
        }
        return tmp;
    }

    public File receiveList() throws IOException {
        long start = System.currentTimeMillis();
        long current = 0;
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

        // Dlugosc dlugosci
        int dlugosc_t = is.read();

        // Dlugosc pliku

        byte[] dlugosc_te = new byte[dlugosc_t];
        is.read(dlugosc_te, 0, dlugosc_te.length);
        String dlugosc = new String(dlugosc_te);
        System.out.println(dlugosc);
        int dlugosc_pliku = new Integer(dlugosc);

        // Dlugosc probki
        int dlugosc_ile_probek = is.read();
        byte[] ile_probek = new byte[dlugosc_ile_probek];
        is.read(ile_probek, 0, ile_probek.length);
        String probek = new String(ile_probek);
        System.out.println(probek);
        int ile = new Integer(probek);

        /**
         * Tu jest definiowana sciezka zapisu pliku
         */
        File myFile = new File("list_z_serwera");
        FileOutputStream fos = new FileOutputStream(myFile);

        byte[] c = new byte[WIELKOSC_PROBKI];


        for (int i = 0; i < ile; i++) {
            int size = is.read(c, 0, WIELKOSC_PROBKI);
            fos.write(c, 0, size);
            fos.flush();
            current += size;
        }

        int dopelnienie = dlugosc_pliku - WIELKOSC_PROBKI * ile;

        byte[] tmp = new byte[dopelnienie];
        int size = is.read(tmp, 0, dopelnienie);
        // System.out.write(tmp);
        fos.write(tmp, 0, size);



        os.write("TrEn".getBytes());


        fos.flush();
        fos.close();




        System.out.println(myFile.hashCode());
        long stop = System.currentTimeMillis();
//        double przepl = dlugosc_pliku/(stop-start);
//      System.out.println(przepl);


        //is.close();
        return myFile;
    }
}
