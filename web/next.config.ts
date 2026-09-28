import type { NextConfig } from "next";

// Check if we are running locally vs in GitHub Actions
const isGithubActions = process.env.GITHUB_ACTIONS || false;

const nextConfig: NextConfig = {
  output: 'export',
  trailingSlash: true,

  // ONLY use the basePath when building on GitHub Actions
  // Locally, it will stay use the root `/` path
  basePath: isGithubActions ? '/3AM' : '',

  images: {
      unoptimized: true,
  },
};

export default nextConfig;
