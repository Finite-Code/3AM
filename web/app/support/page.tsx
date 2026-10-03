"use client";

import { motion } from "motion/react";
import Link from "next/link";
import { ArrowLeft, Github, Bug } from "lucide-react";
import { Geist } from 'next/font/google';

const geist = Geist({ subsets: ['latin'], weight: '400', display: 'swap' });
const geistBold = Geist({ subsets: ['latin'], weight: '700', display: 'swap' });

export default function SupportPage() {
  return (
    <main className={`min-h-screen bg-black text-white selection:bg-purple-500/30 px-6 py-24 sm:py-32 ${geist.className}`}>

      <div className="absolute top-8 left-8 z-50">
        <Link href="/" className="inline-flex items-center gap-2 text-zinc-400 hover:text-white transition-colors">
          <ArrowLeft className="w-5 h-5" />
          <span className="text-sm font-medium tracking-wide uppercase">Back</span>
        </Link>
      </div>

      <div className="mx-auto max-w-3xl">
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }}>
          <h1 className={`text-4xl sm:text-6xl font-bold tracking-tighter mb-6 ${geistBold.className}`}>
            Support & FAQ
          </h1>
          <p className="text-xl text-zinc-400 mb-12">
            How can we help you get back to the music?
          </p>
        </motion.div>

        <div className="space-y-8 mb-16">
          <FaqItem
            question="Where does 3AM look for my music?"
            answer="The app automatically scans the default 'Music' and 'Downloads' folders on your Android device's internal storage and SD card. Make sure your files are placed there."
          />
          <FaqItem
            question="Why isn't my album art showing up?"
            answer="3AM extracts album art directly from the ID3 tags embedded in your audio files (like FLAC or MP3). If the art is missing, you'll need to embed it using a tag editor tool on your PC before transferring the files."
          />
          <FaqItem
            question="Does this app support Android Auto?"
            answer="Not currently, but it is high on our priority list for future updates!"
          />
        </div>

        <motion.div
          initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.4 }}
          className="rounded-3xl border border-white/10 bg-white/5 p-8 sm:p-12 text-center"
        >
          <h2 className={`text-2xl font-bold mb-4 ${geistBold.className}`}>Found a bug?</h2>
          <p className="text-zinc-400 mb-8 max-w-md mx-auto">
            Since 3AM is an open-source project, the best way to get support or request a feature is by opening an issue on GitHub.
          </p>
          <div className="flex flex-col sm:flex-row justify-center gap-4">
            <a href="https://github.com/Finite-Code/3AM/issues" target="_blank" rel="noopener noreferrer" className="inline-flex h-12 items-center justify-center gap-2 rounded-full bg-white px-8 font-medium text-black transition-transform hover:scale-105 active:scale-95">
              <Bug className="w-4 h-4" />
              Report an Issue
            </a>
            <a href="https://github.com/Finite-Code/3AM" target="_blank" rel="noopener noreferrer" className="inline-flex h-12 items-center justify-center gap-2 rounded-full border border-white/20 bg-transparent px-8 font-medium text-white transition-colors hover:bg-white/10">
              <Github className="w-4 h-4" />
              View Source Code
            </a>
          </div>
        </motion.div>
      </div>
    </main>
  );
}

function FaqItem({ question, answer }: { question: string, answer: string }) {
  return (
    <motion.div initial={{ opacity: 0, y: 10 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }}>
      <h3 className="text-lg font-semibold text-white mb-2">{question}</h3>
      <p className="text-zinc-400 leading-relaxed">{answer}</p>
    </motion.div>
  );
}
