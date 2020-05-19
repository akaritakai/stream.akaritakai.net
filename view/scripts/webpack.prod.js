const path = require('path');
const HTMLWebpackPlugin = require('html-webpack-plugin');
const { VueLoaderPlugin } = require('vue-loader');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const TerserJSPlugin = require('terser-webpack-plugin');
const FaviconsWebpackPlugin = require('favicons-webpack-plugin');
const MomentLocalesPlugin = require('moment-locales-webpack-plugin');

const buildDir = path.join(__dirname, '..', 'build');

module.exports = {
  mode: 'production',
  entry: {
    app: path.join(__dirname, '..', 'src', 'main.js'),
  dashboard: path.join(__dirname, '..', 'src', 'dashboard', 'dashboard.js')
  },
  output: {
    path: buildDir,
    filename: '[name].js'
  },
  optimization: {
    minimize: true,
    minimizer: [
      new TerserJSPlugin({
        terserOptions: {
          output: {
            comments: false
          }
        },
        extractComments: false
      }),
      new OptimizeCSSAssetsPlugin()
    ]
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
        </head>
        <body>
          <div id="app"></div>
        </body>
        </html>
      `,
      scriptLoading: 'defer',
      meta: {
        description: 'streaming service',
        viewport: 'width=device-width, initial-scale=1'
      },
      minify: {
        collapseWhitespace: true,
        html5: true,
        removeComments: true,
        removeRedundantAttributes: true,
        removeScriptTypeAttributes: true,
        removeStyleLinkTypeAttributes: true,
        sortAttributes: true,
        sortClassName: true,
        useShortDoctype: true
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
      },
      minify: {
        collapseWhitespace: true,
        html5: true,
        removeComments: true,
        removeRedundantAttributes: true,
        removeScriptTypeAttributes: true,
        removeStyleLinkTypeAttributes: true,
        sortAttributes: true,
        sortClassName: true,
        useShortDoctype: true
      }
    }),

    new FaviconsWebpackPlugin({
      logo: path.join(__dirname, '..', 'assets', 'favicon.png'),
      prefix: "",
      favicons: {
        appName: null,
        appShortName: null,
        appDescription: null,
        developerName: null,
        developerURL: null,
        lang: "en-US",
        logging: false,
        pixel_art: false,
        icons: {
          android: false,
          appleIcon: false,
          appleStartup: false,
          coast: false,
          favicons: true,
          firefox: false,
          windows: false,
          yandex: false
        }
      }
    }),

    // Copy custom static assets
    new CopyWebpackPlugin([
      { from: 'static' }
    ])
  ]
};
