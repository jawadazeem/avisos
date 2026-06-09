import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  define: {
    global: "globalThis",
  },
  build: {
    outDir: "../resources/static",
    emptyOutDir: true,
  },
  server: {
    proxy: {
      "/api": "http://localhost:8083",
      "/actuator": "http://localhost:8083",
      "/ws": {
        target: "http://localhost:8083",
        ws: true,
      },
    },
  },
});
