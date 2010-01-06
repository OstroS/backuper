/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package backuper;

import java.util.*;
import java.io.*;
import java.io.File.*;
import javax.swing.*;

/**
 * Klasa przechowująca informacje o plikach zdalnych
 * Zawiera sciezke dostepu do pliku w lokalnym systemie plikow
 * Date modyfikacji pliku na serwerze
 * Status pliku mowiacy o rodzaju jego backupi
 * @author Ostros
 */
public class RemoteFileContainer {

    String filename = "list_z_serwera";
    private File listaPlikow;
    BufferedReader in;
    private ArrayList pliki;
    private ArrayList timestamp;

    /**
     * Konstrutktor, odpalany dopiero po połączeniu z serwerem
     *
     */
    public RemoteFileContainer() {


        timestamp = new ArrayList();


        listaPlikow = new File(filename);


        try {
            in = new BufferedReader(new FileReader(listaPlikow));
            pliki = this.loadContainer();

            //  out = new PrintWriter(new FileWriter(listaPlikow), true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // this.saveList()
    }

    /**
     * Ładowanie kontenera danymi
     * @return
     */
    private ArrayList loadContainer() {
        timestamp.clear();
        String path;
        ArrayList returnable = new ArrayList();
        try {
            int i = 0;
            while ((path = in.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(path, ";");
                path = st.nextToken();
                File tmp = new File(path);
                returnable.add(tmp);
                timestamp.add(st.nextToken());
                i++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return returnable;
    }

    public void printAll() {
        for (int i = 0; i < pliki.size(); i++) {
            System.out.println(pliki.get(i).toString() + " " + timestamp.get(i).toString());
        }
    }

    /**
     * Funkcja zwraca date ostatniej modyfikacji pliku, ktory znajduje sie na serwerze
     * @param path Sciezka dostepu do pliku w systemie lokalnym
     * @return Data pliku w unix time (chyba :)) albo -1 jesli dany plik nie istnieje w backupie
     */
    public Long getLastMod(String path) {
        int position = pliki.indexOf(new File(path));
        Long mod;
        if (position != -1) {
            //System.out.println("pos: " + position);
            mod = Long.parseLong(timestamp.get(position).toString());
            // System.out.println(mod);

        } else {
            mod = new Long(-1);  // bliku nie ma w backupie!
        }
        return mod;
    }

    public void reload() {
        try {
            in.close();
            in = new BufferedReader(new FileReader(listaPlikow));
            pliki = this.loadContainer();
        } catch (IOException ex) {
        }

    }
}
