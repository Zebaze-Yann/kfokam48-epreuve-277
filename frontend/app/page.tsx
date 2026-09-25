import Link from "next/link";

export default function Accueil() {
  return (
    <main>
      <h1>KFOKAM48 — Suivi de présence et relecture</h1>
      <ul>
        <li>
          <Link href="/formateur">Espace formateur</Link>
        </li>
        <li>
          <Link href="/etudiant">Espace étudiant</Link>
        </li>
        <li>
          <Link href="/relecteur">Espace relecteur</Link>
        </li>
      </ul>
    </main>
  );
}
