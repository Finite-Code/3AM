import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: 'export',
  trailingSlash: true,
  basePath: '/3AM',
  images: {
      unoptimized: true,
  },
};

export default nextConfig;
