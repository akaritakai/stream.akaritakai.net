const path = require('path');
const webpack = require('webpack');
const webpackProdConfig = require('./webpack.prod');
const webpackDevConfig = require('./webpack.dev');
const ora = require('ora');
const rimraf = require('rimraf');
const chalk = require('chalk');

const spinner = ora('Building...');
spinner.start();

const buildDir = path.resolve(__dirname, '../build');

rimraf(buildDir, err => {
  if (err) throw err;

  let webpackConfig = webpackProdConfig;
  const devMode = process.argv[2] === "dev";
  if (devMode) {
    console.log("Compiling in DEV mode...");
    webpackConfig = webpackDevConfig;
  } else {
    console.log("Compiling in PROD mode...");
  }

  webpack(webpackConfig, function(err, stats) {
    spinner.stop();
    if (err) throw err;
    process.stdout.write(stats.toString({
      colors: true,
      modules: false,
      children: false,
      chunks: false,
      chunkModules: false
    }) + '\n\n');

    if (stats.hasErrors()) {
      console.log(chalk.red('  Build failed with errors.\n'));
      process.exit(1);
    }

    console.log(chalk.cyan('  Build complete.\n'));
  });
});
