import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class Tests {

    private Modele modele;
    private VueCommandes vue;
    private Bandit b1, b2;
    private Marshall m;

    public static void testDeplacement(Bandit b, Deplacer d, Direction dir) {
        d.setDir(dir);
        b.ordered(d);
        System.out.println("Wagon: " + b.getNumWagon() + " \nToit : " + b.estSurToit() + "\n") ;
    }

    public static void main(String[] args) {
        ArrayList<String> s = new ArrayList<>(2);
        s.add("bandit1");
        s.add("bandit2");
        Modele modele = new Modele(2, s, 3, 3, 3, 0.3);
        VueCommandes vue = new VueCommandes(modele);
        // Tests unitaires pour les actions des bandies (Partie 1)
        Bandit b1 = new Bandit(modele, "1", true, 1, 3);
        Bandit b2 = new Bandit(modele, "2", false, 2, 3);
        Bandit b3 = new Bandit(modele, "3", true, 3, 3);

        Deplacer d = new Deplacer(modele, vue, Direction.HAUT);

        Direction dir1 = Direction.HAUT;
        Direction dir2 = Direction.BAS;
        Direction dir3 = Direction.AVANT;
        Direction dir4 = Direction.ARRIERE;

        // Nombre de wagons : 4
        /*
        testDeplacement(b1, d, dir3); // Toit 2 -> Toit 1
        testDeplacement(b2, d, dir4); // Toit 4 -> Toit 5 (impossible, dernier wagon)
        testDeplacement(b2, d, dir2); // Wagon 4 BAS (impossible, deja dans wagon)
        testDeplacement(b3, d, dir3); // Toit 1 -> Toit 0 (impossible, premier wagon)
        testDeplacement(b1, d, dir1); // Wagon 1 HAUT (impossible, deja sur toit)
        */
    }

    @Before
    public void setUp() {
        ArrayList<String> s = new ArrayList<>(2);
        s.add("bandit1");
        s.add("bandit2");
        modele = new Modele(2, s, 3, 3, 4, 0.3);        // Initialisation avec les bandits en position contrôlée pour tester
        vue = new VueCommandes(modele);

        b1 = new Bandit(modele, s.get(0), true, 1, 3); // Sur le toit du premier wagon
        b2 = new Bandit(modele, s.get(1), false, Modele.train.getNbWagons(), 3); // Dans le dernier wagon
        m = new Marshall(modele, "Marshall1", false, 2, 0.3);
    }

    /*************************************
     *                                   *
     *     DEPLACEMENTS IMPOSSIBLES      *
     *                                   *
     ************************************/

    // AVANCER DU WAGON 1
    @Test
    public void testAvancerPremierWagon() {
        b1.ordered(new Deplacer(modele, vue, Direction.AVANT));
        assertEquals("Le bandit devrait rester sur le premier wagon", 1, b1.getNumWagon());
        assertTrue("Le bandit devrait rester sur le toit", b1.estSurToit());
    }

    // RECULER DU WAGON N
    @Test
    public void testReculerDernierWagon() {
        b2.ordered(new Deplacer(modele,  vue, Direction.ARRIERE));
        assertEquals("Le bandit devrait rester dans le dernier wagon", 4, b2.getNumWagon());
        assertFalse("Le bandit devrait être dans le wagon", b2.estSurToit());
    }

    // ALLER EN HAUT DU TOIT
    @Test
    public void testDeplacementHautImpossible() {
        b1.ordered(new Deplacer(modele,  vue, Direction.HAUT));
        assertEquals("Le bandit devrait rester sur le premier wagon", 1, b1.getNumWagon());
        assertTrue("Le bandit devrait rester sur le toit", b1.estSurToit());
    }

    // ALLER EN BAS DU WAGON
    @Test
    public void testDeplacementBasImpossible() {
        b2.ordered(new Deplacer(modele,  vue, Direction.BAS));
        assertEquals("Le bandit devrait rester dans le dernier wagon", 4, b2.getNumWagon());
        assertFalse("Le bandit devrait rester dans le wagon", b2.estSurToit());
    }


    /*************************************
     *                                   *
     *       DEPLACEMENTS LAMBDAS        *
     *                                   *
     ************************************/

    // RECULER WAGON 1 -> 2
    @Test
    public void testReculerPremierWagon() {
        b1.ordered(new Deplacer(modele,  vue, Direction.ARRIERE));
        assertEquals("Le bandit devrait aller au deuxième wagon", 2, b1.getNumWagon());
        assertTrue("Le bandit devrait être sur le toit", b1.estSurToit());
    }

    // AVANCER WAGON N -> N-1
    @Test
    public void testAvancerDernierWagon() {
        b2.ordered(new Deplacer(modele,  vue, Direction.AVANT));
        assertEquals("Le bandit devrait aller au deuxième wagon", 3, b2.getNumWagon());
        assertFalse("Le bandit devrait être sur le toit", b2.estSurToit());
    }

}

