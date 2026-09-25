# D2 — Diagramme de classes / modèle de données

```mermaid
classDiagram
    class Etudiant {
        +Long id
        +String nom
        +Long promotionId
    }

    class Session {
        +Long id
        +String titre
        +Long promotionId
        +String code
        +DateTime ouvertureAt
        +DateTime expirationAt
        +DateTime clotureAt
        +boolean estCloturee()
    }

    class Presence {
        +Long id
        +Long sessionId
        +Long etudiantId
        +String source
        +DateTime marqueeAt
    }

    class Exercice {
        +Long id
        +Long sessionId
        +Long etudiantId
        +String lien
        +String statut
        +DateTime deposeAt
    }

    class Relecture {
        +Long id
        +Long exerciceId
        +Long relecteurId
        +Integer note
        +String commentaire
        +DateTime rendueAt
    }

    Session "1" --> "0..*" Presence : concerne
    Session "1" --> "0..*" Exercice : contient
    Etudiant "1" --> "0..*" Presence : marque
    Etudiant "1" --> "0..*" Exercice : dépose
    Etudiant "1" --> "0..*" Relecture : relit (RG4 : jamais son propre exercice)
    Exercice "1" --> "0..1" Relecture : reçoit (RG5 : un seul relecteur)
```

Notes de correspondance avec les migrations à venir :
- `Presence.source` porte les valeurs `ETUDIANT` ou `FORMATEUR` (RG13, contrat d'API).
- `Exercice.statut` porte les valeurs `DEPOSE`, `EN_RELECTURE`, `RELU` (cycle de vie, cf. bonus D4 optionnel).
- La cardinalité `Exercice 1 --> 0..1 Relecture` traduit RG5 (un seul relecteur) et RG10 (l'exercice peut rester sans relecture, donc `0..1`, pas `1..1`).
- Aucune colonne mot de passe : conforme à Q1 (pas d'authentification par mot de passe).
