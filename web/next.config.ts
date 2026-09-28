import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: 'export',
  basePath: '/3AM',
  images: {
      unoptimized: true,
  },
};

export default nextConfig;
