module.exports = {
  devServer: {
    port: 3000,
    historyApiFallback: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  publicPath: './',
  outputDir: 'dist',
  assetsDir: 'static'
}