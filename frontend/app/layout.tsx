import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "KFOKAM48 — Suivi de présence et relecture",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="fr">
      <body>{children}</body>
    </html>
  );
}
