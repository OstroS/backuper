/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.*;
import java.io.*;
import java.io.File.*;

/**
 * Klasa przechowująca listę plików zbackupowanych/do backupu
 * Była poprawka związana z zapisywaniem dobrych lastmodify do pliku
 * @author Ostros
 */
public class FileContainer {

    private ArrayList pliki;        // lokalizacja w lokalnym systemie plikow uzytkownika
    private ArrayList naSerwerze; // lokalizacja danego pliku w systemie plikow serwera
    private File listaPlikow;       // plik z lista plikow
    PrintWriter out;
    BufferedReader in;
    String username;
    String password;

    public static boolean checkUser(String username, String password) {
        File tmp = new File(System.getProperty("user.dir") + "\\back\\" + username + "_" + password + "\\");

        if(tmp.isDirectory()) return true;
        else return false;
    }
    public FileContainer(String username, String password) {
        this.username = username;
        this.password = password;
        listaPlikow = new File(System.getProperty("user.dir") + "\\back\\" + username + "_" + password + "\\");
       // listaPlikow.mkdirs();

        listaPlikow = new File(listaPlikow.getAbsolutePath() + "\\list");

        //System.out.println(listaPlikow.getAbsolutePath());

        try {
            //listaPlikow.createNewFile();
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
                naSerwerze.add(this.makePath(file));
                this.print("Użytkownik dodał plik " + file.toString());
                out.println(file.toString() + ";" + file.lastModified());
                return 1;
            }
            return 0;
        }



    }

    public String getFile(int no) {
        return ((File) pliki.get(no)).getPath();
    }

    public String getPath(int no) {
        return ((File) naSerwerze.get(no)).getAbsolutePath();
    }

    public File getFileAtSrv(int no) {
        return (File) naSerwerze.get(no);
    }

    public String getLocalPath(int no) {
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
        naSerwerze = new ArrayList();
        try {
            int i = 0;
            while ((path = in.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(path, ";");
                path = st.nextToken();
                File tmp = new File(path);
                returnable.add(tmp);
                naSerwerze.add(this.makePath(tmp));
                System.out.println(naSerwerze.get(i));
                i++;

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return returnable;
    }

    public File makePath(File plik) {
        String nazwa = plik.getName();
        String dysk = plik.getAbsolutePath().substring(0, 1);
        String sciezka = plik.getAbsolutePath().substring(3, plik.getAbsolutePath().length() - nazwa.length());

        String path = new String(System.getProperty("user.dir") + "\\back\\" + username + "_" + password + "\\" + dysk + "\\" + sciezka);

        File returnable = new File(path);
        returnable.mkdirs();
        path += nazwa;
        returnable = new File(path);
        try {
            returnable.createNewFile();

        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        return returnable;
    }

    public void saveList() {
        try {
            out.close();
            in.close();

            try {
              //  listaPlikow.delete();
              //  listaPlikow.createNewFile();

                out = new PrintWriter(new FileWriter(listaPlikow), true);
                in = new BufferedReader(new FileReader(listaPlikow));
                for (int i = 0; i < pliki.size(); i++) {
                    out.println(((File) pliki.get(i)).getAbsolutePath() + ";" + ((File) naSerwerze.get(i)).lastModified());
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            finally {
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getListLocation() {
        return listaPlikow.getAbsolutePath();
    }

    public void delFileFromList(String path) {
        File plik = new File(path);
        int position = pliki.indexOf(plik);
        if(position != -1) {
            pliki.remove(position);
            naSerwerze.remove(position);
            this.saveList();
            System.out.println("Usunieto plik: " + path);
            File toDelete = (this.makePath(plik));
            toDelete.delete();
        }
    }
}
