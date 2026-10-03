import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { Geist } from 'next/font/google';

const geist = Geist({ subsets: ['latin'], weight: '400', display: 'swap' });
const geistBold = Geist({ subsets: ['latin'], weight: '700', display: 'swap' });

export default function PrivacyPage() {
  return (
    <main className={`min-h-screen bg-black text-zinc-300 selection:bg-purple-500/30 px-6 py-24 sm:py-32 ${geist.className}`}>

      <div className="absolute top-8 left-8 z-50">
        <Link href="/" className="inline-flex items-center gap-2 text-zinc-500 hover:text-white transition-colors">
          <ArrowLeft className="w-5 h-5" />
          <span className="text-sm font-medium tracking-wide uppercase">Back</span>
        </Link>
      </div>

      <div className="mx-auto max-w-2xl prose prose-invert prose-zinc">
        <h1 className={`text-4xl sm:text-5xl font-bold text-white mb-2 ${geistBold.className}`}>Privacy Policy</h1>
        <p className="text-zinc-500 mb-12">Last updated: October 2, 2026</p>

        <section className="mb-10">
          <h2 className="text-2xl font-semibold text-white mb-4">1. The Short Version</h2>
          <p className="leading-relaxed">
            3AM is a completely offline, local music player. We do not collect, store, transmit, or share any of your personal data, listening habits, or audio files. Everything happens entirely on your device.
          </p>
        </section>

        <section className="mb-10">
          <h2 className="text-2xl font-semibold text-white mb-4">2. Device Permissions</h2>
          <p className="leading-relaxed mb-4">
            To function properly, 3AM requires the following permissions on your Android device:
          </p>
          <ul className="list-disc pl-6 space-y-2">
            <li><strong>Storage Access (READ_MEDIA_AUDIO):</strong> Required strictly to scan and play audio files stored locally on your device.</li>
            <li><strong>Foreground Service:</strong> Required to keep music playing smoothly while the app is in the background or your screen is off.</li>
            <li><strong>Wake Lock:</strong> Prevents the processor from sleeping mid-song, ensuring gapless playback.</li>
          </ul>
        </section>

        <section className="mb-10">
          <h2 className="text-2xl font-semibold text-white mb-4">3. Data Retention</h2>
          <p className="leading-relaxed">
            Features like "Listening Stats," "Favorites," and custom playlists are saved locally in a SQLite database directly on your phone's storage. If you uninstall the app or clear its data, this information is permanently deleted. We have no way to recover it because we never receive a copy of it.
          </p>
        </section>

        <section>
          <h2 className="text-2xl font-semibold text-white mb-4">4. Open Source</h2>
          <p className="leading-relaxed">
            3AM is entirely open-source. You don't have to take our word for it—you can inspect every line of code regarding how your data is handled on our <a href="https://github.com/Finite-Code/3AM" target="_blank" rel="noopener noreferrer" className="text-purple-400 hover:underline">GitHub repository</a>.
          </p>
        </section>
      </div>
    </main>
  );
}
