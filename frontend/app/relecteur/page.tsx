"use client";

import { useState } from "react";
import { rendreNote, ApiError, NoteResponse } from "@/lib/api";

export default function EcranRelecteur() {
  const [relectureId, setRelectureId] = useState("");
  const [note, setNote] = useState("10");
  const [commentaire, setCommentaire] = useState("");

  const [resultat, setResultat] = useState<NoteResponse | null>(null);
  const [erreur, setErreur] = useState<string | null>(null);
  const [chargement, setChargement] = useState(false);

  async function gererNotation(evenement: React.FormEvent) {
    evenement.preventDefault();
    setChargement(true);
    setErreur(null);
    try {
      const reponse = await rendreNote(Number(relectureId), {
        note: Number(note),
        commentaire,
      });
      setResultat(reponse);
    } catch (erreur) {
      setErreur(
        erreur instanceof ApiError ? erreur.message : "Erreur inattendue."
      );
    } finally {
      setChargement(false);
    }
  }

  return (
    <main>
      <h1>Espace relecteur</h1>

      <section>
        <h2>Noter un exercice assigné</h2>
        <form onSubmit={gererNotation}>
          <label>
            Identifiant de la relecture
            <input
              type="number"
              value={relectureId}
              onChange={(e) => setRelectureId(e.target.value)}
              required
            />
          </label>
          <label>
            Note (sur 20)
            <input
              type="number"
              min={0}
              max={20}
              value={note}
              onChange={(e) => setNote(e.target.value)}
              required
            />
          </label>
          <label>
            Commentaire
            <textarea
              value={commentaire}
              onChange={(e) => setCommentaire(e.target.value)}
              required
            />
          </label>
          <button type="submit" disabled={chargement}>
            {chargement ? "Envoi..." : "Rendre ma note"}
          </button>
        </form>

        {erreur && <p role="alert">{erreur}</p>}

        {resultat && (
          <p>
            Note enregistrée : <strong>{resultat.note}/20</strong> —{" "}
            {resultat.commentaire}
          </p>
        )}
      </section>
    </main>
  );
}
