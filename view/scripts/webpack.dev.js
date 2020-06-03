const path = require('path');
const HTMLWebpackPlugin = require('html-webpack-plugin');
const { VueLoaderPlugin } = require('vue-loader');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const MomentLocalesPlugin = require('moment-locales-webpack-plugin');

const buildDir = path.join(__dirname, '..', 'build');

module.exports = {
  mode: 'development',
  entry: {
    app: path.join(__dirname, '..', 'src', 'main.js'),
    dashboard: path.join(__dirname, '..', 'src', 'dashboard', 'dashboard.js')
  },
  output: {
    path: buildDir,
    filename: '[name].js'
  },
  optimization: {
    minimize: false
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        loader: 'babel-loader',
        options: {
          presets: ['@babel/preset-env'],
          compact: true
        }
      },
      {
        test: /\.vue$/,
        loader: 'vue-loader'
      },
      {
        test: /\.css$/,
        use: [
          MiniCssExtractPlugin.loader,
          'css-loader'
        ]
      },
      {
        test: /\.scss$/,
        use: [
          MiniCssExtractPlugin.loader,
          'css-loader',
          'sass-loader'
        ]
      },
      {
        test: /\.(woff2)$/,
        use: [
          {
            loader: 'file-loader',
            options: {
              name: '[name].[ext]'
            }
          }
        ]
      }
    ]
  },
  plugins: [
    new VueLoaderPlugin(),
    new MiniCssExtractPlugin({
      filename: '[name].css'
    }),
    new MomentLocalesPlugin(),

    // Pack /index.html
    new HTMLWebpackPlugin({
      filename: 'index.html',
      chunks: ['app'],
      inject: true,
      templateContent: `
        <!DOCTYPE html>
        <html lang="en-US">
        <head>
          <meta charset="utf-8">
          <title>Stream</title>
          <link href="favicon.png" rel="shortcut icon">
        </head>
        <body>
          <div id="app"></div>
        </body>
        </html>
      `,
      scriptLoading: 'defer',
      meta: {
        description: 'streaming service',
        viewport: 'width=device-width, initial-scale=1, maximum-scale=1'
      }
    }),

    // Pack /dashboard.html
    new HTMLWebpackPlugin({
      filename: 'dashboard.html',
      chunks: ['dashboard'],
      inject: true,
      templateContent: `
        <!DOCTYPE html>
        <html lang="en-US">
        <head>
          <meta charset="utf-8">
          <title>Stream Dashboard</title>
          <link href="favicon.png" rel="shortcut icon">
        </head>
        <body>
          <div id="app"></div>
        </body>
        </html>
      `,
      scriptLoading: 'defer',
      meta: {
        description: 'streaming service dashboard',
        viewport: 'width=device-width, initial-scale=1'
      }
    }),

    new CopyWebpackPlugin([
      { from: 'assets/favicon.png' }
    ]),

    // Copy custom static assets
    new CopyWebpackPlugin([
      { from: 'assets/favicon.png' },
      { from: 'static' }
    ])
  ]
};
