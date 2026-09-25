"use client";

import { useState } from "react";
import {
  marquerPresence,
  deposerExercice,
  ApiError,
  PresenceResponse,
  DepotExerciceResponse,
} from "@/lib/api";

export default function EcranEtudiant() {
  const [etudiantId, setEtudiantId] = useState("1");

  const [code, setCode] = useState("");
  const [presence, setPresence] = useState<PresenceResponse | null>(null);
  const [erreurPresence, setErreurPresence] = useState<string | null>(null);
  const [chargementPresence, setChargementPresence] = useState(false);

  const [sessionId, setSessionId] = useState("");
  const [lien, setLien] = useState("");
  const [depot, setDepot] = useState<DepotExerciceResponse | null>(null);
  const [erreurDepot, setErreurDepot] = useState<string | null>(null);
  const [chargementDepot, setChargementDepot] = useState(false);

  async function gererPresence(evenement: React.FormEvent) {
    evenement.preventDefault();
    setChargementPresence(true);
    setErreurPresence(null);
    try {
      const resultat = await marquerPresence({
        code,
        etudiantId: Number(etudiantId),
      });
      setPresence(resultat);
    } catch (erreur) {
      setErreurPresence(
        erreur instanceof ApiError ? erreur.message : "Erreur inattendue."
      );
    } finally {
      setChargementPresence(false);
    }
  }

  async function gererDepot(evenement: React.FormEvent) {
    evenement.preventDefault();
    setChargementDepot(true);
    setErreurDepot(null);
    try {
      const resultat = await deposerExercice({
        sessionId: Number(sessionId),
        etudiantId: Number(etudiantId),
        lien,
      });
      setDepot(resultat);
    } catch (erreur) {
      setErreurDepot(
        erreur instanceof ApiError ? erreur.message : "Erreur inattendue."
      );
    } finally {
      setChargementDepot(false);
    }
  }

  return (
    <main>
      <h1>Espace étudiant</h1>

      <section>
        <h2>Mon identité</h2>
        <label>
          Identifiant de l&apos;étudiant
          <input
            type="number"
            value={etudiantId}
            onChange={(e) => setEtudiantId(e.target.value)}
            required
          />
        </label>
      </section>

      <section>
        <h2>Marquer ma présence</h2>
        <form onSubmit={gererPresence}>
          <label>
            Code de présence
            <input
              value={code}
              onChange={(e) => setCode(e.target.value)}
              required
            />
          </label>
          <button type="submit" disabled={chargementPresence}>
            {chargementPresence ? "Envoi..." : "Marquer ma présence"}
          </button>
        </form>

        {erreurPresence && <p role="alert">{erreurPresence}</p>}

        {presence && (
          <p>
            Présence enregistrée (session {presence.sessionId}, source{" "}
            {presence.source}).
          </p>
        )}
      </section>

      <section>
        <h2>Déposer mon exercice</h2>
        <form onSubmit={gererDepot}>
          <label>
            Identifiant de la session
            <input
              type="number"
              value={sessionId}
              onChange={(e) => setSessionId(e.target.value)}
              required
            />
          </label>
          <label>
            Lien de l&apos;exercice
            <input
              type="url"
              value={lien}
              onChange={(e) => setLien(e.target.value)}
              placeholder="https://..."
              required
            />
          </label>
          <button type="submit" disabled={chargementDepot}>
            {chargementDepot ? "Dépôt..." : "Déposer mon exercice"}
          </button>
        </form>

        {erreurDepot && <p role="alert">{erreurDepot}</p>}

        {depot && (
          <p>
            Exercice déposé (identifiant {depot.id}, statut {depot.statut}).
          </p>
        )}
      </section>
    </main>
  );
}
