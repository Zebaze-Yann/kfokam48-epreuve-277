# Cahier des charges — Application de gestion de présence et de relecture par les pairs

Auteur : KF48-...-277
Version 1
Frontend choisi : Next.js, parce que déjà maîtrisé (TypeScript, React Query) et adapté à une application avec trois écrans distincts et des appels API fréquents.

## 1. Contexte et objectif

Dans un contexte où le numérique facilite la gestion de nombreux processus du quotidien, KFOKAM48 gère encore manuellement le suivi de présence de ses étudiants et l'évaluation de leurs exercices. Cette gestion manuelle présente des limites concrètes : un étudiant peut signer la présence d'un camarade absent, et le suivi des exercices déposés ainsi que de leur relecture repose sur une organisation informelle, difficile à centraliser pour le formateur.

L'objectif de ce projet est de fournir à KFOKAM48 un outil numérique permettant de fiabiliser la prise de présence, de structurer le dépôt et la relecture des exercices entre pairs, et de donner au formateur une vue d'ensemble fiable de l'activité de chaque étudiant.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire |
|---|---|
| Formateur | Ouvrir une session de cours (génère un code de présence) ; ajouter manuellement une présence ; clôturer une session ; consulter le tableau de bord (présence, dépôts, moyennes, relectures en attente) |
| Étudiant | Marquer sa présence avec un code ; déposer le lien de son exercice ; remplacer ce lien tant qu'il n'est pas en cours de relecture |
| Étudiant en tant que relecteur | Recevoir l'assignation aléatoire d'un exercice à relire (jamais le sien) ; attribuer une note entière (0-20) et un commentaire ; modifier sa note tant que la session n'est pas clôturée |

Un même étudiant peut cumuler les deux dernières casquettes selon le moment : ce ne sont pas deux acteurs distincts, mais deux rôles qu'une même personne peut jouer.

## 3. Périmètre

**Inclus :**
- Ouverture de session et génération d'un code de présence
- Marquage de présence par code (avec expiration et blocage anti-devinette)
- Ajout manuel de présence par le formateur
- Dépôt et remplacement du lien d'un exercice
- Assignation aléatoire d'un relecteur parmi les étudiants présents
- Notation et commentaire d'un exercice par le relecteur
- Tableau de bord de suivi pour le formateur

**Exclus explicitement :**
- Authentification par mot de passe (Q1 : l'étudiant choisit son nom dans une liste)
- Hébergement du contenu de l'exercice lui-même (seul le lien est stocké, pas de dépôt de fichier)
- Gestion de plusieurs centres de formation ou promotions imbriquées au-delà du champ `promotionId`
- Notifications (email, push) — non demandées par le client
- Choix manuel du relecteur par le formateur ou l'étudiant (l'assignation est aléatoire et automatique, Q7)

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | Le formateur ouvre une session de cours | Un code de présence et une date d'expiration sont générés automatiquement à la création | Must |
| EF2 | L'étudiant marque sa présence avec un code | Un code valide et non expiré enregistre la présence, visible aussitôt dans le tableau du formateur | Must |
| EF3 | Le code de présence expire | Passé le délai de RG1, toute tentative renvoie une erreur 410 `CODE_EXPIRE` | Must |
| EF4 | Blocage après erreurs répétées | Après 5 codes erronés, l'étudiant est bloqué 2 minutes avant de pouvoir réessayer | Should |
| EF5 | L'étudiant dépose le lien de son exercice | Le dépôt est accepté tant que la session n'est pas clôturée ; un lien invalide renvoie 400 | Must |
| EF6 | L'étudiant remplace le lien de son exercice | Le remplacement est accepté tant qu'aucune relecture n'a commencé, refusé (409) sinon | Should |
| EF7 | Le système assigne un relecteur | Le relecteur est choisi aléatoirement parmi les étudiants présents à la session, et n'est jamais l'auteur de l'exercice | Must |
| EF8 | Le relecteur note et commente l'exercice | Une note entière de 0 à 20 est acceptée, toute autre valeur renvoie 400 | Must |
| EF9 | Le relecteur modifie sa note | La modification est acceptée tant que le formateur n'a pas clôturé la session | Should |
| EF10 | L'étudiant relu consulte sa note | La note et le commentaire sont visibles, sans révéler l'identité du relecteur | Must |
| EF11 | Le formateur ajoute une présence manuelle | La présence créée porte une marque distincte "ajouté par le formateur" (source = FORMATEUR) | Should |
| EF12 | Le formateur consulte le tableau de bord | Pour chaque étudiant : présences par session, nombre d'exercices déposés, moyenne des notes, relectures encore en attente | Must |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| ENF1 | Utilisation depuis un smartphone (les étudiants marquent leur présence en session, souvent sur mobile) | Interface testée en résolution mobile (375px de large minimum) |
| ENF2 | Temps de réponse court sur les opérations de présence, pour éviter un embouteillage en début de session | Réponse de `POST /api/presences` en moins d'1 seconde en conditions normales |
| ENF3 | Support de plusieurs étudiants marquant leur présence simultanément à l'ouverture d'une session | Test manuel ou script simulant des requêtes concurrentes sur le même code |
| ENF4 | Message d'erreur compréhensible pour un non-technicien (formateur, étudiant) | Chaque erreur API respecte le format imposé `{ code, message }`, jamais de stack trace |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Le code de présence expire 15 minutes après l'ouverture de la session | Q2 |
| RG2 | Impossible de marquer sa présence après la fin (clôture) de la session | Q3 |
| RG3 | Après 5 erreurs de code consécutives, l'étudiant est bloqué 2 minutes | Q4 |
| RG4 | Un étudiant ne peut jamais relire son propre exercice | Q5 |
| RG5 | Un exercice n'a qu'un seul relecteur | Q6 |
| RG6 | Le relecteur est choisi aléatoirement parmi les étudiants présents à la session concernée | Q7 |
| RG7 | L'étudiant relu voit sa note et le commentaire, mais jamais le nom du relecteur | Q8 |
| RG8 | La note est un nombre entier compris entre 0 et 20 | Q9 |
| RG9 | Le relecteur peut corriger sa note tant que le formateur n'a pas clôturé la session (prévaut sur Q15, voir section 7) | Q10 |
| RG10 | Un exercice sans relecture rendue reste au statut "en attente" et doit apparaître comme tel dans le tableau de bord | Q11 |
| RG11 | Le dépôt d'un exercice reste possible jusqu'à la clôture de la session | Q12 |
| RG12 | Le lien d'un exercice peut être remplacé tant qu'aucune relecture n'a commencé | Q13 |
| RG13 | Une présence ajoutée manuellement par le formateur est marquée distinctement comme telle | Q14 |
| RG14 | Le tableau de bord affiche, par étudiant : présence par session, nombre d'exercices déposés, moyenne des notes, relectures encore en attente | Q16 |

## 7. Zones d'ombre, hypothèses et contradictions

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Pourquoi |
|---|---|---|---|
| Modification de la note après soumission | Q10 dit que le relecteur peut corriger sa note tant que la session n'est pas clôturée ; Q15 dit que la note est définitive une fois envoyée | RG9 (basée sur Q10) prévaut | Q11 décrit un usage réel et nécessaire (une relecture peut rester "en attente", donc le processus n'est pas figé) ; Q15 exprime une intention générale, mais Q10 est une réponse plus précise et opérationnelle à une question directe sur ce cas exact |
| Relecteur indisponible si un seul étudiant présent à la session | Non couvert par CLIENT.md | L'exercice reste au statut "en attente" (RG10) tant qu'aucun autre étudiant éligible n'est présent ; aucune notification n'est déclenchée automatiquement | Le client n'a pas anticipé ce cas limite ; on applique la règle générale déjà validée (RG10) plutôt que d'inventer un mécanisme non demandé |
| Un étudiant absent à la session peut-il être choisi comme relecteur ? | Non couvert directement, mais Q7 précise "parmi les étudiants présents" | Un étudiant absent ne peut jamais être assigné comme relecteur | Application stricte de Q7 |
| Que se passe-t-il si un formateur clôture une session avant que toutes les relectures soient rendues ? | Non couvert par CLIENT.md | Les relectures manquantes restent visibles comme "en attente" dans le tableau, sans blocage de la clôture | Cohérent avec RG10 : le tableau doit refléter cet état, pas l'empêcher |

## 8. Contraintes techniques

- Backend : Java 17 ou plus, Spring Boot, Maven avec wrapper `mvnw` commité (B1)
- Le contrat `api/contrat.yaml` est respecté à la lettre : chemins, verbes, codes de statut, format d'erreur (B2)
- Séparation stricte contrôleur / service / repository ; aucune entité JPA exposée directement en JSON, DTO obligatoires (B3)
- Validation des entrées et gestion centralisée des erreurs via `@RestControllerAdvice` (B4)
- Schéma de base de données versionné par Flyway (migrations commitées, `ddl-auto=update` interdit hors tests) (B5)
- Au moins un test unitaire sur une règle métier réelle et un test d'intégration sur un endpoint, exécutables sur poste vierge (B6)
- Frontend : Next.js, choix justifié dans le README, build fonctionnel (F1)
- Trois écrans : formateur, étudiant, relecteur (F2)
- Appels API centralisés dans une couche dédiée, gestion des états de chargement/erreur, aucun recalcul côté frontend d'une donnée déjà fournie par l'API (moyenne notamment) (F3)
- Démarrage documenté en 3 commandes maximum ou `docker compose up`, avec données de démonstration chargées automatiquement

## 9. Livrables

- Dépôt GitHub public `kfokam48-epreuve-277` avec la structure imposée (`docs/`, `api/`, `backend/`, `frontend/`)
- `docs/CAHIER_DES_CHARGES.md` (ce document)
- `docs/diagrammes/` : diagramme de cas d'utilisation, diagramme de classes/modèle de données, diagramme de séquence "marquer sa présence"
- Backlog sous forme d'issues GitHub, avec critères d'acceptation et priorité
- `api/contrat.yaml` complété
- Code source backend (Spring Boot) et frontend (Next.js)
- `docs/JOURNAL.md` tenu à jour à chaque étape
- `CHANGELOG.md` et `README.md` d'installation testé depuis un clone vierge
- `SOUMISSION.md` déposé sur la plateforme avant 18h00

## 10. Démarche prévue

1. Analyse et conception (ce document, diagrammes, backlog, contrat d'API), clôturée par le commit `[JALON] analyse`
2. Développement des issues priorité *Must* uniquement, une branche et une PR par issue, clôturé par le commit `[JALON] v0.1`
3. Prise en compte du changement transmis après l'étape 2 (bug + évolution de besoin), avec mise à jour de l'analyse si elle devient obsolète
4. Finalisation : `[JALON] v1.0`, `CHANGELOG.md`, `README.md` vérifié depuis un clone vierge
5. Rédaction et dépôt de `SOUMISSION.md` sur la plateforme avant 18h00

**Definition of Done** d'une issue : le code est mergé sur `main` via une PR liée à l'issue (qui se ferme automatiquement via `Closes #n` dans un commit), les tests concernés passent, et le comportement correspond au critère d'acceptation de l'exigence EFx associée.
