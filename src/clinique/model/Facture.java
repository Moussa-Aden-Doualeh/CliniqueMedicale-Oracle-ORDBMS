package clinique.model;

public class Facture {
    private int numFacture;
    private String dateFacture;
    private double montant;
    private String nomPatient;       // Issu de f.patientConcerne.nomPat (REF)
    private String villeFacturation; // Issu de f.patientConcerne.adr.ville (TAD via REF)
    private String nomMedecin;       // Issu de f.medecinEmetteur.nomMed (REF)

    public Facture(int numFacture, String dateFacture, double montant, String nomPatient, String villeFacturation, String nomMedecin) {
        this.numFacture = numFacture;
        this.dateFacture = dateFacture;
        this.montant = montant;
        this.nomPatient = nomPatient;
        this.villeFacturation = villeFacturation;
        this.nomMedecin = nomMedecin;
    }

    public int getNumFacture() { return numFacture; }
    public String getDateFacture() { return dateFacture; }
    public double getMontant() { return montant; }
    public String getNomPatient() { return nomPatient; }
    public String getVilleFacturation() { return villeFacturation; }
    public String getNomMedecin() { return nomMedecin; }
}