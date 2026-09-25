## Étape 1 — Analyse et conception

Fait : cahier des charges rédigé (10 sections, 12 EF, 14 RG) ; 3 diagrammes
Mermaid (cas d'utilisation, modèle de données, séquence "marquer sa
présence") ; contrat d'API complété avec 4 opérations ajoutées (clôture de
session, présence manuelle, remplacement de lien, modification de note) en
plus des 5 imposées ; backlog de 13 issues Must créées sur GitHub, labels
must/should/could créés et appliqués.

Bloqué : contradiction entre Q10 et Q15 sur la modification de la note,
tranchée en faveur de Q10 (détail et justification dans la section 7 du
cahier des charges). Une erreur de structure dans le contrat d'API — deux
chemins /api/relectures/{id} dupliqués qui s'écrasaient silencieusement en
YAML — repérée avant de committer, en validant le fichier avec un script de
lecture YAML. Quelques minutes perdues à trouver où créer un nouveau label
GitHub : ce n'est pas possible depuis le menu rapide ouvert sur une issue, il
faut passer par la page de gestion des labels du dépôt.

IA : vu la contrainte de temps (rendu à 18h00), le cahier des charges, les
diagrammes et le contrat d'API ont été rédigés avec l'appui de l'IA à partir
de CLIENT.md et du contrat imposé. Vérification faite en relisant chaque
exigence (EFx) et règle (RGx) face à la question Qx correspondante, en
comparant les codes HTTP des diagrammes à ceux du contrat, et en validant la
syntaxe du contrat d'API avec un parseur YAML avant de l'intégrer au dépôt —
c'est cette vérification qui a permis de détecter la duplication de chemin.

## Étape 2 — Construction de la v0.1

Fait : tickets Must #1 à #6 implémentés sur des branches dédiées ; sessions,
présences, expiration du code, dépôt d'exercice, assignation et notation d'une
relecture sont couverts par des endpoints et des tests d'intégration ciblés.
Le contrat documente maintenant le corps de réponse de `POST /api/relectures/{id}`.
Bloqué : les tests Maven n'ont pas pu être exécutés, car Maven n'est pas installé
dans l'environnement de travail (`mvn: not found`). La validation statique Java
et `git diff --check` ont été exécutés.
IA : l'IA a proposé les services, contrôleurs et tests ; j'ai vérifié chaque
réponse contre `api/contrat.yaml`, notamment les statuts `200`, `400`, `403`,
`409` et la note entière entre 0 et 20. Le journal est mis à jour dans cette
branche avant son commit.

## Ticket #7 — Consultation de la note

Fait : ajout de `GET /api/exercices/{id}/note`, qui expose uniquement la note
et le commentaire après relecture, sans exposer l'identité du relecteur.
Le wrapper Maven officiel est maintenant disponible et `./mvnw clean test`
est passé avec succès après installation de Maven.
Bloqué : aucun blocage technique restant sur le backend pour ce ticket.
IA : l'implémentation est vérifiée contre le contrat d'API et par un test
d'intégration ciblé.
