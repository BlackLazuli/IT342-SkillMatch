// vite.config.js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
      '/uploads': {  // Proxy for images/files
        target: 'http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080',
        changeOrigin: true,
      },
    },
  },
});