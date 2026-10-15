--------------------------------------------------------------------------------
-- PROJET BASES DE DONNÉES AVANCÉES : CLINIQUE MEDICAL CENTER
-- SCRIPT COMPLET MIS À JOUR (AVEC MÉDICAMENTS, STATUT RDV, EMAIL, HORAIRES, SEXE)
--------------------------------------------------------------------------------

-- =============================================================================
-- 0. NETTOYAGE PRÉVENTIF (ORDRE STRICT DE SUPPRESSION)
-- =============================================================================
DROP TABLE Factures CASCADE CONSTRAINTS;
DROP TABLE Patients CASCADE CONSTRAINTS;
DROP TABLE Medecins CASCADE CONSTRAINTS;
DROP TABLE Departements CASCADE CONSTRAINTS;

DROP TYPE patient_t FORCE;
DROP TYPE consultations_nt FORCE;
DROP TYPE consultation_t FORCE;
DROP TYPE prescriptions_va FORCE;
DROP TYPE telephones_va FORCE;
DROP TYPE medecin_t FORCE;
DROP TYPE departement_t FORCE;
DROP TYPE adresse_t FORCE;

--------------------------------------------------------------------------------
-- 1. CRÉATION DES TYPES ABSTRAITS DE DONNÉES (TAD) & COLLECTIONS
--------------------------------------------------------------------------------

-- A. TAD Composite : Adresse (Partagé)
CREATE OR REPLACE TYPE adresse_t AS OBJECT (
    rue VARCHAR2(50),
    ville VARCHAR2(50),
    codePostal NUMBER(5)
)
/

-- B. TAD Département médical
CREATE OR REPLACE TYPE departement_t AS OBJECT (
    idDep NUMBER,
    nomDep VARCHAR2(30),
    etage NUMBER
)
/

-- C. TAD Médecin (avec email, horaires, REF vers département et méthode)
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

-- D. COLLECTION : VARRAY pour les Médicaments prescrits (Ordonnance, max 5)
CREATE OR REPLACE TYPE prescriptions_va AS VARRAY(5) OF VARCHAR2(50)
/

-- E. TAD Consultation (avec statut RDV/Consultation et le VARRAY de médicaments)
CREATE OR REPLACE TYPE consultation_t AS OBJECT (
    numCons NUMBER,
    dateCons DATE,
    motif VARCHAR2(100),
    tarif NUMBER,
    statut VARCHAR2(20),             -- 'Planifié' (Rendez-vous) ou 'Terminé' (Consultation)
    medicaments prescriptions_va     -- IMBRICATION DE COLLECTION : VARRAY dans Nested Table !
)
/

-- F. COLLECTION : VARRAY pour les téléphones d'urgence du patient (max 3)
CREATE OR REPLACE TYPE telephones_va AS VARRAY(3) OF VARCHAR2(15)
/

-- G. COLLECTION : NESTED TABLE pour l'historique des consultations
CREATE OR REPLACE TYPE consultations_nt AS TABLE OF consultation_t
/

-- H. TAD Patient (avec sexe, adresse TAD, telephones VARRAY, historique NESTED TABLE et REF)
CREATE OR REPLACE TYPE patient_t AS OBJECT (
    idPat NUMBER,
    nomPat VARCHAR2(30),
    dateNaissance DATE,
    sexe VARCHAR2(1),                -- Nouvel attribut ('M' ou 'F')
    adr adresse_t,                   -- TAD composite
    tels telephones_va,              -- VARRAY
    historique consultations_nt,     -- NESTED TABLE
    medecinTraitant REF medecin_t,   -- Référence vers Medecins
    
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

--------------------------------------------------------------------------------
-- 2. CRÉATION DES TABLES TYPÉES ET NON TYPÉES
--------------------------------------------------------------------------------

-- A. Table Typée des Départements
CREATE TABLE Departements OF departement_t (
    CONSTRAINT pk_dep PRIMARY KEY (idDep)
);

-- B. Table Typée des Médecins
CREATE TABLE Medecins OF medecin_t (
    CONSTRAINT pk_med PRIMARY KEY (idMed)
);

-- C. Table Typée des Patients (avec stockage de la table imbriquée)
CREATE TABLE Patients OF patient_t (
    CONSTRAINT pk_pat PRIMARY KEY (idPat)
)
NESTED TABLE historique STORE AS tab_consultations_stockage;

-- D. Table NON TYPÉE des Factures
CREATE TABLE Factures (
    numFacture NUMBER PRIMARY KEY,
    dateFacture DATE NOT NULL,
    montant NUMBER NOT NULL,
    patientConcerne REF patient_t,
    medecinEmetteur REF medecin_t,
    numCons NUMBER
);

--------------------------------------------------------------------------------
-- 3. INSERTIONS DE DONNÉES : CLINIQUE MEDICAL CENTER (10 LIGNES PAR TABLE)
--------------------------------------------------------------------------------

-- =============================================================================
-- A. TABLE TYPÉE : DEPARTEMENTS (10 LIGNES)
-- =============================================================================
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

-- =============================================================================
-- B. TABLE TYPÉE : MEDECINS (10 LIGNES AVEC EMAIL ET HORAIRES)
-- =============================================================================
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

-- =============================================================================
-- C. TABLE TYPÉE : PATIENTS (10 LIGNES AVEC SEXE, STATUTS RDV ET MÉDICAMENTS)
-- =============================================================================
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

-- =============================================================================
-- D. TABLE NON TYPÉE : FACTURES (10 LIGNES)
-- =============================================================================
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

-- =============================================================================
-- 6. Requette SQL
-- =============================================================================

SELECT 
    p.nomPat AS Patient, 
    p.medecinTraitant.nomMed AS Medecin, 
    p.medecinTraitant.dept.nomDep AS Service
FROM Patients p;

SELECT 
    p.nomPat, 
    DEREF(p.medecinTraitant) AS Objet_Medecin_Complet
FROM Patients p
WHERE p.idPat = 1001;

SELECT 
    p.nomPat, 
    DEREF(p.medecinTraitant).idMed AS ID_Med,
    DEREF(p.medecinTraitant).nomMed AS Nom_Med,
    DEREF(p.medecinTraitant).specialite AS Specialite,
    DEREF(p.medecinTraitant).salaireBase AS Salaire
FROM Patients p
WHERE p.idPat = 1001;

-- Calcul de l'âge dynamique des patients via la méthode du TAD
SELECT 
    p.nomPat, 
    p.dateNaissance, 
    p.calculer_age() AS Age_Calcule
FROM Patients p;

-- Calcul de prime des médecins via leur méthode
SELECT 
    m.nomMed, 
    m.salaireBase, 
    m.calculer_prime(0.15) AS Prime_15_Pourcent
FROM Medecins m;


SELECT 
    p.nomPat, 
    c.dateCons, 
    c.motif, 
    c.tarif
FROM Patients p, TABLE(p.historique) c;


SELECT 
    p.nomPat, 
    p.tels AS Numeros_Urgence
FROM Patients p;

SELECT 
    f.numFacture, 
    f.dateFacture,
    f.montant || ' DT' AS Montant, 
    f.patientConcerne.nomPat AS Patient_Facture,
    f.patientConcerne.adr.ville AS Ville_Facturation, -- Navigation REF -> TAD -> Ville
    f.medecinEmetteur.nomMed AS Medecin_Traitant
FROM Factures f;

SELECT 
    p.nomPat AS Patient,
    c.dateCons AS Date_RDV,
    c.motif AS Motif,
    c.statut AS Statut_Acte
FROM Patients p, TABLE(p.historique) c
WHERE c.statut = 'Planifié';

SELECT 
    p.nomPat AS Patient,
    c.motif AS Diagnostic,
    c.medicaments AS Ordonnance_Prescrite
FROM Patients p, TABLE(p.historique) c
WHERE c.statut = 'Terminé';

SELECT 
    m.nomMed, 
    m.specialite, 
    m.horaires AS Disponibilites, 
    m.email,
    m.dept.nomDep AS Service
FROM Medecins m;

SELECT count(*) AS Nb_References_Perdues
FROM Patients p
WHERE p.medecinTraitant IS DANGLING;


--------------------------------------------------------------------------------
-- REQUÊTE 1 : Navigation pointée à travers 2 niveaux de REF (Sans AUCUN "JOIN")
-- Concept démontré : Remplacement des jointures relationnelles par des pointeurs d'objets (OID).
-- Explication : On part du patient (p), on suit le pointeur de son médecin traitant (p.medecinTraitant),
-- puis depuis ce médecin, on suit le pointeur de son service (dept.nomDep).
-- En SQL classique, cette requête aurait exigé 2 jointures lourdes (INNER JOIN).
--------------------------------------------------------------------------------
SELECT 
    p.idPat AS ID_Patient,
    p.nomPat AS Patient, 
    p.sexe AS Sexe,
    p.medecinTraitant.nomMed AS Medecin_Traitant, 
    p.medecinTraitant.specialite AS Specialite,
    p.medecinTraitant.dept.nomDep AS Service_Medical,
    p.medecinTraitant.dept.etage AS Etage
FROM Patients p;


--------------------------------------------------------------------------------
-- REQUÊTE 2 : Matérialisation d'un objet complet via DEREF()
-- Concept démontré : Fonction DEREF() (Page 63 et 69 du cours).
-- Explication : Le champ 'medecinTraitant' stocke uniquement une adresse hexadécimale (OID).
-- La fonction DEREF() ordonne à Oracle d'aller sur le disque chercher l'objet complet pointé.
--------------------------------------------------------------------------------
SELECT 
    p.nomPat AS Patient, 
    DEREF(p.medecinTraitant) AS Objet_Medecin_Complet
FROM Patients p
WHERE p.idPat = 1001;


--------------------------------------------------------------------------------
-- REQUÊTE 3 : Projection ciblée sur les attributs d'un objet déréférencé
-- Concept démontré : Extraction des sous-attributs d'un objet ciblé par DEREF().
-- Explication : Permet d'extraire champ par champ les caractéristiques du médecin rattaché.
--------------------------------------------------------------------------------
SELECT 
    p.nomPat AS Patient, 
    DEREF(p.medecinTraitant).idMed AS ID_Med,
    DEREF(p.medecinTraitant).nomMed AS Nom_Medecin,
    DEREF(p.medecinTraitant).specialite AS Specialite,
    DEREF(p.medecinTraitant).salaireBase || ' DT' AS Salaire_Base,
    DEREF(p.medecinTraitant).email AS Contact_Email
FROM Patients p
WHERE p.idPat = 1001;



-- =============================================================================
-- SECTION 2 : EXÉCUTION DES MÉTHODES MEMBRES (ENCAPSULATION PL/SQL)
-- =============================================================================

--------------------------------------------------------------------------------
-- REQUÊTE 4 : Calcul dynamique de l'âge via la méthode membre du TAD
-- Concept démontré : Encapsulation (Page 6 et 26 du cours) - Appel d'une MEMBER FUNCTION.
-- Explication : On ne stocke pas d'attribut 'âge' redondant dans la table. L'âge est calculé
-- à la volée en soustrayant la date de naissance à la date courante (SYSDATE).
--------------------------------------------------------------------------------
SELECT 
    p.idPat AS ID,
    p.nomPat AS Nom_Patient, 
    TO_CHAR(p.dateNaissance, 'DD/MM/YYYY') AS Date_Naissance, 
    p.calculer_age() || ' ans' AS Age_Calcule_Dynamiquement
FROM Patients p;


--------------------------------------------------------------------------------
-- REQUÊTE 5 : Calcul de prime personnalisée avec paramètre sur le médecin
-- Concept démontré : Méthode membre avec passage de paramètre.
-- Explication : On appelle la fonction 'm.calculer_prime(taux)' en lui passant 0.15 (15%).
-- Oracle exécute le code PL/SQL compilé dans le TYPE BODY.
--------------------------------------------------------------------------------
SELECT 
    m.idMed AS ID,
    m.nomMed AS Medecin, 
    m.specialite AS Specialite,
    m.salaireBase || ' DT' AS Salaire_Fixe, 
    m.calculer_prime(0.15) || ' DT' AS Prime_15_Pourcent,
    (m.salaireBase + m.calculer_prime(0.15)) || ' DT' AS Salaire_Total_Estime
FROM Medecins m;



-- =============================================================================
-- SECTION 3 : MANIPULATION DES COLLECTIONS (VARRAY ET NESTED TABLE)
-- =============================================================================

--------------------------------------------------------------------------------
-- REQUÊTE 6 : Affichage direct de la collection VARRAY
-- Concept démontré : Collection ordonnée à taille bornée (Page 43 et 45 du cours).
-- Explication : Affiche le tableau des téléphones stocké directement "in-line" dans le tuple.
--------------------------------------------------------------------------------
SELECT 
    p.idPat AS ID,
    p.nomPat AS Patient, 
    p.tels AS Contacts_Urgence_VARRAY
FROM Patients p;


--------------------------------------------------------------------------------
-- REQUÊTE 7 : Déroulement (Unnesting) de la Nested Table avec TABLE()
-- Concept démontré : Opérateur TABLE() / Unnesting (Page 53 et 55 du cours).
-- Explication : Déroule la table imbriquée 'historique' pour afficher chaque acte médical
-- sous forme de ligne relationnelle standard, avec son tarif et son motif.
--------------------------------------------------------------------------------
SELECT 
    p.nomPat AS Patient, 
    c.numCons AS No_Acte,
    TO_CHAR(c.dateCons, 'DD/MM/YYYY') AS Date_Consultation, 
    c.motif AS Diagnostic, 
    c.statut AS Statut,
    c.tarif || ' DT' AS Honoraires
FROM Patients p, TABLE(p.historique) c
ORDER BY p.idPat, c.numCons;


--------------------------------------------------------------------------------
-- REQUÊTE 8 : Imbrication de Collections (VARRAY dans une Nested Table !)
-- Concept démontré : Imbrication avancée de collections (Page 57 du cours).
-- Explication : La Nested Table 'historique' contient un objet 'consultation_t' qui contient
-- lui-même une collection VARRAY 'medicaments' (l'ordonnance médicale prescrite).
--------------------------------------------------------------------------------
SELECT 
    p.nomPat AS Patient,
    c.motif AS Diagnostic_Medical,
    c.statut AS Statut_Acte,
    c.medicaments AS Ordonnance_Prescrite_VARRAY
FROM Patients p, TABLE(p.historique) c
WHERE c.statut = 'Terminé';



-- =============================================================================
-- SECTION 4 : GESTION DES RENDEZ-VOUS VS CONSULTATIONS (CYCLE DE VIE)
-- =============================================================================

--------------------------------------------------------------------------------
-- REQUÊTE 9 : Filtrage des Rendez-vous futurs ('Planifié')
-- Concept démontré : Exploitation du statut pour gérer les rendez-vous sans table redondante.
-- Explication : Isole les actes médicaux à venir (rendez-vous) qui n'ont pas encore d'ordonnance.
--------------------------------------------------------------------------------
SELECT 
    p.nomPat AS Patient_Concerne,
    p.tels AS Contacts,
    TO_CHAR(c.dateCons, 'DD/MM/YYYY') AS Date_Rendez_Vous,
    c.motif AS Motif_Prevu,
    c.tarif || ' DT' AS Tarif_Prevu
FROM Patients p, TABLE(p.historique) c
WHERE c.statut = 'Planifié';



-- =============================================================================
-- SECTION 5 : TABLE NON TYPÉE ET NAVIGATION COMPOSITE MULTI-NIVEAUX
-- =============================================================================

--------------------------------------------------------------------------------
-- REQUÊTE 10 : Exploitation de la Table Non Typée (Factures)
-- Concept démontré : Table relationnelle classique avec colonnes REF (Page 39 du cours).
-- Explication : 'Factures' est une table classique (avec simple ROWID). Pour imprimer
-- la ville de facturation, la requête traverse le pointeur vers le patient, puis pénètre
-- dans son TAD composite d'adresse : f.patientConcerne.adr.ville !
--------------------------------------------------------------------------------
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



-- =============================================================================
-- SECTION 6 : DISPONIBILITÉS DES MÉDECINS ET COHÉRENCE RÉFÉRENTIELLE
-- =============================================================================

--------------------------------------------------------------------------------
-- REQUÊTE 11 : Consultation des Médecins avec Horaires et Service
-- Concept démontré : Modélisation des disponibilités et liaison avec le département.
--------------------------------------------------------------------------------
SELECT 
    m.nomMed AS Praticien, 
    m.specialite AS Specialite, 
    m.horaires AS Plage_Horaires, 
    m.email AS Contact_Pro,
    m.dept.nomDep AS Service_Rattache,
    m.dept.etage AS Localisation_Etage
FROM Medecins m;


--------------------------------------------------------------------------------
-- REQUÊTE 12 : Test de validité des pointeurs avec le prédicat IS DANGLING
-- Concept démontré : Références orphelines / Pointers pendants (Page 72 et 90 du cours).
-- Explication : Vérifie qu'aucun patient ne pointe vers un médecin inexistant ou supprimé.
-- Un résultat de '0' atteste de l'intégrité et de la cohérence absolue de la base.
--------------------------------------------------------------------------------
SELECT 
    COUNT(*) AS Nb_Refs_Dangling
FROM Patients p
WHERE p.medecinTraitant IS DANGLING;

/*Quelque requettes*/

SELECT sys_context('USERENV', 'SERVICE_NAME') FROM dual;
SELECT USER FROM dual;
SELECT owner, table_name 
FROM all_tables 
WHERE table_name = 'Patients';

SELECT owner, table_name 
FROM all_tables 
WHERE UPPER(table_name) = 'FACTURES';

SELECT table_name, table_type FROM user_object_tables;
