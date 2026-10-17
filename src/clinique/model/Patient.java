package clinique.model;

import java.util.List;

public class Patient {
    private int idPat;
    private String nomPat;
    private String dateNaissance;
    private String sexe;
    private int age; 
    private String rue;
    private String ville;
    private int codePostal;
    private List<String> tels; 
    private String nomMedecin;

    public Patient(int idPat, String nomPat, String dateNaissance, String sexe, int age, 
                   String rue, String ville, int codePostal, List<String> tels, String nomMedecin) {
        this.idPat = idPat;
        this.nomPat = nomPat;
        this.dateNaissance = dateNaissance;
        this.sexe = sexe;
        this.age = age;
        this.rue = rue;
        this.ville = ville;
        this.codePostal = codePostal;
        this.tels = tels;
        this.nomMedecin = nomMedecin;
    }

    public int getIdPat() { return idPat; }
    public String getNomPat() { return nomPat; }
    public String getDateNaissance() { return dateNaissance; }
    public String getSexe() { return sexe; }
    public int getAge() { return age; }
    public String getRue() { return rue; }
    public String getVille() { return ville; }
    public int getCodePostal() { return codePostal; }
    public List<String> getTels() { return tels; }
    public String getNomMedecin() { return nomMedecin; }
}