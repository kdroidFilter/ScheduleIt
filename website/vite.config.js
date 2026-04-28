import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  base: '/ScheduleIt/',
  plugins: [react()],
  define: {
    __BUILD_ID__: JSON.stringify(Date.now().toString(36)),
  },
})
