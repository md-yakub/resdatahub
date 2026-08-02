import type { Metadata } from "next";
import type { ReactNode } from "react";
import Link from "next/link";
import "./globals.css";

export const metadata: Metadata = {
  title: "ResDataHub",
  description: "Public research dataset discovery for ResDataHub"
};

const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/+$/, "");

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="en">
      <body className="min-h-screen font-sans antialiased">
        <header className="border-b border-line bg-white">
          <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
            <Link href="/" className="text-lg font-semibold tracking-normal text-ink">
              ResDataHub
            </Link>
            <nav className="flex items-center gap-5 text-sm text-muted">
              <Link href="/datasets" className="hover:text-ink">
                Datasets
              </Link>
              <Link href="/sparql" className="hover:text-ink">
                Knowledge Graph
              </Link>
              <Link href="/manage" className="hover:text-ink">
                Manage
              </Link>
              {apiBaseUrl && (
                <a href={`${apiBaseUrl}/api/public/catalog/metadata`} className="hover:text-ink">
                  Catalog RDF
                </a>
              )}
            </nav>
          </div>
        </header>
        <main>{children}</main>
      </body>
    </html>
  );
}
