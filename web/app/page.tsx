"use client";

import { TextMorph } from "@/components/shadcn-space/animated-text/animated-text-07";
import { Six_Caps, Geist } from 'next/font/google';
import {motion} from "motion/react";
import {Download, WifiOff, AudioLines, Cpu} from "lucide-react";

const sixCaps = Six_Caps({
  weight: '400', // Six Caps ONLY supports weight 400
  subsets: ['latin'],
  display: 'swap',
});

const geist = Geist({
  weight: '700',
  subsets: ['latin'],
  display: 'swap',
});

export default function Home() {
  return (
    <main className={`relative min-h-screen bg-black text-white selection:bg-purple-500/30 overflow-hidden ${geist.className}`}>

      <motion.div
        animate={{
            scale: [1, 1.1, 1],
            opacity: [0.3, 0.5, 0.3],
            }}
        transition={{
            duration: 8,
            repeat: Infinity,
            ease: "easeInOut"
        }}
        className="pointer-events-none absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] sm:h-[800px] bg-purple-900/20 rounded-full blur-[120px]"
      />

      <div className="pointer-events-none fixed inset-0 z-50 h-full w-full opacity-[0.04]">
        <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg" className="h-full w-full">
            <filter id="noiseFilter">
                <feTurbulence type="fractalNoise" baseFrequency="0.65" numOctaves="3" stitchTitles="stitch" />
            </filter>
        </svg>
      </div>

      <section className="relative z-10 flex min-h-screen flex-col items-center justify-center px-4">
        <div className="flex flex-col items-center">
            <TextMorph
                    words={["Private", "on-device", "Music", "3AM", "3AM"]}
                    interval={1250}
                    className={`text-4xl sm:text-[150px] font-normal text-white uppercase tracking-normal ${sixCaps.className}`}
            />
        </div>

        <motion.p
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5, duration: 0.8 }}
            className="mt-6 text-center text-lg sm:text-xl text-zinc-400 max-w-md font-light tracking-wide">
                Listen to better, richer music. <br/> All offline and on-device.
        </motion.p>

        <motion.button
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.8, duration: 0.8 }}
          className="mt-12 group relative inline-flex h-14 items-center justify-center gap-3 overflow-hidden rounded-full bg-white px-8 font-medium text-black transition-all hover:scale-105 active:scale-95">

          <div className="relative flex items-center justify-center w-5 h-5 overflow-hidden">
            <Download className="w-5 h-5 absolute transition-transform duration-300 group-hover:-translate-y-6" />
            <Download className="w-5 h-5 absolute transition-transform duration-300 translate-y-6 group-hover:translate-y-0" />
          </div>
          <span>Download for Android</span>
        </motion.button>

      </section>

      <section className="relative z-10 mx-auto max-w-5xl px-6 py-32">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">

          <FeatureCard
            icon={<WifiOff className="w-6 h-6 text-zinc-300" />}
            title="Zero Connectivity"
            description="Your library lives on your device. No buffering, no data drops, just instant playback anywhere in the world."/>

          <FeatureCard
            icon={<AudioLines className="w-6 h-6 text-zinc-300" />}
            title="Audiophile Grade"
            description="Built to handle high-fidelity audio formats without compromise. Hear every detail exactly as the artist intended."/>

          <FeatureCard
            icon={<Cpu className="w-6 h-6 text-zinc-300" />}
            title="On-Device Processing"
            description="Lightning fast EQ and metadata indexing powered entirely by your phone's processor. Total privacy."/>

        </div>
     </section>
    </main>
  );
}

function FeatureCard({ icon, title, description }: {icon: React.ReactNode, title: string, description: string}) {
    return(
        <div className="group relative rounded-3xl border border-white/10 bg-white/5 p-8 transition-colors hover:bg-white/10">
            <div className="mb-6 inline-flex h-12 w-12 items-center justify-center rounded-full bg-white/10 backdrop-blur-md">
                {icon}
            </div>
            <h3 className="mb-3 text-xl font-medium text-white">{title}</h3>
            <p className="text-zinc-400 leading-relaxed text-sm">
                {description}
            </p>
        </div>
    );
}
