package clinique.model;

import java.util.List;

public class Consultation {
    private int numCons;
    private String dateCons;
    private String motif;
    private String statut;
    private double tarif;
    private List<String> medicaments; // Issu du VARRAY medicaments

    public Consultation(int numCons, String dateCons, String motif, String statut, double tarif, List<String> medicaments) {
        this.numCons = numCons;
        this.dateCons = dateCons;
        this.motif = motif;
        this.statut = statut;
        this.tarif = tarif;
        this.medicaments = medicaments;
    }

    public int getNumCons() { return numCons; }
    public String getDateCons() { return dateCons; }
    public String getMotif() { return motif; }
    public String getStatut() { return statut; }
    public double getTarif() { return tarif; }
    public List<String> getMedicaments() { return medicaments; }
}