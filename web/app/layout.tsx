import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "3AM | Music Player",
  description: "An offline, on-device music player for Android devices.",
  keywords: [ "3AM", "music player", "android app", "offline music", "flac player", "open source"],
  authors: [{name: "FiniteCode"}],

  openGraph: {
      title: "3AM | Music Player",
      description: "Listen to better, richer music. All offline and on-device.",
      url: "https://Finite-Code.github.io/3AM/",
      siteName: "3AM",
      images: [
          {
              url: "https://Finite-Code.github.io/3AM/icon.png",
              width: 512,
              height: 511,
          }
      ],
      locale: "en_US",
      type: "website",
  },
  themeColor: "#000000", // prefer dark theme xD
  icons: {
    icon: '/icon.png',
    apple: '/icon.png',
  },
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="en"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col">{children}</body>
    </html>
  );
}
