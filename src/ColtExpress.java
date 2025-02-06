import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.awt.event.KeyEvent;

enum Direction { AVANT,
    ARRIERE,
    HAUT,
    BAS }


/*************************************
 *                                   *
 *          PARTIE OBSERVER          *
 *           / OBVSERVABLE           *
 *                                   *
 ************************************/

interface Observer {
    void update();
}

abstract class Observable {
    private ArrayList<Observer> observers;

    public Observable() {
        this.observers = new ArrayList<Observer>();
    }

    public void addObserver(Observer o) {
        observers.add(o);
    }

    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }
}


/*************************************
 *                                   *
 *        PARTIE COLTEXPRESS         *
 *                                   *
 ************************************/

public class ColtExpress {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "Welcome to Colt Express!\n\n" +
                    "Instructions:\n" +
                    "- Enter the number of bandits (1+) actions & ammunitions per bandit (1+ each).\n" +
                    "- Enter the number of wagons for the train you'll board, and the Marshal's impatience level. \n" +
                    "- Use the arrow keys to move and interact.\n\n" +
                    "- Navigate your bandit through the train, rob treasures and try avoiding the Marshal !\n" +
                    "Good luck!", "Welcome to Colt Express", JOptionPane.INFORMATION_MESSAGE);

            JOptionPane.showMessageDialog(null, "Here are the keybinds !\n\n" +
                    "Movement:\n" +
                    "- Q :  Move Left\n" +
                    "- D :  Move Right\n" +
                    "- Z :  Move Up (to the roof)\n" +
                    "- S :  Move Down (into the wagon)\n\n" +
                    "Actions:\n" +
                    "- B : Rob (when inside a wagon)\n" +
                    "- T : Toggle Shooting Mode\n" +
                    "- Arrow keys (← ↑ → ↓): Shoot in respective directions when in shooting mode\n\n" +
                    "Setup:\n" +
                    "- Backspace : Go back on previously done action\n" +
                    "- Enter : Execute the primary action button\n\n", "Keybinds", JOptionPane.INFORMATION_MESSAGE);

            int NB_BANDITS = Integer.parseInt(JOptionPane.showInputDialog("Enter the number of bandits:"));
            ArrayList<String> NOMS_BANDITS = new ArrayList<>(NB_BANDITS);

            for (int i = 0 ; i < NB_BANDITS ; i++)
                NOMS_BANDITS.add(JOptionPane.showInputDialog("Enter player name for bandit " + (i+1) + " :"));

            int NB_ACTIONS = Integer.parseInt(JOptionPane.showInputDialog("Enter the number of actions per bandit: "));
            int NB_BALLES = Integer.parseInt(JOptionPane.showInputDialog("Enter the number of ammunitions per bandit: "));
            int NB_WAGONS = Integer.parseInt(JOptionPane.showInputDialog("Enter the number of wagons:"));
            double NERVOSITE_MARSHALL = Double.parseDouble(JOptionPane.showInputDialog("Enter the Marshals nervosity: (1-10)"));

            Modele modele = new Modele(NB_BANDITS, NOMS_BANDITS, NB_ACTIONS, NB_BALLES, NB_WAGONS, NERVOSITE_MARSHALL);
            Vue vue = new Vue(modele);
        });
    }
}


/*************************************
 *                                   *
 *          PARTIE MODELE            *
 *                                   *
 ************************************/

class Modele extends Observable {
    protected static Train train;
    protected static Bandit[] bandits;
    protected static Action[][] actions;
    protected static Marshall marshall;
    protected static int NB_BANDITS;
    protected static int NB_ACTIONS;
    protected static int NB_BALLES;
    protected static ArrayList<String> NOMS_BANDITS;

    public Modele(int nbBandits, ArrayList<String> nomsBandits, int nbActions, int nbBalles, int nbWagons, double NERVOSITE_MARSHALL) {
        NB_BANDITS = nbBandits;
        NB_ACTIONS = nbActions;
        NB_BALLES = nbBalles;
        NOMS_BANDITS = nomsBandits;
        train = new Train(nbWagons);
        marshall = new Marshall(this, "Marshall", false, 1, NERVOSITE_MARSHALL/10);
        bandits = new Bandit[NB_BANDITS];
        actions = new Action[NB_BANDITS][NB_ACTIONS];

        for (int i = 0; i < NB_BANDITS; i ++) {
            bandits[i] = new Bandit(this, NOMS_BANDITS.get(i), true, 2, NB_BALLES);
        }
    }
}

/*************************************
 *                                   *
 *          PARTIE TRAIN             *
 *                                   *
 ************************************/

class Train {
    private static int NB_WAGONS;
    private final Wagon[] train;

    public Train(int nbWagons) {
        NB_WAGONS = nbWagons;
        train = new Wagon[NB_WAGONS]; // On inclut la locomotive
        train[0] = new Wagon(1, true); // La locomotive

        for (int i = 1; i < NB_WAGONS ; i++) {
            train[i] = new Wagon(i+1, false);
        }
    }

    /* GETTERS */
    public int getNbWagons() { return NB_WAGONS; }

    public Wagon getWagon(int numWagon) { return train[numWagon]; }

    public Wagon[] getTrain() { return this.train; }
}


/*************************************
 *                                   *
 *          PARTIE WAGON             *
 *                                   *
 ************************************/

class Wagon {
    private final int numWagon;
    private boolean avecMagot;
    private int nbButin; // On va considerer qu'un magot est un butin
    private ArrayList<Tresor> butins;



    public Wagon(int numWagon, boolean aMagot) {
        if (numWagon <= 0)
            numWagon = 1;
        this.numWagon = numWagon;
        this.avecMagot = aMagot;

        Random r = new Random();
        this.nbButin = r.nextInt(4)  + (avecMagot ? 1 : 0);
        this.butins = new ArrayList<>(nbButin);

        for (int i = 0 ; i < nbButin - (avecMagot ? 1 : 0); i++) {
            // Probabilite de 3/4 qu'un butin soit une bourse
            Tresor bourse = new Tresor(r.nextInt(501), "Bourse");
            Tresor bijou = new Tresor(500, "Bijou");
            butins.add(r.nextInt(4) <= 2 ? bourse : bijou);
        }

        // Le magot sera contenu dans la derniere case de la liste butins
        if (aMagot) {
            butins.add(new Tresor(1000, "Magot"));
        }
    }

    /* GETTERS */
    public int getNumWagon() { return this.numWagon; }

    public int getNbButins() { return this.nbButin; }

    public ArrayList<Tresor> getButins() { return this.butins; }

    public Tresor getRandomTresor() {
        Random r = new Random();
        int nbTresor = this.getNbButins();
        int nbRandom = r.nextInt(0, nbTresor); // Eviter le cas où r.nextInt(0, 0) doit être evalué (renvoie 1 -> erreur d'indice)

        return this.getButins().get(nbRandom);
    }

    /* SETTERS */
    public void addTresor(Tresor t) {
        this.butins.add(t);
        this.nbButin++;
    }

    public void removeTresor(Tresor t) {
        this.butins.remove(t);
        this.nbButin--;
    }
}


/*************************************
 *                                   *
 *          PARTIE ACTION            *
 *                                   *
 ************************************/

abstract class Action {
    protected Modele modele;
    protected Bandit bandit;
    protected Marshall marshall;
    VueCommandes vue;

    public Action(Modele m, VueCommandes v) {
        this.modele = m;
        this.vue = v;
    }

    public abstract void executer();

    /* SETTERS */
    public void setBandit(Bandit b) { this.bandit = b; }

    public void setMarshall(Marshall m) { this.marshall = m; }
}

/** DEPLACER */
class Deplacer extends Action {
    private Direction dir;

    public Deplacer (Modele m, VueCommandes v, Direction d) {
        super(m, v);
        this.dir = d;
    }

    @Override
    public void executer() {
        switch(dir) {
            case AVANT : avancer(); break;
            case ARRIERE : reculer(); break;
            case HAUT : monter(); break;
            case BAS : descendre(); break;
        }
        for (Bandit b : Modele.bandits) {
            Wagon banditWag = b.getPosition();

            // Si le bandit tombe sur le Marshall DANS un wagon
            if (banditWag.getNumWagon() == Modele.marshall.getNumWagon() && !b.estSurToit()) {
                vue.addMessage("\nLe Marshall est dans le même wagon que " + b.getNom() + " !\n" );

                // S'il a des butins, il va en lacher un au hasard
                if (b.nbTresorsRamasses() >= 1) {
                    Tresor tresorRandomBandit = b.getRandomTresorBandit();

                    b.perdreTresor(tresorRandomBandit);

                    vue.addMessage(b.getNom() + " a lâché le butin: " + tresorRandomBandit.getNom() +
                            " de la valeur de " + tresorRandomBandit.getValeur() + "$ dans le wagon " + banditWag.getNumWagon() + ".\n");
                }

                b.monterOuDescendre(); // En particulier, il montera sur le toit
                vue.addMessage( b.getNom() + " se retranche sur le toit.\n" );
            }
        }
        modele.notifyObservers();
    }

    /** Fait aller en "AVANT" le bandit (wagon N+1), si possible.
     *  Sinon, renvoie une erreur. */
    public void avancer() {
        Individu individu = (bandit != null) ? bandit : marshall; // Pour factoriser en partie le code
        int destination = individu.getNumWagon() - 1;

        if (destination <= 0) {
            if (individu instanceof Bandit)
                vue.addMessage(individu.getNom() + " est déjà sur le premier wagon.\n");
            else
                return; // Ne pas afficher "Marshall est deja sur le ___ wagon"

        } else {
            Wagon nvPos = Modele.train.getWagon(destination - 1); // comme numWagon est un indice i+1
            vue.addMessage(individu.getNom() + " infiltre le wagon " + destination + ".\n");

            if (individu instanceof Bandit)
                bandit.setPosition(nvPos);
            else
                marshall.setPosition(nvPos);
        }
    }

    /** Fait aller en "ARRIERE" le bandit (wagon N+1), si possible.
     *  Sinon, renvoie une erreur. */
    public void reculer() {
        Individu individu = (bandit != null) ? bandit : marshall; // Pour factoriser en partie le code
        int destination = individu.getNumWagon() + 1;

        if (destination > Modele.train.getNbWagons()) {
            if (individu instanceof Bandit)
                vue.addMessage(individu.getNom() + " est déjà sur le dernier wagon.\n");
            else
                return; // Ne pas afficher "Marshall est deja sur le ___ wagon"

        } else {
            Wagon nvPos = Modele.train.getWagon(destination - 1); // comme numWagon est un indice i+1
            vue.addMessage(individu.getNom() + " infiltre le wagon " + destination + ".\n");

            if (individu instanceof Bandit)
                bandit.setPosition(nvPos);
            else
                marshall.setPosition(nvPos);
        }
    }

    /** Fait aller en "HAUT" le bandit (wagon N+1), si possible.
     *  Sinon, renvoie une erreur. */
    public void monter() {
        if (bandit.estSurToit())
            vue.addMessage(bandit.getNom() + " est déjà sur le toit.\n");
        else {
            vue.addMessage(bandit.getNom() + " grimpe sur le toit.\n");
            bandit.monterOuDescendre();
        }
    }

    /** Fait aller en "BAS" le bandit (wagon N+1), si possible.
     *  Sinon, renvoie une erreur. */
    public void descendre() {
        if (!bandit.estSurToit())
            vue.addMessage(bandit.getNom() + " est déjà dans le wagon.\n");
        else {
            vue.addMessage(bandit.getNom() + " se faufile dans le wagon.\n");
            bandit.monterOuDescendre();
        }
    }

    public void setDir(Direction d) { this.dir = d; }
}


/** BRAQUER */
class Braquer extends Action {

    public Braquer (Modele m, VueCommandes v) {
        super(m, v);
    }

    @Override
    public void executer() {
        Wagon banditWag = this.bandit.getPosition();

        // Si le bandit est sur le toit, rien à faire.
        if (bandit.estSurToit()) {
            vue.addMessage(bandit.getNom() + " est sur le toit.\n");

            // Si le wagon contient toujours des tresors (+ bandit dans le wagon)
        } else if (banditWag.getNbButins() >= 1 && !bandit.estSurToit()) {
            Tresor tresorRandom = banditWag.getRandomTresor();

            bandit.volerTresor(tresorRandom);

            vue.addMessage(bandit.getNom() + " a récupéré le butin: " + tresorRandom.getNom() +
                    " de la valeur de " + tresorRandom.getValeur() + "$.\n");

        } else {
            vue.addMessage("Il n'y a rien à braquer, pour " + bandit.getNom() + ".\n");
        }
    }
}


/** TIRER */
class Tirer extends Action {
    private Direction dir;

    public Tirer (Modele m, VueCommandes v, Direction d) {
        super(m, v);
        this.dir = d;
    }

    @Override
    public void executer() {
        switch(dir) {
            case AVANT : tirerDevant(); break;
            case ARRIERE : tirerDerriere(); break;
            case HAUT : tirerEnHaut(); break;
            case BAS : tirerEnBas(); break;
        }
        modele.notifyObservers();
    }


    // "devant" <=> "avant" comme avancer.
    public void tirerDevant() {
        // Si le bandit n'a plus de balle, il ne peut pas tirer
        if (bandit.getNbBalle() == 0) {
            vue.addMessage(bandit.getNom() + " n'a plus d'ammunition !\n");
            return;
        }

        int numWagonCible = bandit.getNumWagon() - 1;
        bandit.decreaseNbBalle(); // Le bandit utilise 1 balle

        if (numWagonCible <= 0)
            vue.addMessage(bandit.getNom() + " a essayé de tirer en dehors de la locomotive !\n");

        else {
            Random r = new Random();

            // Que le bandit soit sur le toit ou non (= dans tous les cas), on tire sur le toit
            ArrayList<Bandit> banditsIci = bandit.getBanditsWag(numWagonCible, bandit.estSurToit()); // Liste des bandits du wagon/toit derriere
            int nbBanditsIci = banditsIci.size();

            // S'il y a un bandit sur qui tirer
            if (nbBanditsIci != 0) {
                int nbRandom = r.nextInt(0, nbBanditsIci);
                Bandit victime = banditsIci.get(nbRandom); // On choisit au hasard la victime

                // Si la victime a un tresor a perdre
                if (victime.nbTresorsRamasses() >= 1) {
                    Tresor tresorRandom = victime.getRandomTresorBandit();

                    victime.perdreTresor(tresorRandom);

                    vue.addMessage(victime.getNom() + " s'est fait tirer dessus par " + bandit.getNom() +
                            " et a fait tomber un " + tresorRandom.getNom() + " d'une valeur de " +
                            tresorRandom.getValeur() + "$ !\n");
                } else {
                    vue.addMessage(victime.getNom() + " s'est fait tirer dessus par " + bandit.getNom() +
                            " mais ne possède aucun trésor !\n");
                }
            } else
                vue.addMessage(bandit.getNom() + " n'a personne sur qui tirer !\n");
        }
    }

    // "derriere" <=> comme reculer.
    public void tirerDerriere() {
        // Si le bandit n'a plus de balle, il ne peut pas tirer
        if (bandit.getNbBalle() == 0) {
            vue.addMessage(bandit.getNom() + " n'a plus d'ammunition !\n");
            return;
        }

        int numWagonCible = bandit.getNumWagon() + 1;
        bandit.decreaseNbBalle(); // Le bandit utilise 1 balle

        if (numWagonCible > Modele.train.getNbWagons()) {
            vue.addMessage(bandit.getNom() + " a essayé de tirer en dehors du dernier wagon !\n");

        } else {
            Random r = new Random();

            // Que le bandit soit sur le toit ou non (= dans tous les cas), on tire sur le toit
            ArrayList<Bandit> banditsIci = bandit.getBanditsWag(numWagonCible, bandit.estSurToit()); // Liste des bandits du wagon/toit derriere
            int nbBanditsIci = banditsIci.size();

            // S'il y a un bandit sur qui tirer
            if (nbBanditsIci != 0) {
                int nbRandom = r.nextInt(0, nbBanditsIci);
                Bandit victime = banditsIci.get(nbRandom); // On choisit au hasard la victime

                // Si la victime a un tresor a perdre
                if (victime.nbTresorsRamasses() >= 1) {
                    Tresor tresorRandom = victime.getRandomTresorBandit();

                    victime.perdreTresor(tresorRandom);

                    vue.addMessage(victime.getNom() + " s'est fait tirer dessus par " + bandit.getNom() +
                            " et a fait tomber un " + tresorRandom.getNom() + " d'une valeur de " +
                            tresorRandom.getValeur() + "$ !\n");
                } else {
                    vue.addMessage(victime.getNom() + " s'est fait tirer dessus par " + bandit.getNom() +
                            " mais ne possède aucun trésor !\n");
                }
            } else
                vue.addMessage(bandit.getNom() + " n'a personne sur qui tirer !\n");
        }
    }

    public void tirerEnHaut() {
        // Si le bandit n'a plus de balle, il ne peut pas tirer
        if (bandit.getNbBalle() == 0) {
            vue.addMessage(bandit.getNom() + " n'a plus d'ammunition !\n");
            return;
        }

        Random r = new Random();
        bandit.decreaseNbBalle(); // Le bandit utilise 1 balle

        // Que le bandit soit sur le toit ou non (= dans tous les cas), on tire sur le toit
        ArrayList<Bandit> banditsIci = bandit.getBanditsWag(bandit.getNumWagon(), true); // Liste des bandits sur le toit du wagon
        int nbBanditsIci = banditsIci.size();

        // S'il y a un bandit sur qui tirer
        if (nbBanditsIci != 0) {
            int nbRandom = r.nextInt(0, nbBanditsIci);
            Bandit victime = banditsIci.get(nbRandom); // On choisit au hasard la victime

            // Si la victime a un tresor a perdre
            if (victime.nbTresorsRamasses() >= 1) {
                Tresor tresorRandom = victime.getRandomTresorBandit();

                victime.perdreTresor(tresorRandom);

                vue.addMessage(victime.getNom() + "s'est fait tirer dessus par " + bandit.getNom() +
                        " et a fait tomber un(e) " + tresorRandom.getNom() + " d'une valeur de " +
                        tresorRandom.getValeur() + "$ !\n");
            } else {
                vue.addMessage(victime.getNom() + " s'est fait tirer dessus par " + bandit.getNom() +
                        " mais ne possède aucun trésor !\n");
            }
        } else
            vue.addMessage(bandit.getNom() + " n'a personne sur qui tirer !\n");
    }

    public void tirerEnBas() {
        // Si le bandit n'a plus de balle, il ne peut pas tirer
        if (bandit.getNbBalle() == 0) {
            vue.addMessage(bandit.getNom() + " n'a plus d'ammunition !\n");
            return;
        }

        Random r = new Random();
        bandit.decreaseNbBalle(); // Le bandit utilise 1 balle

        // Que le bandit soit sur le toit ou non (= dans tous les cas), on tire dans le wagon
        ArrayList<Bandit> banditsIci = bandit.getBanditsWag(bandit.getNumWagon(), false); // Liste des bandits a l'interieur du wagon
        int nbBanditsIci = banditsIci.size();

        // S'il y a un bandit sur qui tirer
        if (nbBanditsIci != 0) {
            int nbRandom = r.nextInt(0, nbBanditsIci);
            Bandit victime = banditsIci.get(nbRandom); // On choisit au hasard la victime

            // Si la victime a un tresor a perdre
            if (victime.nbTresorsRamasses() >= 1) {
                Tresor tresorRandom = victime.getRandomTresorBandit();

                victime.perdreTresor(tresorRandom);

                vue.addMessage(victime.getNom() + " s'est fait tirer dessus par " + bandit.getNom() +
                        " et a fait tomber un " + tresorRandom.getNom() + " d'une valeur de " +
                        tresorRandom.getValeur() + "$ !\n");
            } else {
                vue.addMessage(victime.getNom() + " s'est fait tirer dessus par " + bandit.getNom() +
                        " mais ne possède aucun trésor !\n");
            }
        } else
            vue.addMessage(bandit.getNom() + " n'a personne sur qui tirer !\n");
    }
}


/*************************************
 *                                   *
 *          PARTIE INDIVIDU          *
 *                                   *
 ************************************/

class Individu {
    protected Modele modele;
    private final String nom;
    private boolean estToit;
    protected Wagon position;

    public Individu(Modele m, String nom, Boolean estToit, int numWagon) {
        this.modele = m;
        this.nom = nom;
        this.estToit = estToit;
        this.position = Modele.train.getWagon(numWagon-1);
    }

    public void monterOuDescendre() { estToit = !estToit; }

    /* GETTERS */
    public String getNom() { return nom; }

    public Wagon getPosition() { return this.position; }

    public int getNumWagon() { return position.getNumWagon(); }

    public boolean estSurToit() { return this.estToit; }

    /* SETTERS */
    public void setPosition(Wagon t) { this.position = t; }

}

/** BANDIT */
class Bandit extends Individu {
    private int somme;
    private int NB_BALLES;
    private ArrayList<Tresor> tresorRamasses;

    public Bandit(Modele modele, String nom, Boolean estToit, int numWagon, int nbBalle) {
        super(modele, nom, estToit, numWagon);
        this.somme = 0;
        this.tresorRamasses = new ArrayList<>();
        NB_BALLES = nbBalle;
    }

    public void ordered(Action a) {
        a.setBandit(this);
        a.executer();
    }

    // TRESORS
    public Tresor getRandomTresorBandit() {
        Random r = new Random();
        int nbTresor = this.nbTresorsRamasses();
        int nbRandom = r.nextInt(0, nbTresor); // Eviter le cas où r.nextInt(0, 0) doit être evalué (renvoie 1 -> erreur d'indice)

        return this.tresorRamasses.get(nbRandom);
    }

    public int nbTresorsRamasses() { return this.tresorRamasses.size(); }

    public void volerTresor(Tresor t) {
        this.position.removeTresor(t); // Le bandit vole le tresor du wagon
        this.tresorRamasses.add(t);
        this.addSomme(t.getValeur());
    }

    public void perdreTresor(Tresor t) {
        this.position.addTresor(t); // Le bandit echappe le tresor dans le wagon
        this.tresorRamasses.remove(t);
        this.decreaseSomme(t.getValeur());
    }

    // SOMME
    public void addSomme(int s) { this.somme += s; }

    public void decreaseSomme(int s) { this.somme -= s; }

    public int getSomme() { return this.somme; }

    // TIRER
    public int getNbBalle() { return this.NB_BALLES; }

    public void decreaseNbBalle() { this.NB_BALLES--; }

    // En supposant que le nom des joueurs soient differents lors d'une partie
    // Retourne la liste des bandits (bandit courant exclu) dans le wagon n et sur le toit ou non (selon estToit)
    public ArrayList<Bandit> getBanditsWag(int n, boolean estToit) {
        ArrayList<Bandit> listeBandit = new ArrayList<>();
        for (Bandit b : Modele.bandits) {
            if (b.getNumWagon() == n && b.estSurToit() == estToit && !Objects.equals(this.getNom(), b.getNom()))
                listeBandit.add(b);
        }
        return listeBandit;
    }
}


/** MARSHALL */
class Marshall extends Individu {
    private final double NERVOSITE_MARSHALL;
    private VueCommandes vue; // Pour pouvoir passer la vue a l'action Deplacer

    public Marshall(Modele m, String nom, Boolean estToit, int numWagon, double nervosite) {
        super(m, nom, estToit, numWagon);
        this.NERVOSITE_MARSHALL = nervosite;
    }

    public void ordered() {
        Random r = new Random();
        if (r.nextDouble() < NERVOSITE_MARSHALL) {
            Direction dir = r.nextBoolean() ? Direction.AVANT : Direction.ARRIERE;
            Action act = new Deplacer(modele, vue, dir);
            act.setMarshall(this);
            act.executer();
        }
    }

    public void setVueCommandes(VueCommandes v) { this.vue = v; }
}


/*************************************
 *                                   *
 *          PARTIE TRESOR            *
 *                                   *
 ************************************/

class Tresor {
    private final int valeur;
    private final String nom;

    public Tresor(int v, String nom) {
        this.valeur = v;
        this.nom = nom;
    }

    /* GETTERS */
    public int getValeur() { return this.valeur; }

    public String getNom() { return this.nom; }

}


/*************************************
 *                                   *
 *            PARTIE VUE             *
 *                                   *
 ************************************/

class Vue implements Observer {
    private JFrame frame;
    private VueTrain train;
    private VueCommandes commandes;


    /** Construction d'une vue attachée à un modèle. */
    public Vue(Modele modele) {
        frame = new JFrame();
        frame.setTitle("Jeu Colt Express");
        frame.getContentPane().setBackground(Color.CYAN);

        frame.setLayout(new BorderLayout());

        train = new VueTrain(modele);
        frame.add(train);

        commandes = new VueCommandes(modele);
        commandes.setBackground(new Color(255, 165, 0));
        frame.add(commandes, BorderLayout.SOUTH);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        modele.addObserver(this);
    }

    public void update() {
        train.repaint();
        commandes.repaint();
        frame.repaint();
    }
}

/** VUE BANDIT **/
class VueBandit {
    private Bandit bandit;

    public VueBandit(Bandit b){
        this.bandit = b;
    }

    public void affichage(Graphics g, int l, int h, int nb, int i) {
        int wagon = bandit.getNumWagon();
        int x = ((wagon - 1) * l)/nb + 60;
        int y = l/nb + h - 40 - i;
        //si le bandit est sur le toit
        if (bandit.estSurToit()) y -= (h-30);

        g.setColor(Color.RED);
        g.drawString(bandit.getNom(), x, y);
    }
}

/** VUE TOIGON  **/
class VueWagon {
    private Wagon wagon;
    private ArrayList<Tresor> butins;

    public VueWagon(Wagon t) {
        this.wagon = t;
        this.butins = t.getButins();
    }

    public void affichage(Graphics g, int x, int y, int nb) {
        int wag = wagon.getNumWagon();
        int xi = (wag-1) * x/nb + 50;
        int yi = x/nb;
        int width = x/nb;

        // Le wagon sera gris
        g.setColor(Color.GRAY);
        g.fillRect(xi, yi, width, y);

        // Dessiner le contour du wagon
        g.setColor(Color.BLACK);
        g.drawRect(xi, yi, width, y);

        // Dessiner les roues
        int rayonRoue = 60;
        int roueY = yi + 170;
        g.setColor(Color.BLACK);
        g.fillOval(xi + 30, roueY, rayonRoue, rayonRoue); // Roue gauche
        g.fillOval(xi + width - 110, roueY, rayonRoue, rayonRoue); // Roue droite

        int xTresor = ((wag)*x) / nb - 160;
        int i = 0;
        int nbButins = wagon.getNbButins();

        BufferedImage imgBourse = loadImage("bourse.png");
        BufferedImage imgBijou = loadImage("bijou.png");
        BufferedImage imgMagot = loadImage("magot.png");

        if (nbButins > 0) {
            int b = 0;

            for (Tresor t : butins){
                if ( b % (nbButins+1) == 0){
                    i = 0;
                    xTresor += 50;
                }
                int yTresor = x/nb + y - 40 - i;
                i += 40;


                if (Objects.equals(t.getNom(), "Bourse"))
                    g.drawImage(imgBourse, xTresor, yTresor, null);
                else if (Objects.equals(t.getNom(), "Bijou"))
                    g.drawImage(imgBijou, xTresor, yTresor, null);
                else
                    g.drawImage(imgMagot, xTresor, yTresor, null);

                b ++;
            }
        }
    }
    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}


/** VUE TRAIN **/
class VueTrain extends JPanel implements Observer  {
    protected final static int LARGEUR = 1400;
    protected final static int HAUTEUR = 200;

    private VueWagon[] vueWagons ;
    private VueBandit[] vueBandits ;

    public VueTrain(Modele modele) {
        // Wagons
        int nbWagons = Modele.train.getNbWagons();
        vueWagons = new VueWagon[nbWagons];

        for (int i = 0 ; i < nbWagons ; i++) {
            Wagon[] toiGons = Modele.train.getTrain();
            vueWagons[i] = new VueWagon(toiGons[i]);
        }

        // Bandits
        int nbBandits = Modele.NB_BANDITS;
        vueBandits = new VueBandit[nbBandits];

        int i = 0;
        for (Bandit b : Modele.bandits) {
            vueBandits[i] = new VueBandit(b);
            i++;
        }

        Dimension dim = new Dimension(LARGEUR*2, HAUTEUR*2);
        this.setPreferredSize(dim);

        modele.addObserver(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int nbWagons = Modele.train.getNbWagons();

        // ORDRE IMPORTANT
        // Dessiner le ciel en haut
        g.setColor(Color.CYAN);
        g.fillRect(0, 0, getWidth(), HAUTEUR*2);

        // Dessiner des nuages
        drawNuage(g, 0, 0);
        drawNuage(g, 350, 0);
        drawNuage(g, 850, 150);
        drawNuage(g, 1400, 120);

        // Dessiner le soleil
        g.setColor(new Color(255, 255, 0));
        g.fillOval(1690, 20, 150, 150);

        // Dessiner le sol en bas
        g.setColor(new Color(255, 165, 0));  // Orange/marron
        g.fillRect(0, HAUTEUR*2, getWidth(), HAUTEUR*2 + 200);

        // Un cactus des deux cotes
        drawCactus(g, 20, 710);
        drawCactus(g, 1600, 680);
        drawCactus(g, 1740, 650);

        // Wagons
        for (VueWagon v : vueWagons) {
            v.affichage(g, LARGEUR, HAUTEUR, nbWagons);
        }

        // Bandits
        int i = 0;
        for (VueBandit v : vueBandits) {
            v.affichage(g, LARGEUR, HAUTEUR, nbWagons, i);
            i += 15; // à modifier selon la taille du texte (représente l'espace entre les mots sur y)
        }

        // Marshall
        int marshallWagon = Modele.marshall.getNumWagon();
        int x = ((marshallWagon - 1)*LARGEUR)/nbWagons + LARGEUR/nbWagons;
        int y = LARGEUR / nbWagons + HAUTEUR - 60;
        g.setColor(Color.BLUE);
        g.drawString(Modele.marshall.getNom(), x, y);
    }

    public void update() { this.repaint(); }

    private void drawNuage(Graphics g, int dec, int rec) {
        g.setColor(Color.WHITE);
        g.fillOval(50 + dec, 30 + dec/4 - rec, 100, 50);
        g.fillOval(90 + dec, 10 + dec/4 - rec, 100, 50);
        g.fillOval(130 + dec, 30 + dec/4 - rec, 100, 50);
        g.fillOval(100 + dec, 40 + dec/4 - rec, 120, 50);
    }

    private void drawCactus(Graphics g, int x, int y) {
        g.setColor(new Color(34, 139, 34));

        g.fillRect(50 + x, 50 + y, 20, 100);

        g.fillArc(30 + x, 60 + y, 40, 40, 90, 180); // Branche gauche
        g.fillArc(50 + x, 20 + y, 40, 40, 270, 180); // Branche du haut
        g.fillArc(50 + x, 70 + y, 40, 40, 270, 180); // Branche droite
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}


/** VUE COMMANDES **/
class VueCommandes extends JPanel {
    protected final JButton boutonAction, boutonRetour;
    private final JButton boutonAvance, boutonRecule, boutonMonter, boutonDescendre, boutonBraquer, boutonTirer;
    private JLabel labelEtat, labelJoueurActuel;

    private JPanel panneauTir;
    private JButton boutonTirerDevant, boutonTirerArriere, boutonTirerHaut, boutonTirerBas;

    private JTextArea zoneTexte;

    private boolean toggleShoot = false;


    public VueCommandes(Modele modele) {
        Controleur ctrl = new Controleur(modele, this);

        // LABEL PHASE DU JEU
        labelEtat = new JLabel("Phase de planification");
        this.add(labelEtat);


        // ACTION
        boutonAction = new JButton("Action!");
        this.add(boutonAction);
        boutonAction.addActionListener(ctrl); // Enregistrement du contrôleur comme auditeur du bouton.
        boutonAction.setEnabled(false); // On désactive "Action !" au depart

        // RETOUR
        boutonRetour = new JButton("Retour");
        this.add(boutonRetour);
        boutonRetour.addActionListener(ctrl);
        boutonRetour.setEnabled(false); // On désactive "Retour " au depart

        // DEPLACEMENTS
        boutonAvance = new JButton("←");
        boutonRecule = new JButton("→");
        boutonMonter = new JButton("↑");
        boutonDescendre = new JButton("↓");

        boutonAvance.addActionListener(ctrl);
        boutonRecule.addActionListener(ctrl);
        boutonMonter.addActionListener(ctrl);
        boutonDescendre.addActionListener(ctrl);

        JPanel panneauDeplacement = new JPanel();
        panneauDeplacement.setLayout(new BoxLayout(panneauDeplacement, BoxLayout.Y_AXIS)); // Aligner les composants le long de l'axe Y

        // Ajout des boutons dans des panneaux séparés pour l'alignement
        JPanel topPanelDeplacement = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel middlePanelDeplacement = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel bottomPanelDeplacement = new JPanel(new FlowLayout(FlowLayout.CENTER));

        topPanelDeplacement.add(boutonMonter);
        middlePanelDeplacement.add(boutonAvance);
        middlePanelDeplacement.add(boutonRecule);
        bottomPanelDeplacement.add(boutonDescendre);

        // Ajout des sous-panneaux au panneau principal de déplacement
        panneauDeplacement.add(topPanelDeplacement);
        panneauDeplacement.add(middlePanelDeplacement);
        panneauDeplacement.add(bottomPanelDeplacement);

        this.add(panneauDeplacement);


        // BRAQUER
        boutonBraquer = new JButton("Braquer");
        this.add(boutonBraquer);
        boutonBraquer.addActionListener(ctrl);


        // TIRER
        boutonTirer = new JButton("Tirer");
        this.add(boutonTirer);
        boutonTirer.addActionListener(e -> togglePanneauTir());

        // Configuration du panneau de tir
        panneauTir = new JPanel();
        panneauTir.setLayout(new BoxLayout(panneauTir, BoxLayout.Y_AXIS)); // Aligner les composants le long de l'axe Y
        panneauTir.setVisible(false);

        boutonTirerHaut = new JButton("↟");
        boutonTirerBas = new JButton("↡");
        boutonTirerDevant = new JButton("↞");
        boutonTirerArriere = new JButton("↠");

        // Ajout des boutons dans des panneaux séparés pour l'alignement
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        topPanel.add(boutonTirerHaut);
        middlePanel.add(boutonTirerDevant);
        middlePanel.add(boutonTirerArriere);
        bottomPanel.add(boutonTirerBas);

        // On ajoute les sous-panneaux au panneau principal de tir
        panneauTir.add(topPanel);
        panneauTir.add(middlePanel);
        panneauTir.add(bottomPanel);

        // Ajout d'actions pour cacher le panneau après un clic
        ActionListener tirActionListener = e -> panneauTir.setVisible(false);
        boutonTirerDevant.addActionListener(tirActionListener);
        boutonTirerArriere.addActionListener(tirActionListener);
        boutonTirerHaut.addActionListener(tirActionListener);
        boutonTirerBas.addActionListener(tirActionListener);

        boutonTirerDevant.addActionListener(ctrl);
        boutonTirerArriere.addActionListener(ctrl);
        boutonTirerHaut.addActionListener(ctrl);
        boutonTirerBas.addActionListener(ctrl);

        this.add(panneauTir);

        enableKeyButtons(true); // On active les boutons de deplacements au depart


        // LABEL DU JOUEUR COURANT
        labelJoueurActuel = new JLabel("Tour de : " + Modele.bandits[0].getNom()); // Texte initial
        this.add(labelJoueurActuel);


        // ZONE DE TEXTE
        zoneTexte = new JTextArea(10, 39); // 10 lignes de haut ; 37 caractères de large
        zoneTexte.setEditable(false);
        zoneTexte.setFocusable(false); // Pour que les fleches du clavier puissent etre detectees
        JScrollPane scrollPane = new JScrollPane(zoneTexte); // Scroll pane pour la zone de texte
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(scrollPane);


        // CONFIGURATION RACCOURCIS CLAVIERS
        setupKeyBindings();
    }

    // Eteint ou allume le panneau de tir selon sa visibilite actuelle
    private void togglePanneauTir() {
        panneauTir.setVisible(!panneauTir.isVisible());
    }

    public void enableKeyButtons(boolean enable) {
        boutonAvance.setEnabled(enable);
        boutonRecule.setEnabled(enable);
        boutonMonter.setEnabled(enable);
        boutonDescendre.setEnabled(enable);
        boutonBraquer.setEnabled(enable);
        boutonTirer.setEnabled(enable);
    }

    // Label etat du jeu courant
    public void setEtatText(String text) { this.labelEtat.setText(text); }

    // Label joueur courant
    public void setJoueurActuel(String nom) { labelJoueurActuel.setText("Tour de : " + nom); } // On met à jour le label

    public JButton getBoutonAction() { return this.boutonAction; }

    // Pour afficher quelque chose dans la zone de texte
    public void addMessage(String message) { zoneTexte.append(message); }

    private void setupKeyBindings() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        // ACTION
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "action");
        actionMap.put("action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonAction.doClick();
            }
        });

        // ACTION
        inputMap.put(KeyStroke.getKeyStroke('R', 0), "retour");
        actionMap.put("retour", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonRetour.doClick();
            }
        });

        // MOUVEMENTS
        inputMap.put(KeyStroke.getKeyStroke('Q', 0), "devant");
        actionMap.put("devant", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonAvance.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke('D', 0), "derriere");
        actionMap.put("derriere", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonRecule.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke('Z', 0), "enHaut");
        actionMap.put("enHaut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonMonter.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke('S', 0), "enBas");
        actionMap.put("enBas", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonDescendre.doClick();
            }
        });


        // BRAQUER
        inputMap.put(KeyStroke.getKeyStroke('B', 0), "braquer");
        actionMap.put("braquer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boutonBraquer.doClick();
            }
        });


        // TIRER
        inputMap.put(KeyStroke.getKeyStroke('T', 0), "tirer");
        actionMap.put("tirer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePanneauTir();
                toggleShoot = !toggleShoot; // On switch l'etat : le joueur peut maintenant tirer
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT , 0), "tirerDevant");
        actionMap.put("tirerDevant", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (toggleShoot) {
                    boutonTirerDevant.doClick();
                    toggleShoot = !toggleShoot; // Fleches desactivees jusqu'au prochain 'T'
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT , 0), "tirerArriere");
        actionMap.put("tirerArriere", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (toggleShoot) {
                    boutonTirerArriere.doClick();
                    toggleShoot = !toggleShoot; // Fleches desactivees jusqu'au prochain 'T'
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "tirerEnHaut");
        actionMap.put("tirerEnHaut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (toggleShoot) {
                    boutonTirerHaut.doClick();
                    toggleShoot = !toggleShoot; // Fleches desactivees jusqu'au prochain 'T'
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "tirerEnBas");
        actionMap.put("tirerEnBas", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (toggleShoot) {
                    boutonTirerBas.doClick();
                    toggleShoot = !toggleShoot; // Fleches desactivees jusqu'au prochain 'T'
                }
            }
        });
    }

}

class Controleur implements ActionListener {
    private final Modele modele;
    protected int joueur = 0;
    protected int numAction = 0;
    private VueCommandes vue;
    private boolean etatPlanification = true;

    public Controleur(Modele modele, VueCommandes v) {
        this.modele = modele;
        this.vue = v;
        Modele.marshall.setVueCommandes(v);
    }

    private void updateBoutons() {
        vue.getBoutonAction().setEnabled(!etatPlanification);
        vue.enableKeyButtons(etatPlanification);

        String etatText = etatPlanification ? "Phase de planification" : "Phase d'action";
        vue.setEtatText(etatText);
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (etatPlanification) {
            vue.boutonAction.setEnabled(false);
            vue.enableKeyButtons(true);

            if (joueur == 0)
                vue.setJoueurActuel(Modele.bandits[0].getNom());

            // Si c'est pas le dernier joueur et que sa derniere action a ete choisie
            if (joueur + 1 < Modele.NB_BANDITS && numAction + 1 == Modele.NB_ACTIONS)
                vue.setJoueurActuel(Modele.bandits[joueur + 1].getNom());

            if (numAction < Modele.NB_ACTIONS) {
                gerePlanification(action);
            }

            vue.boutonRetour.setEnabled(numAction >= 1);

            // Une fois toutes les actions effectuées : état planification -> état d'action
            if (numAction == Modele.NB_ACTIONS) {

                if ("Retour".equals(action)) {
                    gerePlanification(action);

                } else if ("Action!".equals(action)) {
                    vue.addMessage("Les actions du joueur : " + Modele.bandits[joueur].getNom() + " ont été validées.\n");
                    numAction = 0;
                    joueur++;
                    vue.boutonRetour.setEnabled(false);
                    vue.addMessage("==========================================================\n");

                } else {
                    vue.boutonAction.setEnabled(true);
                    vue.enableKeyButtons(false);
                }

                if (joueur == Modele.NB_BANDITS) {
                    this.etatPlanification = false;
                    updateBoutons();
                }
            }
        } else {
            vue.boutonRetour.setEnabled(false);
            joueur = 0;
            vue.setJoueurActuel(Modele.bandits[joueur].getNom()); // MAJ du label avec le nom du joueur actuel
            if ("Action!".equals(action))
                gereAction();
        }
    }

    private void gerePlanification(String action) {
        switch (action) {
            case "Retour":
                numAction -= 2;
                break;
            case "←":
                Modele.actions[joueur][numAction] = new Deplacer(modele, vue, Direction.AVANT);
                break;
            case "→":
                Modele.actions[joueur][numAction] = new Deplacer(modele, vue, Direction.ARRIERE);
                break;
            case "↑":
                Modele.actions[joueur][numAction] = new Deplacer(modele, vue, Direction.HAUT);
                break;
            case "↓":
                Modele.actions[joueur][numAction] = new Deplacer(modele, vue, Direction.BAS);
                break;
            case "Braquer":
                Modele.actions[joueur][numAction] = new Braquer(modele, vue);
                break;
            case "↞":
                Modele.actions[joueur][numAction] = new Tirer(modele, vue, Direction.AVANT);
                break;
            case "↠":
                Modele.actions[joueur][numAction] = new Tirer(modele, vue, Direction.ARRIERE);
                break;
            case "↟":
                Modele.actions[joueur][numAction] = new Tirer(modele, vue, Direction.HAUT);
                break;
            case "↡":
                Modele.actions[joueur][numAction] = new Tirer(modele, vue, Direction.BAS);
                break;
        }
        if (!action.equals("Retour"))
            vue.addMessage("Action " + (numAction + 1) + " du joueur " + Modele.bandits[joueur].getNom() + " défini : " + action + "\n");
        else
            vue.addMessage("Action " + (numAction + 2) + " du joueur " + Modele.bandits[joueur].getNom() + " a été supprimé.\n");

        numAction++;
    }

    private void gereAction() {
        // Le marshall se déplace avant chaque action du bandit
        Modele.marshall.ordered();

        // Pour chaque bandit, on leur fait jouer leurs actions
        for (Bandit b : Modele.bandits) {
            b.ordered(Modele.actions[joueur][numAction]);
            if (joueur < Modele.NB_BANDITS - 1)
                joueur++;
        }
        numAction ++;
        vue.addMessage("\n");

        if (numAction == Modele.NB_ACTIONS) {
            vue.addMessage("==========================================================\n");
            for (Bandit b : Modele.bandits)
                vue.addMessage("Somme des butins de " + b.getNom() + ": " + b.getSomme() + "$.\n");
            vue.addMessage("==========================================================\n");

            this.etatPlanification = true; // On revient : état d'action -> état planification
            updateBoutons();
            joueur = 0;
            numAction = 0;
        }
    }

}
