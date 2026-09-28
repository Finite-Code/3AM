import { TextMorph } from "@/components/shadcn-space/animated-text/animated-text-07";
import { Six_Caps, Geist } from 'next/font/google';

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
    <main className="flex min-h-screen flex-col items-center justify-center bg-black text-white">

      <TextMorph
        words={["Private", "on-device", "Music", "Android", "3AM", "3AM"]}
        interval={1250}
        className={`text-4xl sm:text-[150px] font-normal text-white uppercase tracking-normal ${sixCaps.className}`}
      />


      <div><p className={geist.className}>Listen to better, richer music, all offline and on-device.</p></div>
    </main>
  );
}
