/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package backuper;

import javax.swing.JList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.*;
import java.awt.Container;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.File;

/*
 * @author Ostros
 */
public class MainFrame extends JFrame {

    private Client klient;
    //FileContainer lista;
    private MainPanel panel;
    private JMenuBar menuBar;
    private StatusPanel status;
    private JButton edytuj;
    private JButton backupuj;
    private JButton przywroc;
    private JButton usun;
    private JButton ukryj;
    private JButton synchronizuj;
    private JProgressBar pasek;
    private DefaultListModel model;
    private JList list;
    Settings ustawienia;
    String[] params;
    Conf konf;

    public MainFrame(Client client) {

        this.klient = client;
        //this.lista = new FileContainer();
        //lista.add(new File("Backuper2.rar"));
        this.setSize(700, 600);

        menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("Plik");
        menuBar.add(menuFile);
        JMenuItem menuFileOpen = new JMenuItem("Otwórz");
        menuFileOpen.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                runBar();
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
                fc.setMultiSelectionEnabled(true);

                fc.showOpenDialog(fc);
                File[] pliki = fc.getSelectedFiles();
                for (File plik : pliki) {
                    if (plik != null) {
                        klient.listAdd(new File(plik.getPath()), model);
                        panel.repaint();

                    }
                }

                stopBar();


            }
        });
        JMenuItem menuFileZakoncz = new JMenuItem("Zakoncz");
        menuFileZakoncz.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //  lista.saveList();
                System.exit(0);
            }
        });



        menuFile.add(menuFileOpen);
        menuFile.add(menuFileZakoncz);

        JMenu menuKlient = new JMenu("Klient");
        menuBar.add(menuKlient);
        JMenuItem menuKlientConnect = new JMenuItem("Połącz z...");
        menuKlientConnect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent a) {
                LoginForm form = new LoginForm();
                form.setVisible(true);

            }
        });
        menuKlient.add(menuKlientConnect);

        JMenuItem menuKlientDisConnect = new JMenuItem("Rozłącz");
        menuKlientDisConnect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent a) {
                klient.disconnect();

            }
        });
        menuKlient.add(menuKlientDisConnect);
        JMenu menuSerwer = new JMenu("Inne");

        JMenuItem menuSerwerUstawienia = new JMenuItem("Ustawienia");
        menuSerwerUstawienia.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent a) {
                //klient.disconnect();
                ustawienia = new Settings();
                ustawienia.setVisible(true);
            }
        });
        menuSerwer.add(menuSerwerUstawienia);
        menuSerwer.addSeparator();

        JMenuItem menuSerwerAbout = new JMenuItem("O programie...");
        menuSerwerAbout.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent a) {
                AboutBox box = new AboutBox();
                box.setVisible(true);
            }
        });
        menuSerwer.add(menuSerwerAbout);
        menuBar.add(menuSerwer);


        this.setJMenuBar(menuBar);


        edytuj = new JButton("Przywróc wszystko");
        edytuj.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                runBar();
                klient.userAction(edytuj.getText());
                klient.przywroc();
                klient.listSave();
                stopBar();
            }
        });


        backupuj = new JButton("Backupuj");
        backupuj.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                klient.userAction(backupuj.getText());
                int ijak = klient.backup();
                if (ijak == 1) {
                    infoDialog("Transmisja zakończona poprawnie!");
                }
            }
        });

        usun = new JButton("Usuń");
        usun.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                runBar();
                klient.userAction(usun.getText());
                Object[] values = list.getSelectedValues();
                for (Object plik : values) {
                    klient.listDelFile(new File((String) plik));
                    model.removeElement(plik);
                }

                klient.listSave();
                stopBar();
            }
        });
        przywroc = new JButton("Przywróć plik");
        przywroc.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                klient.userAction(przywroc.getText());

                runBar();
                Object[] values = list.getSelectedValues();
                for (Object plik : values) {
                    klient.getFile(new File((plik.toString())));
                }

                klient.listSave();
                refreshList();
                stopBar();


            }
        });

        ukryj = new JButton("Ukryj");
        ukryj.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                klient.userAction(ukryj.getText());
                setVisible(false);

            }
        });

        synchronizuj = new JButton("Synchronizuj");
        synchronizuj.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                klient.userAction(synchronizuj.getText());
                /* Object[] values = list.getSelectedValues();
                for(Object value: values) {
                System.out.println((String)value);
                }*/
                klient.refresh();
                list.repaint();


            }
        });

        pasek = new JProgressBar(0, 100);
        status = new StatusPanel();


        panel = new MainPanel();
        this.add(panel);
        pasek.setValue(100);

    }

    public void changeVisible() {
        setVisible(true);
    }

    public void refreshList() {
        list.repaint();
    }

    public void runBar() {
        pasek.setIndeterminate(true);
    }

    public void stopBar() {
        pasek.setIndeterminate(false);
    }

    public void repaintPanel() {
        panel.repaint();
    }

    public void setParams(Conf konf) {
        this.konf = konf;
        params = konf.getTable();
    }

    class MainPanel extends JPanel {

        public MainPanel() {
            setLayout(new BorderLayout());
            add(status, BorderLayout.SOUTH);

            CenterPanel cpanel = new CenterPanel();
            cpanel.setSize(700, 500);

            //add(cpanel, BorderLayout.CENTER);

            /**
             * To nowe
             */
            model = new DefaultListModel();
            ArrayList pliki = klient.listGetContainer();
            for (int i = 0; i < pliki.size(); i++) {
                model.addElement(pliki.get(i).toString());
            }
            list = new JList(model);
            JScrollPane scrollPane = new JScrollPane(list);
            list.setSize(500, 500);
            ListCellRenderer renderer = new ComplexCellRenderer();
            list.setCellRenderer(renderer);
            add(scrollPane);


        }
    }

    class StatusPanel extends JPanel {

        public StatusPanel() {

            this.add(edytuj);

            this.add(backupuj);

            this.add(przywroc);

            this.add(usun);
            this.add(synchronizuj);
            this.add(ukryj);

            this.add(pasek);


        }

        @Override
        public void paintComponent(Graphics g) {
            edytuj.setEnabled(klient.isConnected());
            backupuj.setEnabled(klient.isConnected());
            przywroc.setEnabled(klient.isConnected());
            usun.setEnabled(klient.isConnected());
            synchronizuj.setEnabled(klient.isConnected());

        }
    }

    class CenterPanel extends JPanel {

        public CenterPanel() {
            this.setSize(700, 500);
            //  final JTable tabela = new JTable(cells,columnNames);
            //  tabela.setSize(700,500);
            //  add(new JScrollPane(tabela));
            model = new DefaultListModel();
            ArrayList pliki = klient.listGetContainer();
            for (int i = 0; i < pliki.size(); i++) {
                model.addElement(pliki.get(i).toString());
            }
            list = new JList(model);
            // JScrollPane scroll = new JScrollPane();
            // add(scroll);
            //scroll.setSize(700,500);
            // scroll.add(list);
            JScrollPane scrollPane = new JScrollPane(list);
            list.setSize(500, 500);
            add(scrollPane);

            //add(list);
        }
        ArrayList temp = klient.listGetContainer();
    }

    /**
     * Klasa wygenerowana przez NetBeans, okienko do logowania
     * akcja naciśnięcia buttona połącz etc...
     */
    public class LoginForm extends javax.swing.JFrame {

        /** Creates new form LoginForm */
        public LoginForm() {
            initComponents();
        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {

            jTextField1 = new javax.swing.JTextField();
            jTextField3 = new javax.swing.JTextField();
            jTextField4 = new javax.swing.JTextField();
            jLabel1 = new javax.swing.JLabel();
            jLabel2 = new javax.swing.JLabel();
            jLabel3 = new javax.swing.JLabel();
            jLabel4 = new javax.swing.JLabel();
            jButton1 = new javax.swing.JButton();
            jPasswordField1 = new javax.swing.JPasswordField();
            jLabel5 = new javax.swing.JLabel();

            setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
            setAlwaysOnTop(true);

            jTextField1.setText(params[0]);

            jTextField3.setText(params[1]);

            jTextField4.setText(params[2]);

            jLabel1.setText("Użytkownik");

            jLabel2.setText("Hasło");

            jLabel3.setText("host");

            jLabel4.setText("port");

            jButton1.setText("Połącz");
            jPasswordField1.setText("pass");
            jLabel5.setText("Podaj parametry do połączeni z serwerem");

            /**
             * Obsługa przycisku
             * Wysłanie do obiektu typu Client informacji o próbie nawiązania połączenia...
             */
            jButton1.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        params[0] = jTextField1.getText();
                        params[1] = jTextField3.getText();
                        params[2] = jTextField4.getText();
                        konf.setTable(params);
                        konf.saveFile();

                        klient.connect(jTextField1.getText(), jPasswordField1.getPassword(), jTextField3.getText(), Integer.parseInt(jTextField4.getText()));

                    } catch (Exception ex) {
                        errorDialog(3, "Wprowadzone dane są niepoprawne");
                    }
                    setVisible(false);


                }
            });






            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap(34, Short.MAX_VALUE).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel1).addComponent(jLabel2).addComponent(jLabel3).addComponent(jLabel4)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE).addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE).addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE).addComponent(jPasswordField1)).addContainerGap()).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(jButton1).addGap(83, 83, 83)).addGroup(layout.createSequentialGroup().addComponent(jLabel5).addContainerGap()))));
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabel5).addGap(18, 18, 18).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel1)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel2).addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel3)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel4)).addGap(18, 18, 18).addComponent(jButton1).addContainerGap(14, Short.MAX_VALUE)));

            pack();


        }// </editor-fold>

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
            // TODO add your handling code here:
        }
        /**
         * @param args the command line arguments
         */
        // Variables declaration - do not modify
        private javax.swing.JButton jButton1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JLabel jLabel5;
        private javax.swing.JPasswordField jPasswordField1;
        private javax.swing.JTextField jTextField1;
        private javax.swing.JTextField jTextField3;
        private javax.swing.JTextField jTextField4;
        // End of variables declaration
    }

    public class Settings extends javax.swing.JFrame {

        /** Creates new form Settings */
        public Settings() {
            initComponents();
        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {
            setTitle("Ustawienia");
//        params = konf.getTable();
            jLabel1 = new javax.swing.JLabel();
            jTextField1 = new javax.swing.JTextField();
            jTextField2 = new javax.swing.JTextField();
            jLabel2 = new javax.swing.JLabel();
            jLabel3 = new javax.swing.JLabel();
            jCheckBox1 = new javax.swing.JCheckBox();
            jCheckBox2 = new javax.swing.JCheckBox();
            jButton1 = new javax.swing.JButton();

            setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

            jLabel1.setText("Ustawienia autobackupu:");

            jTextField1.setText(params[3]);

            jTextField2.setText(params[4]);
            jTextField2.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jTextField2ActionPerformed(evt);
                }
            });

            jLabel2.setText("Co:");

            jLabel3.setText("O godzinie:");

            jCheckBox1.setText("On/Off");
            jCheckBox1.setSelected(((String) params[5]).equals("true") ? true : false);
            jCheckBox1.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jCheckBox1ActionPerformed(evt);
                }
            });

            jCheckBox2.setText("On/Off");
            jCheckBox2.setSelected(((String) params[6]).equals("true") ? true : false);
            jCheckBox2.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jCheckBox2ActionPerformed(evt);
                }
            });

            jButton1.setText("Ustaw");
            jButton1.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(33, 33, 33).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel1).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jLabel3).addComponent(jLabel2)).addGap(18, 18, 18).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(jTextField2).addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)))).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jCheckBox2).addComponent(jCheckBox1)).addContainerGap(33, Short.MAX_VALUE)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap(136, Short.MAX_VALUE).addComponent(jButton1).addGap(127, 127, 127)));
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(21, 21, 21).addComponent(jLabel1).addGap(18, 18, 18).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel2).addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jCheckBox1)).addGap(18, 18, 18).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel3).addComponent(jCheckBox2)).addGap(18, 18, 18).addComponent(jButton1).addContainerGap(30, Short.MAX_VALUE)));

            pack();
        }// </editor-fold>

        private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {
            // TODO add your handling code here:
        }

        private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {
            // TODO add your handling code here:
        }

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
            // TODO add your handling code here:
            int mode = 0;
            setVisible(false);
            if (jCheckBox1.isSelected()) {
                System.out.println("Checked 1");

                mode += 1;
            }
            if (jCheckBox2.isSelected()) {
                System.out.println("Checked 2");
                mode += 2;
            }
            params[3] = jTextField1.getText();
            params[4] = jTextField2.getText();
            params[5] = ((jCheckBox1.isSelected()) ? new String("true") : new String("false"));
            params[6] = ((jCheckBox2.isSelected()) ? new String("true") : new String("false"));
            konf.setTable(params);
            konf.saveFile();
            try {
                StringTokenizer st = new StringTokenizer(params[4], ":");

                int godz = Integer.parseInt(st.nextToken());
                int godz_min = Integer.parseInt(st.nextToken());
                klient.setSpy(mode, Integer.parseInt(jTextField1.getText()), godz, godz_min);
            } catch (Exception ex) {
                errorDialog("Błąd wprowadzonych danych");
            }
        }

        private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {
            // TODO add your handling code here:
        }
        // Variables declaration - do not modify
        private javax.swing.JButton jButton1;
        private javax.swing.JCheckBox jCheckBox1;
        private javax.swing.JCheckBox jCheckBox2;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JTextField jTextField1;
        private javax.swing.JTextField jTextField2;
        // End of variables declaration
    }

    public void errorDialog(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Błąd!", JOptionPane.ERROR_MESSAGE);

    }

    public void errorDialog(int no, String msg) {
        JOptionPane.showMessageDialog(this, msg, "Błąd #" + no, JOptionPane.ERROR_MESSAGE);
    }

    public void infoDialog(String msg) {
        JOptionPane.showMessageDialog(rootPane, msg, "Informacja", JOptionPane.INFORMATION_MESSAGE);
    }

    class DiamondIcon implements Icon {

        private Color color;
        private boolean selected;
        private int width;
        private int height;
        private Polygon poly;
        private static final int DEFAULT_WIDTH = 10;
        private static final int DEFAULT_HEIGHT = 10;

        public DiamondIcon(Color color) {
            this(color, true, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }

        public DiamondIcon(Color color, boolean selected) {
            this(color, selected, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }

        public DiamondIcon(Color color, boolean selected, int width, int height) {
            this.color = color;
            this.selected = selected;
            this.width = width;
            this.height = height;
            initPolygon();
        }

        private void initPolygon() {
            poly = new Polygon();
            int halfWidth = width / 2;
            int halfHeight = height / 2;
            poly.addPoint(0, halfHeight);
            poly.addPoint(halfWidth, 0);
            poly.addPoint(width, halfHeight);
            poly.addPoint(halfWidth, height);
        }

        public int getIconHeight() {
            return height;
        }

        public int getIconWidth() {
            return width;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.translate(x, y);
            if (selected) {
                g.fillPolygon(poly);
            } else {
                g.drawPolygon(poly);
            }
            g.translate(-x, -y);
        }
    }

    class ComplexCellRenderer extends JComponent implements ListCellRenderer {

        protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
        Icon theIcon = null;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected,
                    cellHasFocus);

            int state = klient.listGetEltState(index);
            if (state == 0) {
                theIcon = new DiamondIcon(Color.red);
                renderer.setBackground(Color.red);
                renderer.setForeground(Color.white);
                renderer.setToolTipText("Plik zmienił położenie lub został usunięty!");
            } else if (state == 1) {
                theIcon = new DiamondIcon(Color.green);
                renderer.setToolTipText("Lokalna i zdalna kopia są identyczne.");
            } else if (state == 2) {
                theIcon = new DiamondIcon(Color.orange);
                renderer.setToolTipText("Kopia lokalna różni się od kopii zdalnej.");
            } else {
                theIcon = new DiamondIcon(Color.gray);
                renderer.setToolTipText("Plik nie został jeszcze zbackupowany");
            }
            renderer.setIcon(theIcon);
            renderer.setText(value.toString());

            //renderer.setBackground(Color.LIGHT_GRAY);
            //renderer.setForeground(Color.red);
            return renderer;
        }
    }

    public class AboutBox extends javax.swing.JFrame {

        /** Creates new form AboutBox */
        public AboutBox() {
            initComponents();
        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {

            jLabel1 = new javax.swing.JLabel();
            jLabel2 = new javax.swing.JLabel();
            jLabel3 = new javax.swing.JLabel();
            jLabel4 = new javax.swing.JLabel();
            jLabel5 = new javax.swing.JLabel();
            jButton1 = new javax.swing.JButton();

            setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

            jLabel1.setText("BackOPAr ver 1.0");

            jLabel2.setText("Projekt zaliczeniowy z przedmiotu OPA");

            jLabel3.setText("Obiektowe programowanie aplikacji rozproszonych i współbieżnych");

            jLabel4.setText("Semestr 09Z");

            jLabel5.setText("Krzysztof K. Ostrowski 206136");

            jButton1.setText("Zamknij");
            jButton1.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent a) {
                    setVisible(false);
                }
            });
            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabel1).addGap(99, 99, 99)).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabel2)).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabel3)).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabel4)).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabel5)).addGroup(layout.createSequentialGroup().addGap(130, 130, 130).addComponent(jButton1))).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabel1).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jLabel3).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jLabel4).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jLabel5).addGap(18, 18, 18).addComponent(jButton1).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

            pack();
        }// </editor-fold>
        /**
         * @param args the command line arguments
         */
        // Variables declaration - do not modify
        private javax.swing.JButton jButton1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JLabel jLabel5;
        // End of variables declaration
    }
}
