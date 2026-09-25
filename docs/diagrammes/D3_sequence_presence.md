# D3 — Séquence : marquer sa présence

```mermaid
sequenceDiagram
    participant E as Étudiant
    participant F as Front
    participant API as PresenceController
    participant S as PresenceService

    E->>F: saisit le code
    F->>API: POST /api/presences { code, etudiantId }
    API->>S: enregistrer(code, etudiantId)
    alt code expiré (RG1)
        S-->>API: CodeExpireException
        API-->>F: 410 { code: "CODE_EXPIRE" }
    else déjà présent
        S-->>API: DejaPresentException
        API-->>F: 409 { code: "DEJA_PRESENT" }
    else cas nominal
        S-->>API: Presence
        API-->>F: 201 { id, sessionId, etudiantId, source }
    end
    F-->>E: affiche le résultat
```

Correspondance avec le contrat d'API (`api/contrat.yaml`) :
- `410` et le code `CODE_EXPIRE` reprennent exactement les erreurs attendues sur `POST /api/presences`.
- `409` correspond au cas "déjà présent" du contrat.
- `201` avec le corps `{ id, sessionId, etudiantId, source }` reprend le format de succès imposé.
