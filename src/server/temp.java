/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;
import java.io.File;
/**
 *
 * @author Ostros
 */
public class temp {
    public static void main(String args[]) {
        File plik = new File("F://Projekty//JAVA//Backuper//manifest.mf");
        String nazwa = plik.getName();
        String dysk = plik.getAbsolutePath().substring(0, 1);
        String sciezka = plik.getAbsolutePath().substring(3,plik.getAbsolutePath().length() - nazwa.length());
        
        String path = new String(System.getProperty("user.dir") + "\\back\\" + dysk + "\\" + sciezka);
        System.out.println(plik.getAbsolutePath());
        System.out.println("Nazwa: " + nazwa);
        System.out.println("Dysk: " + dysk);
        System.out.println("Sch: " + sciezka);
        System.out.println("Path: " + path);
       
        File tmp = new File(path);
        boolean status;
        status = tmp.mkdirs();
        report(status);
        path += nazwa;
       
        File tmp2 = new File(path);
        try {
            tmp2.createNewFile();

        }
        catch(java.io.IOException ex) {
            ex.printStackTrace();
        }
       


    }
    static void report(boolean b) {
    System.out.println(b ? "success" : "failure");
  }
}
