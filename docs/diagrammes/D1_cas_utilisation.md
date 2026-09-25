# D1 — Diagramme de cas d'utilisation

```mermaid
graph LR
    Formateur((Formateur))
    Etudiant((Étudiant))
    Relecteur((Étudiant en tant<br/>que relecteur))

    Formateur --> UC1[Ouvrir une session]
    Formateur --> UC2[Ajouter une présence manuelle]
    Formateur --> UC3[Clôturer une session]
    Formateur --> UC4[Consulter le tableau de bord]

    Etudiant --> UC5[Marquer sa présence avec un code]
    Etudiant --> UC6[Déposer le lien de son exercice]
    Etudiant --> UC7[Remplacer le lien de son exercice]
    Etudiant --> UC8[Consulter sa note et son commentaire]

    Relecteur --> UC9[Recevoir l'assignation d'un exercice]
    Relecteur --> UC10[Noter et commenter un exercice]
    Relecteur --> UC11[Modifier sa note avant clôture]
```

Remarque : "Étudiant en tant que relecteur" n'est pas un acteur séparé mais une
casquette que porte un Étudiant une fois qu'il a été assigné à une relecture
(RG6). L'assignation elle-même (UC9) est déclenchée par le système, pas par
une action volontaire de l'étudiant — elle est représentée ici du point de vue
du relecteur qui la reçoit.
