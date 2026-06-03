import { resolve } from 'node:path';
import { defineConfig } from 'vite';

export default defineConfig({
  root: 'src/main/frontend',
  build: {
    outDir: '../resources/static',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        map: resolve(__dirname, 'src/main/frontend/map.html'),
      },
    },
  },
});
