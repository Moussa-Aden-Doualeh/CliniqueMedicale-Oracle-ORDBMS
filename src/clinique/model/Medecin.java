package clinique.model;

public class Medecin {
    private int idMed;
    private String nomMed;
    private String specialite;
    private double salaireBase;
    private String nomDepartement; 
    private String horaires;
    private String email;

    public Medecin(int idMed, String nomMed, String specialite, double salaireBase, String nomDepartement, String horaires, String email) {
        this.idMed = idMed;
        this.nomMed = nomMed;
        this.specialite = specialite;
        this.salaireBase = salaireBase;
        this.nomDepartement = nomDepartement;
        this.horaires = horaires;
        this.email = email;
    }

    public int getIdMed() { return idMed; }
    public String getNomMed() { return nomMed; }
    public String getSpecialite() { return specialite; }
    public double getSalaireBase() { return salaireBase; }
    public String getNomDepartement() { return nomDepartement; }
    public String getHoraires() { return horaires; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return nomMed + " (" + specialite + ")";
    }
}