package ma.projet.service;

import ma.projet.classes.EmployeTache;
import ma.projet.classes.Projet;
import ma.projet.classes.Tache;
import ma.projet.dao.IDao;
import ma.projet.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProjetService implements IDao<Projet> {

    @Override
    public boolean create(Projet o) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(o);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Projet o) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(o);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Projet o) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.delete(session.contains(o) ? o : session.merge(o));
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Projet findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Projet.class, id);
        }
    }

    @Override
    public List<Projet> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Projet", Projet.class).list();
        }
    }

    /** Tâches planifiées (dates prévues) pour un projet donné. */
    public List<Tache> getTachesPlanifiees(int projetId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Tache t WHERE t.projet.id = :pid ORDER BY t.dateDebut",
                    Tache.class)
                    .setParameter("pid", projetId)
                    .list();
        }
    }

    /**
     * Affiche les tâches réalisées (dates réelles) pour un projet, dans le format
     * demandé dans l'énoncé.
     */
    public void afficherTachesRealisees(int projetId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Projet projet = session.get(Projet.class, projetId);
            if (projet == null) {
                System.out.println("Projet introuvable (id=" + projetId + ")");
                return;
            }

            SimpleDateFormat longFmt  = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
            SimpleDateFormat shortFmt = new SimpleDateFormat("dd/MM/yyyy");

            System.out.printf("Projet : %-6d Nom : %-25s Date début : %s%n",
                    projet.getId(), projet.getNom(), longFmt.format(projet.getDateDebut()));
            System.out.println("Liste des tâches:");
            System.out.printf("%-4s %-20s %-20s %-20s%n",
                    "Num", "Nom", "Date Début Réelle", "Date Fin Réelle");

            List<EmployeTache> items = session.createQuery(
                    "FROM EmployeTache et WHERE et.tache.projet.id = :pid ORDER BY et.dateDebutReelle",
                    EmployeTache.class)
                    .setParameter("pid", projetId)
                    .list();

            if (items.isEmpty()) {
                System.out.println("  (aucune tâche réalisée enregistrée)");
            }
            for (EmployeTache et : items) {
                System.out.printf("%-4d %-20s %-20s %-20s%n",
                        et.getTache().getId(),
                        et.getTache().getNom(),
                        shortFmt.format(et.getDateDebutReelle()),
                        shortFmt.format(et.getDateFinReelle()));
            }
        }
    }
}
