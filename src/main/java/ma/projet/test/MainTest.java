package ma.projet.test;

import ma.projet.classes.*;
import ma.projet.service.*;
import ma.projet.util.HibernateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MainTest {

    static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    public static void main(String[] args) throws ParseException {

        ProjetService      projetService      = new ProjetService();
        TacheService       tacheService       = new TacheService();
        EmployeService     employeService     = new EmployeService();
        EmployeTacheService employeTacheService = new EmployeTacheService();

        // ── 1. Créer les employés ─────────────────────────────────────────────
        Employe emp1 = new Employe("Alami",   "Youssef", "0661234567");
        Employe emp2 = new Employe("Benali",  "Fatima",  "0672345678");
        Employe emp3 = new Employe("Chakir",  "Omar",    "0683456789");
        employeService.create(emp1);
        employeService.create(emp2);
        employeService.create(emp3);

        // ── 2. Créer les projets ──────────────────────────────────────────────
        Projet p1 = new Projet("Gestion de stock",
                SDF.parse("14/01/2013"), SDF.parse("30/06/2013"), emp1);
        Projet p2 = new Projet("Gestion RH",
                SDF.parse("01/03/2013"), SDF.parse("31/12/2013"), emp2);
        projetService.create(p1);
        projetService.create(p2);

        // ── 3. Créer les tâches du projet 1 ──────────────────────────────────
        Tache t1 = new Tache("Analyse",       SDF.parse("01/02/2013"), SDF.parse("28/02/2013"),  800.0, p1);
        Tache t2 = new Tache("Conception",    SDF.parse("01/03/2013"), SDF.parse("31/03/2013"), 1200.0, p1);
        Tache t3 = new Tache("Développement", SDF.parse("01/04/2013"), SDF.parse("30/04/2013"), 2500.0, p1);
        Tache t4 = new Tache("Tests",         SDF.parse("01/05/2013"), SDF.parse("31/05/2013"),  600.0, p1);

        // ── 4. Créer les tâches du projet 2 ──────────────────────────────────
        Tache t5 = new Tache("Analyse des besoins", SDF.parse("01/03/2013"), SDF.parse("31/03/2013"), 1500.0, p2);
        Tache t6 = new Tache("Développement",       SDF.parse("01/05/2013"), SDF.parse("30/09/2013"), 3000.0, p2);
        Tache t7 = new Tache("Recette",             SDF.parse("01/10/2013"), SDF.parse("31/10/2013"),  900.0, p2);

        tacheService.create(t1); tacheService.create(t2); tacheService.create(t3);
        tacheService.create(t4); tacheService.create(t5); tacheService.create(t6);
        tacheService.create(t7);

        // ── 5. Affecter des employés aux tâches (EmployeTache) ───────────────
        EmployeTache et1 = new EmployeTache(SDF.parse("10/02/2013"), SDF.parse("20/02/2013"), emp1, t1);
        EmployeTache et2 = new EmployeTache(SDF.parse("10/03/2013"), SDF.parse("15/03/2013"), emp2, t2);
        EmployeTache et3 = new EmployeTache(SDF.parse("10/04/2013"), SDF.parse("25/04/2013"), emp3, t3);
        EmployeTache et4 = new EmployeTache(SDF.parse("01/03/2013"), SDF.parse("05/04/2013"), emp1, t5);
        EmployeTache et5 = new EmployeTache(SDF.parse("10/05/2013"), SDF.parse("20/09/2013"), emp2, t6);

        employeTacheService.create(et1); employeTacheService.create(et2);
        employeTacheService.create(et3); employeTacheService.create(et4);
        employeTacheService.create(et5);

        separator("TEST 1 – ProjetService.afficherTachesRealisees(projetId)");
        projetService.afficherTachesRealisees(p1.getId());

        separator("TEST 2 – ProjetService.getTachesPlanifiees(projetId)");
        List<Tache> planifiees = projetService.getTachesPlanifiees(p1.getId());
        System.out.printf("%-4s %-20s %-12s %-12s %8s%n", "Id", "Nom", "Début prévu", "Fin prévue", "Prix (DH)");
        for (Tache t : planifiees) {
            System.out.printf("%-4d %-20s %-12s %-12s %8.2f%n",
                    t.getId(), t.getNom(),
                    SDF.format(t.getDateDebut()), SDF.format(t.getDateFin()),
                    t.getPrix());
        }

        separator("TEST 3 – TacheService.getTachesParPrix(1000) [requête nommée]");
        List<Tache> cheres = tacheService.getTachesParPrix(1000.0);
        System.out.println("Tâches dont le prix > 1000 DH :");
        for (Tache t : cheres) {
            System.out.printf("  • %-25s %.2f DH  (Projet: %s)%n",
                    t.getNom(), t.getPrix(), t.getProjet().getNom());
        }

        separator("TEST 4 – TacheService.getTachesEntreDeuxDates(01/03/2013, 30/04/2013)");
        List<EmployeTache> entreDates = tacheService.getTachesEntreDeuxDates(
                SDF.parse("01/03/2013"), SDF.parse("30/04/2013"));
        System.out.println("Tâches réalisées entre le 01/03/2013 et le 30/04/2013 :");
        for (EmployeTache et : entreDates) {
            System.out.printf("  • Tâche: %-20s | Début réel: %s | Fin réelle: %s | Employé: %s %s%n",
                    et.getTache().getNom(),
                    SDF.format(et.getDateDebutReelle()),
                    SDF.format(et.getDateFinReelle()),
                    et.getEmploye().getNom(),
                    et.getEmploye().getPrenom());
        }

        separator("TEST 5 – EmployeService.getTachesRealisees(employeId)");
        List<Tache> tachesEmp1 = employeService.getTachesRealisees(emp1.getId());
        System.out.println("Tâches réalisées par " + emp1.getNom() + " " + emp1.getPrenom() + " :");
        for (Tache t : tachesEmp1) {
            System.out.println("  • " + t.getNom() + " (" + t.getProjet().getNom() + ")");
        }

        separator("TEST 6 – EmployeService.getProjetsGeres(employeId)");
        List<Employe> tousEmployes = employeService.findAll();
        for (Employe emp : tousEmployes) {
            List<Projet> projets = employeService.getProjetsGeres(emp.getId());
            System.out.println("Projets gérés par " + emp.getNom() + " " + emp.getPrenom() + " :");
            if (projets.isEmpty()) {
                System.out.println("  (aucun)");
            } else {
                projets.forEach(p -> System.out.println("  • " + p.getNom()));
            }
        }

        separator("TEST 7 – findAll sur chaque entité");
        System.out.println("Employés  : " + employeService.findAll().size());
        System.out.println("Projets   : " + projetService.findAll().size());
        System.out.println("Tâches    : " + tacheService.findAll().size());
        System.out.println("Affectations : " + employeTacheService.findAll().size());

        HibernateUtil.shutdown();
    }

    private static void separator(String title) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  " + title);
        System.out.println("=".repeat(70));
    }
}
