const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  code: string;

  constructor(code: string, message: string) {
    super(message);
    this.code = code;
  }
}

async function appelerApi<T>(chemin: string, options?: RequestInit): Promise<T> {
  const reponse = await fetch(`${BASE_URL}${chemin}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });

  if (!reponse.ok) {
    const corps = await reponse.json().catch(() => null);
    throw new ApiError(
      corps?.code ?? "ERREUR_INCONNUE",
      corps?.message ?? "Une erreur est survenue."
    );
  }

  if (reponse.status === 204) {
    return undefined as T;
  }

  return reponse.json() as Promise<T>;
}

export interface OuvertureSessionRequest {
  titre: string;
  promotionId: number;
}

export interface OuvertureSessionResponse {
  id: number;
  code: string;
  ouvertureAt: string;
  expirationAt: string;
}

export interface LigneTableau {
  etudiantId: number;
  nom: string;
  presences: number;
  exercicesDeposes: number;
  moyenne: number | null;
  relecturesEnAttente: number;
}

export function ouvrirSession(
  request: OuvertureSessionRequest
): Promise<OuvertureSessionResponse> {
  return appelerApi<OuvertureSessionResponse>("/api/sessions", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function consulterTableau(promotionId: number): Promise<LigneTableau[]> {
  return appelerApi<LigneTableau[]>(`/api/tableau?promotionId=${promotionId}`);
}

export interface MarquerPresenceRequest {
  code: string;
  etudiantId: number;
}

export interface PresenceResponse {
  id: number;
  sessionId: number;
  etudiantId: number;
  source: string;
}

export interface DeposerExerciceRequest {
  sessionId: number;
  etudiantId: number;
  lien: string;
}

export interface DepotExerciceResponse {
  id: number;
  statut: string;
}

export function marquerPresence(
  request: MarquerPresenceRequest
): Promise<PresenceResponse> {
  return appelerApi<PresenceResponse>("/api/presences", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function deposerExercice(
  request: DeposerExerciceRequest
): Promise<DepotExerciceResponse> {
  return appelerApi<DepotExerciceResponse>("/api/exercices", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export interface NoteRequest {
  note: number;
  commentaire: string;
}

export interface NoteResponse {
  note: number;
  commentaire: string;
}

export function rendreNote(
  relectureId: number,
  request: NoteRequest
): Promise<NoteResponse> {
  return appelerApi<NoteResponse>(`/api/relectures/${relectureId}`, {
    method: "POST",
    body: JSON.stringify(request),
  });
}
