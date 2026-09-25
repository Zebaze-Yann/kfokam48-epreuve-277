"use client";

import { useState } from "react";
import {
  ouvrirSession,
  consulterTableau,
  ApiError,
  OuvertureSessionResponse,
  LigneTableau,
} from "@/lib/api";

export default function EcranFormateur() {
  const [titre, setTitre] = useState("");
  const [promotionId, setPromotionId] = useState("1");
  const [session, setSession] = useState<OuvertureSessionResponse | null>(null);
  const [erreurSession, setErreurSession] = useState<string | null>(null);
  const [chargementSession, setChargementSession] = useState(false);

  const [tableau, setTableau] = useState<LigneTableau[] | null>(null);
  const [erreurTableau, setErreurTableau] = useState<string | null>(null);
  const [chargementTableau, setChargementTableau] = useState(false);

  async function gererOuvertureSession(evenement: React.FormEvent) {
    evenement.preventDefault();
    setChargementSession(true);
    setErreurSession(null);
    try {
      const resultat = await ouvrirSession({
        titre,
        promotionId: Number(promotionId),
      });
      setSession(resultat);
    } catch (erreur) {
      setErreurSession(
        erreur instanceof ApiError ? erreur.message : "Erreur inattendue."
      );
    } finally {
      setChargementSession(false);
    }
  }

  async function gererConsultationTableau() {
    setChargementTableau(true);
    setErreurTableau(null);
    try {
      const resultat = await consulterTableau(Number(promotionId));
      setTableau(resultat);
    } catch (erreur) {
      setErreurTableau(
        erreur instanceof ApiError ? erreur.message : "Erreur inattendue."
      );
    } finally {
      setChargementTableau(false);
    }
  }

  return (
    <main>
      <h1>Espace formateur</h1>

      <section>
        <h2>Ouvrir une session</h2>
        <form onSubmit={gererOuvertureSession}>
          <label>
            Titre de la session
            <input
              value={titre}
              onChange={(e) => setTitre(e.target.value)}
              required
            />
          </label>
          <label>
            Identifiant de la promotion
            <input
              type="number"
              value={promotionId}
              onChange={(e) => setPromotionId(e.target.value)}
              required
            />
          </label>
          <button type="submit" disabled={chargementSession}>
            {chargementSession ? "Ouverture..." : "Ouvrir la session"}
          </button>
        </form>

        {erreurSession && <p role="alert">{erreurSession}</p>}

        {session && (
          <p>
            Session ouverte. Code de présence : <strong>{session.code}</strong>{" "}
            (expire à {new Date(session.expirationAt).toLocaleTimeString()})
          </p>
        )}
      </section>

      <section>
        <h2>Tableau de bord</h2>
        <button onClick={gererConsultationTableau} disabled={chargementTableau}>
          {chargementTableau ? "Chargement..." : "Actualiser le tableau"}
        </button>

        {erreurTableau && <p role="alert">{erreurTableau}</p>}

        {tableau && (
          <table>
            <thead>
              <tr>
                <th>Étudiant</th>
                <th>Présences</th>
                <th>Exercices déposés</th>
                <th>Moyenne</th>
                <th>Relectures en attente</th>
              </tr>
            </thead>
            <tbody>
              {tableau.map((ligne) => (
                <tr key={ligne.etudiantId}>
                  <td>{ligne.nom}</td>
                  <td>{ligne.presences}</td>
                  <td>{ligne.exercicesDeposes}</td>
                  <td>{ligne.moyenne ?? "—"}</td>
                  <td>{ligne.relecturesEnAttente}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </main>
  );
}
