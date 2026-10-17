// PROJET BASES DE DONNÉES AVANCÉES : CLINIQUE MEDICAL CENTER


// 1. CRÉATION DES TYPES ABSTRAITS DE DONNÉES (TAD) & COLLECTIONS


// A. TAD Composite : Adresse

CREATE OR REPLACE TYPE adresse_t AS OBJECT (
    rue VARCHAR2(50),
    ville VARCHAR2(50),
    codePostal NUMBER(5)
)
/

//B. TAD Département médical

CREATE OR REPLACE TYPE departement_t AS OBJECT (
    idDep NUMBER,
    nomDep VARCHAR2(30),
    etage NUMBER
)
/

// C. TAD Médecin (avec email, horaires, REF vers département et méthode)

CREATE OR REPLACE TYPE medecin_t AS OBJECT (
    idMed NUMBER,
    nomMed VARCHAR2(30),
    specialite VARCHAR2(30),
    salaireBase NUMBER,
    dept REF departement_t,         -- Référence vers la table Departements
    email VARCHAR2(50),             -- Nouvel attribut
    horaires VARCHAR2(50),          -- Nouvel attribut (ex: "08h00 - 14h00")
    
    -- Méthode associée au TAD
    MEMBER FUNCTION calculer_prime(taux NUMBER) RETURN NUMBER
)
/

CREATE OR REPLACE TYPE BODY medecin_t AS
    MEMBER FUNCTION calculer_prime(taux NUMBER) RETURN NUMBER IS
    BEGIN
        RETURN (self.salaireBase * taux);
    END;
END;
/

// D. COLLECTION : VARRAY pour les Médicaments prescrits (Ordonnance, max 5)

CREATE OR REPLACE TYPE prescriptions_va AS VARRAY(5) OF VARCHAR2(50)
/

// E. TAD Consultation (avec statut RDV/Consultation et le VARRAY de médicaments)

CREATE OR REPLACE TYPE consultation_t AS OBJECT (
    numCons NUMBER,
    dateCons DATE,
    motif VARCHAR2(100),
    tarif NUMBER,
    statut VARCHAR2(20),             -- 'Planifié' (Rendez-vous) ou 'Terminé' (Consultation)
    medicaments prescriptions_va     -- IMBRICATION DE COLLECTION : VARRAY dans Nested Table !
)
/

// F. COLLECTION : VARRAY pour les téléphones d'urgence du patient (max 3)

CREATE OR REPLACE TYPE telephones_va AS VARRAY(3) OF VARCHAR2(15)
/

// G. COLLECTION : NESTED TABLE pour l'historique des consultations

CREATE OR REPLACE TYPE consultations_nt AS TABLE OF consultation_t
/

-- H. TAD Patient (avec sexe, adresse TAD, telephones VARRAY, historique NESTED TABLE et REF)
CREATE OR REPLACE TYPE patient_t AS OBJECT (
    idPat NUMBER,
    nomPat VARCHAR2(30),
    dateNaissance DATE,
    sexe VARCHAR2(1),                
    adr adresse_t,                   
    tels telephones_va,           
    historique consultations_nt,    
    medecinTraitant REF medecin_t,  
    
    -- Méthode associée au TAD
    MEMBER FUNCTION calculer_age RETURN NUMBER
)
/

CREATE OR REPLACE TYPE BODY patient_t AS
    MEMBER FUNCTION calculer_age RETURN NUMBER IS
    BEGIN
        RETURN TRUNC(MONTHS_BETWEEN(SYSDATE, self.dateNaissance) / 12);
    END;
END;
/


// 2. CRÉATION DES TABLES TYPÉES ET NON TYPÉES

// A. Table Typée des Départements

CREATE TABLE Departements OF departement_t (
    CONSTRAINT pk_dep PRIMARY KEY (idDep)
);

// B. Table Typée des Médecins

CREATE TABLE Medecins OF medecin_t (
    CONSTRAINT pk_med PRIMARY KEY (idMed)
);

// C. Table Typée des Patients

CREATE TABLE Patients OF patient_t (
    CONSTRAINT pk_pat PRIMARY KEY (idPat)
)

NESTED TABLE historique STORE AS tab_consultations_stockage;

// D. Table NON TYPÉE des Factures

CREATE TABLE Factures (
    numFacture NUMBER PRIMARY KEY,
    dateFacture DATE NOT NULL,
    montant NUMBER NOT NULL,
    patientConcerne REF patient_t,
    medecinEmetteur REF medecin_t,
    numCons NUMBER
);




// 3. INSERTIONS DE DONNÉES : CLINIQUE MEDICAL CENTER (10 LIGNES PAR TABLE)

// A. TABLE TYPÉE : DEPARTEMENTS (10 LIGNES)

INSERT INTO Departements VALUES (departement_t(1, 'Cardiologie', 2));
INSERT INTO Departements VALUES (departement_t(2, 'Pédiatrie', 1));
INSERT INTO Departements VALUES (departement_t(3, 'Neurologie', 3));
INSERT INTO Departements VALUES (departement_t(4, 'Orthopédie', 2));
INSERT INTO Departements VALUES (departement_t(5, 'Dermatologie', 1));
INSERT INTO Departements VALUES (departement_t(6, 'Ophtalmologie', 1));
INSERT INTO Departements VALUES (departement_t(7, 'Gynécologie', 3));
INSERT INTO Departements VALUES (departement_t(8, 'Radiologie', 2));
INSERT INTO Departements VALUES (departement_t(9, 'Urologie', 2));
INSERT INTO Departements VALUES (departement_t(10, 'Oncologie', 4));



// B. TABLE TYPÉE : MEDECINS (10 LIGNES AVEC EMAIL ET HORAIRES)

INSERT INTO Medecins VALUES (
    medecin_t(10, 'Dr. Mansour', 'Cardiologue', 3500, (SELECT REF(d) FROM Departements d WHERE d.idDep = 1), 'dr.mansour@clinique.tn', '08h00 - 14h00')
);

INSERT INTO Medecins VALUES (
    medecin_t(20, 'Dr. Trabelsi', 'Pédiatre', 3000, (SELECT REF(d) FROM Departements d WHERE d.idDep = 2), 'dr.trabelsi@clinique.tn', '09h00 - 15h00')
);

INSERT INTO Medecins VALUES (
    medecin_t(30, 'Dr. Ben Salem', 'Cardiologue', 3800, (SELECT REF(d) FROM Departements d WHERE d.idDep = 1), 'dr.bensalem@clinique.tn', '08h30 - 14h30')
);

INSERT INTO Medecins VALUES (
    medecin_t(40, 'Dr. Jaziri', 'Neurologue', 4200, (SELECT REF(d) FROM Departements d WHERE d.idDep = 3), 'dr.jaziri@clinique.tn', '10h00 - 16h00')
);

INSERT INTO Medecins VALUES (
    medecin_t(50, 'Dr. Mezhoud', 'Orthopédiste', 3600, (SELECT REF(d) FROM Departements d WHERE d.idDep = 4), 'dr.mezhoud@clinique.tn', '08h00 - 14h00')
);

INSERT INTO Medecins VALUES (
    medecin_t(60, 'Dr. Karray', 'Dermatologue', 3200, (SELECT REF(d) FROM Departements d WHERE d.idDep = 5), 'dr.karray@clinique.tn', '09h00 - 15h00')
);

INSERT INTO Medecins VALUES (
    medecin_t(70, 'Dr. Ayadi', 'Ophtalmologue', 3400, (SELECT REF(d) FROM Departements d WHERE d.idDep = 6), 'dr.ayadi@clinique.tn', '08h00 - 13h00')
);

INSERT INTO Medecins VALUES (
    medecin_t(80, 'Dr. Cherif', 'Gynécologue', 3900, (SELECT REF(d) FROM Departements d WHERE d.idDep = 7), 'dr.cherif@clinique.tn', '09h00 - 16h00')
);

INSERT INTO Medecins VALUES (
    medecin_t(90, 'Dr. Bouazizi', 'Radiologue', 3100, (SELECT REF(d) FROM Departements d WHERE d.idDep = 8), 'dr.bouazizi@clinique.tn', '08h00 - 14h00')
);

INSERT INTO Medecins VALUES (
    medecin_t(100, 'Dr. Hammami', 'Urologue', 3700, (SELECT REF(d) FROM Departements d WHERE d.idDep = 9), 'dr.hammami@clinique.tn', '08h30 - 15h00')
);


// C. TABLE TYPÉE : PATIENTS (10 LIGNES AVEC SEXE, STATUTS RDV ET MÉDICAMENTS)

INSERT INTO Patients VALUES (
    patient_t(
        1001, 'Yassine Gharbi', TO_DATE('15/04/1995', 'DD/MM/YYYY'), 'M',
        adresse_t('Avenue Habib Bourguiba', 'Tunis', 1001),
        telephones_va('98123456', '55987654'),
        consultations_nt(
            consultation_t(1, TO_DATE('10/01/2026', 'DD/MM/YYYY'), 'Contrôle tension', 80, 'Terminé', prescriptions_va('Amlor 5mg', 'Kardegic 75mg')),
            consultation_t(2, TO_DATE('05/02/2026', 'DD/MM/YYYY'), 'Électrocardiogramme', 150, 'Terminé', prescriptions_va('Tenormine 50mg'))
        ),
        (SELECT REF(m) FROM Medecins m WHERE m.idMed = 10)
    )
);

INSERT INTO Patients VALUES (
    patient_t(
        1002, 'Sarra Riahi', TO_DATE('22/09/2012', 'DD/MM/YYYY'), 'F',
        adresse_t('Rue de la Liberté', 'Ariana', 2080),
        telephones_va('22334455'),
        consultations_nt(
            consultation_t(3, TO_DATE('12/01/2026', 'DD/MM/YYYY'), 'Vaccination annuelle', 60, 'Terminé', prescriptions_va('Infanrix Hexa', 'Doliprane Sirop'))
        ),
        (SELECT REF(m) FROM Medecins m WHERE m.idMed = 20)
    )
);

INSERT INTO Patients VALUES (
    patient_t(
        1003, 'Heithem Sassi', TO_DATE('03/11/1978', 'DD/MM/YYYY'), 'M',
        adresse_t('Route de la Marsa', 'Carthage', 2016),
        telephones_va('99887766', '50112233', '71999888'),
        consultations_nt(
            consultation_t(4, TO_DATE('15/01/2026', 'DD/MM/YYYY'), 'Migraines sévères', 90, 'Terminé', prescriptions_va('Zomig 2.5mg', 'Magnésium B6')),
            consultation_t(5, TO_DATE('20/02/2026', 'DD/MM/YYYY'), 'Scanner cérébral', 220, 'Planifié', prescriptions_va()) -- RDV futur sans ordonnance
        ),
        (SELECT REF(m) FROM Medecins m WHERE m.idMed = 40)
    )
);

INSERT INTO Patients VALUES (
    patient_t(
        1004, 'Mariem Chahed', TO_DATE('30/06/1988', 'DD/MM/YYYY'), 'F',
        adresse_t('Rue Palestine', 'Sfax', 3000),
        telephones_va('25445566'),
        consultations_nt(
            consultation_t(6, TO_DATE('01/02/2026', 'DD/MM/YYYY'), 'Bilan cardiaque', 110, 'Terminé', prescriptions_va('Sintrom 4mg', 'Atorvastatine 20mg'))
        ),
        (SELECT REF(m) FROM Medecins m WHERE m.idMed = 30)
    )
);

INSERT INTO Patients VALUES (
    patient_t(
        1005, 'Amine Ben Amor', TO_DATE('12/01/2001', 'DD/MM/YYYY'), 'M',
        adresse_t('Avenue de France', 'Ben Arous', 2013),
        telephones_va('54321098', '98765432'),
        consultations_nt(
            consultation_t(7, TO_DATE('03/02/2026', 'DD/MM/YYYY'), 'Entorse cheville', 75, 'Terminé', prescriptions_va('Bi-Profenid 150mg', 'Pansement élastique')),
            consultation_t(8, TO_DATE('18/02/2026', 'DD/MM/YYYY'), 'Séance plâtre', 120, 'Planifié', prescriptions_va())
        ),
        (SELECT REF(m) FROM Medecins m WHERE m.idMed = 50)
    )
);

INSERT INTO Patients VALUES (
    patient_t(
        1006, 'Nour El Houda', TO_DATE('18/08/1999', 'DD/MM/YYYY'), 'F',
        adresse_t('Rue Ibn Khaldoun', 'Sousse', 4000),
        telephones_va('21212121'),
        consultations_nt(
            consultation_t(9, TO_DATE('05/02/2026', 'DD/MM/YYYY'), 'Acné sévère', 70, 'Terminé', prescriptions_va('Curacne 20mg', 'Crème hydratante'))
        ),
        (SELECT REF(m) FROM Medecins m WHERE m.idMed = 60)
    )
);

INSERT INTO Patients VALUES (
    patient_t(
        1007, 'Karim Ziedi', TO_DATE('07/03/1965', 'DD/MM/YYYY'), 'M',
        adresse_t('Boulevard 14 Janvier', 'Bizerte', 7000),
        telephones_va('97654321', '53123456'),
        consultations_nt(
            consultation_t(10, TO_DATE('08/02/2026', 'DD/MM/YYYY'), 'Baisse vision', 85, 'Terminé', prescriptions_va('Collyre Tobradex', 'Larmes artificielles'))
        ),
        (SELECT REF(m) FROM Medecins m WHERE m.idMed = 70)
    )
);

INSERT INTO Patients VALUES (
    patient_t(
        1008, 'Ines Mahjoub', TO_DATE('25/12/1990', 'DD/MM/YYYY'), 'F',
        adresse_t('Rue des Orangers', 'Nabeul', 8000),
        telephones_va('26337788', '92114455', '72334455'),
        consultations_nt(
            consultation_t(11, TO_DATE('10/02/2026', 'DD/MM/YYYY'), 'Suivi grossesse', 95, 'Terminé', prescriptions_va('Acide Folique 5mg', 'Elevit B9')),
            consultation_t(12, TO_DATE('25/02/2026', 'DD/MM/YYYY'), 'Échographie 3D', 140, 'Planifié', prescriptions_va())
        ),
        (SELECT REF(m) FROM Medecins m WHERE m.idMed = 80)
    )
);

INSERT INTO Patients VALUES (
    patient_t(
        1009, 'Tarek Dridi', TO_DATE('14/05/1982', 'DD/MM/YYYY'), 'M',
        adresse_t('Avenue Habib Thameur', 'Monastir', 5000),
        telephones_va('51515151'),
        consultations_nt(
            consultation_t(13, TO_DATE('14/02/2026', 'DD/MM/YYYY'), 'Radio pulmonaire', 110, 'Terminé', prescriptions_va('Augmentin 1g', 'Solupred 20mg'))
        ),
        (SELECT REF(m) FROM Medecins m WHERE m.idMed = 90)
    )
);

INSERT INTO Patients VALUES (
    patient_t(
        1010, 'Fatma Baccouche', TO_DATE('09/10/1974', 'DD/MM/YYYY'), 'F',
        adresse_t('Rue de la République', 'Kairouan', 3100),
        telephones_va('93445566', '58990011'),
        consultations_nt(
            consultation_t(14, TO_DATE('16/02/2026', 'DD/MM/YYYY'), 'Calcul rénal', 130, 'Terminé', prescriptions_va('Spasfon Lyoc', 'Kétoprofène 100mg')),
            consultation_t(15, TO_DATE('28/02/2026', 'DD/MM/YYYY'), 'Contrôle urologique', 80, 'Planifié', prescriptions_va())
        ),
        (SELECT REF(m) FROM Medecins m WHERE m.idMed = 100)
    )
);


// D. TABLE NON TYPÉE : FACTURES (10 LIGNES)

INSERT INTO Factures VALUES (5001, TO_DATE('10/01/2026', 'DD/MM/YYYY'), 80, (SELECT REF(p) FROM Patients p WHERE p.idPat = 1001), (SELECT REF(m) FROM Medecins m WHERE m.idMed = 10), 1);

INSERT INTO Factures VALUES (5002, TO_DATE('12/01/2026', 'DD/MM/YYYY'), 60, (SELECT REF(p) FROM Patients p WHERE p.idPat = 1002), (SELECT REF(m) FROM Medecins m WHERE m.idMed = 20), 3);

INSERT INTO Factures VALUES (5003, TO_DATE('15/01/2026', 'DD/MM/YYYY'), 90, (SELECT REF(p) FROM Patients p WHERE p.idPat = 1003), (SELECT REF(m) FROM Medecins m WHERE m.idMed = 40), 4);

INSERT INTO Factures VALUES (5004, TO_DATE('01/02/2026', 'DD/MM/YYYY'), 110, (SELECT REF(p) FROM Patients p WHERE p.idPat = 1004), (SELECT REF(m) FROM Medecins m WHERE m.idMed = 30), 6);

INSERT INTO Factures VALUES (5005, TO_DATE('03/02/2026', 'DD/MM/YYYY'), 75, (SELECT REF(p) FROM Patients p WHERE p.idPat = 1005), (SELECT REF(m) FROM Medecins m WHERE m.idMed = 50), 7);

INSERT INTO Factures VALUES (5006, TO_DATE('05/02/2026', 'DD/MM/YYYY'), 70, (SELECT REF(p) FROM Patients p WHERE p.idPat = 1006), (SELECT REF(m) FROM Medecins m WHERE m.idMed = 60), 9);

INSERT INTO Factures VALUES (5007, TO_DATE('08/02/2026', 'DD/MM/YYYY'), 85, (SELECT REF(p) FROM Patients p WHERE p.idPat = 1007), (SELECT REF(m) FROM Medecins m WHERE m.idMed = 70), 10);

INSERT INTO Factures VALUES (5008, TO_DATE('10/02/2026', 'DD/MM/YYYY'), 95, (SELECT REF(p) FROM Patients p WHERE p.idPat = 1008), (SELECT REF(m) FROM Medecins m WHERE m.idMed = 80), 11);

INSERT INTO Factures VALUES (5009, TO_DATE('14/02/2026', 'DD/MM/YYYY'), 110, (SELECT REF(p) FROM Patients p WHERE p.idPat = 1009), (SELECT REF(m) FROM Medecins m WHERE m.idMed = 90), 13);

INSERT INTO Factures VALUES (5010, TO_DATE('16/02/2026', 'DD/MM/YYYY'), 130, (SELECT REF(p) FROM Patients p WHERE p.idPat = 1010), (SELECT REF(m) FROM Medecins m WHERE m.idMed = 100), 14);

COMMIT;


// 6. Requette SQL AFIN DE VERIFIER LE BON FONCTIONNEMENT DU SYSTEMES


// REQUÊTE 1 

SELECT 
    p.idPat AS ID_Patient,
    p.nomPat AS Patient, 
    p.sexe AS Sexe,
    p.medecinTraitant.nomMed AS Medecin_Traitant, 
    p.medecinTraitant.specialite AS Specialite,
    p.medecinTraitant.dept.nomDep AS Service_Medical,
    p.medecinTraitant.dept.etage AS Etage
FROM Patients p;



// REQUÊTE 2 

SELECT 
    p.nomPat AS Patient, 
    DEREF(p.medecinTraitant) AS Objet_Medecin_Complet
FROM Patients p
WHERE p.idPat = 1001;



// REQUÊTE 3 

SELECT 
    p.nomPat AS Patient, 
    DEREF(p.medecinTraitant).idMed AS ID_Med,
    DEREF(p.medecinTraitant).nomMed AS Nom_Medecin,
    DEREF(p.medecinTraitant).specialite AS Specialite,
    DEREF(p.medecinTraitant).salaireBase || ' DT' AS Salaire_Base,
    DEREF(p.medecinTraitant).email AS Contact_Email
FROM Patients p
WHERE p.idPat = 1001;



// REQUÊTE 4 

SELECT 
    p.idPat AS ID,
    p.nomPat AS Nom_Patient, 
    TO_CHAR(p.dateNaissance, 'DD/MM/YYYY') AS Date_Naissance, 
    p.calculer_age() || ' ans' AS Age_Calcule_Dynamiquement
FROM Patients p;


// REQUÊTE 5

SELECT 
    m.idMed AS ID,
    m.nomMed AS Medecin, 
    m.specialite AS Specialite,
    m.salaireBase || ' DT' AS Salaire_Fixe, 
    m.calculer_prime(0.15) || ' DT' AS Prime_15_Pourcent,
    (m.salaireBase + m.calculer_prime(0.15)) || ' DT' AS Salaire_Total_Estime
FROM Medecins m;


//REQUÊTE 6 

SELECT 
    p.idPat AS ID,
    p.nomPat AS Patient, 
    p.tels AS Contacts_Urgence_VARRAY
FROM Patients p;


// REQUÊTE 7 

SELECT 
    p.nomPat AS Patient, 
    c.numCons AS No_Acte,
    TO_CHAR(c.dateCons, 'DD/MM/YYYY') AS Date_Consultation, 
    c.motif AS Diagnostic, 
    c.statut AS Statut,
    c.tarif || ' DT' AS Honoraires
FROM Patients p, TABLE(p.historique) c
ORDER BY p.idPat, c.numCons;



// REQUÊTE 8 

SELECT 
    p.nomPat AS Patient,
    c.motif AS Diagnostic_Medical,
    c.statut AS Statut_Acte,
    c.medicaments AS Ordonnance_Prescrite_VARRAY
FROM Patients p, TABLE(p.historique) c
WHERE c.statut = 'Terminé';



// REQUÊTE 9

SELECT 
    p.nomPat AS Patient_Concerne,
    p.tels AS Contacts,
    TO_CHAR(c.dateCons, 'DD/MM/YYYY') AS Date_Rendez_Vous,
    c.motif AS Motif_Prevu,
    c.tarif || ' DT' AS Tarif_Prevu
FROM Patients p, TABLE(p.historique) c
WHERE c.statut = 'Planifié';



// REQUÊTE 10 : Exploitation de la Table Non Typée 
SELECT 
    f.numFacture AS N_Facture, 
    TO_CHAR(f.dateFacture, 'DD/MM/YYYY') AS Date_Emission,
    f.montant || ' DT' AS Total_A_Payer, 
    f.patientConcerne.nomPat AS Nom_Debiteur,
    f.patientConcerne.adr.ville AS Ville_Facturation,       -- REF Patient -> TAD Adresse -> Ville
    f.patientConcerne.adr.rue AS Rue_Facturation,           -- REF Patient -> TAD Adresse -> Rue
    f.medecinEmetteur.nomMed AS Medecin_Prescripteur        -- REF Médecin -> Nom
FROM Factures f
ORDER BY f.numFacture;



// REQUÊTE 11 : Consultation des Médecins avec Horaires et Service

SELECT 
    m.nomMed AS Praticien, 
    m.specialite AS Specialite, 
    m.horaires AS Plage_Horaires, 
    m.email AS Contact_Pro,
    m.dept.nomDep AS Service_Rattache,
    m.dept.etage AS Localisation_Etage
FROM Medecins m;


// REQUÊTE 12 : Test de validité des pointeurs 

SELECT 
    COUNT(*) AS Nb_Refs_Dangling
FROM Patients p
WHERE p.medecinTraitant IS DANGLING;


