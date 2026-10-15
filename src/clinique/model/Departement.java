package clinique.model;

public class Departement {
    private int idDep;
    private String nomDep;
    private int etage;
    private int nbMedecins;

    public Departement(int idDep, String nomDep, int etage, int nbMedecins) {
        this.idDep = idDep;
        this.nomDep = nomDep;
        this.etage = etage;
        this.nbMedecins = nbMedecins;
    }

    public int getIdDep() { return idDep; }
    public String getNomDep() { return nomDep; }
    public int getEtage() { return etage; }
    public int getNbMedecins() { return nbMedecins; }

    @Override
    public String toString() {
        return nomDep + " (Étage " + etage + ")";
    }
}