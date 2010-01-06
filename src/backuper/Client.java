/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package backuper;

import javax.swing.JFrame;
import java.io.*;
import java.awt.*;
import java.util.*;

import java.awt.event.*;
import java.net.URL;
import javax.swing.*;

/**
 * Klasa Client, obsługa wszystkich akcji związanych z połączeniem, interfejsem graficznym i przesyłaniem plików
 * Kontroluje działania całej aplikacji klienta
 *
 * @author Ostros
 */
public class Client {

    Conf konf;
    MainFrame ramka;
    Connector conn;
    FileContainer lista;
    private boolean isConnected;
    private Spy spy;

    /**
     * Konstruktor, jedyny
     * Tworzy ramkę dla klienta i uruchamia wątek połączeniowy;
     */
    public Client() {
        lista = new FileContainer();
        ramka = new MainFrame(this);
        konf = Conf.getInstance();
        ramka.setParams(konf);
        ramka.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ramka.setVisible(true);
        ramka.setTitle(Conf.version + " Rozłączony");
        isConnected = false;
        spy = new Spy(this);


    }

    /**
     * Wyświetlenie komunikatu o akcji użytkownika na standardowym wyjściu
     * @param name Informacje o akcji.
     */
    public void userAction(String name) {
        System.out.println("Uzytkownik wykonal: " + name);
    }

    public void connect(String name, char[] pass, String host, int port) {
        try {
            conn = new Connector(name, pass, host, port);

            if (conn.getConnection()) {
                lista.checkRemote();
                isConnected = true;
                ramka.setTitle(Conf.version + "Połączony z " + host + ":" + port);
                StringTokenizer st = new StringTokenizer(konf.getGodz(), ":");

                int godz = Integer.parseInt(st.nextToken());
                int godz_min = Integer.parseInt(st.nextToken());

                spy.makeSpy(konf.getMode(), Integer.parseInt(konf.getMin()), godz, godz_min);
            } else {
                ramka.errorDialog(1, "Problem z połączeniem\nSerwer nie odpowiada lub Twoje dane są nieprawidłowe");
            }
            ramka.repaintPanel();
        } catch (Connector.ConnectionException ex) {
            ex.printStackTrace();
            ramka.errorDialog(2, "Błąd autentyfikacji!\nZły użytkownik i/lub hasło");
        }


    }

    public void disconnect() {
        try {
            spy.stopSpy();
            conn.disconnect();
            isConnected = false;
            ramka.setTitle(Conf.version + " Rozłączony");
            ramka.repaintPanel();
        } catch (NullPointerException ex) {
            ramka.infoDialog("Nie jesteś połączony z serwerem");
        }

    }

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Funkcja uruchamiająca backup
     *
     */
    public int backup() {
        try {
            ramka.runBar();
        } catch (NullPointerException ex) {
        }
        int ret = 0;
        try {
            // To jest ryzykowne, możliwe że przez to coś nie będzie działać!
            int czyPotrzebnyBackup = lista.prepareBackup();
            if (czyPotrzebnyBackup > 0) {
                int i = conn.doBackup(lista);
                if (i == 1) {
                    this.getListFromServer();
                    // ramka.infoDialog("Transmisja zakończona poprawnie!");
                    // Tworzenie stanów plików na dysku lokalnym
                    lista.createStates();
                    // Odświeżenie listy wyświetlanej użytkownikowi
                    ramka.refreshList();

                    /**
                     * Trzeba przeslac jeszcze raz plik z lista plikow zdalnych!
                     */
                    ret = 1;
                }
            } else {
                System.out.println("Backup niepotrzebny");
            }

        } catch (NullPointerException ex) {
            ramka.errorDialog("Błąd połączenia z serwerem!");

        }
        ramka.stopBar();
        return ret;

    }

    public void przywroc() {
        ramka.runBar();
        try {
            int i = conn.przywroc();
            if (i == 1) {
                ramka.infoDialog("Transmisja zakończona poprawnie!");
            }
            this.getListFromServer();
            lista.createStates();
            ramka.refreshList();
        } catch (NullPointerException ex) {
            ramka.errorDialog("Błąd połączenia z serwerem!");
        }
        ramka.stopBar();
    }

    public void getFile(File plik) {
        conn.getFile(plik);
        this.getListFromServer();
        lista.createStates();

    }

    public void changeVisible() {
        ramka.changeVisible();
    }

    /**
     * Metody zarządzania aktualną listą plików do backupowania
     */
    public void listAdd(File file) {
        int i = lista.add(file);
        if (i == 0) {
            ramka.infoDialog("Plik " + file.getName() + "\nznajduje się już na liście");
        }
    }

    public void listAdd(File file, DefaultListModel model) {
        int i = lista.add(file, model);
        if (i == 0) {
            ramka.infoDialog("Plik " + file.getName() + "\nznajduje się już na liście");
        }
    }

    public String listGet(int no) {
        return lista.get(no);
    }

    public ArrayList listGetContainer() {
        return lista.getContainer();
    }

    public int listGetSize() {
        return lista.getSize();
    }

    public void listSave() {
        lista.saveList();
    }

    public void listDelFile(File plik) {
        lista.delFile(plik);
        try {
            conn.delFileFromServer(plik);
            conn.receiveList();
        } catch (NullPointerException ex) {
        }

    }

    public int listGetEltState(int index) {

        return lista.getFileStatus(index);
    }

    public void getListFromServer() {
        conn.receiveList();
    }

    public void refresh() {
        this.getListFromServer();
        lista.reloadRemoteList();
        lista.createStates();
    }

    public void setSpy(int mode, int min, int h, int h_min) {
        spy.changeParam(mode, min, h, h_min);
    }
}
