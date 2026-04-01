/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        admin: {
          bg: '#F3F4F6',
          sidebar: '#1F2937',
          card: '#FFFFFF',
          text: '#111827',
          muted: '#6B7280',
          primary: '#3B82F6',
          success: '#10B981',
          danger: '#EF4444'
        }
      }
    },
  },
  plugins: [],
}
