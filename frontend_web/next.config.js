module.exports = {
    experimental: {
      allowMiddlewareResponseBody: true,
    },
    async headers() {
      return [
        {
          source: '/api/(.*)',
          headers: [
            { 
              key: 'Content-Security-Policy',
              value: "upgrade-insecure-requests" 
            }
          ],
        },
      ];
    },
  };