var path = require('path');
var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var pkg = require('./package.json');

module.exports = {
  devtool: 'cheap-module-eval-source-map',
  entry: [
    path.join(__dirname, 'index'),
  ],
  output: {
    path: path.join(__dirname, 'public'),
    filename: 'signal.js',
    publicPath: '/',
  },
  devServer: {
    historyApiFallback: true,
    contentBase: './public',
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        secure: false,
      },
    },
  },
  plugins: [
    new webpack.optimize.OccurenceOrderPlugin(),
    new webpack.NoErrorsPlugin(),
    new webpack.DefinePlugin({
      VERSION: JSON.stringify(pkg.version),
    }),
    new HtmlWebpackPlugin({
      template: 'index.ejs',
      filename: 'index.html',
      hash: true,
    }),
  ],
  module: {
    loaders: [
      {
        test: /\.js$/,
        loaders: ['babel'],
        exclude: /node_modules/,
        include: __dirname,
      }, {
        test: /\.js$/,
        exclude: /node_modules/,
        loader: 'eslint-loader',
      }, {
        test: /\.less$/,
        loader: 'style-loader!css-loader!less-loader',
      },
    ],
  },
  resolve: {
    alias: {
      config: path.join(__dirname, 'config', process.env.NODE_ENV || 'local'),
    },
  },
};
