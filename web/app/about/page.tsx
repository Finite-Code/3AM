"use client";

import { motion } from "motion/react";
import { ArrowLeft, Zap, Shield, HardDrive } from "lucide-react";
import Link from "next/link";
import { Geist } from 'next/font/google';

const geist = Geist({ subsets: ['latin'], weight: '400', display: 'swap' });
const geistBold = Geist({ subsets: ['latin'], weight: '700', display: 'swap' });

export default function AboutPage() {
  return (
    <main className={`min-h-screen bg-black text-white selection:bg-purple-500/30 px-6 py-24 sm:py-32 ${geist.className}`}>

      {/* Back Button */}
      <div className="absolute top-8 left-8 z-50">
        <Link href="/" className="inline-flex items-center gap-2 text-zinc-400 hover:text-white transition-colors">
          <ArrowLeft className="w-5 h-5" />
          <span className="text-sm font-medium tracking-wide uppercase">Back to Home</span>
        </Link>
      </div>

      <div className="mx-auto max-w-3xl">
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.8 }}>
          <h1 className={`text-5xl sm:text-7xl font-bold tracking-tighter mb-8 ${geistBold.className}`}>
            Built for the <br/><span className="text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-zinc-400">Audiophile.</span>
          </h1>

          <p className="text-xl text-zinc-400 leading-relaxed mb-16">
            In an era of endless buffering, subscription fatigue, and compressed audio streams, 3AM was built as a sanctuary for your local music library.
            It is a love letter to high-fidelity audio, designed to run entirely on your device with zero compromises.
          </p>
        </motion.div>

        <div className="grid gap-12 sm:grid-cols-2">
          <FeatureBlock
            icon={<HardDrive className="w-6 h-6 text-purple-400" />}
            title="Local First"
            description="Your music belongs to you. 3AM scans your device storage directly, requiring absolutely no internet connection to play your FLAC, ALAC, and MP3 files."
          />
          <FeatureBlock
            icon={<Zap className="w-6 h-6 text-purple-400" />}
            title="Zero Latency"
            description="Because everything happens locally on your device's processor, EQ adjustments, gapless transitions, and metadata indexing happen instantly."
          />
          <FeatureBlock
            icon={<Shield className="w-6 h-6 text-purple-400" />}
            title="Absolute Privacy"
            description="No telemetry. No tracking algorithms analyzing your listening habits. Your playback history and preferences never leave your phone."
          />
        </div>
      </div>
    </main>
  );
}

function FeatureBlock({ icon, title, description }: { icon: React.ReactNode, title: string, description: string }) {
  return (
    <motion.div initial={{ opacity: 0, y: 10 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }} transition={{ duration: 0.5 }}>
      <div className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-white/5 border border-white/10">
        {icon}
      </div>
      <h3 className={`text-2xl font-bold mb-3 ${Geist({ subsets: ['latin'], weight: '700' }).className}`}>{title}</h3>
      <p className="text-zinc-400 leading-relaxed">{description}</p>
    </motion.div>
  );
}
