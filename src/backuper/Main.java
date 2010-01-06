/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package backuper;

import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import java.io.File;

/**
 *
 * @author Ostros
 */
public class Main {

    /**
     * Główna klasa programu, wywołuje uruchomienie Klienta, nie robi nic szczegolnego
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int portNo = 8000;
        /*
        if ((args.length > 0) && (args[0].equals("serve"))) {
        if(args.length == 2) {
        portNo = Integer.parseInt(args[1]);
        }
        server.Server srv = new server.Server(portNo);
        }
        else if ((args.length > 0) && (args[1].equals("adduser"))) {
        File path = new File(System.getProperty("user.dir") + "\\back\\" + args[2] + "_" + args[3] + "\\");
        path.mkdirs();
        path = new File(path.getAbsolutePath() + "\\list");
        try {
        path.createNewFile();
        }
        catch(Exception ex) {
        ex.printStackTrace();
        }
        }
         */
        if (args.length > 0 && args[0].equals("serve")) {
            if (args.length == 1) {
                System.out.println("wlacz serwer");
                server.Server srv = new server.Server(8000);

            } else if (args.length == 2) {
                System.out.println("wlacz serwer na innym porcie");
                portNo = Integer.parseInt(args[1]);
                server.Server srv = new server.Server(portNo);
            } else if (args.length == 4 && args[1].endsWith("adduser")) {
                System.out.println("dodaj uzytkownika");
                File path = new File(System.getProperty("user.dir") + "\\back\\" + args[2] + "_" + args[3] + "\\");
                path.mkdirs();
                path = new File(path.getAbsolutePath() + "\\list");
                try {
                    path.createNewFile();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


        } else if (args.length > 0) {
            System.out.println("Nierozpoznane polecenie\n" +
                    "Pomoc programu BackOPAr\n" +
                    "- uruchom bez parametru, aby odpalić aplikację klienta\n" +
                    "- uruchom z parametrem \"serve\" aby odpalić serwer (nie uruchamia się w tle)\n" +
                    "- Inne parametry\n" +
                    "\tadduser username password - tworzy uzytkownika o nazwie username i hasle password\n" +
                    "\n" +
                    "\n" +
                    "Autor: Krzysztof K. Ostrowski (K.K.Ostrowski@stud.elka.pw.edu.pl\n" +
                    "Projekt zaliczeniowy OPA 09Z ");
        } else {
            System.out.println("Ramka otwarta");
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            } catch (UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            /* Turn off metal's use of bold fonts */
            // UIManager.put("swing.boldMetal", Boolean.FALSE);

            final Client klient = new Client();
            if (!SystemTray.isSupported()) {
                System.out.println("SystemTray is not supported");
                return;
            }
            final PopupMenu popup = new PopupMenu();

            final TrayIcon trayIcon =
                    new TrayIcon(createImage("icon_16.jpg", "tray icon"));
            final SystemTray tray = SystemTray.getSystemTray();

            // Create a popup menu components
            final MenuItem przywroc = new MenuItem("Przywroc");
            CheckboxMenuItem cb1 = new CheckboxMenuItem("Autobackup");
            CheckboxMenuItem cb2 = new CheckboxMenuItem("Ustawienia");
            Menu displayMenu = new Menu("Display");
            MenuItem errorItem = new MenuItem("Error");
            MenuItem warningItem = new MenuItem("Warning");
            MenuItem infoItem = new MenuItem("Info");
            MenuItem noneItem = new MenuItem("None");
            MenuItem exitItem = new MenuItem("Exit");

            przywroc.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent a) {
                    klient.changeVisible();
                }
            });
            exitItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent a) {
                    System.exit(0);
                }
            });
            //Add components to popup menu
            popup.add(przywroc);
            popup.addSeparator();
            // popup.add(cb1);
            // popup.add(cb2);
            // popup.addSeparator();
            //  popup.add(displayMenu);
            //  displayMenu.add(errorItem);
            //  displayMenu.add(warningItem);
            //  displayMenu.add(infoItem);
            //  displayMenu.add(noneItem);
            popup.add(exitItem);

            trayIcon.setPopupMenu(popup);


            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.out.println("TrayIcon could not be added.");
                return;
            }


            // TODO code application logic here
        }
    }

    protected static Image createImage(String path, String description) {
        URL imageURL = TrayIconDemo.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}
