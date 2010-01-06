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
 * Klasa przechowująca listę plików zbackupowanych/do backupu
 * ???
 * @author Ostros
 */
public class FileContainer {

    /**
     * Dotyczy plikow lokalnych
     */
    static String filename = "list";
    private ArrayList pliki;        // lista plików na UI
    private ArrayList doBackupu;    // dynamicznie generowana lista plikow do backupu
    private File listaPlikow;
    PrintWriter out;
    BufferedReader in;
    private ArrayList status; // status pliku w zdalnym systemie
    RemoteFileContainer rfc;  // pojemnik na elementy w zdalnym serwerze (tylko dowiązania symboliczne i timestampy

    public FileContainer() {
        status = new ArrayList();
        listaPlikow = new File(filename);
        doBackupu = new ArrayList();

        try {
            if (!listaPlikow.isFile()) {
                listaPlikow.createNewFile();
            }
            in = new BufferedReader(new FileReader(listaPlikow));
            pliki = this.loadContainer();

            out = new PrintWriter(new FileWriter(listaPlikow), true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.saveList();


    }

    public int add(File file) {
        if (pliki.contains(file)) {
            this.print("Plik znajduje się już na liście");
            return 0;
        } else {
            if (file.isDirectory()) {
                File[] katalogi = file.listFiles();
                for (int i = 0; i < katalogi.length; i++) {
                    add(katalogi[i]);
                }
                return 1;
            } else if (file.isFile()) {
                pliki.add(file);
                this.print("Użytkownik dodał plik " + file.toString());
                out.println(file.toString());
                return 1;
            }
            return 0;
        }
    }

    public int add(File file, DefaultListModel model) {
        if (pliki.contains(file)) {
            this.print("Plik znajduje się już na liście");
            return 0;
        } else {
            if (file.isDirectory()) {
                File[] katalogi = file.listFiles();
                for (int i = 0; i < katalogi.length; i++) {
                    add(katalogi[i], model);
                }
                return 1;
            } else if (file.isFile()) {
                pliki.add(file);
                model.addElement(file.toString());
                this.print("Użytkownik dodał plik " + file.toString());
                out.println(file.toString());
                this.createStates();
                return 1;
            }
            return 0;
        }

    }

    public String get(int no) {
        return ((File) pliki.get(no)).getPath();
    }

    public ArrayList getContainer() {
        return pliki;
    }

    public int getSize() {
        return pliki.size();
    }

    private void print(String tekst) {
        System.out.println("FileContainer: " + tekst);
    }

    private ArrayList loadContainer() {
        String path;
        ArrayList returnable = new ArrayList();
        try {
            while ((path = in.readLine()) != null) {
                returnable.add(new File(path));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return returnable;
    }

    public void saveList() {
        try {
            out.close();
            in.close();

            try {
                listaPlikow.delete();
                listaPlikow.createNewFile();

                out = new PrintWriter(new FileWriter(listaPlikow), true);
                in = new BufferedReader(new FileReader(listaPlikow));
                for (int i = 0; i < pliki.size(); i++) {
                    out.println(((File) pliki.get(i)).getAbsolutePath());
                }
            } finally {
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }


    }

    public void delFile(File plik) {
        System.out.println(pliki.remove(plik));
        /*for(int i = 0; i < pliki.size(); i++) {
        System.out.println(pliki.get(i));

        }*/
        this.createStates();
    }

    /**
     *  Metoda ktora porowna sobie pliki lokalne i zdalne
     *  dzieki czemu bedziemy znali ich status
     *  Idziemy wg. plikow lokalnych...
     */
    public void checkRemote() {
        rfc = new RemoteFileContainer();
        this.createStates();

    }

    /**
     * Metoda która tworzy stany dla wszystkich plików na zdalnym serwerze
     * Dzięki temu wiemy czy plik jest aktualny czy nie
     */
    public void createStates() {
        status.clear();
        for (int i = 0; i < pliki.size(); i++) {
            status.add(this.createFileStatus(i));
            System.out.println(i + ": " + status.get(i));
        }
    }

    private int createFileStatus(int index) {
        try {
            rfc.reload();
            long lastmod = rfc.getLastMod(pliki.get(index).toString());
            long lastLocal = (((File) pliki.get(index)).lastModified());
            System.out.println(lastmod);
            System.out.println(lastLocal);
            if (!((File) pliki.get(index)).exists()) {
                return 0;
            }
            if (lastmod == -1) {
                return 3; // plik nie istnieje w backupie
            } else if (lastmod >= lastLocal) {
                // plik w backupie jest rownolatkiem lub mlodszy niz na dysku
                return 1;
            } else if (lastmod < lastLocal) {
                // plik w backupie jest starszy niz na dysku
                return 2;
            }
        } catch (Exception ex) {
        }

        return -1;
    }

    public int getFileStatus(int index) {
        if (status.isEmpty() || status.size() < index + 1) {
            return 5;
        } else {
            return Integer.parseInt(status.get(index).toString());
        }

    }

    public int sendFiles(Object[] pliki) {


        return 0;
    }

    /**
     * Metoda przygotowująca tablicę doBackupu do backupu
     * Wybiera tylko elementy, które wymagają wysłania na serwer
     * (spełnione jedno z założeń projektu)
     */
    public int prepareBackup() {
        doBackupu.clear();

        for (int i = 0; i < pliki.size(); i++) {
            if (Integer.parseInt(status.get(i).toString()) >= 2) {
                System.out.println("Do backupu: " + i);
                doBackupu.add(pliki.get(i));
            }
        }
        return doBackupu.size();
    }

    /**
     * To będzie metoda do przesyłania plików, które użytkownik sobie zaznaczył
     * na liście.
     * @param pliki Bezpośrednia tablica Objectów, którą generuje zaznaczenie na JList
     */
    public void prepareBackup(Object[] pliki) {
    }

    public int getBackupSize() {
        return doBackupu.size();
    }

    public String getBackup(int no) {
        return ((File) doBackupu.get(no)).getPath();
    }

    public void reloadRemoteList() {
        rfc.reload();
    }
}
