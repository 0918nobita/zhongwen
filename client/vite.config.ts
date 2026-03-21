import tailwindcss from '@tailwindcss/vite';
import vue from '@vitejs/plugin-vue';
import { defineConfig } from 'vite';

const host = process.env.TAURI_DEV_HOST;

export default defineConfig(() => ({
  plugins: [tailwindcss(), vue()],
  clearScreen: false,
  // Tauri expects a fixed port, fail if that port is not available
  server: {
    ...(typeof host === 'string' && host !== ''
      ? {
          hmr: {
            host,
            port: 1421,
            protocol: 'ws',
          },
        }
      : {}),
    host,
    port: 1420,
    strictPort: true,
    watch: {
      ignored: ['**/src-tauri/**'],
    },
  },
}));
