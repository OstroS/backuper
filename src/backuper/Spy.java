/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package backuper;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Klasa która co określony przez użytkownika czas dokonuje backupu
 * Może uda się to zrobić np. o ustalonej z góry porze...
 * @author Ostros
 */
public class Spy {

    private Client klient;
    private SpyThread watek;
    Thread t;
    Runnable r;

    public Spy(Client klient) {
        this.klient = klient;


    }

    public void makeSpy(int mode, int min, int h, int h_min) {
        r = new SpyThread(mode, min, h, h_min);
        t = new Thread(r);
        t.start();
    }

    public void stopSpy() {
        t.stop();
    }

    public void changeParam(int mode, int min, int godz, int godz_min) {
        try {
            ((SpyThread) r).changeParam(mode, min, godz, godz_min);
        } catch (NullPointerException ex) {
        }
    }

    /**
     * Watek "sledzacy"
     * O okreslonym czasie bedzie wywolywal backup w kliencie
     */
    public class SpyThread implements Runnable {

        int liczDo;
        int godz;
        int mode;
        int act;
        int godz_min;

        /**
         * Konstruktor klasy śledzącej
         * @param mode Tryb pracy: 0 - nic, 1 - minutowy, 2 - cogodzinny, 3 - oba
         * @param min Co ile minut wykonać backup
         * @param godz O ktorej godzinie wykonac backup
         */
        public SpyThread(int mode, int min, int godz, int godz_min) {
            liczDo = min;
            this.mode = mode;
            this.godz = godz;
            this.godz_min = godz_min;
        }

        public void changeParam(int mode, int min, int godz, int godz_min) {
            this.liczDo = min;
            this.mode = mode;
            this.godz = godz;
            this.godz_min = godz_min;
            System.out.println("changed: co " + min + " o " + godz + ":" + godz_min + " mode " + mode);
            act = 0;
        }

        public void run() {
            act = 0;

            while (true) {
                int tmp;
                if (liczDo <= 0) {
                    tmp = 1;
                } else {
                    tmp = liczDo;
                }
                try {

                    if (act == tmp) {
                        if (mode == 1 || mode == 3) {
                            minute();
                        }
                        act = 0;


                    } else {
                        System.out.println(act);
                        act++;
                    }
                    /**
                     * Sprawdzany warunek na godzine
                     */
                    if (mode == 3 || mode == 2) {
                        Calendar kal = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("mm");
                        int minuta = Integer.parseInt(sdf.format(kal.getTime()));
                        sdf = new SimpleDateFormat("h");
                        int godzina = Integer.parseInt(sdf.format(kal.getTime()));
                        System.out.println(godzina + ":" + minuta + kal.getTime());
                        // warunek na godzine i minuty
                        if (godzina == this.godz && minuta == this.godz_min) {
                            hours();

                        }


                    }


                    // sleep na minute
                    Thread.sleep(60000);
                } // co minute sprawdzanie
                catch (Exception ex) {
                    // ex.printStackTrace();
                }
            }
        }

        private void minute() {
            System.out.println("Perform backup");
            try {
                klient.backup();
            } catch (NullPointerException ex) {
            }
        }

        private void hours() {
            System.out.println("Perform backup");
            klient.backup();
        }
    }
}
